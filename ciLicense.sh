#!/bin/bash
set -ev

if [ "$TRAVIS_REPO_SLUG" == "cyclestreets/android" ] &&[ "$CI" == "true" ] && [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo "Running a non-PR build on master - set up signing info"
  openssl aes-256-cbc -k $openssl_file_encryption_key -md md5 -in cyclestreets.app/license.properties.enc -out cyclestreets.app/license.properties -d
  openssl aes-256-cbc -k $openssl_file_encryption_key -md md5 -in cyclestreets.app/signature.asc.enc -out cyclestreets.app/signature.asc -d
  openssl aes-256-cbc -k $openssl_file_encryption_key -md md5 -in cyclestreets.app/keystore.enc -out cyclestreets.app/keystore -d
  openssl aes-256-cbc -k $openssl_file_encryption_key -md md5 -in cyclestreets.app/play-api-key.json.enc -out cyclestreets.app/play-api-key.json -d
  openssl aes-256-cbc -k $openssl_file_encryption_key -md md5 -in libraries/cyclestreets-core/src/test/resources/cyclestreets-api.key.enc -out libraries/cyclestreets-core/src/test/resources/cyclestreets-api.key -d
else
  echo "Running a PR or branch build - the compiled artifact will have a dummy signature"
fi
