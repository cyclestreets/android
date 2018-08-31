## Releasing - notes for project maintainers

1.  Ensure that What's New is up-to-date (`whatsnew.html`, plus `whatsnew` and `whatsnew-beta`) for the Play store listings
2.  Ensure that the latest code built successfully on Travis and was published to the Beta track
3.  Run `./gradlew publishListingRelease` to update the Play store listing
4.  Tag the latest commit - convention is e.g. RELEASE_3_5 for version 3.5
5.  Bump `versionName` in version.properties to the next release candidate
6.  In the Google Play developer console, promote the beta build to production
