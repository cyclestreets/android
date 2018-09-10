package net.cyclestreets.views.place

import android.os.AsyncTask
import net.cyclestreets.contacts.Contact
import net.cyclestreets.contacts.Contacts
import net.cyclestreets.util.Dialog
import net.cyclestreets.util.ProgressDialog
import net.cyclestreets.view.R

internal class AsyncContactLoad(private val view: PlaceViewBase) : AsyncTask<Void, Void, List<Contact>>() {
    internal val progress: ProgressDialog = Dialog.createProgressDialog(view.context, R.string.placeview_contacts_loading)

    override fun onPreExecute() {
        progress.show()
    }

    override fun doInBackground(vararg params: Void): List<Contact> {
        return Contacts.load(view.context)
    }

    override fun onPostExecute(results: List<Contact>) {
        progress.dismiss()
        view.onContactsLoaded(results)
    }
}
