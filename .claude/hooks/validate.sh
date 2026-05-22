#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$REPO_ROOT"

LAYER="${VALIDATE_LAYER:-spec}"

case "$LAYER" in
spec)
  bb validate:spec >&2
  exit 2
  ;;
logic)
  bb validate:logic >&2
  exit 2
  ;;
component)
  bb validate:component >&2
  exit 2
  ;;
*)
  echo "Unknown layer: $LAYER" >&2
  exit 2
  ;;
esac
