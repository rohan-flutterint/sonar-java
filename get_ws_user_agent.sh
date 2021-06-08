#! /usr/bin/env bash

readonly URL="https://unified-agent.s3.amazonaws.com/wss-unified-agent.jar"
readonly UNIFIED_AGENT_JAR="wss-unified-agent.jar"
readonly MD5_CHECKSUM="8E51FDC3C9EF7FCAE250737BD226C8F6"

main() {
  if [[ ! -f "${UNIFIED_AGENT_JAR}" ]]; then
    curl \
      --location \
      --remote-name \
      --remote-header-name \
      "${URL}"
  fi
  if [[ ! -f "${UNIFIED_AGENT_JAR}" ]]; then
    echo "Could not find downloaded Unified Agent" >&2
    exit 1
  fi

  # Verify JAR checksum
  local checksum="$(md5sum ${UNIFIED_AGENT_JAR} | cut --delimiter=" " --fields=1 | awk ' {print toupper($0) }')"
  if [[ "${checksum}" != "${MD5_CHECKSUM}" ]]; then
    echo -e "MD5 checksum mismatch.\nexpected: ${MD5_CHECKSUM}\ncomputed: ${checksum}" >&2
    exit 2
  fi

  # Verify JAR signature
  if ! jarsigner -verify "${UNIFIED_AGENT_JAR}"; then
    echo "Could not verify jar signature" >&2
    exit 3
  fi
}

main
