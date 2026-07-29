#!/bin/sh

# Shared Maven 3.9.14 launch implementation.  The production entry point calls
# this function with fixed system-rc paths and the Maven home that mvnw has
# already pinned.  The private test driver calls the same function with
# positional fixture paths; no environment switch can select that driver or
# alter the arguments supplied by the public entry point.

phase00_maven_launcher_main() {
  phase00_maven_usr_local_rc=$1
  phase00_maven_etc_rc=$2
  phase00_maven_input_home=$3
  shift
  shift
  shift

  command -v phase00_mvnw_capture >/dev/null 2>&1 || {
    printf '%s\n' \
      'tool=controlled-maven-launcher-library result=NOT_EVALUATED exit=1' >&2
    exit 1
  }

  # mvnw enters with errexit, nounset and noglob enabled.  Apache Maven
  # 3.9.14's bin/mvn enters without those wrapper-private options, and mavenrc
  # is allowed to choose its own shell options.  Restore the official launcher
  # baseline before sourcing any system or user configuration.
  set +e
  set +u
  set +f

  [ -n "$phase00_maven_input_home" ] &&
    [ -d "$phase00_maven_input_home" ] || {
    printf '%s\n' \
      'tool=controlled-maven-home result=NOT_EVALUATED exit=1' >&2
    exit 1
  }

  # Apache Maven 3.9.14 bin/mvn lines 31-45: non-empty MAVEN_SKIP_RC
  # skips the complete chain; otherwise system files precede the user file.
  if [ -z "${MAVEN_SKIP_RC-}" ]; then
  if [ -f "$phase00_maven_usr_local_rc" ]; then
    [ -r "$phase00_maven_usr_local_rc" ] || {
      printf 'Maven rc file is not readable: %s\n' \
        "$phase00_maven_usr_local_rc" >&2
      printf '%s\n' 'tool=mavenrc-source result=NOT_EVALUATED exit=1' >&2
      exit 1
    }
    phase00_mvnw_capture mavenrc-source \
      sh -n "$phase00_maven_usr_local_rc" || exit $?
    . "$phase00_maven_usr_local_rc"
    phase00_maven_rc_status=$?
    case "$phase00_maven_rc_status" in
      2|126|127)
        printf 'tool=mavenrc-source result=NOT_EVALUATED exit=%s\n' \
          "$phase00_maven_rc_status" >&2
        exit "$phase00_maven_rc_status"
        ;;
    esac
  fi
  if [ -f "$phase00_maven_etc_rc" ]; then
    [ -r "$phase00_maven_etc_rc" ] || {
      printf 'Maven rc file is not readable: %s\n' "$phase00_maven_etc_rc" >&2
      printf '%s\n' 'tool=mavenrc-source result=NOT_EVALUATED exit=1' >&2
      exit 1
    }
    phase00_mvnw_capture mavenrc-source \
      sh -n "$phase00_maven_etc_rc" || exit $?
    . "$phase00_maven_etc_rc"
    phase00_maven_rc_status=$?
    case "$phase00_maven_rc_status" in
      2|126|127)
        printf 'tool=mavenrc-source result=NOT_EVALUATED exit=%s\n' \
          "$phase00_maven_rc_status" >&2
        exit "$phase00_maven_rc_status"
        ;;
    esac
  fi
  if [ -f "${HOME-}/.mavenrc" ]; then
    [ -r "${HOME-}/.mavenrc" ] || {
      printf 'Maven rc file is not readable: %s\n' "${HOME-}/.mavenrc" >&2
      printf '%s\n' 'tool=mavenrc-source result=NOT_EVALUATED exit=1' >&2
      exit 1
    }
    phase00_mvnw_capture mavenrc-source \
      sh -n "${HOME-}/.mavenrc" || exit $?
    . "${HOME-}/.mavenrc"
    phase00_maven_rc_status=$?
    case "$phase00_maven_rc_status" in
      2|126|127)
        printf 'tool=mavenrc-source result=NOT_EVALUATED exit=%s\n' \
          "$phase00_maven_rc_status" >&2
        exit "$phase00_maven_rc_status"
        ;;
    esac
  fi
  fi

  MAVEN_HOME=$phase00_maven_input_home

cygwin=false
mingw=false
phase00_mvnw_capture uname uname || exit $?
case "$PHASE00_MVNW_CAPTURE" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true ;;
esac

if $cygwin; then
  phase00_mvnw_capture cygpath cygpath --unix "$MAVEN_HOME" || exit $?
  MAVEN_HOME=$PHASE00_MVNW_CAPTURE
  if [ -n "${JAVA_HOME-}" ]; then
    phase00_mvnw_capture cygpath cygpath --unix "$JAVA_HOME" || exit $?
    JAVA_HOME=$PHASE00_MVNW_CAPTURE
  fi
  if [ -n "${CLASSPATH-}" ]; then
    phase00_mvnw_capture cygpath cygpath --path --unix "$CLASSPATH" || exit $?
    CLASSPATH=$PHASE00_MVNW_CAPTURE
  fi
fi

if $mingw; then
  phase00_maven_home_status=0
  phase00_maven_home=$(
    CDPATH= cd -- "$MAVEN_HOME" && pwd
  ) || phase00_maven_home_status=$?
  if [ "$phase00_maven_home_status" -ne 0 ]; then
    printf 'tool=cd result=NOT_EVALUATED exit=%s\n' \
      "$phase00_maven_home_status" >&2
    exit "$phase00_maven_home_status"
  fi
  MAVEN_HOME=$phase00_maven_home
  if [ -n "${JAVA_HOME-}" ]; then
    phase00_maven_java_home_status=0
    phase00_maven_java_home=$(
      CDPATH= cd -- "$JAVA_HOME" && pwd
    ) || phase00_maven_java_home_status=$?
    if [ "$phase00_maven_java_home_status" -ne 0 ]; then
      printf 'tool=cd result=NOT_EVALUATED exit=%s\n' \
        "$phase00_maven_java_home_status" >&2
      exit "$phase00_maven_java_home_status"
    fi
    JAVA_HOME=$phase00_maven_java_home
  fi
fi

phase00_maven_home=$MAVEN_HOME

if [ -z "${JAVA_HOME-}" ]; then
  phase00_mvnw_capture 'command(java)' command -v java || exit $?
  phase00_maven_java=$PHASE00_MVNW_CAPTURE
else
  phase00_maven_java=$JAVA_HOME/bin/java
fi
[ -x "$phase00_maven_java" ] || {
  printf '%s\n' \
    'The JAVA_HOME environment variable is not defined correctly,' \
    'this environment variable is needed to run this program.' >&2
  printf '%s\n' 'tool=java result=NOT_EVALUATED exit=1' >&2
  exit 1
}
# This silent probe distinguishes a missing/broken executable prerequisite from
# a later evaluated JVM-option or Maven result.  It runs after mavenrc so a
# user/system JAVA_HOME mutation is checked at the actual controlled boundary.
phase00_mvnw_capture java "$phase00_maven_java" -version || exit $?

phase00_maven_classworlds_jar=
for phase00_maven_candidate in \
  "$phase00_maven_home"/boot/plexus-classworlds-*.jar
do
  if [ -f "$phase00_maven_candidate" ]; then
    [ -z "$phase00_maven_classworlds_jar" ] || {
      printf '%s\n' 'multiple Maven Classworlds launchers found' >&2
      printf '%s\n' \
        'tool=maven-classworlds-inventory result=NOT_EVALUATED exit=1' >&2
      exit 1
    }
    phase00_maven_classworlds_jar=$phase00_maven_candidate
  fi
done
[ -n "$phase00_maven_classworlds_jar" ] || {
  printf '%s\n' 'Maven Classworlds launcher jar is missing' >&2
  printf '%s\n' \
    'tool=maven-classworlds-inventory result=NOT_EVALUATED exit=1' >&2
  exit 1
}
[ -f "$phase00_maven_home/bin/m2.conf" ] &&
  [ ! -L "$phase00_maven_home/bin/m2.conf" ] || {
    printf '%s\n' 'Maven Classworlds configuration is missing or not regular' >&2
    printf '%s\n' \
      'tool=maven-launcher-config result=NOT_EVALUATED exit=1' >&2
    exit 1
  }

if $cygwin; then
  phase00_mvnw_capture cygpath \
    cygpath --path --windows "$phase00_maven_home" || exit $?
  phase00_maven_home=$PHASE00_MVNW_CAPTURE
  phase00_mvnw_capture cygpath \
    cygpath --path --windows "$phase00_maven_classworlds_jar" || exit $?
  phase00_maven_classworlds_jar=$PHASE00_MVNW_CAPTURE
  if [ -n "${JAVA_HOME-}" ]; then
    phase00_mvnw_capture cygpath \
      cygpath --path --windows "$JAVA_HOME" || exit $?
    JAVA_HOME=$PHASE00_MVNW_CAPTURE
  fi
  if [ -n "${CLASSPATH-}" ]; then
    phase00_mvnw_capture cygpath \
      cygpath --path --windows "$CLASSPATH" || exit $?
    CLASSPATH=$PHASE00_MVNW_CAPTURE
  fi
fi

if [ -n "${MAVEN_BASEDIR-}" ]; then
  phase00_maven_basedir=$MAVEN_BASEDIR
else
  phase00_maven_basedir=$(pwd)
  phase00_maven_expect_file=0
  for phase00_maven_argument in "$@"; do
    if [ "$phase00_maven_expect_file" -eq 1 ]; then
      if [ -d "$phase00_maven_argument" ]; then
        phase00_maven_basedir_status=0
        phase00_maven_basedir=$(
          CDPATH= cd -- "$phase00_maven_argument" && pwd -P
        ) || phase00_maven_basedir_status=$?
        if [ "$phase00_maven_basedir_status" -ne 0 ]; then
          printf 'tool=cd result=NOT_EVALUATED exit=%s\n' \
            "$phase00_maven_basedir_status" >&2
          exit "$phase00_maven_basedir_status"
        fi
      elif [ -f "$phase00_maven_argument" ]; then
        phase00_mvnw_capture dirname dirname "$phase00_maven_argument" || exit $?
        phase00_maven_basedir=$PHASE00_MVNW_CAPTURE
        phase00_maven_basedir_status=0
        phase00_maven_basedir=$(
          CDPATH= cd -- "$phase00_maven_basedir" && pwd -P
        ) || phase00_maven_basedir_status=$?
        if [ "$phase00_maven_basedir_status" -ne 0 ]; then
          printf 'Directory %s extracted from the -f/--file command-line argument %s does not exist\n' \
            "$phase00_maven_basedir" "$phase00_maven_argument" >&2
          printf 'tool=cd result=NOT_EVALUATED exit=%s\n' \
            "$phase00_maven_basedir_status" >&2
          exit "$phase00_maven_basedir_status"
        fi
      else
        # Official bin/mvn emits this once in basedir discovery and Maven CLI
        # emits the same diagnostic again before returning status 1.
        printf 'POM file %s specified with the -f/--file command line argument does not exist\n' \
          "$phase00_maven_argument" >&2
        printf 'POM file %s specified with the -f/--file command line argument does not exist\n' \
          "$phase00_maven_argument" >&2
        exit 1
      fi
      break
    fi
    case "$phase00_maven_argument" in
      -f|--file) phase00_maven_expect_file=1 ;;
    esac
  done

  while [ ! -d "$phase00_maven_basedir/.mvn" ] &&
    [ "$phase00_maven_basedir" != / ]; do
    phase00_maven_basedir_status=0
    phase00_maven_basedir=$(
      CDPATH= cd -- "$phase00_maven_basedir/.." && pwd
    ) || phase00_maven_basedir_status=$?
    if [ "$phase00_maven_basedir_status" -ne 0 ]; then
      printf 'tool=cd result=NOT_EVALUATED exit=%s\n' \
        "$phase00_maven_basedir_status" >&2
      exit "$phase00_maven_basedir_status"
    fi
  done
fi

MAVEN_PROJECTBASEDIR=$phase00_maven_basedir
phase00_maven_opts=${MAVEN_OPTS-}
if [ -f "$phase00_maven_basedir/.mvn/jvm.config" ]; then
  phase00_mvnw_capture_file_input tr \
    "$phase00_maven_basedir/.mvn/jvm.config" tr -s '\r\n' '  ' || exit $?
  phase00_maven_opts="$PHASE00_MVNW_CAPTURE $phase00_maven_opts"
else
  phase00_maven_opts=" $phase00_maven_opts"
fi

if $cygwin; then
  phase00_mvnw_capture cygpath \
    cygpath --path --windows "$MAVEN_PROJECTBASEDIR" || exit $?
  MAVEN_PROJECTBASEDIR=$PHASE00_MVNW_CAPTURE
fi

if "$phase00_maven_java" \
  --enable-native-access=ALL-UNNAMED -version >/dev/null 2>&1
then
  phase00_maven_opts="--enable-native-access=ALL-UNNAMED $phase00_maven_opts"
  if $mingw; then
    phase00_maven_opts="--add-opens java.base/java.lang=ALL-UNNAMED $phase00_maven_opts"
  fi
fi

MAVEN_CMD_LINE_ARGS="${MAVEN_CONFIG-} $@"
export MAVEN_PROJECTBASEDIR MAVEN_CMD_LINE_ARGS

# Maven goal/test failures are evaluated results from Classworlds and must keep
# their original status without being relabeled as launcher execution errors.
  exec "$phase00_maven_java" \
  $phase00_maven_opts \
  ${MAVEN_DEBUG_OPTS-} \
  -classpath "$phase00_maven_classworlds_jar" \
  "-Dclassworlds.conf=${phase00_maven_home}/bin/m2.conf" \
  "-Dmaven.home=${phase00_maven_home}" \
  "-Dlibrary.jansi.path=${phase00_maven_home}/lib/jansi-native" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  org.codehaus.plexus.classworlds.launcher.Launcher \
    ${MAVEN_ARGS-} "$@"
}
