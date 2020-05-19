package net.cyclestreets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.ListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import net.cyclestreets.content.LocationDatabase
import net.cyclestreets.content.SavedLocation
import net.cyclestreets.fragments.R
import net.cyclestreets.iconics.IconicsHelper.materialIcon
import net.cyclestreets.util.MenuHelper
import net.cyclestreets.util.Theme


class LocationsFragment : ListFragment() {

    private lateinit var locDb: LocationDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locDb = LocationDatabase(requireActivity())
        listAdapter = LocationsAdapter(requireActivity(), locDb)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val layout = inflater.inflate(R.layout.locations_list, container, false)

        val addLocationIcon = materialIcon(requireContext(), GoogleMaterial.Icon.gmd_add_location, Theme.lowlightColor(requireContext()))

        val addLocationButton: FloatingActionButton = layout.findViewById(R.id.addlocation)
        addLocationButton.setImageDrawable(addLocationIcon)
        addLocationButton.setOnClickListener { _ -> editLocation(-1) }

        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        registerForContextMenu(listView)
    }

    override fun onCreateContextMenu(menu: ContextMenu,
                                     v: View,
                                     menuInfo: ContextMenuInfo?) {
        MenuHelper.createMenuItem(menu, R.string.ic_menu_edit)
        MenuHelper.createMenuItem(menu, R.string.ic_menu_delete)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return try {
            val info = item.menuInfo as AdapterContextMenuInfo
            val localId = listAdapter!!.getItemId(info.position).toInt()
            val menuId = item.itemId

            if (R.string.ic_menu_edit == menuId)
                editLocation(localId)
            if (R.string.ic_menu_delete == menuId)
                deleteLocation(localId)

            true
        } catch (e: ClassCastException) {
            false
        }
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        editLocation(id.toInt())
    }

    private fun editLocation(localId: Int) {
        val edit = Intent(activity, LocationEditorActivity::class.java)
        edit.putExtra("localId", localId)
        startActivityForResult(edit, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        refresh()
    }

    private fun deleteLocation(localId: Int) {
        locDb.deleteLocation(localId)
        refresh()
    }

    private fun refresh() {
        listAdapter!!.refresh()
    }

    override fun getListAdapter(): LocationsAdapter? {
        return super.getListAdapter() as LocationsAdapter?
    }

    //////////////////////////////////
    class LocationsAdapter(context: Context, private val locDb: LocationDatabase) : BaseAdapter() {

        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        private var locations: List<SavedLocation>

        init {
            locations = this.locDb.savedLocations()
        }

        fun refresh() {
            locations = locDb.savedLocations()
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return locations.size
        }

        override fun getItem(position: Int): Any {
            return locations[position]
        }

        override fun getItemId(position: Int): Long {
            return locations[position].localId().toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val location = locations[position]
            val v = inflater.inflate(R.layout.storedroutes_item, parent, false)

            val n = v.findViewById<View>(R.id.route_title) as TextView
            n.text = location.name()

            return v
        }
    }

}