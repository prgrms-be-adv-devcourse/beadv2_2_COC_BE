#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CLUSTER_NAME="${CLUSTER_NAME:-modi}"
NAMESPACE="modi"
OVERLAY_DIR="${ROOT_DIR}/k8s/overlays/kind"
ENV_FILE="${OVERLAY_DIR}/.env"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_cmd docker
require_cmd kind
require_cmd kubectl

if [ ! -f "${ENV_FILE}" ]; then
  if [ -f "${ROOT_DIR}/.env" ]; then
    cp "${ROOT_DIR}/.env" "${ENV_FILE}"
  else
    echo "Missing ${ROOT_DIR}/.env (needed to generate k8s Secret)" >&2
    exit 1
  fi
fi

if ! kind get clusters | grep -qx "${CLUSTER_NAME}"; then
  kind create cluster --name "${CLUSTER_NAME}"
fi

kubectl config use-context "kind-${CLUSTER_NAME}" >/dev/null

if ! kubectl cluster-info >/dev/null 2>&1; then
  echo "Waiting for Kubernetes API to be reachable..." >&2
  for _ in {1..30}; do
    if kubectl cluster-info >/dev/null 2>&1; then
      break
    fi
    sleep 2
  done
fi
if ! kubectl cluster-info >/dev/null 2>&1; then
  echo "Kubernetes API is not reachable. Check kind cluster status." >&2
  exit 1
fi
kubectl wait --for=condition=Ready nodes --all --timeout=120s >/dev/null

docker compose -f "${ROOT_DIR}/docker-compose.yml" build

IMAGES=(
  modi/modi-discovery:local
  modi/modi-config:local
  modi/modi-gateway:local
  modi/account-service:local
  modi/product-service:local
  modi/rental-service:local
  modi/seller-service:local
  modi/member-service:local
)

for img in "${IMAGES[@]}"; do
  kind load docker-image "${img}" --name "${CLUSTER_NAME}"
done

docker exec "${CLUSTER_NAME}-control-plane" sysctl -w vm.max_map_count=262144 >/dev/null

kubectl apply -k "${OVERLAY_DIR}"

kubectl -n "${NAMESPACE}" rollout restart \
  deployment/modi-discovery \
  deployment/modi-config \
  deployment/modi-gateway \
  deployment/account-service \
  deployment/product-service \
  deployment/rental-service \
  deployment/seller-service \
  deployment/member-service

DEPLOYMENTS=(
  deployment/modi-discovery
  deployment/modi-config
  deployment/modi-gateway
  deployment/account-service
  deployment/product-service
  deployment/rental-service
  deployment/seller-service
  deployment/member-service
)
STATEFULSETS=(
  statefulset/pgvector
  statefulset/redis
  statefulset/elasticsearch
)

rollout_failed=false
for res in "${DEPLOYMENTS[@]}"; do
  if ! kubectl -n "${NAMESPACE}" rollout status "${res}" --timeout=180s; then
    rollout_failed=true
  fi
done
for res in "${STATEFULSETS[@]}"; do
  if ! kubectl -n "${NAMESPACE}" rollout status "${res}" --timeout=180s; then
    rollout_failed=true
  fi
done

if ! kubectl -n "${NAMESPACE}" wait --for=condition=Ready pods --all --timeout=180s; then
  rollout_failed=true
fi

if [ "${rollout_failed}" = true ]; then
  echo "Some workloads are not ready. Collecting diagnostics..." >&2
  kubectl -n "${NAMESPACE}" get pods -o wide
  mapfile -t bad_pods < <(kubectl -n "${NAMESPACE}" get pods --no-headers | awk '{split($2,a,"/"); if (a[1] != a[2]) print $1}')
  for pod in "${bad_pods[@]}"; do
    echo "---- describe ${pod} ----" >&2
    kubectl -n "${NAMESPACE}" describe pod "${pod}" || true
    echo "---- logs (current) ${pod} ----" >&2
    kubectl -n "${NAMESPACE}" logs "${pod}" --all-containers --tail=200 || true
    echo "---- logs (previous) ${pod} ----" >&2
    kubectl -n "${NAMESPACE}" logs "${pod}" --all-containers --previous --tail=200 || true
  done
  exit 1
fi

kubectl -n "${NAMESPACE}" get pods -o wide
