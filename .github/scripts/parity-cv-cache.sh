#!/usr/bin/env bash
# Pre-fill a vocabulary cache for the jar/native parity check, as an operator
# would: each vocabulary file alone in <cache>/<KEY>/<version>/ under the name
# that .github/parity-cv/resources.json downloads it as. The jmzTab-m CLI
# adopts such a folder (builds its index and writes its release.json) offline.
#
# Usage: parity-cv-cache.sh <cache folder>
set -euo pipefail

if [ $# -ne 1 ]; then
  echo "usage: parity-cv-cache.sh <cache folder>" >&2
  exit 2
fi
CACHE="$1"
CV="$(cd "$(dirname "$0")/../.." && pwd)/validation/src/test/resources/cv"

prefill() {
  local key="$1" version="$2" name="$3" source="$4"
  mkdir -p "$CACHE/$key/$version"
  cp "$source" "$CACHE/$key/$version/$name"
}

prefill MS 1.0.0 psi-ms.obo "$CV/mini-ms.obo"
# the mini MSIO imports mini-obi.owl, which an offline run cannot fetch
mkdir -p "$CACHE/MSIO/1.0.1"
grep -v 'owl:imports' "$CV/mini-msio.owl" > "$CACHE/MSIO/1.0.1/msio.owl"
# the terms of several vocabularies in one file
for key in BTO CHEBI CL DOID NCBITaxon NCIT PRIDE STATO UO; do
  prefill "$key" 1.0.0 fixture.obo "$CV/mapping-rules.obo"
done
