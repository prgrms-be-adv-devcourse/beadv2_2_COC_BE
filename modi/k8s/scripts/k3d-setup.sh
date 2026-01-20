#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CLUSTER_NAME="${CLUSTER_NAME:-modi}"
REGISTRY_NAME="${K3D_REGISTRY_NAME:-modi-registry}"
REGISTRY_HOST="${K3D_REGISTRY_HOST:-k3d-${REGISTRY_NAME}}"
REGISTRY_PORT="${K3D_REGISTRY_PORT:-5001}"
RECREATE_CLUSTER="${RECREATE_CLUSTER:-false}"
UPDATE_HOSTS="${UPDATE_HOSTS:-false}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_cmd k3d
require_cmd kubectl

if command -v lsof >/dev/null 2>&1; then
  if lsof -iTCP:"${REGISTRY_PORT}" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "Port ${REGISTRY_PORT} is already in use. Set K3D_REGISTRY_PORT to a free port." >&2
    exit 1
  fi
fi

if ! k3d registry list | awk '{print $1}' | grep -qx "${REGISTRY_HOST}"; then
  k3d registry create "${REGISTRY_NAME}" --port "${REGISTRY_PORT}"
else
  echo "Registry ${REGISTRY_HOST} already exists. Skipping create." >&2
fi

if k3d cluster list | awk 'NR>1 {print $1}' | grep -qx "${CLUSTER_NAME}"; then
  if [ "${RECREATE_CLUSTER}" = "true" ]; then
    k3d cluster delete "${CLUSTER_NAME}"
  else
    echo "Cluster ${CLUSTER_NAME} already exists. Set RECREATE_CLUSTER=true to recreate." >&2
  fi
fi

if ! k3d cluster list | awk 'NR>1 {print $1}' | grep -qx "${CLUSTER_NAME}"; then
  k3d cluster create "${CLUSTER_NAME}" \
    --agents 1 \
    --registry-use "${REGISTRY_HOST}:${REGISTRY_PORT}" \
    -p "8080:80@loadbalancer" \
    -p "8443:443@loadbalancer"
fi

kubectl config use-context "k3d-${CLUSTER_NAME}" >/dev/null

if [ "${UPDATE_HOSTS}" = "true" ]; then
  if ! grep -q " ${REGISTRY_HOST}$" /etc/hosts; then
    echo "127.0.0.1 ${REGISTRY_HOST}" | sudo tee -a /etc/hosts >/dev/null
  fi
else
  echo "If registry push fails with name resolution, add this to /etc/hosts:" >&2
  echo "127.0.0.1 ${REGISTRY_HOST}" >&2
fi

kubectl get nodes

if [ "${RUN_TILT:-false}" = "true" ]; then
  if ! command -v tilt >/dev/null 2>&1; then
    echo "Tilt is not installed. Install with: brew install tilt-dev/tap/tilt" >&2
    exit 1
  fi
  TILT_HOME_DIR="${TILT_HOME:-${ROOT_DIR}/.tilt}"
  mkdir -p "${TILT_HOME_DIR}"
  TILT_HOME="${TILT_HOME_DIR}" tilt up -- --k8s_context "k3d-${CLUSTER_NAME}" --registry_port "${REGISTRY_PORT}"
fi
