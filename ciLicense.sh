#!/bin/bash
set -ev

if [ "$CI" == "true" ] && [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo "Running a non-PR build on master - set up signing info"
  openssl aes-256-cbc -k $openssl_file_encryption_key -in cyclestreets.vNext/license.properties.enc -out cyclestreets.vNext/license.properties -d
  openssl aes-256-cbc -k $openssl_file_encryption_key -in cyclestreets.vNext/signature.asc.enc -out cyclestreets.vNext/signature.asc -d
  openssl aes-256-cbc -k $openssl_file_encryption_key -in cyclestreets.vNext/keystore.enc -out cyclestreets.vNext/keystore -d
  openssl aes-256-cbc -k $openssl_file_encryption_key -in cyclestreets.vNext/play-api-key.p12.enc -out cyclestreets.vNext/play-api-key.p12 -d
else
  echo "The compiled artifact will not be signed"
fi
