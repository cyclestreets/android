package net.cyclestreets.views.overlay

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.*
import android.webkit.URLUtil
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import net.cyclestreets.Undoable
import net.cyclestreets.api.POI
import net.cyclestreets.api.POICategories
import net.cyclestreets.api.POICategory
import net.cyclestreets.iconics.IconicsHelper.materialIcon
import net.cyclestreets.util.*
import net.cyclestreets.view.R
import net.cyclestreets.views.CycleMapView
import net.cyclestreets.views.overlay.POIOverlay.POIOverlayItem
import org.osmdroid.api.IGeoPoint
import org.osmdroid.api.IProjection
import org.osmdroid.events.MapListener
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayItem


private val TAG = Logging.getTag(POIOverlay::class.java)


class POIOverlay(mapView: CycleMapView) : LiveItemOverlay<POIOverlayItem?>(mapView, false),
                                          MapListener, MenuListener, PauseResumeListener, Undoable {

    private val context: Context = mapView.context
    private val activeCategories: MutableList<POICategory> = ArrayList()
    private val overlays: OverlayHelper = OverlayHelper(mapView)
    private val curScreenCoords = Point()

    private val poiIcon = materialIcon(context, GoogleMaterial.Icon.gmd_place)

    private var activeItem: POIOverlayItem? = null
    private var lastFix: IGeoPoint? = null
    private var bubble: Rect? = null
    private var chooserShowing: Boolean = false
    private val tapHereText = context.getString(R.string.tap_here)

    /////////////////////////////////////////////////////
    private fun allCategories(): POICategories {
        return POICategories.get()
    }

    private fun routeOverlay(): TapToRouteOverlay? {
        return overlays.get(TapToRouteOverlay::class.java)
    }

    private fun controller(): ControllerOverlay {
        return overlays.controller()
    }

    /////////////////////////////////////////////////////
    override fun onPause(prefs: SharedPreferences.Editor) {
        prefs.putInt("category-count", activeCategories.size)
        for (i in activeCategories.indices)
            prefs.putString("category-$i", activeCategories[i].name)
    }

    override fun onResume(prefs: SharedPreferences) {
        activeCategories.clear()

        val firstTime = !POICategories.loaded()

        try {
            reloadActiveCategories(prefs)
        } catch (e: Exception) {
            // very occasionally this throws a NullException, although it's not something
            // I've been able to replicate :(
            // Let's just carry on
            activeCategories.clear()
        }

        if (firstTime) {
            items().clear()
            clearLastFix()
            activeItem = null
            refreshItems()
        }
    }

    private fun reloadActiveCategories(prefs: SharedPreferences) {
        val count = prefs.getInt("category-count", 0)

        for (i in 0 until count) {
            val name = prefs.getString("category-$i", "")

            for (cat in allCategories()) {
                if (name == cat.name) {
                    activeCategories.add(cat)
                    break
                }
            }
        }
    }

    ///////////////////////////////////////////////////////
    override fun onZoom(event: ZoomEvent): Boolean {
        clearLastFix()
        return super.onZoom(event)
    }

    ///////////////////////////////////////////////////
    override fun onSingleTap(event: MotionEvent): Boolean {
        if (activeItem != null && tappedInBubble(event))
            return true

        return super.onSingleTap(event)
    }

    private fun tappedInBubble(event: MotionEvent): Boolean {
        val pj = mapView().projection
        val screenRect = pj.intrinsicScreenRect
        val eventX = screenRect.left + event.x.toInt()
        val eventY = screenRect.top + event.y.toInt()

        if (!bubble!!.contains(eventX, eventY))
            return false
        // Check if tapped on link
        if (eventY < Draw.titleSectionY) {
            showWebpage(activeItem)
            return true
        }

        return routeMarkerAtItem(activeItem)
    }

    private fun showWebpage(item: POIOverlayItem?) {
        val url = item!!.poi.url()

        if (url != "") {
            val webpage = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                startActivity(context, intent, null)
            }
        }
    }

    override fun onItemSingleTap(item: POIOverlayItem?): Boolean {
        if (activeItem === item)
            hideBubble()
        else
            showBubble(item!!)
        redraw()

        return true
    }

    private fun showBubble(item: POIOverlayItem) {
        hideBubble()
        activeItem = item
        controller().pushUndo(this)
    }

    private fun hideBubble() {
        activeItem = null
        controller().flushUndo(this)
    }

    override fun onItemDoubleTap(item: POIOverlayItem?): Boolean {
        return routeMarkerAtItem(item)
    }

    private fun routeMarkerAtItem(item: POIOverlayItem?): Boolean {
        hideBubble()
        val o = routeOverlay() ?: return false
        o.setNextMarker(item!!.point)
        return true
    }

    /////////////////////////////////////////////////////
    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (activeCategories.isEmpty())
            return

        super.draw(canvas, mapView, shadow)

        if (activeItem == null)
            return

        drawBubble(canvas, mapView)
    }

    private fun drawBubble(canvas: Canvas, mapView: MapView) {
        var url = activeItem!!.poi.url()
        url = if (URLUtil.isValidUrl(url)) url else ""
        val title = if (activeItem!!.title.isNullOrEmpty() && (activeItem!!.poi.url().isNotBlank())) tapHereText else activeItem!!.title

        val bubbleText = listOf(
            title,
            activeItem!!.snippet,
            activeItem!!.poi.phone(),
            activeItem!!.poi.openingHours()
        ).filterNot { it.isNullOrBlank() }.joinToString("\n")

        // find the right place
        val pj: IProjection = mapView.projection
        pj.toPixels(activeItem!!.point, curScreenCoords)

        val x = curScreenCoords.x
        val y = curScreenCoords.y

        val matrix = mapView.matrix
        val matrixValues = FloatArray(9)
        matrix.getValues(matrixValues)

        val scaleX = Math.sqrt(matrixValues[Matrix.MSCALE_X]
                * matrixValues[Matrix.MSCALE_X] + matrixValues[Matrix.MSKEW_Y]
                * matrixValues[Matrix.MSKEW_Y].toDouble()).toFloat()
        val scaleY = Math.sqrt(matrixValues[Matrix.MSCALE_Y]
                * matrixValues[Matrix.MSCALE_Y] + matrixValues[Matrix.MSKEW_X]
                * matrixValues[Matrix.MSKEW_X].toDouble()).toFloat()

        canvas.save()
        canvas.rotate(-mapView.mapOrientation, x.toFloat(), y.toFloat())
        canvas.scale(1 / scaleX, 1 / scaleY, x.toFloat(), y.toFloat())

        bubble = Draw.drawBubble(canvas, textBrush(), urlBrush(), offset(), cornerRadius(), curScreenCoords, bubbleText, url, title)

        canvas.restore()
    }

    private fun updateCategories(newCategories: List<POICategory>) {
        val removed = notIn(activeCategories, newCategories)
        val added = notIn(newCategories, activeCategories)

        if (removed.isNotEmpty()) {
            for (r in removed) {
                Log.d(TAG, "Removed POI category $r")
                hide(r)
            }
            redraw()
        }
        if (added.isNotEmpty()) {
            for (a in added) {
                Log.d(TAG, "Added POI category $a")
                activeCategories.add(a)
            }
            clearLastFix()
            refreshItems()
        }
    }

    private fun hide(cat: POICategory) {
        if (!activeCategories.contains(cat))
            return
        activeCategories.remove(cat)

        for (i in items().indices.reversed()) {
            if (cat == items()[i]!!.category()) {
                items().removeAt(i)
            }
        }

        if (activeItem != null && cat == activeItem!!.category())
            activeItem = null
    }

    private fun notIn(c1: List<POICategory>,
                      c2: List<POICategory>): List<POICategory> {
        return c1.minus(c2)
    }

    override fun fetchItemsInBackground(mapCentre: IGeoPoint,
                                        zoom: Int,
                                        boundingBox: BoundingBox): Boolean {
        if (activeCategories.isEmpty())
            return false

        val moved = if (lastFix != null) GeoHelper.distanceBetween(mapCentre, lastFix) else Int.MAX_VALUE
        val diagonalWidth = (boundingBox.diagonalLengthInMeters / 1000).toInt()

        // first time through width can be zero
        if (diagonalWidth == 0 || moved < diagonalWidth / 2)
            return false

        lastFix = mapCentre
        GetPOIsTask.fetch(this, mapCentre, diagonalWidth * 3 + 1)
        return true
    }

    protected fun clearLastFix() {
        lastFix = null
    }

    /////////////////////////////////////////////////////
    override fun onCreateOptionsMenu(menu: Menu) {
        MenuHelper.createMenuItem(menu, R.string.poi_menu_title, Menu.NONE, poiIcon)
        MenuHelper.enableMenuItem(menu, R.string.poi_menu_title, true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        MenuHelper.enableMenuItem(menu, R.string.poi_menu_title, true)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        if (item.itemId != R.string.poi_menu_title)
            return false

        if (chooserShowing)
            return true

        chooserShowing = true
        val poiAdapter = POICategoryAdapter(context, allCategories(), activeCategories)

        Dialog.listViewDialog(context, R.string.poi_menu_title, poiAdapter,
            { _, _ ->
                chooserShowing = false
                updateCategories(poiAdapter.chosenCategories())
            },
            { _, _ ->
                chooserShowing = false
            })
        return true
    }

    override fun onBackPressed(): Boolean {
        hideBubble()
        redraw()
        return true
    }

    /////////////////////////////////////////////////////
    private class GetPOIsTask(private val overlay: POIOverlay) : AsyncTask<Any, Void?, List<POI>>() {

        // take snapshot of categories to avoid later contention
        private val activeCategories: List<POICategory> = ArrayList(overlay.activeCategories)

        override fun doInBackground(vararg params: Any): List<POI> {
            val centre = params[0] as IGeoPoint
            val radius = params[1] as Int
            val pois: MutableList<POI> = ArrayList()

            for (cat in activeCategories) try {
                pois.addAll(cat.pois(centre, radius))
            } catch (ex: RuntimeException) {
                // never mind, eh?
            }
            return pois
        }

        override fun onPostExecute(pois: List<POI>) {
            val items: MutableList<POIOverlayItem> = ArrayList()
            for (poi in pois) {
                // there used to be dead code here which checked for duplicate with items.contains(poi)
                // which could never work properly, since the lists are of different types.
                // if we find duplicate POIs, maybe we'll have to reintroduce that properly.
                items.add(POIOverlayItem(poi))
            }
            overlay.setItems(items as List<POIOverlayItem?>?)
        }

        companion object {
            fun fetch(overlay: POIOverlay, centre: IGeoPoint, radius: Int) {
                GetPOIsTask(overlay).execute(centre, radius)
            }
        }
    }

    //////////////////////////////////
    internal class POICategoryAdapter(context: Context,
                                      private val cats: POICategories,
                                      initialCategories: List<POICategory>) : BaseAdapter(), View.OnClickListener {

        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        private val selected: MutableList<POICategory> = initialCategories.toMutableList()

        init {
            Log.d(TAG, "Creating POICategoryAdapter - previously-selected categories are: $initialCategories")
        }

        fun chosenCategories(): List<POICategory> {
            return selected
        }

        override fun getCount(): Int {
            return cats.count()
        }

        override fun getItem(position: Int): Any {
            return cats[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int,
                             convertView: View?,
                             parent: ViewGroup): View {
            val cat = cats[position]

            val v = convertView ?: inflater.inflate(R.layout.poicategories_item, parent, false)

            v.findViewById<TextView>(R.id.name).text = cat.name
            v.findViewById<ImageView>(R.id.icon).apply {
                setImageDrawable(cats[cat.name].icon)
            }
            val chk = v.findViewById<CheckBox>(R.id.checkbox).apply {
                setOnCheckedChangeListener(null)
                isChecked = isSelected(cat)
            }

            v.setOnClickListener(this)

            // This has to be done separately from the original initialisation of chk, so inflation
            // of the List view doesn't go mental
            chk.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Log.d(TAG, "Selected POI category $cat")
                    selected.add(cat)
                }
                else {
                    Log.d(TAG, "Deselected POI category $cat")
                    selected.remove(cat)
                }
            }

            return v
        }

        override fun onClick(view: View) {
            view.findViewById<CheckBox>(R.id.checkbox).apply {
                isChecked = !isChecked
            }
        }

        private fun isSelected(cat: POICategory): Boolean {
            for (c in selected)
                if (cat.name == c.name)
                    return true
            return false
        }

    }


    class POIOverlayItem(val poi: POI) : OverlayItem(poi.id().toString(), poi.name(),
                                                     poi.notes(), poi.position()) {

        init {
            setMarker(this.poi.icon())
            markerHotspot = HotspotPlace.CENTER
        }

        fun category(): POICategory {
            return poi.category()
        }

        // Equality testing
        override fun equals(other: Any?): Boolean {
            if (other !is POIOverlayItem) return false
            return (poi.id() == other.poi.id())
        }

        override fun hashCode(): Int {
            return poi.id()
        }

        override fun toString(): String {
            return "POIItem [poi=${poi}]"
        }
    }
}
