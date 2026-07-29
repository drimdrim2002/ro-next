#!/bin/sh
set -eu

phase00_script_path=$0
case "$phase00_script_path" in
    */*) ;;
    *) phase00_script_path=$(command -v "$phase00_script_path") ;;
esac
phase00_script_directory=${phase00_script_path%/*}
repository=$(CDPATH= cd -- "$phase00_script_directory/.." && pwd)
. "$repository/build/lib/fail-closed-gates.sh"
properties="$repository/.mvn/wrapper/maven-wrapper.properties"
gate_make_temporary_directory 'mktemp(toolchain-verify)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-toolchain.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

test -x "$repository/mvnw"
test -f "$properties"
gate_require_match 'Maven wrapper version' \
    "$temporary/wrapper-version.matches" "$temporary/wrapper-version.err" \
    grep -Fx 'wrapperVersion=3.3.4' "$properties"
gate_require_match 'Maven wrapper distribution type' \
    "$temporary/wrapper-type.matches" "$temporary/wrapper-type.err" \
    grep -Fx 'distributionType=only-script' "$properties"
gate_require_match 'Maven wrapper distribution URL' \
    "$temporary/wrapper-url.matches" "$temporary/wrapper-url.err" \
    grep -Fx 'distributionUrl=https://archive.apache.org/dist/maven/maven-3/3.9.14/binaries/apache-maven-3.9.14-bin.zip' "$properties"
gate_require_match 'Maven wrapper distribution checksum' \
    "$temporary/wrapper-sha.matches" "$temporary/wrapper-sha.err" \
    grep -Fx 'distributionSha256Sum=55fadd669532a3205d5db95f490bf13971d8b0843526f407f29db0e61f074ab3' "$properties"

gate_capture_checked 'mvnw(toolchain-version)' \
    "$temporary/maven-version.stdout" \
    "$temporary/maven-version.stderr" \
    "$repository/mvnw" --version
: >"$temporary/maven-version.txt"
gate_replay_text_file "$temporary/maven-version.stdout" \
    >>"$temporary/maven-version.txt"
gate_replay_text_file "$temporary/maven-version.stderr" \
    >>"$temporary/maven-version.txt"
gate_capture_checked 'java(toolchain-version)' \
    "$temporary/java-version.stdout" \
    "$temporary/java-version.stderr" \
    java -version
: >"$temporary/java-version.txt"
gate_replay_text_file "$temporary/java-version.stdout" \
    >>"$temporary/java-version.txt"
gate_replay_text_file "$temporary/java-version.stderr" \
    >>"$temporary/java-version.txt"

gate_require_match 'Maven executable version' \
    "$temporary/maven-version.matches" "$temporary/maven-version.err" \
    grep -F 'Apache Maven 3.9.14 ' "$temporary/maven-version.txt"
gate_require_match 'Maven runtime Java version' \
    "$temporary/maven-java.matches" "$temporary/maven-java.err" \
    grep -E 'Java version: 25([.]|,)' "$temporary/maven-version.txt"
gate_require_match 'Java executable version' \
    "$temporary/java-version.matches" "$temporary/java-version.err" \
    grep -E 'version "25([.]|")' "$temporary/java-version.txt"

printf '%s\n' 'PHASE00_TOOLCHAIN_POLICY=PASS'
printf '%s\n' 'WRAPPER_VERSION=3.3.4'
printf '%s\n' 'MAVEN_VERSION=3.9.14'
printf '%s\n' 'JAVA_RANGE=[25,26)'
printf '%s\n' 'DISTRIBUTION_SHA256=55fadd669532a3205d5db95f490bf13971d8b0843526f407f29db0e61f074ab3'
gate_replay_text_file "$temporary/maven-version.txt"
