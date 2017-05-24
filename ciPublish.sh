#!/bin/bash
set -ev

# We don't want to publish the artifact unless this is a non-PR build on master.
if [ $CI = "true" ] && [ $TRAVIS_BRANCH == "master" ] && [ ! $TRAVIS_PULL_REQUEST = "true" ]; then
  echo "DUMMY STEP where we should publish the artifact we've built"
fi
