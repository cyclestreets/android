#!/bin/bash
set -ev

if [ ${CI} = "true" ] && [ ! "${TRAVIS_PULL_REQUEST}" = "true" ]; then
  echo "DUMMY STEP where we should publish the artifact we've built"
fi
