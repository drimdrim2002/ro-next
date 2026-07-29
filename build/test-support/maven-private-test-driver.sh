#!/bin/sh
set -eu

# Private-only fault driver.  Public mvnw never sources or selects this file.
# Fixture paths are positional arguments so the production launcher has no
# environment gate, marker, or override that a caller can activate.

phase00_driver_path=$0
case "$phase00_driver_path" in
  */*) ;;
  *) phase00_driver_path=$(command -v "$phase00_driver_path") ;;
esac
phase00_driver_directory=${phase00_driver_path%/*}
repository=$(CDPATH= cd -- "$phase00_driver_directory/../.." && pwd)
fail_closed_library="$repository/build/lib/maven-wrapper-fail-closed.sh"
launcher_core="$repository/build/lib/maven-launcher-core.sh"

[ -f "$fail_closed_library" ] && [ ! -L "$fail_closed_library" ] || {
  printf '%s\n' \
    'tool=private-maven-test-driver-library result=NOT_EVALUATED exit=1' >&2
  exit 1
}
. "$fail_closed_library"

mode=${1-}
[ -n "$mode" ] || {
  printf '%s\n' 'private Maven test driver mode is required' >&2
  exit 64
}
shift

case "$mode" in
  launcher)
    maven_home=${1-}
    usr_local_rc=${2-}
    etc_rc=${3-}
    [ "$#" -ge 3 ] || {
      printf '%s\n' 'private launcher requires home and two rc paths' >&2
      exit 64
    }
    shift
    shift
    shift
    [ -f "$launcher_core" ] && [ ! -L "$launcher_core" ] || {
      printf '%s\n' \
        'tool=private-maven-launcher-core result=NOT_EVALUATED exit=1' >&2
      exit 1
    }
    . "$launcher_core"
    phase00_maven_launcher_main \
      "$usr_local_rc" "$etc_rc" "$maven_home" "$@"
    ;;
  conditional-tool)
    tool=${1-}
    [ "$#" -eq 1 ] || {
      printf '%s\n' 'private conditional-tool requires one tool name' >&2
      exit 64
    }
    case "$tool" in
      cat)
        phase00_mvnw_capture cat cat /dev/null
        ;;
      cygpath|curl|expr|javac|ls|mkdir|mktemp|mv|rm|sha256sum|\
      unzip|wget|tar)
        phase00_mvnw_capture "$tool" "$tool" --phase00-fault-probe
        ;;
      *)
        printf 'unknown private Maven conditional fault: %s\n' "$tool" >&2
        printf '%s\n' \
          'tool=private-maven-conditional-driver result=NOT_EVALUATED exit=64' >&2
        exit 64
        ;;
    esac
    ;;
  dynamic-executable)
    executable=${1-}
    [ -n "$executable" ] || {
      printf '%s\n' 'private dynamic executable path is required' >&2
      exit 64
    }
    shift
    if "$executable" "$@"; then
      exit 0
    else
      status=$?
      printf 'tool=dynamic-maven-executable result=NOT_EVALUATED exit=%s\n' \
        "$status" >&2
      exit "$status"
    fi
    ;;
  preflight-launcher)
    maven_executable=${1-}
    wrapper_directory=${2-}
    [ "$#" -eq 2 ] || {
      printf '%s\n' \
        'private preflight requires Maven executable and wrapper directory' >&2
      exit 64
    }
    phase00_mvnw_preflight_launcher \
      "$maven_executable" "$wrapper_directory"
    ;;
  *)
    printf 'unknown private Maven test driver mode: %s\n' "$mode" >&2
    exit 64
    ;;
esac
