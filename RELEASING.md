## Releasing - notes for project maintainers

1.  Ensure that What's New (`whatsnew.html`, plus `whatsnew` and `whatsnew-beta`) is all up-to-date
2.  Ensure that the latest code built successfully on Travis and was published to the Beta track
3.  Tag the latest commit - convention is e.g. RELEASE_3_6 for version 3.6
4.  In the Google Play developer console, promote the beta build to production
5.  Run `./gradlew publishListingRelease` to update the Play store listings

### Post-release
1.  Bump `versionName` in version.properties to the next release candidate
2.  Clear the contents of `whatsnew-beta`; add a new section in `whatsnew.html` for the next round of development.
