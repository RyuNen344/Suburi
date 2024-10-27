#!/usr/bin/env bash
set -eo pipefail

readonly _APP_PACKAGE_="io.github.ryunen344.suburi"

. "$(git rev-parse --show-toplevel)/scripts/utilities"

go_to_repo_root

arg=$1
uri=""
case "$arg" in
  "example" )
    uri="https://www.example.com/uuid/47277417-a40f-43ac-9d27-009835c3e3a4"
    ;;
  "google" )
    uri="https://www.google.com"
    ;;
  * )
    fatal "Unknown argument: $arg"
esac

run adb shell am start -W -a android.intent.action.VIEW -d "$uri" "$_APP_PACKAGE_"
