package net.cyclestreets

import net.cyclestreets.fragments.R

import net.cyclestreets.api.GeoPlace

import net.cyclestreets.util.MessageBox
import net.cyclestreets.views.place.PlaceView

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import net.cyclestreets.views.place.PlaceViewBase

import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.BoundingBox

object FindPlace {
    fun launch(context: Context, boundingBox: BoundingBox, onPlaceFound: (IGeoPoint) -> Unit) {
        val builder = AlertDialog.Builder(context).setTitle(R.string.menu_find_place)
        val fpcb = FindPlaceCallbacks(context, builder, boundingBox, onPlaceFound)

        val ad = builder.create()
        ad.show()
        ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(android.R.style.TextAppearance_Large)

        fpcb.setDialog(ad)
    }
}

private class FindPlaceCallbacks(private val context: Context,
                                 builder: AlertDialog.Builder,
                                 boundingBox: BoundingBox,
                                 private val onPlaceFound: (IGeoPoint) -> Unit) : View.OnClickListener, PlaceViewBase.OnResolveListener {
    private val placeView: PlaceView
    private lateinit var ad: AlertDialog

    init {
        val layout = View.inflate(context, R.layout.findplace, null)
        builder
            .setView(layout)
            .setPositiveButton(R.string.btn_find_place, MessageBox.NoAction)

        placeView = layout.findViewById(R.id.place)
        placeView.setBounds(boundingBox)
        placeView.textView.setOnEditorActionListener { view, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    onClick(view)
                    true
                }
                else -> false
            }
        }
    }

    fun setDialog(ad: AlertDialog) {
        this.ad = ad
        this.ad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this)
    }

    private fun placeSelected(place: GeoPlace?) {
        if (place?.coord() != null) {
            placeView.addHistory(place)

            onPlaceFound(place.coord())
            ad.dismiss()
        }
    }

    override fun onClick(view: View) {
        if (placeView.getText()!!.isEmpty()) {
            Toast.makeText(context, R.string.lbl_choose_place, Toast.LENGTH_LONG).show()
            return
        }

        placeView.geoPlace(this)
    }

    override fun onResolve(place: GeoPlace) {
        placeSelected(place)
    }
}
