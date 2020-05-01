package net.cyclestreets.views.place

import android.os.AsyncTask
import net.cyclestreets.api.GeoPlaces
import net.cyclestreets.contacts.Contact
import net.cyclestreets.util.Dialog
import net.cyclestreets.util.ProgressDialog
import net.cyclestreets.view.R
import org.osmdroid.util.BoundingBox

internal class AsyncContactLookup(private val view: PlaceViewBase,
                                  private val listener: PlaceViewBase.OnResolveListener) : AsyncTask<Any, Void, GeoPlaces>() {
    private val progress: ProgressDialog = Dialog.createProgressDialog(view.context, R.string.placeview_location_search)

    override fun onPreExecute() {
        progress.show()
    }

    override fun doInBackground(vararg params: Any): GeoPlaces {
        val bounds = params[1] as BoundingBox
        return if (params[0] is String)
            doSearch(params[0] as String, bounds)
        else
            doContactSearch(params[0] as Contact, bounds)
    }

    override fun onPostExecute(result: GeoPlaces) {
        progress.dismiss()
        view.resolvedContacts(result, listener)
    }

    private fun doContactSearch(contact: Contact, bounds: BoundingBox): GeoPlaces {
        var r = doSearch(contact.address(), bounds)
        if (!r.isEmpty)
            return r

        r = doSearch(contact.postcode(), bounds)
        if (!r.isEmpty)
            return r

        return doSearch(contact.city(), bounds)
    }

    private fun doSearch(search: String, bounds: BoundingBox): GeoPlaces {
        return try {
            GeoPlaces.search(search, bounds)
        } catch (e: Exception) {
            GeoPlaces.EMPTY
        }
    }
}
