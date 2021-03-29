package net.cyclestreets

// Defaults for cycle maps
const val DEFAULT_GPS_STATE = true
const val DEFAULT_ZOOM_LEVEL = 14.0
const val FINDPLACE_ZOOM_LEVEL = 16.0
const val ITEM_ZOOM_LEVEL = 16.0
const val MAX_ZOOM_LEVEL = 19.0
const val MIN_ZOOM_LEVEL = 2.0
// Greenwich!
const val DEFAULT_MAP_CENTRE_LONGITUDE = 0
const val DEFAULT_MAP_CENTRE_LATITUDE = 51477841
// No cycling at the Poles
const val MAX_LATITUDE_NORTH = 80
const val MAX_LATITUDE_SOUTH = -80

// Intent constants
const val EXTRA_ROUTE_TYPE = "net.cyclestreets.extra.ROUTE_TYPE"
const val EXTRA_ROUTE_SPEED = "net.cyclestreets.extra.ROUTE_SPEED"
const val EXTRA_ROUTE_NUMBER = "net.cyclestreets.extra.ROUTE"
const val EXTRA_CIRCULAR_ROUTE_DISTANCE = "net.cyclestreets.extra.CIRCULAR_ROUTE_DISTANCE"
const val EXTRA_CIRCULAR_ROUTE_DURATION = "net.cyclestreets.extra.CIRCULAR_ROUTE_DURATION"
const val ROUTE_ID = "net.cyclestreets.extra.ROUTE_ID"

// Permission request codes
const val GENERIC_PERMISSION_REQUEST = 1
const val LIVERIDE_LOCATION_PERMISSION_REQUEST = 2
const val FOLLOW_LOCATION_PERMISSION_REQUEST = 3
