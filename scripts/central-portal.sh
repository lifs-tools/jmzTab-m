#!/usr/bin/env bash
# Talks to the Central Portal Publisher API (https://central.sonatype.org/publish/publish-portal-api/).
# scripts/release.sh uses it; it can also be called directly.
#
#   central-portal.sh upload <bundle.zip> <deployment name>   prints the deployment id
#   central-portal.sh status <id>                             prints the status JSON
#   central-portal.sh wait <id> <state> [max seconds]         waits for <state>, fails on FAILED
#   central-portal.sh publish <id>                            publishes a VALIDATED deployment
#   central-portal.sh drop <id>                               drops a VALIDATED or FAILED deployment
#
# The user token comes from CENTRAL_TOKEN_USERNAME and CENTRAL_TOKEN_PASSWORD. It is
# passed to curl in a header file, never on the command line. CENTRAL_BASE_URL
# (default https://central.sonatype.com) is meant for tests only.
set -euo pipefail

BASE_URL="${CENTRAL_BASE_URL:-https://central.sonatype.com}"
API="${BASE_URL%/}/api/v1/publisher"
POLL_SECONDS="${CENTRAL_POLL_SECONDS:-15}"

die() { echo "central-portal: $*" >&2; exit 1; }

[ -n "${CENTRAL_TOKEN_USERNAME:-}" ] || die "CENTRAL_TOKEN_USERNAME is not set"
[ -n "${CENTRAL_TOKEN_PASSWORD:-}" ] || die "CENTRAL_TOKEN_PASSWORD is not set"

HEADERS="$(umask 077 && mktemp)"
trap 'rm -f "$HEADERS"' EXIT
TOKEN="$(printf '%s:%s' "$CENTRAL_TOKEN_USERNAME" "$CENTRAL_TOKEN_PASSWORD" | base64 | tr -d '\n')"
printf 'Authorization: Bearer %s\n' "$TOKEN" > "$HEADERS"
unset TOKEN

check_id() {
  [[ "$1" =~ ^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$ ]] \
    || die "not a deployment id: '$1'"
}

# api <expected HTTP status> <curl args...>: prints the response body, fails on any other status
api() {
  local expected="$1" body status
  shift
  body="$(mktemp)"
  status="$(curl -sS --proto '=https,http' --max-time 600 -H @"$HEADERS" -o "$body" -w '%{http_code}' "$@")" \
    || { rm -f "$body"; die "request failed: ${*: -1}"; }
  if [ "$status" != "$expected" ]; then
    echo "central-portal: HTTP $status (expected $expected) from ${*: -1}" >&2
    head -c 4000 "$body" >&2; echo >&2
    rm -f "$body"
    exit 1
  fi
  cat "$body"
  rm -f "$body"
}

status() {
  check_id "$1"
  api 200 -X POST "$API/status?id=$1"
}

case "${1:-}" in
  upload)
    [ $# -eq 3 ] || die "usage: upload <bundle.zip> <deployment name>"
    [ -f "$2" ] || die "no such bundle: $2"
    name="$(jq -rn --arg n "$3" '$n | @uri')"
    id="$(api 201 -F "bundle=@$2;type=application/octet-stream" \
      "$API/upload?publishingType=USER_MANAGED&name=$name" | tr -d '[:space:]')"
    check_id "$id"
    echo "$id"
    ;;
  status)
    [ $# -eq 2 ] || die "usage: status <id>"
    status "$2"
    ;;
  wait)
    [ $# -ge 3 ] || die "usage: wait <id> <state> [max seconds]"
    id="$2" want="$3" max="${4:-3600}" start="$SECONDS"
    while :; do
      json="$(status "$id")"
      state="$(jq -r '.deploymentState' <<<"$json")"
      echo "deployment $id: $state" >&2
      if [ "$state" = "$want" ]; then echo "$json"; exit 0; fi
      case "$state" in
        FAILED) jq '.errors' <<<"$json" >&2; die "deployment $id failed" ;;
        PUBLISHED) [ "$want" = PUBLISHED ] || die "deployment $id is already published" ;;
      esac
      [ $((SECONDS - start)) -lt "$max" ] || die "deployment $id is still $state after ${max}s"
      sleep "$POLL_SECONDS"
    done
    ;;
  publish)
    [ $# -eq 2 ] || die "usage: publish <id>"
    check_id "$2"
    api 204 -X POST "$API/deployment/$2" > /dev/null
    ;;
  drop)
    [ $# -eq 2 ] || die "usage: drop <id>"
    check_id "$2"
    api 204 -X DELETE "$API/deployment/$2" > /dev/null
    ;;
  *)
    die "usage: central-portal.sh upload|status|wait|publish|drop ..."
    ;;
esac
