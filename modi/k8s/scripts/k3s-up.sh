#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
NAMESPACE="modi"
OVERLAY_DIR="${ROOT_DIR}/k8s/overlays/k3s"
ENV_FILE="${OVERLAY_DIR}/.env"
ROLLOUT_TIMEOUT="${ROLLOUT_TIMEOUT:-0s}"
WAIT_TIMEOUT="${WAIT_TIMEOUT:-0s}"
K3D_CLUSTER_NAME="${K3D_CLUSTER_NAME:-modi}"
K3D_IMPORT_IMAGES="${K3D_IMPORT_IMAGES:-true}"
GRADLE_PREFETCH="${GRADLE_PREFETCH:-true}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_cmd kubectl
if [ "${K3D_IMPORT_IMAGES}" = "true" ]; then
  require_cmd docker
  require_cmd k3d
fi

if [ ! -f "${ENV_FILE}" ]; then
  if [ -f "${ROOT_DIR}/.env" ]; then
    cp "${ROOT_DIR}/.env" "${ENV_FILE}"
  else
    echo "Missing ${ROOT_DIR}/.env (needed to generate k8s Secret)" >&2
    exit 1
  fi
fi

if [ -n "${KUBE_CONTEXT:-}" ]; then
  kubectl config use-context "${KUBE_CONTEXT}" >/dev/null
fi

if ! kubectl cluster-info >/dev/null 2>&1; then
  echo "Kubernetes API is not reachable. Check k3s cluster status." >&2
  exit 1
fi

if [ "${K3D_IMPORT_IMAGES}" = "true" ]; then
  if [ "${GRADLE_PREFETCH}" = "true" ]; then
    echo "Prefetching Gradle distribution cache..." >&2
    docker build \
      --progress=plain \
      --target builder \
      --build-arg MODULE=modi-discovery \
      -f "${ROOT_DIR}/modi-discovery/Dockerfile" \
      "${ROOT_DIR}"
  fi

  echo "Building local images with docker compose..." >&2
  docker compose -f "${ROOT_DIR}/docker-compose.yml" build
  echo "Building additional local images..." >&2
  docker build -f "${ROOT_DIR}/support-service/Dockerfile" -t modi/support-service:local "${ROOT_DIR}"
  docker build -f "${ROOT_DIR}/ai-service/Dockerfile" -t modi/ai-service:local "${ROOT_DIR}"

  IMAGES=(
    modi/modi-discovery:local
    modi/modi-config:local
    modi/modi-gateway:local
    modi/account-service:local
    modi/product-service:local
    modi/rental-service:local
    modi/seller-service:local
    modi/member-service:local
    modi/support-service:local
    modi/ai-service:local
  )

  echo "Importing images into k3d cluster '${K3D_CLUSTER_NAME}'..." >&2
  k3d image import "${IMAGES[@]}" -c "${K3D_CLUSTER_NAME}"
fi

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
  deployment/support-service
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
