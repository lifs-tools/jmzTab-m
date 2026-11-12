#!/usr/bin/env bash
# Pre-fill a vocabulary cache for the jar/native parity check, as an operator
# would: each vocabulary file alone in <cache>/<KEY>/<version>/ under the name
# that .github/parity-cv/resources.json downloads it as. The jmzTab-m CLI
# adopts such a folder (builds its index and writes its release.json) offline.
# MS and MSIO get the mini vocabulary under every release that resources.json
# lists for them, so that every MS version a corpus file declares is there.
#
# Usage: parity-cv-cache.sh <cache folder>
set -euo pipefail

if [ $# -ne 1 ]; then
  echo "usage: parity-cv-cache.sh <cache folder>" >&2
  exit 2
fi
CACHE="$1"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
CV="$ROOT/validation/src/test/resources/cv"
RESOURCES="$ROOT/.github/parity-cv/resources.json"

prefill() {
  local key="$1" version="$2" name="$3" source="$4"
  mkdir -p "$CACHE/$key/$version"
  cp "$source" "$CACHE/$key/$version/$name"
}

listed() {
  local versions
  versions=$(jq -r --arg key "$1" '.vocabularies[$key].releases.releases[].version' "$RESOURCES")
  if [ -z "$versions" ]; then
    echo "parity-cv-cache.sh: $RESOURCES lists no release of $1" >&2
    exit 1
  fi
  echo "$versions"
}

for version in $(listed MS); do
  prefill MS "$version" psi-ms.obo "$CV/mini-ms.obo"
done
# the mini MSIO imports mini-obi.owl, which an offline run cannot fetch
for version in $(listed MSIO); do
  mkdir -p "$CACHE/MSIO/$version"
  grep -v 'owl:imports' "$CV/mini-msio.owl" > "$CACHE/MSIO/$version/msio.owl"
done
# the terms of several vocabularies in one file
for key in BTO CHEBI CL DOID NCBITaxon NCIT PRIDE STATO UO; do
  prefill "$key" 1.0.0 fixture.obo "$CV/mapping-rules.obo"
done
