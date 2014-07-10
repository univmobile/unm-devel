#!/bin/bash

#
# File: macos-job-xcodebuild-test.sh
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
#    "${1}": TEST_REPORT: Where to write the test report. No default. 
#               (This script will create ${WORKSPACE}/target/)
#    "${2}": WORKSPACE: Where to put the build files. Default to ../../../ 
#               (This script will create ${WORKSPACE}/target/)
#

# ======== 1. ENVIRONMENT ========

TEST_REPORT="${1}"
WORKSPACE="${2}"

# ======== 1.1. ENVIRONMENT: WORKSPACE ========

if [ -z "${WORKSPACE}" ]; then
  WORKSPACE="$(cd "$(dirname "${0}")/../../../"; pwd)"
fi

echo "WORKSPACE: ${WORKSPACE}"

if [ ! -d "${WORKSPACE}/target" ]; then mkdir -p "${WORKSPACE}/target"; fi

PREV_HEAD="${WORKSPACE}/target/macos-job-xcode-test-prev_head"
LATEST_HEAD="${WORKSPACE}/target/macos-job-xcode-test-latest_head"
BUILD_LOG="${WORKSPACE}/target/macos-job-xcode-test-build.log"
CMD_FILE="${WORKSPACE}/target/macos-job-xcode-test-cmd.sh"
UNM_IOS_REPO="${WORKSPACE}/target/unm-ios"

if [ ! -d "${WORKSPACE}/target" ]; then mkdir -p "${WORKSPACE}/target"; fi

# ======== 1.2. ENVIRONMENT: TEST_REPORT ========

if [ -z "${TEST_REPORT}" ]; then
  echo "** Error: TEST_REPORT must be set and must be in a git repo. e.g. ../unm-ios-test-results/data/unm-ios-xcodebuild-test.log"
  echo "Exiting"
  exit 1
fi

if ! cd "$(dirname "${TEST_REPORT}")"; then
  echo "** Error: Cannot find parent dir for: ${TEST_REPORT}" >&2 
  echo "Exiting"
  exit 1
fi

TEST_REPORT="$(pwd)/$(basename "${TEST_REPORT}")"

echo "pwd: $(pwd)" >> "${BUILD_LOG}"

git status  >> "${BUILD_LOG}" 2>&1
if [ $? -ne 0 ]; then
  echo "** Error: TEST_REPORT=${TEST_REPORT} is not in a git repo. e.g. ../unm-ios-test-results/data/unm-ios-xcodebuild-test.log"
  echo "Exiting"
  exit 1
fi

echo "TEST_REPORT: ${TEST_REPORT}"

# ======== 1.3. ENVIRONMENT: UNM_IOS_REPO AND OTHERS ========

if [ ! -d "${UNM_IOS_REPO}" ]; then mkdir -p "${UNM_IOS_REPO}"; fi

if ! cd "${UNM_IOS_REPO}"; then
  echo "** Error: Cannot change dir to: ${UNM_IOS_REPO}" >&2 
  echo "Exiting"
  exit 1
fi

echo "pwd: $(pwd)" >> "${BUILD_LOG}"

touch "${CMD_FILE}"
    
chmod +x "${CMD_FILE}"

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
  echo "Fetch from git done"
  git merge FETCH_HEAD >> "${BUILD_LOG}" 2>&1
  git rev-parse master > "${LATEST_HEAD}"
  #if ! diff "${LATEST_HEAD}" "${PREV_HEAD}" > /dev/null;  then
    echo "Merge via git done..."
    cat "${LATEST_HEAD}" > "${PREV_HEAD}" # update stored HEAD
    
    # there has been a change, build
    
    # ======== 4. BUILD COMMAND ========
    
    BUILD_OPTS="-workspace UnivMobile.xcworkspace \
      -scheme UnivMobileTests \
      -configuration Debug \
      -sdk iphonesimulator7.1 \
      -destination OS=7.1,name=\"iPhone Retina (4-inch)\""
    BUILD_CMD="/usr/bin/xcodebuild clean build ${BUILD_OPTS}"  
    TEST_CMD="/usr/bin/xcodebuild test ${BUILD_OPTS}"

    # ======== 3. TEST REPORT INITIALIZATION ========

    GIT_COMMIT="$(git rev-parse HEAD)"
    
    SEP="----------------------------------------"
    
    echo "Begin Date: $(date)" > "${TEST_REPORT}"
    echo "Hostname: $(hostname)" >> "${TEST_REPORT}"
    echo "Script: $(basename "${0}") ${1} ${2}" >> "${TEST_REPORT}"
    echo "Current Directory: $(pwd)" >> "${TEST_REPORT}"
    echo "Test Report: ${TEST_REPORT}" >> "${TEST_REPORT}"
    echo "Git Commit: $GIT_COMMIT" >> "${TEST_REPORT}" 
    echo "Test Command: ${TEST_CMD}" >> "${TEST_REPORT}"
    echo "${SEP}" >> "${TEST_REPORT}"
    
    echo >> "${TEST_REPORT}"
    
    # ======== 4. RUN ========
    
    echo "Building..."

    echo "${BUILD_CMD}"
    echo "${BUILD_CMD}" > "${CMD_FILE}"    
    echo "${BUILD_CMD}" > "${BUILD_LOG}"
    
    # "${CMD_FILE}" >> "${TEST_REPORT}" 2>&1 // xcodebuild clean build
    
    "${CMD_FILE}" // xcodebuild clean build
    
    RET=$?
    
    if [ "${RET}" -ne 0 ]; then
    
      echo "** Error: \"xcodebuild clean build\" failed with return code: ${RET}" | tee -a "${TEST_REPORT}"
    
    else

      echo >> "${TEST_REPORT}"

      echo "Build succeeded." >> "${TEST_REPORT}"
      echo "Test Date: $(date)" >> "${TEST_REPORT}"      
      echo "${SEP}" >> "${TEST_REPORT}"
      
      echo "Testing..."

      echo "${TEST_CMD}"
      echo "${TEST_CMD}" > "${CMD_FILE}"    
      echo "${TEST_CMD}" > "${BUILD_LOG}"
    
      "${CMD_FILE}" >> "${TEST_REPORT}" 2>&1 // xcodebuild test
    
    fi
    
    # ======== 5. TEST REPORT FINALIZATION ========
    
    echo "${SEP}" >> "${TEST_REPORT}" 
    echo "End Date: $(date)" >> "${TEST_REPORT}"
    
    # ======== 6. TEST REPORT GIT COMMIT ========
    
    cd "$(dirname "${TEST_REPORT}")"
    
    TEST_REPORT_FILENAME="$(basename "${TEST_REPORT}")"
    
    git add "${TEST_REPORT_FILENAME}"
    
    git commit -m "xcodebuild test, git commit: ${GIT_COMMIT}" "${TEST_REPORT_FILENAME}"     
    
    git push
    
  #fi
fi

