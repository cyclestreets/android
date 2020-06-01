This folder contains the photomap icons in their original SVG format.

Most have been imported into Android XML vector graphics as-is with an output height of 17dp.

Those which use SVG functionality unsupported by android, have been converted via `svg2png` with
the config.json file present in this folder.  For example:

```
java -jar svg2png-0.2.3.jar -c config.json -f pm_closure_bad.svg
```

`svg2png` itself can be found at https://github.com/sterlp/svg2png/releases
