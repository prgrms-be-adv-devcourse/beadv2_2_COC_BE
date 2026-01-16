#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CLUSTER_NAME="${CLUSTER_NAME:-modi}"
NAMESPACE="modi"
OVERLAY_DIR="${ROOT_DIR}/k8s/overlays/kind"
ENV_FILE="${OVERLAY_DIR}/.env"
KIND_CONFIG="${KIND_CONFIG:-${ROOT_DIR}/k8s/scripts/kind-config.yaml}"
RECREATE_CLUSTER="${RECREATE_CLUSTER:-false}"
ROLLOUT_TIMEOUT="${ROLLOUT_TIMEOUT:-0s}"
WAIT_TIMEOUT="${WAIT_TIMEOUT:-0s}"

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

if [ "${RECREATE_CLUSTER}" = "true" ]; then
  if kind get clusters | grep -qx "${CLUSTER_NAME}"; then
    kind delete cluster --name "${CLUSTER_NAME}"
  fi
  kind create cluster --name "${CLUSTER_NAME}" --config "${KIND_CONFIG}"
else
  if ! kind get clusters | grep -qx "${CLUSTER_NAME}"; then
    kind create cluster --name "${CLUSTER_NAME}" --config "${KIND_CONFIG}"
  else
    echo "kind cluster '${CLUSTER_NAME}' already exists. Port mappings won't change unless you delete and recreate it." >&2
  fi
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

LOAD_EXTERNAL_IMAGES="${LOAD_EXTERNAL_IMAGES:-false}"
if [ "${LOAD_EXTERNAL_IMAGES}" = "true" ]; then
  EXTERNAL_IMAGES_DEFAULT=(
    apache/kafka:3.8.1
    redis:7
    docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    pgvector/pgvector:pg18
  )
  if [ -n "${EXTERNAL_IMAGES_OVERRIDE:-}" ]; then
    read -r -a EXTERNAL_IMAGES <<< "${EXTERNAL_IMAGES_OVERRIDE}"
  else
    EXTERNAL_IMAGES=("${EXTERNAL_IMAGES_DEFAULT[@]}")
  fi

  for img in "${EXTERNAL_IMAGES[@]}"; do
    if ! docker image inspect "${img}" >/dev/null 2>&1; then
      docker pull "${img}"
    fi
  done

  ALL_IMAGES=("${IMAGES[@]}" "${EXTERNAL_IMAGES[@]}")
else
  ALL_IMAGES=("${IMAGES[@]}")
fi

for img in "${ALL_IMAGES[@]}"; do
  kind load docker-image "${img}" --name "${CLUSTER_NAME}"
done

docker exec "${CLUSTER_NAME}-control-plane" sysctl -w vm.max_map_count=262144 >/dev/null

kubectl apply -k "${OVERLAY_DIR}"

STATEFULSETS=(
  statefulset/pgvector
  statefulset/kafka
  statefulset/redis
  statefulset/elasticsearch
)

DEPLOYMENTS=(
  deployment/modi-discovery
  deployment/modi-config
  deployment/modi-gateway
  deployment/account-service
  deployment/product-service
  deployment/rental-service
  deployment/seller-service
  deployment/member-service
  deployment/notification-service
  deployment/review-service
  deployment/delivery-service
)

echo "Scaling down services before infra readiness..." >&2
for res in "${DEPLOYMENTS[@]}"; do
  kubectl -n "${NAMESPACE}" scale "${res}" --replicas=0
done

echo "Waiting for infrastructure services to be ready..." >&2
for res in "${STATEFULSETS[@]}"; do
  kubectl -n "${NAMESPACE}" rollout status "${res}" --timeout="${ROLLOUT_TIMEOUT}"
done

echo "Scaling up services after infra readiness..." >&2
for res in "${DEPLOYMENTS[@]}"; do
  kubectl -n "${NAMESPACE}" scale "${res}" --replicas=1
done

rollout_failed=false
for res in "${DEPLOYMENTS[@]}"; do
  if ! kubectl -n "${NAMESPACE}" rollout status "${res}" --timeout="${ROLLOUT_TIMEOUT}"; then
    rollout_failed=true
  fi
done
for res in "${STATEFULSETS[@]}"; do
  if ! kubectl -n "${NAMESPACE}" rollout status "${res}" --timeout="${ROLLOUT_TIMEOUT}"; then
    rollout_failed=true
  fi
done

if ! kubectl -n "${NAMESPACE}" wait --for=condition=Ready pods --all --timeout="${WAIT_TIMEOUT}"; then
  rollout_failed=true
fi

if [ "${rollout_failed}" = true ]; then
  echo "Some workloads are not ready. Collecting diagnostics..." >&2
  kubectl -n "${NAMESPACE}" get pods -o wide
  bad_pods=$(kubectl -n "${NAMESPACE}" get pods --no-headers | awk '{split($2,a,"/"); if (a[1] != a[2]) print $1}')
  for pod in ${bad_pods}; do
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
