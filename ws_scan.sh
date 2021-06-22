#! /usr/bin/env bash

readonly SCRIPT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

readonly UNIFIED_AGENT_JAR="wss-unified-agent.jar"
readonly UNIFIED_AGENT_JAR_MD5_CHECKSUM="F2EB843816A572904954052756EB66E7" # MD5 hash for version 21.6.1
readonly UNIFIED_AGENT_JAR_URL="https://unified-agent.s3.amazonaws.com/wss-unified-agent.jar"

get_unified_agent() {
  if [[ ! -f "${UNIFIED_AGENT_JAR}" ]]; then
    curl \
      --location \
      --remote-name \
      --remote-header-name \
      "${UNIFIED_AGENT_JAR_URL}"
  fi
  if [[ ! -f "${UNIFIED_AGENT_JAR}" ]]; then
    echo "Could not find downloaded Unified Agent" >&2
    exit 1
  fi

  # Verify JAR checksum
  local checksum="$(md5sum ${UNIFIED_AGENT_JAR} | cut --delimiter=" " --fields=1 | awk ' { print toupper($0) }')"
  if [[ "${checksum}" != "${UNIFIED_AGENT_JAR_MD5_CHECKSUM}" ]]; then
    echo "MD5 checksum mismatch." >&2
    echo "expected: ${UNIFIED_AGENT_JAR_MD5_CHECKSUM}" >&2
    echo "computed: ${checksum}" >&2
    exit 2
  fi

  # Verify JAR signature
  if ! jarsigner -verify "${UNIFIED_AGENT_JAR}"; then
    echo "Could not verify jar signature" >&2
    exit 3
  fi
}

local_maven_expression() {
  mvn -q -Dexec.executable="echo" -Dexec.args="\${${1}}" --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec
}

get_product_name() {
  local property="project.name"
  if which maven_expression >&2 2>/dev/null; then
    maven_expression "${property}"
  else
    local_maven_expression "${property}"
  fi
}

get_project_version() {
  local property="project.version"
  if which maven_expression >&2 2>/dev/null; then
    maven_expression "${property}"
  else
    local_maven_expression "${property}"
  fi
}

scan() {
  export WS_PRODUCTNAME=$(get_product_name)
  if [[ -z "${PROJECT_VERSION}" ]]; then
    PROJECT_VERSION=$(get_project_version)
  fi
  export WS_PROJECTNAME="${WS_PRODUCTNAME} ${PROJECT_VERSION%.*}"
  echo "${WS_PRODUCTNAME} - ${WS_PROJECTNAME}"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/docs/java-custom-rules-example/target/java-custom-rules-example-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/docs/java-custom-rules-example"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/external-reports/target/external-reports-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/external-reports"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/its/plugin/tests/target/it-java-plugin-tests-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/its/plugin/tests"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/its/plugin/plugins/java-extension-plugin/target/java-extension-plugin-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/its/plugin/plugins/java-extension-plugin"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/its/ruling/target/it-java-ruling-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/its/ruling"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/java-checks/target/java-checks-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/java-checks"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/java-checks-testkit/target/java-checks-testkit-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/java-checks-testkit"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/java-frontend/target/java-frontend-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/java-frontend"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/java-jsp/target/java-jsp-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/java-jsp"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/java-surefire/target/java-surefire-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/java-surefire"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/java-symbolic-execution/target/java-symbolic-execution-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/java-symbolic-execution"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/jdt/target/jdt-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/jdt"
  java -jar wss-unified-agent.jar -c whitesource.properties -appPath "${SCRIPT_DIRECTORY}/sonar-java-plugin/target/sonar-java-plugin-${PROJECT_VERSION}.jar" -d "${SCRIPT_DIRECTORY}/sonar-java-plugin"
}

get_unified_agent
scan
