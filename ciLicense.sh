#!/bin/bash
set -ev

# We don't want to sign the artifact unless this is a non-PR build on master.
if [ $CI = "true" ] && [ $TRAVIS_BRANCH == "master" ] && [ ! $TRAVIS_PULL_REQUEST = "true" ]; then
  openssl aes-256-cbc -K $encrypted_29b04bc3212e_key -iv $encrypted_29b04bc3212e_iv -in license.properties.enc -out license.properties -d
  openssl aes-256-cbc -K $encrypted_29b04bc3212e_key -iv $encrypted_29b04bc3212e_iv -in signature.asc.enc -out signature.asc -d
  openssl aes-256-cbc -K $encrypted_29b04bc3212e_key -iv $encrypted_29b04bc3212e_iv -in android-keystore.enc -out android-keystore -d
fi
