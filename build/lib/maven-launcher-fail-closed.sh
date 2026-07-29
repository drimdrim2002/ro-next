#!/bin/sh

# Production-only Maven launcher entry.  Public mvnw fixes every launch
# dependency before this file is sourced; test fixtures are available only
# through build/test-support/maven-private-test-driver.sh.

[ -n "${PHASE00_MVNW_FAIL_CLOSED_LIB-}" ] &&
  [ -f "$PHASE00_MVNW_FAIL_CLOSED_LIB" ] || {
    printf '%s\n' \
      'tool=controlled-maven-launcher-library result=NOT_EVALUATED exit=1' >&2
    exit 1
  }
. "$PHASE00_MVNW_FAIL_CLOSED_LIB"

[ -n "${PHASE00_MVNW_CONTROLLED_LAUNCHER_CORE-}" ] &&
  [ -f "$PHASE00_MVNW_CONTROLLED_LAUNCHER_CORE" ] &&
  [ ! -L "$PHASE00_MVNW_CONTROLLED_LAUNCHER_CORE" ] || {
    printf '%s\n' \
      'tool=controlled-maven-launcher-core result=NOT_EVALUATED exit=126' >&2
    exit 126
  }
. "$PHASE00_MVNW_CONTROLLED_LAUNCHER_CORE"

phase00_maven_launcher_main \
  /usr/local/etc/mavenrc \
  /etc/mavenrc \
  "$PHASE00_MVNW_MAVEN_HOME" \
  "$@"
