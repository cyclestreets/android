package net.cyclestreets.views.place

import java.util.ArrayList

import net.cyclestreets.views.PlaceAutoCompleteTextView
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.BoundingBox

import net.cyclestreets.content.LocationDatabase
import net.cyclestreets.content.SavedLocation
import net.cyclestreets.view.R
import net.cyclestreets.api.GeoPlace
import net.cyclestreets.api.GeoPlaces
import net.cyclestreets.contacts.Contact
import net.cyclestreets.util.Dialog
import net.cyclestreets.util.MessageBox

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageButton
import android.widget.LinearLayout

open class PlaceViewBase protected constructor(private val context_: Context, layout: Int, attrs: AttributeSet?) :
            LinearLayout(context_, attrs), OnClickListener, DialogInterface.OnClickListener {

    private val textView: PlaceAutoCompleteTextView = findViewById(R.id.placeBox)
    private val allowedPlaces = ArrayList<GeoPlace>()
    private val savedLocations: List<SavedLocation> = LocationDatabase(context_).savedLocations()
    private var options: MutableList<String>? = null
    private var contacts: List<Contact>? = null

    companion object {
        private var CURRENT_LOCATION: String? = null
        private var LOCATION_NOT_FOUND: String? = null
        private var CONTACTS: String? = null
        private var NO_CONTACTS_WITH_ADDRESSES: String? = null
        private var SAVED_LOCATIONS: String? = null
    }

    init {
        orientation = LinearLayout.HORIZONTAL
        LayoutInflater.from(context_).inflate(layout, this)
        findViewById<ImageButton>(R.id.optionsBtn).setOnClickListener(this)
        attrs?.getAttributeValue("http://schemas.android.com/apk/res/android", "hint")?.apply {
            hint = this
        }
        loadStrings(context_)
    }

    // Helper methods
    var text: String
        get() = textView.text.toString()
        set(text) = textView.setText(text)
    var hint: String
        get() = textView.hint.toString()
        set(text) {
            textView.hint = text
        }

    interface OnResolveListener {
        fun onResolve(place: GeoPlace)
    }

    private fun loadStrings(context: Context) {
        if (CURRENT_LOCATION != null)
            return
        val res = context.resources
        CURRENT_LOCATION = res.getString(R.string.placeview_current_location)
        LOCATION_NOT_FOUND = res.getString(R.string.placeview_location_not_found)
        CONTACTS = res.getString(R.string.placeview_contacts)
        NO_CONTACTS_WITH_ADDRESSES = res.getString(R.string.placeview_no_contacts_with_addresses)
        SAVED_LOCATIONS = res.getString(R.string.placeview_saved_locations)
    }

    ////////////////////////////////////
    fun allowCurrentLocation(loc: IGeoPoint?, hint: Boolean) {
        if (loc == null)
            return
        val gp = GeoPlace(loc, CURRENT_LOCATION, "")
        allowedPlaces.add(gp)
        if (hint)
            setPlaceHint(gp)
    }

    fun allowLocation(loc: IGeoPoint?, label: String) {
        if (loc == null)
            return
        allowedPlaces.add(GeoPlace(loc, label, ""))
    }

    fun geoPlace(listener: OnResolveListener) {
        if (textView.geoPlace() != null) {
            listener.onResolve(textView.geoPlace())
            return
        }

        if (textView.contact() != null)
            lookup(textView.contact(), listener)
        else if (text != null)
            lookup(text, listener)
    }

    fun addHistory(place: GeoPlace) {
        if (place !in allowedPlaces)
            textView.addHistory(place)
    }

    private fun bounds(): BoundingBox {
        return textView.bounds()
    }

    fun setBounds(bounds: BoundingBox) {
        textView.setBounds(bounds)
    }

    private fun setPlace(geoPlace: GeoPlace) {
        textView.setGeoPlace(geoPlace)
    }

    private fun setPlaceHint(geoPlace: GeoPlace) {
        textView.setGeoPlaceHint(geoPlace)
    }

    private fun setContact(contact: Contact) {
        textView.setContact(contact)
    }

    private fun setSavedLocation(location: SavedLocation) {
        val gp = GeoPlace(location.where(), location.name(), null)
        setPlace(gp)
    }

    override fun onClick(v: View) {
        options = ArrayList()
        for (gp in allowedPlaces)
            options!!.add(gp.name())

        if (contactsAvailable())
            options!!.add(CONTACTS!!)
        if (savedLocationsAvailable())
            options!!.add(SAVED_LOCATIONS!!)

        Dialog.listViewDialog(context_,
                              R.string.placeview_choose_location,
                              options,
                              this)
    }

    private fun contactsAvailable(): Boolean {
        val pm = context_.packageManager
        val hasPerm = pm.checkPermission(
            Manifest.permission.READ_CONTACTS,
            context_.packageName
        )
        return hasPerm == PackageManager.PERMISSION_GRANTED
    }

    private fun savedLocationsAvailable(): Boolean {
        return savedLocations.isNotEmpty()
    }

    override fun onClick(dialog: DialogInterface, whichButton: Int) {
        val option = options!![whichButton]

        for (gp in allowedPlaces)
            if (gp.name() == option)
                setPlaceHint(gp)

        if (CONTACTS == option)
            pickContact()

        if (SAVED_LOCATIONS == option)
            pickSavedLocation()
    }

    private fun pickContact() {
        if (contacts == null) {
            loadContacts()
            return
        }
        if (contacts!!.isEmpty()) {
            MessageBox.OK(this, NO_CONTACTS_WITH_ADDRESSES)
            return
        }

        Dialog.listViewDialog(context_,
                              R.string.placeview_contacts,
                              contacts,
                              ContactsListener())
    }

    private inner class ContactsListener : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface, whichButton: Int) {
            val c = contacts!![whichButton]
            setContact(c)
        }
    }

    private fun pickSavedLocation() {
        Dialog.listViewDialog(context_,
                              R.string.placeview_saved_locations,
                              savedLocations,
                              SavedLocationListener())
    }

    private inner class SavedLocationListener : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface, whichButton: Int) {
            val l = savedLocations[whichButton]
            setSavedLocation(l)
        }
    }

    ///////////////////////////////////////////////////////////
    private fun loadContacts() {
        val acl = AsyncContactLoad(this)
        acl.execute()
    }

    internal fun onContactsLoaded(contacts: List<Contact>) {
        this.contacts = contacts
        pickContact()
    }

    ///////////////////////////////////////////////////////////
    private fun lookup(what: Any, listener: OnResolveListener) {
        val asc = AsyncContactLookup(this, listener)
        asc.execute(what, bounds())
    }

    internal fun resolvedContacts(results: GeoPlaces, listener: OnResolveListener) {
        if (results.isEmpty) {
            MessageBox.OK(this, LOCATION_NOT_FOUND)
            return
        }

        if (results.size() == 1) {
            textView.setGeoPlace(results.get(0))
            listener.onResolve(results.get(0))
            return
        }

        Dialog.listViewDialog(context_,
                              R.string.placeview_choose_location,
                              results.asList(),
                              PlaceListener(results, listener))
    }

    private inner class PlaceListener(private val results_: GeoPlaces,
                                      private val listener_: OnResolveListener) : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface, whichButton: Int) {
            textView.setGeoPlace(results_.get(whichButton))
            listener_.onResolve(results_.get(whichButton))
        }
    }
}
