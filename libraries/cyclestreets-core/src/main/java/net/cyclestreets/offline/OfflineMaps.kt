package net.cyclestreets.offline

private const val downloadRoot = "http://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/v4/europe/"

//val offlineMaps: List<OfflineMap> = listOf(
//    OfflineMap("England",     505, "europe/great-britain/england"),
//    OfflineMap("Scotland",    110, "europe/great-britain/scotland"),
//    OfflineMap("Wales",        47, "europe/great-britain/wales"),
//    OfflineMap("Isle of Man",   2, "europe/isle-of-man")
//)

internal val supportedMaps = mapOf(
    "isle-of-man.map" to "Isle of Man",
    "monaco.map" to "Monaco"
)

class OfflineMap(val name: String, val url: String, val lastModified: String, val sizeMb: Int) {
    override fun toString(): String {
        return "OfflineMap(name='$name', url='$url', lastModified='$lastModified', sizeMb=$sizeMb)"
    }


}
