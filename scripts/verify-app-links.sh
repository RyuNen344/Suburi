#!/usr/bin/env bash
set -eo pipefail

readonly _APP_PACKAGE_="io.github.ryunen344.suburi"

. "$(git rev-parse --show-toplevel)/scripts/utilities"

go_to_repo_root

# https://developer.android.com/training/app-links/verify-android-applinks#support-updated-domain-verification
info "Support the updated domain verification process"
run adb shell am compat enable 175408749 "$_APP_PACKAGE_" || true

# https://developer.android.com/training/app-links/verify-android-applinks#reset-state
info "Reset the state of Android App Links on a device"
run adb shell pm set-app-links --package "$_APP_PACKAGE_" 0 all || true

# https://developer.android.com/training/app-links/verify-android-applinks#invoke-domain-verification
info "Invoke the domain verification process"
run adb shell pm verify-app-links --re-verify "$_APP_PACKAGE_" || true

# https://developer.android.com/training/app-links/verify-android-applinks#review-results
info "Review the verification results"
run adb shell pm get-app-links "$_APP_PACKAGE_" || true
