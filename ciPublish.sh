#!/bin/bash
set -ev

if [ "$CI" == "true" ] && [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo "Running a non-PR build on master - publish artifact"
  echo "DUMMY STEP where we should publish the artifact we've built"
else
  echo "The artifact will not be published"
fi
