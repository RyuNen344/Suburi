#!/usr/bin/env bash
set -eo pipefail

readonly _ARTIFACT_DIRECTORY_="./artifact"

. "$(git rev-parse --show-toplevel)/scripts/utilities"

go_to_repo_root

run rm -rf "$_ARTIFACT_DIRECTORY_"
run rm -f "$_ARTIFACT_DIRECTORY_.zip"
run mkdir -p "$_ARTIFACT_DIRECTORY_"

fcopy() {
  if [ $(uname -s) = "Darwin" ]; then
    find . -type f \( -regex './$_ARTIFACT_DIRECTORY_.*' -prune \) -o \( -type f -regex "$1" -exec rsync -Rr {} artifact \; \)
  else
    find . -type f \( -regex './$_ARTIFACT_DIRECTORY_.*' -prune \) -o \( -type f -regex "$1" -exec \cp -rf --parents {} "$_ARTIFACT_DIRECTORY_" \; \)
  fi
}

dcopy() {
  if [ $(uname -s) = "Darwin" ]; then
    find . -type d \( -regex './$_ARTIFACT_DIRECTORY_.*' -prune \) -o \( -type d -regex "$1" -exec rsync -Rr {} artifact \; \)
  else
    find . -type d \( -regex './$_ARTIFACT_DIRECTORY_.*' -prune \) -o \( -type d -regex "$1" -exec \cp -rf --parents {} "$_ARTIFACT_DIRECTORY_" \; \)
  fi
}

# coverage
info "collect coverage files"
run fcopy ".*coverage.ec"
run fcopy ".*.exec"
run dcopy ".*/build/generated/ksp/debug/java"
run dcopy ".*/build/generated/ksp/debug/kotlin"
run dcopy ".*/build/generated/sqldelight/code"
run dcopy ".*/build/generated/hilt/component_sources/debug"
run dcopy ".*/build/generated/hilt/component_tree/debug"
run dcopy ".*/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes"
run dcopy ".*/build/tmp/kotlin-classes/debug"

# apk
info "collect apk"
run fcopy ".*/build/outputs/.*.apk"

run rm -rf "$_ARTIFACT_DIRECTORY_/artifact"
run zip -r artifact.zip artifact
sleep 5
run rm -rf "$_ARTIFACT_DIRECTORY_";
