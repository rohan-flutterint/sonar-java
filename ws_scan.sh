#! /usr/bin/env bash

readonly UNIFIED_AGENT_JAR="wss-unified-agent.jar"
readonly UNIFIED_AGENT_JAR_MD5_CHECKSUM="8E51FDC3C9EF7FCAE250737BD226C8F6"
readonly UNIFIED_AGENT_JAR_URL="https://unified-agent.s3.amazonaws.com/wss-unified-agent.jar"

readonly MODULE_ANALYZER_JAR="xModuleAnalyzer-21.4.1.jar"
readonly MODULE_ANALYZER_JAR_URL="https://unified-agent.s3.amazonaws.com/xModuleAnalyzer/xModuleAnalyzer-21.4.1.jar"
readonly MODULE_ANALYZER_JAR_MD5_CHECKSUM="2944089B0402957132B3BCDB8EF4E5DB"
readonly MODULE_SETUP_FILE="setup.txt"

get_wss_agent() {
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
    echo -e "MD5 checksum mismatch.\nexpected: ${UNIFIED_AGENT_JAR_MD5_CHECKSUM}\ncomputed: ${checksum}" >&2
    exit 2
  fi

  # Verify JAR signature
  if ! jarsigner -verify "${UNIFIED_AGENT_JAR}"; then
    echo "Could not verify jar signature" >&2
    exit 3
  fi
}

get_multi_module_agent() {
  if [[ ! -f "${MODULE_ANALYZER_JAR}" ]]; then
    curl \
      --location \
      --remote-name \
      --remote-header-name \
      "${MODULE_ANALYZER_JAR_URL}"
  fi
  if [[ ! -f "${MODULE_ANALYZER_JAR}" ]]; then
    echo "Could not find downloaded Unified Agent" >&2
    exit 1
  fi

  # Verify JAR checksum
  local checksum="$(md5sum ${MODULE_ANALYZER_JAR} | cut --delimiter=" " --fields=1 | awk ' { print toupper($0) }')"
  if [[ "${checksum}" != "${MODULE_ANALYZER_JAR_MD5_CHECKSUM}" ]]; then
    echo -e "MD5 checksum mismatch.\nexpected: ${MODULE_ANALYZER_JAR_MD5_CHECKSUM}\ncomputed: ${checksum}" >&2
    exit 2
  fi

  # Verify JAR signature
  if ! jarsigner -verify "${MODULE_ANALYZER_JAR}"; then
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

build() {
  mvn clean install
}

scan() {
  export WS_PRODUCTNAME=$(get_product_name)
  if [[ -z "${PROJECT_VERSION}" ]]; then
    PROJECT_VERSION=$(get_project_version)
  fi
  export WS_PROJECTNAME="${WS_PRODUCTNAME} ${PROJECT_VERSION%.*}"
  echo "${WS_PRODUCTNAME} - ${WS_PROJECTNAME}"
  if [[ ! -f "${MODULE_SETUP_FILE}" ]]; then
    java -jar "${UNIFIED_AGENT_JAR}" -c whitesource.properties -d . -analyzeMultiModule "${MODULE_SETUP_FILE}"
  fi
  java -jar "${MODULE_ANALYZER_JAR}" -xModulePath "${MODULE_SETUP_FILE}" -fsaJarPath "${UNIFIED_AGENT_JAR}" -c whitesource.properties -statusDisplay dynamic
  #java -jar wss-unified-agent.jar -c whitesource.properties -appPath "sonar-java-plugin/target/sonar-java-plugin-${PROJECT_VERSION}.jar" -d sonar-java-plugin
}

build
get_wss_agent
get_multi_module_agent
scan
