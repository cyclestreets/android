This folder contains the navigation direction icons in their original SVG format.

They make use of SVG functionality unsupported by android, and therefore have been converted via
`svg2png` with the config.json file present in this folder, to give 24dp FAB icons.  For example:

```
java -jar svg2png-0.2.3.jar -c config.json -f compass.svg
```

`svg2png` itself can be found at https://github.com/sterlp/svg2png/releases
