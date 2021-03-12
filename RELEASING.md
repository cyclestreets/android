## Releasing - notes for project maintainers

1.  Ensure that What's New (`whatsnew.html`, plus `beta.txt` and `production.txt`) is all up-to-date
    (don't forget to remove "Beta" from the latest whatsnew.html entry!)
2.  Ensure that the latest code built successfully on Travis and was published to the Beta track
3.  Tag the latest commit - convention is e.g. release/3.9 for version 3.9
4.  In the Google Play developer console, promote the beta build to production

### Post-release
1.  Bump `versionName` in cyclestreets.app/build.gradle to the next release candidate
2.  Clear the contents of `beta.txt`; add a new section in `whatsnew.html` for the next round of development.
