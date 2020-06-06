#!/bin/bash
set -ev

if [ "$TRAVIS_REPO_SLUG" == "cyclestreets/android" ] &&[ "$CI" == "true" ] && [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo "Running a non-PR build on master - check whether POI type icons are up-to-date"
  pushd assets/pois
  python --version
  python getPoiTypeIcons.py
  if [[ $? -ne 0 ]]
  then
    echo 'An error occurred while trying to check whether POI type icons need updating.'
    exit 1
  fi
  popd
fi

if [[ -n $(git status -s) ]]
then
  echo 'POI type icons need updating.  Look at the documentation of getPoiTypeIcons.py for details.'
  git status
  exit 1
else
  echo 'POI type icons are all up to date, continuing with build.'
fi
