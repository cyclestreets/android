#!/bin/bash
set -ev

if [ "$CI" == "true" ] && [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo "Running a non-PR build on master - set up signing info"
  openssl aes-256-cbc -K $encrypted_29b04bc3212e_key -iv $encrypted_29b04bc3212e_iv -in license.properties.enc -out license.properties -d
  openssl aes-256-cbc -K $encrypted_29b04bc3212e_key -iv $encrypted_29b04bc3212e_iv -in signature.asc.enc -out signature.asc -d
  openssl aes-256-cbc -K $encrypted_29b04bc3212e_key -iv $encrypted_29b04bc3212e_iv -in android-keystore.enc -out android-keystore -d
else
  echo "The compiled artifact will not be signed"
fi
