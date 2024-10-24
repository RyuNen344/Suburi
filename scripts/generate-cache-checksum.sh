#!/usr/bin/env bash
set -eo pipefail

readonly _FIND_SEED_FILE_COMMAND_=". -name \"build.gradle*\" -o -name \"settings.gradle*\" -o -name \"libs.versions.toml\""

. "$(git rev-parse --show-toplevel)/scripts/utilities"

go_to_repo_root

info "The following are the files used to generate the cache checksum:"
run eval find "$_FIND_SEED_FILE_COMMAND_"
run eval find "$_FIND_SEED_FILE_COMMAND_" | sort | xargs cat | shasum | awk '{print $1}' > ./scripts/gradle_cache_seed
