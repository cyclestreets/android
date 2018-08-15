package net.cyclestreets.views.place

import net.cyclestreets.view.R
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton

class PlaceViewWithCancel @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        PlaceViewBase(context, R.layout.placetextviewcancel, attrs) {

    private val cancel: ImageButton = findViewById(R.id.cancelBtn)

    fun enableCancel(enabled: Boolean) {
        cancel.isEnabled = enabled
    }

    fun setCancelOnClick(listener: View.OnClickListener) {
        cancel.setOnClickListener(listener)
    }
}
