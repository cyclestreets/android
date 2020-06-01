This folder contains the waypoint icons that we use in their original SVG format.

For placement of waypoints on the map, we use the PNG export of the XCF files (which were created
by GIMP).  These have the 'point' of the wisp centred on the icon, so that matches wherever the
userhas clicked.

For the 'waymark' icon in LiveRide, the `waypoint.svg` file has been converted via `svg2png` with
the config.json file present in this folder.  For example:

```
java -jar svg2png-0.2.3.jar -c config.json -f lr_waymark.svg
```

`svg2png` itself can be found at https://github.com/sterlp/svg2png/releases
