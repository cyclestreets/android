#!/bin/bash
set -ev

if [ "$CI" == "true" ] && [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo "Running a non-PR build on master - publish artifact"
  ./gradlew publishReleaseApk -Dorg.gradle.java.home=${JAVA17_HOME} -x lint
else
  echo "The artifact will not be published"
fi
