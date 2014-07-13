#!/bin/bash

#
# File: macos_job-xcodebuild_test.sh
#
# This script is intended to be run regularly, typically once every 5 min.
# It must be run on a Mac OS X system with Xcode installed.
# It aims at getting the last revision of the "unm-ios" GitHub repository, which
# is an Xcode project, run the logic tests on it, and export the test results to
# an other, named alike, "unm-ios-ut-results" GitHub repository.
# ("-ut-" means: "Unit Tests")
#
# The script works like this:
#
#  1. Just as a basic Jenkins job, poll the (source) GitHub repository every
#       5 min., thanks to crontab.
#
#  2. If a change is detected, pull the repository,
#
#  3. then run "xcodebuild test" to run the XCTest suites from the project,
#       capturing the standard output and error into a file.
#
#  4. Once this is done, add the test results file to another GitHub repository,
#       "unm-ios-ut-results", commit and push to GitHub.
#
# Parameters:
#    "${1}": WORKSPACE: Where to put the build files. Defaults to ../../../ 
#               (This script will create ${WORKSPACE}/target/)
#

# ======== 1. ENVIRONMENT ========

WORKSPACE="${1}"

# ======== 1.1. ENVIRONMENT: WORKSPACE ========

if [ -z "${WORKSPACE}" ]; then
  WORKSPACE="$(cd "$(dirname "${0}")/../../../"; pwd)"
fi

echo "WORKSPACE: ${WORKSPACE}"

if [ ! -d "${WORKSPACE}/target" ]; then mkdir -p "${WORKSPACE}/target"; fi

PREV_HEAD="${WORKSPACE}/target/macos_job-xcodebuild_test-prev_head"
LATEST_HEAD="${WORKSPACE}/target/macos_job-xcodebuild_test-latest_head"
BUILD_LOG="${WORKSPACE}/target/macos_job-xcodebuild_test-build.log"
UNM_IOS_REPO="${WORKSPACE}/target/unm-ios"

if [ ! -d "${WORKSPACE}/target" ]; then mkdir -p "${WORKSPACE}/target"; fi

# ======== 1.2. ENVIRONMENT: UNM_IOS_REPO AND OTHERS ========

if [ ! -d "${UNM_IOS_REPO}" ]; then mkdir -p "${UNM_IOS_REPO}"; fi

if ! cd "${UNM_IOS_REPO}"; then
  echo "** Error: Cannot change dir to: ${UNM_IOS_REPO}" >&2 
  echo "Exiting"
  exit 1
fi

echo "pwd: $(pwd)" > "${BUILD_LOG}"

echo "CURRENT_DIR=UNM_IOS_REPO: $(pwd)"

# ======== 2. GIT POLLING ========

if [ ! -d .git ]; then # git clone if the repo is not present already
  rm -f "${PREV_HEAD}"
  git clone https://github.com/univmobile/unm-ios "${UNM_IOS_REPO}"
elif [ ! -f "${PREV_HEAD}" ]; then # initialize if this is the 1st poll
  git rev-parse master > "${PREV_HEAD}"
fi

# fetch & merge, then inspect head
git fetch  >> "${BUILD_LOG}" 2>&1
if [ $? -eq 0 ]; then
  date
  echo "Fetch from git done"
  git merge FETCH_HEAD >> "${BUILD_LOG}" 2>&1
  git rev-parse master > "${LATEST_HEAD}"
  if ! diff "${LATEST_HEAD}" "${PREV_HEAD}" > /dev/null;  then
    echo "Merge via git done..."
    cat "${LATEST_HEAD}" > "${PREV_HEAD}" # update stored HEAD
    
    # there has been a change, build
    
    # ======== 4. BUILD ========
    
    "${UNM_IOS_REPO}/src/main/shell/xcodebuild_test.sh"
    
  fi
fi

