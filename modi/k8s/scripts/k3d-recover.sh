#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
NAMESPACE="${NAMESPACE:-modi}"
K3D_CLUSTER_NAME="${K3D_CLUSTER_NAME:-modi}"
WAIT_TIMEOUT="${WAIT_TIMEOUT:-120s}"
PGVECTOR_TIMEOUT="${PGVECTOR_TIMEOUT:-180s}"
RESTART_DEPLOYMENTS="${RESTART_DEPLOYMENTS:-true}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_cmd kubectl
require_cmd k3d

if [ -n "${KUBE_CONTEXT:-}" ]; then
  kubectl config use-context "${KUBE_CONTEXT}" >/dev/null
fi

if ! k3d cluster list | awk 'NR>1 {print $1}' | grep -qx "${K3D_CLUSTER_NAME}"; then
  echo "k3d cluster '${K3D_CLUSTER_NAME}' not found." >&2
  exit 1
fi

echo "Starting k3d cluster '${K3D_CLUSTER_NAME}'..." >&2
k3d cluster start "${K3D_CLUSTER_NAME}" >/dev/null

if ! kubectl cluster-info >/dev/null 2>&1; then
  echo "Kubernetes API is not reachable." >&2
  exit 1
fi

echo "Waiting for nodes to become Ready..." >&2
kubectl wait --for=condition=Ready node --all --timeout="${WAIT_TIMEOUT}"

if ! kubectl get namespace "${NAMESPACE}" >/dev/null 2>&1; then
  echo "Namespace '${NAMESPACE}' not found." >&2
  exit 1
fi

if kubectl -n "${NAMESPACE}" get statefulset pgvector >/dev/null 2>&1; then
  echo "Ensuring pgvector is Ready..." >&2
  if ! kubectl -n "${NAMESPACE}" rollout status statefulset/pgvector --timeout="${PGVECTOR_TIMEOUT}"; then
    echo "pgvector is not ready. Forcing pod recreation..." >&2
    kubectl -n "${NAMESPACE}" delete pod -l app=pgvector --force --grace-period=0 || true
    kubectl -n "${NAMESPACE}" rollout status statefulset/pgvector --timeout="${PGVECTOR_TIMEOUT}"
  fi
fi

if [ "${RESTART_DEPLOYMENTS}" = "true" ]; then
  mapfile -t deployments < <(kubectl -n "${NAMESPACE}" get deploy -o name)
  if [ "${#deployments[@]}" -gt 0 ]; then
    echo "Restarting deployments to refresh DB connections..." >&2
    kubectl -n "${NAMESPACE}" rollout restart "${deployments[@]}"
    for dep in "${deployments[@]}"; do
      kubectl -n "${NAMESPACE}" rollout status "${dep}" --timeout="${WAIT_TIMEOUT}" || true
    done
  fi
fi

echo "Recovery completed." >&2
