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

if [ "$#" -ne 2 ]; then
    printf 'usage: %s <effective-pom-output> <comparison-report>\n' "$0" >&2
    exit 2
fi

absolute_output() {
    case "$1" in
        /*) printf '%s\n' "$1" ;;
        *) printf '%s/%s\n' "$repository" "$1" ;;
    esac
}

output=$(absolute_output "$1")
report=$(absolute_output "$2")
if [ -f "$report" ] && [ ! -L "$report" ]; then
    : >"$report"
fi
maven_repository=${PHASE00_MAVEN_REPO:-"$repository/target/phase-00-m2"}
plugin_coordinate=org.apache.maven.plugins:maven-help-plugin:3.5.1

case "$maven_repository" in
    /*) ;;
    *) maven_repository="$repository/$maven_repository" ;;
esac
for target in "$output" "$report"; do
    if [ -e "$target" ] || [ -L "$target" ]; then
        printf 'effective POM evidence output already exists: %s\n' "$target" >&2
        exit 1
    fi
    target_parent=${target%/*}
    test -d "$target_parent"
done
test -d "$maven_repository"
test -f "$maven_repository/org/apache/maven/plugins/maven-help-plugin/3.5.1/maven-help-plugin-3.5.1.jar"
test -f "$maven_repository/org/apache/maven/plugins/maven-help-plugin/3.5.1/maven-help-plugin-3.5.1.pom"

gate_make_temporary_directory 'mktemp(effective-pom)' \
    "${TMPDIR:-/tmp}/ro-next-effective-pom.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

gate_capture_checked 'mvnw(version-for-effective-pom)' \
    "$temporary/maven-version.txt" "$temporary/maven-version.err" \
    "$repository/mvnw" --version
gate_require_match 'pinned Maven executable version' \
    "$temporary/maven-version-match.txt" "$temporary/maven-version.detector.err" \
    grep -F 'Apache Maven 3.9.14 ' "$temporary/maven-version.txt"

generate_official_effective_pom() {
    run_id=$1
    run_output=$2
    gate_capture_checked "maven-help-plugin:effective-pom(run-$run_id)" \
        "$temporary/run-$run_id.stdout" "$temporary/run-$run_id.stderr" \
        "$repository/mvnw" -B -ntp -o -Dstyle.color=never \
        -Dmaven.repo.local="$maven_repository" \
        "$plugin_coordinate:effective-pom" \
        -Doutput="$run_output"
    test -s "$run_output"
}

first="$temporary/effective-pom-first.xml"
second="$temporary/effective-pom-second.xml"
generate_official_effective_pom 1 "$first"
generate_official_effective_pom 2 "$second"
gate_compare_files 'official effective POM repeatability' \
    "$first" "$second" \
    "$temporary/effective-pom.cmp.out" "$temporary/effective-pom.cmp.err"

require_plugin_pin() {
    artifact=$1
    version=$2
    label=$3
    slug=$4
    gate_require_match "$label" \
        "$temporary/$slug.matches" "$temporary/$slug.detector.err" \
        awk -v artifact="$artifact" -v version="$version" '
            /<plugin>/ {
                in_plugin = 1
                artifact_ok = version_ok = 0
            }
            in_plugin && $0 ~ "<artifactId>" artifact "</artifactId>" {
                artifact_ok = 1
            }
            in_plugin && $0 ~ "<version>" version "</version>" {
                version_ok = 1
            }
            in_plugin && /<\/plugin>/ {
                if (artifact_ok && version_ok) {
                    print
                    found = 1
                    exit
                }
                in_plugin = 0
            }
            END { if (!found) exit 1 }
        ' "$first"
}

require_plugin_execution() {
    artifact=$1
    execution_id=$2
    phase=$3
    goal=$4
    slug=$5
    gate_require_match "active lifecycle execution $artifact:$execution_id" \
        "$temporary/$slug.matches" "$temporary/$slug.detector.err" \
        awk -v artifact="$artifact" -v execution_id="$execution_id" \
            -v phase="$phase" -v goal="$goal" '
            /<plugin>/ {
                in_plugin = 1
                plugin_artifact = ""
            }
            in_plugin && /<artifactId>/ && plugin_artifact == "" {
                value = $0
                sub(/^.*<artifactId>/, "", value)
                sub(/<\/artifactId>.*$/, "", value)
                plugin_artifact = value
            }
            in_plugin && plugin_artifact == artifact && /<execution>/ {
                in_execution = 1
                id_ok = phase_ok = goal_ok = 0
            }
            in_execution && $0 ~ "<id>" execution_id "</id>" { id_ok = 1 }
            in_execution && $0 ~ "<phase>" phase "</phase>" { phase_ok = 1 }
            in_execution && $0 ~ "<goal>" goal "</goal>" { goal_ok = 1 }
            in_execution && /<\/execution>/ {
                if (id_ok && phase_ok && goal_ok) {
                    print
                    found = 1
                    exit
                }
                in_execution = 0
            }
            in_plugin && /<\/plugin>/ {
                in_plugin = 0
                in_execution = 0
            }
            END { if (!found) exit 1 }
        ' "$first"
}

require_dependency_management_entry() {
    group=$1
    artifact=$2
    version=$3
    slug=$4
    gate_require_match "dependencyManagement $group:$artifact:$version" \
        "$temporary/$slug.matches" "$temporary/$slug.detector.err" \
        awk -v group="$group" -v artifact="$artifact" -v version="$version" '
            /<dependencyManagement>/ { in_management = 1 }
            in_management && /<dependency>/ {
                in_dependency = 1
                group_ok = artifact_ok = version_ok = 0
            }
            in_dependency && $0 ~ "<groupId>" group "</groupId>" {
                group_ok = 1
            }
            in_dependency && $0 ~ "<artifactId>" artifact "</artifactId>" {
                artifact_ok = 1
            }
            in_dependency && $0 ~ "<version>" version "</version>" {
                version_ok = 1
            }
            in_dependency && /<\/dependency>/ {
                if (group_ok && artifact_ok && version_ok) {
                    print
                    found = 1
                    exit
                }
                in_dependency = 0
            }
            /<\/dependencyManagement>/ { in_management = 0 }
            END { if (!found) exit 1 }
        ' "$first"
}

gate_require_match 'effective root coordinate structure' \
    "$temporary/root-coordinate.matches" \
    "$temporary/root-coordinate.detector.err" \
    awk '
        /<project xmlns=/ { project_count++ }
        project_count == 1 && /<groupId>com[.]ronext<\/groupId>/ {
            group_ok = 1
        }
        project_count == 1 && /<artifactId>ro-next-parent<\/artifactId>/ {
            artifact_ok = 1
        }
        project_count == 1 && /<version>0[.]1[.]0-SNAPSHOT<\/version>/ {
            version_ok = 1
        }
        project_count == 1 && group_ok && artifact_ok && version_ok {
            print
            found = 1
            exit
        }
        END { if (!found) exit 1 }
    ' "$first"
gate_require_match 'effective reactor project count' \
    "$temporary/reactor-project-count.matches" \
    "$temporary/reactor-project-count.detector.err" \
    awk '
        /<project xmlns=/ { project_count++ }
        END {
            if (project_count == 13) {
                print project_count
                exit 0
            }
            exit 1
        }
    ' "$first"
gate_require_no_match 'unresolved effective POM expression' \
    "$temporary/unresolved.matches" "$temporary/unresolved.detector.err" \
    grep -E '[$][{][^}]+}' "$first"
gate_require_match 'effective Java compiler release 25' \
    "$temporary/release.matches" "$temporary/release.detector.err" \
    grep -F '<release>25</release>' "$first"
gate_require_match 'resolved multi-module repository property' \
    "$temporary/repository-property.matches" \
    "$temporary/repository-property.detector.err" \
    grep -F "<phase00.repoRoot>$repository</phase00.repoRoot>" "$first"

require_plugin_pin maven-compiler-plugin 3.14.1 \
    'compiler pluginManagement pin' compiler-pin
require_plugin_pin maven-enforcer-plugin 3.6.1 \
    'enforcer pluginManagement pin' enforcer-pin
require_plugin_pin maven-surefire-plugin 3.5.4 \
    'Surefire pluginManagement pin' surefire-pin
require_plugin_pin maven-toolchains-plugin 3.2.0 \
    'toolchains pluginManagement pin' toolchains-pin
require_plugin_pin maven-help-plugin 3.5.1 \
    'Help Plugin pluginManagement pin' help-pin

require_dependency_management_entry \
    org.junit.jupiter junit-jupiter 5.13.1 junit-jupiter-management
require_dependency_management_entry \
    org.junit.platform junit-platform-launcher 1.13.1 junit-platform-management

gate_require_no_match 'Phase 00 root profile declarations' \
    "$temporary/profiles.matches" "$temporary/profiles.detector.err" \
    grep -E '<profiles>|<profile>' "$first"
for module in rpdptw build legacy; do
    gate_require_match "effective root module $module" \
        "$temporary/module-$module.matches" \
        "$temporary/module-$module.detector.err" \
        grep -F "<module>$module</module>" "$first"
done
gate_require_match 'effective Enforcer policy execution' \
    "$temporary/enforcer-execution.matches" \
    "$temporary/enforcer-execution.detector.err" \
    grep -F '<id>enforce-phase-00-build-policy</id>' "$first"
gate_require_match 'effective Toolchains policy execution' \
    "$temporary/toolchains-execution.matches" \
    "$temporary/toolchains-execution.detector.err" \
    grep -F '<id>select-reviewed-java-25-toolchain</id>' "$first"
gate_require_match 'effective Surefire zero-test policy' \
    "$temporary/surefire-zero-test.matches" \
    "$temporary/surefire-zero-test.detector.err" \
    grep -F '<failIfNoTests>false</failIfNoTests>' "$first"
gate_require_match 'effective reproducible-build timestamp policy' \
    "$temporary/output-timestamp.matches" \
    "$temporary/output-timestamp.detector.err" \
    grep -F '<project.build.outputTimestamp>2026-07-28T00:00:00Z</project.build.outputTimestamp>' \
    "$first"

require_plugin_execution maven-clean-plugin default-clean clean clean \
    lifecycle-clean
require_plugin_execution maven-install-plugin default-install install install \
    lifecycle-install
require_plugin_execution maven-deploy-plugin default-deploy deploy deploy \
    lifecycle-deploy
require_plugin_execution maven-site-plugin default-site site site \
    lifecycle-site

effective_pom_stage="$temporary/effective-pom-publish.xml"
report_stage="$temporary/effective-pom-comparison-publish.txt"
gate_capture_checked 'cp(stage-official-effective-pom-evidence)' \
    "$temporary/publish-stage.out" "$temporary/publish-stage.err" \
    cp "$first" "$effective_pom_stage"
gate_sha256_file "$effective_pom_stage" "$temporary/effective-pom-output"
effective_pom_digest=$GATE_SHA256

{
    printf '%s\n' 'EFFECTIVE_POM_GENERATOR=OFFICIAL_MAVEN_HELP_PLUGIN'
    printf 'PLUGIN_COORDINATE=%s\n' "$plugin_coordinate"
    printf '%s\n' 'MAVEN_VERSION=3.9.14'
    printf 'COMMAND_ENV_MAVEN_SKIP_RC=%s\n' "${MAVEN_SKIP_RC-<unset>}"
    printf '%s\n' 'COMMAND=./mvnw -B -ntp -o -Dstyle.color=never -Dmaven.repo.local=<controlled-isolated-cache> org.apache.maven.plugins:maven-help-plugin:3.5.1:effective-pom -Doutput=<run-output>'
    printf '%s\n' 'RUN_1_EXIT=0'
    printf '%s\n' 'RUN_2_EXIT=0'
    printf '%s\n' 'NETWORK_MODE=OFFLINE'
    printf '%s\n' 'CACHE_PROVENANCE=ALLOWLISTED_SHA256_VERIFIED_MAVEN_CENTRAL_SNAPSHOT'
    printf '%s\n' 'DETERMINISTIC_BYTE_COMPARISON=PASS'
    printf '%s\n' 'STRUCTURAL_COMPARISON=PASS'
    printf '%s\n' 'UNRESOLVED_EXPRESSION_COUNT=0'
    printf '%s\n' 'DECLARED_PROFILE_COUNT=0'
    printf '%s\n' 'REACTOR_PROJECT_COUNT=13'
    printf '%s\n' 'LIFECYCLE_EXECUTIONS=clean,install,deploy,site'
    printf '%s\n' 'MODULE_POLICY=rpdptw,build,legacy'
    printf 'OUTPUT_SHA256=%s\n' "$effective_pom_digest"
} >"$report_stage"

output_publish_stage="${output%/*}/.${output##*/}.publishing.$$"
report_publish_stage="${report%/*}/.${report##*/}.publishing.$$"
gate_capture_checked 'cp(prepare-effective-pom-atomic-publish)' \
    "$temporary/output-publish-copy.out" \
    "$temporary/output-publish-copy.err" \
    cp "$effective_pom_stage" "$output_publish_stage"
gate_capture_checked 'cp(prepare-effective-pom-report-atomic-publish)' \
    "$temporary/report-publish-copy.out" \
    "$temporary/report-publish-copy.err" \
    cp "$report_stage" "$report_publish_stage"
gate_capture_checked 'mv(publish-official-effective-pom)' \
    "$temporary/output-publish-move.out" \
    "$temporary/output-publish-move.err" \
    mv "$output_publish_stage" "$output"
gate_capture_checked 'mv(publish-effective-pom-comparison-report)' \
    "$temporary/report-publish-move.out" \
    "$temporary/report-publish-move.err" \
    mv "$report_publish_stage" "$report"

gate_replay_text_file "$report"
printf 'OUTPUT=%s\n' "$output"
