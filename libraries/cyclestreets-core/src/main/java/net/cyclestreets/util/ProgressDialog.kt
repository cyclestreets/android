package net.cyclestreets.util

import android.content.Context

open class ProgressDialog : android.app.ProgressDialog {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, theme: Int) : super(context, theme)
}
