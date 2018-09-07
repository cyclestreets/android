package net.cyclestreets.photos

import android.app.AlertDialog
import android.content.Context
import android.graphics.Point
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import net.cyclestreets.api.Photo
import net.cyclestreets.view.R

internal interface ImageDisplay {
    fun show()
}

internal abstract class DisplayDialog protected constructor(protected val photo: Photo,
                                                            protected val context: Context) : ImageDisplay, View.OnTouchListener, GestureDetector.OnGestureListener {
    private val gD: GestureDetector = GestureDetector(context, this)
    private lateinit var dialog: AlertDialog

    override fun show() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title())

        val layout = loadLayout()
        builder.setView(layout)

        val text = layout.findViewById<View>(R.id.caption) as TextView
        text.text = caption()

        preShowSetup(builder)

        dialog = builder.create()
        dialog.show()

        postShowSetup(dialog)

        layout.setOnTouchListener(this)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean { return gD.onTouchEvent(event) }
    override fun onDown(motionEvent: MotionEvent): Boolean { return false }
    override fun onShowPress(motionEvent: MotionEvent) {}
    override fun onSingleTapUp(motionEvent: MotionEvent): Boolean { return false }
    override fun onScroll(motionEvent: MotionEvent, motionEvent1: MotionEvent, v: Float, v1: Float): Boolean { return false }
    override fun onLongPress(motionEvent: MotionEvent) {}

    override fun onFling(motionEvent: MotionEvent, motionEvent1: MotionEvent, v: Float, v1: Float): Boolean {
        dialog.cancel()
        return true
    }

    protected abstract fun title(): String
    protected abstract fun caption(): String
    protected abstract fun loadLayout(): View

    protected open fun preShowSetup(builder: AlertDialog.Builder) {}
    protected open fun postShowSetup(dialog: AlertDialog) {}

    companion object {
        @JvmStatic
        protected fun sizeView(v: View, context: Context) {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val point = Point()
            wm.defaultDisplay.getSize(point)
            val deviceHeight = point.y
            val deviceWidth = point.x
            val height = if (deviceHeight > deviceWidth)
                deviceHeight / 10 * 5
            else
                deviceHeight / 10 * 6
            v.layoutParams = LinearLayout.LayoutParams(deviceWidth, height)
        }
    }
}
