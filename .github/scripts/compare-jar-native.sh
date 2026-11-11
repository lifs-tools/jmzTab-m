#!/usr/bin/env bash
# Compare the jmzTab-m CLI jar with the native validator on a corpus of example
# files. For every file both builds must agree on
#   - the exit code,
#   - the validation messages ([Info-…], [Warn-…], [Error-…]),
#   - the converted output (--toJson for mzTab files, --fromJson for JSON files).
# With CV_CACHE set, every file of at most 512 KiB also gets a semantic
# validation with the local CV lookup, offline (-s --cvOffline --cvCache),
# whose exit code, validation messages and "CV <label>: ..." report lines must
# agree as well. Larger files are skipped for it, and listed: the semantic
# validation of the large examples takes many minutes in the native build.
# The check fails if no semantic run resolved a vocabulary from the cache.
# A non-zero exit without any validation message or logged error counts as a
# failure as well, since it leaves users without any hint about what went wrong.
#
# Usage: compare-jar-native.sh <jar> <native binary> <file or directory>...
#
# Environment:
#   KNOWN_DIFFERENCES  Optional file listing known differences, one per line as
#                      "<file name> <issue reference>". Matching files are
#                      reported as warnings instead of failures.
#   CV_CACHE           Optional pre-filled vocabulary cache folder (see
#                      parity-cv-cache.sh). Each semantic run gets a fresh copy
#                      of it, at the same path, so that both builds adopt the
#                      pre-filled releases themselves.
#   CV_RESOURCES       Optional preferred-resources file for the semantic runs
#                      (--cvResources).
set -uo pipefail

if [ $# -lt 3 ]; then
  echo "usage: compare-jar-native.sh <jar> <native binary> <file or directory>..." >&2
  exit 2
fi
JAR=$(realpath "$1")
NATIVE=$(realpath "$2")
shift 2
SUMMARY="${GITHUB_STEP_SUMMARY:-/dev/null}"
KNOWN="${KNOWN_DIFFERENCES:-}"
CV_SOURCE_CACHE="${CV_CACHE:+$(realpath "$CV_CACHE")}"
CV_RESOURCES_FILE="${CV_RESOURCES:+$(realpath "$CV_RESOURCES")}"
# files above this size get no semantic run (wc -c: GNU and BSD stat differ)
CV_MAX_BYTES=524288
if [ -n "${CV_CACHE:-}" ] && { [ ! -d "$CV_SOURCE_CACHE" ] || [ -z "$(ls -A "$CV_SOURCE_CACHE")" ]; }; then
  echo "::error title=No CV cache::CV_CACHE '${CV_CACHE}' is not a folder with a pre-filled cache."
  exit 1
fi

WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

# Run one build on a copy of the input in its own directory, since --toJson
# writes into the working directory and --fromJson next to the input file.
run_build() {
  local dir="$1" file="$2"
  shift 2
  local base mode
  base=$(basename "$file")
  if [[ "$base" == *.json ]]; then mode=--fromJson; else mode=--toJson; fi
  mkdir -p "$dir"
  cp "$file" "$dir/"
  (cd "$dir" && "$@" -c "$base" "$mode" -l Info > out.log 2>&1; echo $? > exit.code)
  grep -E '^\[(Info|Warn|Error)-' "$dir/out.log" | tr -d '\r' | sort > "$dir/messages.txt"
  if [[ "$base" == *.json ]]; then
    [ -f "$dir/$base.mztab" ] && cp "$dir/$base.mztab" "$dir/converted"
  elif [ -f "$dir/$base.json" ]; then
    # Canonicalize so that only content differences count.
    jq -S . "$dir/$base.json" > "$dir/converted" 2>/dev/null || cp "$dir/$base.json" "$dir/converted"
  fi
}

# The semantic validation with the local CV lookup, offline, on a fresh copy of
# the pre-filled cache. The semantic messages are printed as "code: [...]"
# lines of a ValidationMessage; the report names each vocabulary used ("CV ").
run_semantic() {
  local dir="$1" file="$2"
  shift 2
  local base cache="$WORK/cv-cache"
  local args=(-s --cvOffline --cvCache "$cache")
  [ -n "$CV_RESOURCES_FILE" ] && args+=(--cvResources "$CV_RESOURCES_FILE")
  base=$(basename "$file")
  [[ "$base" == *.json ]] && args+=(--fromJson)
  rm -rf "$cache"
  if ! cp -R "$CV_SOURCE_CACHE" "$cache"; then
    echo "::error title=No CV cache::Could not copy ${CV_SOURCE_CACHE} to ${cache}."
    exit 1
  fi
  mkdir -p "$dir"
  cp "$file" "$dir/"
  (cd "$dir" && "$@" -c "$base" "${args[@]}" -l Info > out.log 2>&1; echo $? > exit.code)
  tr -d '\r' < "$dir/out.log" | grep -E '^(\[(Info|Warn|Error)-| *code: \[(Info|Warn|Error)-|CV )' \
    | sed -E 's/^ *code: //' | sort > "$dir/messages.txt"
}

# Exit non-zero without a validation message or a logged error.
silent_failure() {
  local dir="$1"
  [ "$(cat "$dir/exit.code")" -ne 0 ] && [ ! -s "$dir/messages.txt" ] \
    && ! grep -qE 'ERROR|Exception' "$dir/out.log"
}

known_issue() {
  [ -n "$KNOWN" ] && [ -f "$KNOWN" ] || return 1
  awk -v f="$1" '$1 == f { $1 = ""; sub(/^ /, ""); print; found = 1; exit } END { exit !found }' "$KNOWN"
}

mapfile -t FILES < <(find "$@" -type f \( -iname '*.mztab' -o -iname '*.json' -o -iname '*.txt' \) | sort)
if [ ${#FILES[@]} -eq 0 ]; then
  echo "::error title=No files found::No example files found in: $*"
  exit 1
fi

{
  echo "### Jar vs. native validator"
  echo
  echo "| File | Exit (jar / native) | Semantic exit (jar / native) | Result |"
  echo "|------|---------------------|------------------------------|--------|"
} >> "$SUMMARY"

FAIL_COUNT=0
KNOWN_COUNT=0
SEMANTIC_RUNS=0
SEMANTIC_LOCAL=0
SEMANTIC_SKIPPED=()
i=0
for file in "${FILES[@]}"; do
  i=$((i + 1))
  # set again for each file, so that a skipped file never shows another one's semantic run
  semantic_ran=false
  semantic_exits="–"
  jar_sem=""
  native_sem=""
  jar_dir="$WORK/$i/jar"
  native_dir="$WORK/$i/native"
  run_build "$jar_dir" "$file" java -jar "$JAR"
  run_build "$native_dir" "$file" "$NATIVE"
  jar_exit=$(cat "$jar_dir/exit.code")
  native_exit=$(cat "$native_dir/exit.code")

  problems=()
  [ "$jar_exit" = "$native_exit" ] || problems+=("exit codes differ")
  cmp -s "$jar_dir/messages.txt" "$native_dir/messages.txt" || problems+=("validation messages differ")
  if [ -f "$jar_dir/converted" ] || [ -f "$native_dir/converted" ]; then
    if [ ! -f "$jar_dir/converted" ] || [ ! -f "$native_dir/converted" ]; then
      problems+=("only one build wrote converted output")
    elif ! cmp -s "$jar_dir/converted" "$native_dir/converted"; then
      problems+=("converted output differs")
    fi
  fi
  silent_failure "$jar_dir" && problems+=("jar exited ${jar_exit} without any message")
  silent_failure "$native_dir" && problems+=("native exited ${native_exit} without any message")
  size=$(wc -c < "$file" | tr -d '[:space:]')
  if [ -n "$CV_SOURCE_CACHE" ] && [ "$size" -gt "$CV_MAX_BYTES" ]; then
    echo "Skipping the semantic run of ${file}: ${size} bytes, above ${CV_MAX_BYTES}"
    SEMANTIC_SKIPPED+=("${file} (${size} bytes)")
    semantic_exits="skipped (${size} bytes)"
  elif [ -n "$CV_SOURCE_CACHE" ]; then
    semantic_ran=true
    jar_sem="$WORK/$i/jar-semantic"
    native_sem="$WORK/$i/native-semantic"
    run_semantic "$jar_sem" "$file" java -jar "$JAR"
    run_semantic "$native_sem" "$file" "$NATIVE"
    jar_sem_exit=$(cat "$jar_sem/exit.code")
    native_sem_exit=$(cat "$native_sem/exit.code")
    [ "$jar_sem_exit" = "$native_sem_exit" ] || problems+=("semantic exit codes differ (${jar_sem_exit} / ${native_sem_exit})")
    cmp -s "$jar_sem/messages.txt" "$native_sem/messages.txt" || problems+=("semantic validation messages differ")
    silent_failure "$jar_sem" && problems+=("jar exited ${jar_sem_exit} without any semantic message")
    silent_failure "$native_sem" && problems+=("native exited ${native_sem_exit} without any semantic message")
    semantic_exits="${jar_sem_exit} / ${native_sem_exit}"
    SEMANTIC_RUNS=$((SEMANTIC_RUNS + 1))
    grep -q '^CV ' "$jar_sem/messages.txt" && SEMANTIC_LOCAL=$((SEMANTIC_LOCAL + 1))
  fi

  if [ ${#problems[@]} -eq 0 ]; then
    echo "✅ ${file}"
    if issue=$(known_issue "$(basename "$file")"); then
      echo "::notice file=${file},title=Known difference resolved::Jar and native now agree; remove this file from ${KNOWN} (${issue})."
    fi
    echo "| \`${file}\` | ${jar_exit} / ${native_exit} | ${semantic_exits} | ✅ identical |" >> "$SUMMARY"
    continue
  fi

  details=$(IFS=';'; echo "${problems[*]}")
  details="${details//;/; }"
  if issue=$(known_issue "$(basename "$file")"); then
    KNOWN_COUNT=$((KNOWN_COUNT + 1))
    echo "::warning file=${file},title=Known jar/native difference::${details} (${issue})"
    echo "| \`${file}\` | ${jar_exit} / ${native_exit} | ${semantic_exits} | ⚠️ known: ${details} (${issue}) |" >> "$SUMMARY"
  else
    FAIL_COUNT=$((FAIL_COUNT + 1))
    echo "::error file=${file},title=Jar/native difference::${details}"
    echo "| \`${file}\` | ${jar_exit} / ${native_exit} | ${semantic_exits} | ❌ ${details} |" >> "$SUMMARY"
  fi
  echo "::group::Details for ${file}"
  echo "--- validation messages (jar vs. native)"
  diff "$jar_dir/messages.txt" "$native_dir/messages.txt"
  if [ -f "$jar_dir/converted" ] && [ -f "$native_dir/converted" ]; then
    echo "--- converted output (jar vs. native, first 40 lines)"
    diff "$jar_dir/converted" "$native_dir/converted" | head -40
  fi
  echo "--- jar log (tail)"
  tail -20 "$jar_dir/out.log"
  echo "--- native log (tail)"
  tail -20 "$native_dir/out.log"
  if $semantic_ran; then
    echo "--- semantic validation messages (jar vs. native)"
    diff "$jar_sem/messages.txt" "$native_sem/messages.txt"
    echo "--- semantic jar log (tail)"
    tail -20 "$jar_sem/out.log"
    echo "--- semantic native log (tail)"
    tail -20 "$native_sem/out.log"
  fi
  echo "::endgroup::"
done

echo "" >> "$SUMMARY"
echo "Compared ${#FILES[@]} files: ${FAIL_COUNT} unexpected and ${KNOWN_COUNT} known difference(s)."
if [ -n "$CV_SOURCE_CACHE" ]; then
  echo "Semantic runs: ${SEMANTIC_RUNS}, ${SEMANTIC_LOCAL} of them resolved a vocabulary from the cache; ${#SEMANTIC_SKIPPED[@]} file(s) above ${CV_MAX_BYTES} bytes skipped."
  {
    echo "Semantic runs with the local CV lookup: ${SEMANTIC_RUNS}, ${SEMANTIC_LOCAL} of them resolved a vocabulary from the cache."
    if [ ${#SEMANTIC_SKIPPED[@]} -gt 0 ]; then
      echo
      echo "Skipped for the semantic run (above ${CV_MAX_BYTES} bytes):"
      echo
      for skipped in "${SEMANTIC_SKIPPED[@]}"; do
        echo "- \`${skipped}\`"
      done
    fi
  } >> "$SUMMARY"
  if [ "$SEMANTIC_LOCAL" -eq 0 ]; then
    echo "::error title=Semantic parity tested nothing::No semantic run resolved a vocabulary from the pre-filled cache; check CV_CACHE and CV_RESOURCES."
    exit 1
  fi
fi
if [ "$FAIL_COUNT" -ne 0 ]; then
  echo "::error title=Jar/native parity::${FAIL_COUNT} of ${#FILES[@]} file(s) behave differently in the jar and the native validator."
  exit 1
fi
