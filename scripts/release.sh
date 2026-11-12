#!/usr/bin/env bash
# Releases jmzTab-m to Maven Central in two stages, on your machine, see RELEASES.adoc.
#
#   scripts/release.sh stage [maven args]   build, test, sign, check and upload; nothing is published
#   scripts/release.sh publish <id>         publish the staged deployment <id> (cannot be undone)
#   scripts/release.sh drop <id>            drop the staged deployment <id>
#   scripts/release.sh status <id>          show the state of a deployment
#
# The Central Portal user token is read from CENTRAL_TOKEN_USERNAME and
# CENTRAL_TOKEN_PASSWORD, or asked for. Maven args, e.g. -Dgpg.keyname=<KEY_ID>, are
# passed to the build.
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"
PORTAL=scripts/central-portal.sh
BUNDLE=target/central-publishing/central-bundle.zip
MODULES="jmztabm-api jmztabm-io jmztabm-validation jmztabm-cli jmztabm-test-utils"

die() { echo "release: $*" >&2; exit 1; }

ask_token() {
  if [ -z "${CENTRAL_TOKEN_USERNAME:-}" ]; then
    read -r -p "Central Portal token username: " CENTRAL_TOKEN_USERNAME
  fi
  if [ -z "${CENTRAL_TOKEN_PASSWORD:-}" ]; then
    read -r -s -p "Central Portal token password: " CENTRAL_TOKEN_PASSWORD
    echo
  fi
  export CENTRAL_TOKEN_USERNAME CENTRAL_TOKEN_PASSWORD
}

# check_bundle <version>: all modules with POM, jar, sources, javadoc and signatures
check_bundle() {
  local version="$1" files missing=0 module suffix f
  files="$(unzip -Z1 "$BUNDLE")"
  for module in $MODULES; do
    for suffix in .jar .pom -sources.jar -javadoc.jar; do
      for f in "$module-$version$suffix" "$module-$version$suffix.asc"; do
        grep -qx "org/lifs-tools/$module/$version/$f" <<<"$files" || { echo "missing: $f" >&2; missing=1; }
      done
    done
  done
  grep -qx "org/lifs-tools/jmztabm/$version/jmztabm-$version.pom.asc" <<<"$files" \
    || { echo "missing: jmztabm-$version.pom.asc" >&2; missing=1; }
  [ "$missing" -eq 0 ] || die "the bundle is incomplete"
}

# check_deployment <id> <action>: the deployment is ours and can be published or dropped
check_deployment() {
  local id="$1" action="$2" json state name
  json="$("$PORTAL" status "$id")"
  state="$(jq -r '.deploymentState' <<<"$json")"
  name="$(jq -r '.deploymentName' <<<"$json")"
  case "$name" in
    'jmzTab-m '*) ;;
    *) die "deployment $id is '$name', not a jmzTab-m deployment" ;;
  esac
  if jq -e '[.purls[] | select(startswith("pkg:maven/org.lifs-tools/jmztabm") | not)] | length > 0' <<<"$json" > /dev/null; then
    die "deployment $id contains components outside org.lifs-tools:jmztabm*"
  fi
  if [ "$state" != VALIDATED ] && ! { [ "$action" = drop ] && [ "$state" = FAILED ]; }; then
    die "deployment $id is $state; only a VALIDATED deployment can be published, and a VALIDATED or FAILED one dropped"
  fi
  echo "$name, deployment $id, $state:"
  jq -r '.purls[] | "  " + .' <<<"$json"
  DEPLOYMENT_NAME="$name"
}

stage() {
  git diff --quiet && git diff --cached --quiet || die "commit or stash your changes first"
  ./mvnw -q -v > /dev/null
  VERSION="$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout | tr -d '\n')"
  case "$VERSION" in
    ''|*SNAPSHOT*) die "'$VERSION' is not a release version; set it with ./mvnw versions:set" ;;
  esac
  tag_commit="$(git rev-parse -q --verify "refs/tags/release-$VERSION^{commit}")" \
    || die "tag release-$VERSION does not exist; tag the release commit first"
  [ "$tag_commit" = "$(git rev-parse HEAD)" ] || die "release-$VERSION is not the checked out commit"
  command -v jq > /dev/null || die "jq is needed"
  ask_token

  echo "Building, testing and signing jmzTab-m $VERSION ..."
  ./mvnw -B -Pcentral-release -DskipUpload=true "$@" clean deploy
  check_bundle "$VERSION"

  id="$("$PORTAL" upload "$BUNDLE" "jmzTab-m $VERSION")"
  echo "Uploaded as deployment $id, waiting for the validation ..."
  "$PORTAL" wait "$id" VALIDATED 1800 > /dev/null
  echo
  check_deployment "$id" publish
  cat <<EOF

jmzTab-m $VERSION is staged and not yet published. Check it at
https://central.sonatype.com/publishing/deployments, then:

  scripts/release.sh publish $id
  scripts/release.sh drop $id      # to throw it away
EOF
}

publish() {
  ask_token
  check_deployment "$1" publish
  echo
  echo "Publishing cannot be undone: a published version cannot be changed or deleted."
  read -r -p "Type the deployment name ($DEPLOYMENT_NAME) to publish it: " answer
  [ "$answer" = "$DEPLOYMENT_NAME" ] || die "not published"
  "$PORTAL" publish "$1"
  echo "Publishing, this usually takes a few minutes ..."
  "$PORTAL" wait "$1" PUBLISHED 6600 > /dev/null
  echo "Published. It can take a while longer until all files are on https://repo1.maven.org/maven2/org/lifs-tools/."
}

drop() {
  ask_token
  check_deployment "$1" drop
  read -r -p "Drop it? [y/N] " answer
  [ "$answer" = y ] || [ "$answer" = Y ] || die "not dropped"
  "$PORTAL" drop "$1"
  echo "Dropped."
}

case "${1:-}" in
  stage) shift; stage "$@" ;;
  publish) [ $# -eq 2 ] || die "usage: release.sh publish <id>"; publish "$2" ;;
  drop) [ $# -eq 2 ] || die "usage: release.sh drop <id>"; drop "$2" ;;
  status) [ $# -eq 2 ] || die "usage: release.sh status <id>"; ask_token; "$PORTAL" status "$2" | jq . ;;
  *) die "usage: release.sh stage [maven args] | publish <id> | drop <id> | status <id>" ;;
esac
