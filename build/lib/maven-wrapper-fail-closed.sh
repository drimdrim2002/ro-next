#!/bin/sh

# Fail-closed boundary shared by the checked-in Maven Wrapper and its pinned
# Apache Maven 3.9.14 launcher.  It deliberately uses shell builtins for
# diagnostic replay so a failed prerequisite cannot be hidden by another tool.

phase00_mvnw_replay_capture() {
  if [ -n "${PHASE00_MVNW_CAPTURE-}" ]; then
    printf '%s\n' "$PHASE00_MVNW_CAPTURE" >&2
  fi
}

phase00_mvnw_capture() {
  phase00_mvnw_capture_label=$1
  shift
  case $- in
    *e*) phase00_mvnw_capture_had_errexit=1 ;;
    *) phase00_mvnw_capture_had_errexit=0 ;;
  esac
  set +e
  PHASE00_MVNW_CAPTURE=$("$@" 2>&1)
  phase00_mvnw_capture_status=$?
  if [ "$phase00_mvnw_capture_had_errexit" -eq 1 ]; then
    set -e
  else
    set +e
  fi
  if [ "$phase00_mvnw_capture_status" -ne 0 ]; then
    phase00_mvnw_replay_capture
    printf 'tool=%s result=NOT_EVALUATED exit=%s\n' \
      "$phase00_mvnw_capture_label" "$phase00_mvnw_capture_status" >&2
    return "$phase00_mvnw_capture_status"
  fi
}

phase00_mvnw_capture_file_input() {
  phase00_mvnw_capture_label=$1
  phase00_mvnw_capture_input=$2
  shift
  shift
  case $- in
    *e*) phase00_mvnw_capture_had_errexit=1 ;;
    *) phase00_mvnw_capture_had_errexit=0 ;;
  esac
  set +e
  PHASE00_MVNW_CAPTURE=$("$@" <"$phase00_mvnw_capture_input" 2>&1)
  phase00_mvnw_capture_status=$?
  if [ "$phase00_mvnw_capture_had_errexit" -eq 1 ]; then
    set -e
  else
    set +e
  fi
  if [ "$phase00_mvnw_capture_status" -ne 0 ]; then
    phase00_mvnw_replay_capture
    printf 'tool=%s result=NOT_EVALUATED exit=%s\n' \
      "$phase00_mvnw_capture_label" "$phase00_mvnw_capture_status" >&2
    return "$phase00_mvnw_capture_status"
  fi
}

phase00_mvnw_run() {
  phase00_mvnw_run_label=$1
  shift
  if "$@"; then
    return 0
  else
    phase00_mvnw_run_status=$?
    printf 'tool=%s result=NOT_EVALUATED exit=%s\n' \
      "$phase00_mvnw_run_label" "$phase00_mvnw_run_status" >&2
    return "$phase00_mvnw_run_status"
  fi
}

phase00_mvnw_probe_tr() {
  set +e
  PHASE00_MVNW_CAPTURE=$(tr -d '[:space:]' </dev/null 2>&1)
  phase00_mvnw_capture_status=$?
  set -e
  if [ "$phase00_mvnw_capture_status" -ne 0 ]; then
    phase00_mvnw_replay_capture
    printf 'tool=tr result=NOT_EVALUATED exit=%s\n' \
      "$phase00_mvnw_capture_status" >&2
    return "$phase00_mvnw_capture_status"
  fi
}

phase00_mvnw_preflight_wrapper() {
  phase00_mvnw_invocation=$1

  phase00_mvnw_capture uname uname
  phase00_mvnw_os=$PHASE00_MVNW_CAPTURE
  PHASE00_MVNW_OS=$phase00_mvnw_os
  phase00_mvnw_capture dirname dirname "$phase00_mvnw_invocation"
  phase00_mvnw_capture basename basename "$phase00_mvnw_invocation"
  phase00_mvnw_probe_tr
  phase00_mvnw_capture sh sh -c :

  case "$phase00_mvnw_os" in
    CYGWIN*|MINGW*)
      phase00_mvnw_capture cygpath cygpath --version
      ;;
  esac
}

phase00_mvnw_preflight_launcher() {
  phase00_mvnw_launcher=$1
  phase00_mvnw_wrapper_directory=$2

  PHASE00_MVNW_SELECTED_LAUNCHER=$phase00_mvnw_launcher
  PHASE00_MVNW_MAVEN_HOME=${phase00_mvnw_launcher%/bin/mvn}
  PHASE00_MVNW_CONTROLLED_LAUNCHER="$phase00_mvnw_wrapper_directory/build/lib/maven-launcher-fail-closed.sh"
  [ -f "$PHASE00_MVNW_CONTROLLED_LAUNCHER" ] &&
    [ ! -L "$PHASE00_MVNW_CONTROLLED_LAUNCHER" ] || {
      printf 'controlled Maven launcher is missing: %s\n' \
        "$PHASE00_MVNW_CONTROLLED_LAUNCHER" >&2
      printf '%s\n' \
        'tool=controlled-maven-launcher result=NOT_EVALUATED exit=126' >&2
      return 126
    }
  PHASE00_MVNW_CONTROLLED_LAUNCHER_CORE="$phase00_mvnw_wrapper_directory/build/lib/maven-launcher-core.sh"
  [ -f "$PHASE00_MVNW_CONTROLLED_LAUNCHER_CORE" ] &&
    [ ! -L "$PHASE00_MVNW_CONTROLLED_LAUNCHER_CORE" ] || {
      printf 'controlled Maven launcher core is missing: %s\n' \
        "$PHASE00_MVNW_CONTROLLED_LAUNCHER_CORE" >&2
      printf '%s\n' \
        'tool=controlled-maven-launcher-core result=NOT_EVALUATED exit=126' >&2
      return 126
    }
  phase00_mvnw_launcher_pin="$phase00_mvnw_wrapper_directory/.mvn/wrapper/maven-launcher-3.9.14.sha256"
  [ -f "$phase00_mvnw_launcher_pin" ] || {
    printf 'pinned Maven launcher digest is missing: %s\n' \
      "$phase00_mvnw_launcher_pin" >&2
    printf '%s\n' \
      'tool=maven-launcher-pin result=NOT_EVALUATED exit=1' >&2
    return 1
  }
  phase00_mvnw_asset_count=0
  while IFS=' ' read -r phase00_mvnw_expected_asset_digest \
    phase00_mvnw_expected_asset_name
  do
    case "$phase00_mvnw_expected_asset_name" in
      bin/mvn)
        phase00_mvnw_asset_label=dynamic-maven-executable
        ;;
      bin/m2.conf)
        phase00_mvnw_asset_label=maven-launcher-config
        ;;
      boot/plexus-classworlds-2.9.0.jar)
        phase00_mvnw_asset_label=maven-classworlds-inventory
        ;;
      *)
        printf 'unexpected Maven launcher asset pin: %s\n' \
          "$phase00_mvnw_expected_asset_name" >&2
        printf '%s\n' \
          'tool=maven-launcher-pin result=NOT_EVALUATED exit=1' >&2
        return 1
        ;;
    esac
    phase00_mvnw_asset_count=$((phase00_mvnw_asset_count + 1))
    phase00_mvnw_actual_asset="$PHASE00_MVNW_MAVEN_HOME/$phase00_mvnw_expected_asset_name"
    [ -f "$phase00_mvnw_actual_asset" ] &&
      [ ! -L "$phase00_mvnw_actual_asset" ] || {
        printf 'pinned Maven launcher asset is missing or not regular: %s\n' \
          "$phase00_mvnw_actual_asset" >&2
        printf 'tool=%s result=NOT_EVALUATED exit=126\n' \
          "$phase00_mvnw_asset_label" >&2
        return 126
      }
    phase00_mvnw_capture 'shasum(maven-launcher-digest)' \
      shasum -a 256 "$phase00_mvnw_actual_asset"
    set -- $PHASE00_MVNW_CAPTURE
    phase00_mvnw_actual_asset_digest=${1-}
    [ "$phase00_mvnw_actual_asset_digest" = \
      "$phase00_mvnw_expected_asset_digest" ] || {
        printf 'pinned Maven launcher asset digest mismatch: %s\n' \
          "$phase00_mvnw_actual_asset" >&2
        printf 'tool=%s result=NOT_EVALUATED exit=126\n' \
          "$phase00_mvnw_asset_label" >&2
        return 126
      }
  done <"$phase00_mvnw_launcher_pin"
  [ "$phase00_mvnw_asset_count" -eq 3 ] || {
    printf 'Maven launcher asset pin count is not 3: %s\n' \
      "$phase00_mvnw_asset_count" >&2
    printf '%s\n' \
      'tool=maven-launcher-pin result=NOT_EVALUATED exit=1' >&2
    return 1
  }
  [ "$PHASE00_MVNW_MAVEN_HOME/bin/mvn" = "$phase00_mvnw_launcher" ] || {
    printf '%s\n' 'dynamic Maven executable is outside pinned Maven home' >&2
    printf '%s\n' \
      'tool=dynamic-maven-executable result=NOT_EVALUATED exit=126' >&2
    return 126
  }
}

phase00_mvnw_exec_launcher() {
  PHASE00_MVNW_FAIL_CLOSED_LIB=$phase00_mvnw_fail_closed
  export PHASE00_MVNW_FAIL_CLOSED_LIB PHASE00_MVNW_MAVEN_HOME \
    PHASE00_MVNW_CONTROLLED_LAUNCHER_CORE PHASE00_MVNW_OS JAVA_HOME
  # Source the immutable controlled launcher in this already-running /bin/sh.
  # This removes a second PATH-resolved shell/executable boundary.  The sourced
  # launcher either emits a structured prerequisite failure or execs Java
  # Classworlds with the original Maven result status.
  . "$PHASE00_MVNW_CONTROLLED_LAUNCHER"
}
