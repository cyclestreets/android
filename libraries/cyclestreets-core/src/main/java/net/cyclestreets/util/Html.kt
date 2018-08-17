package net.cyclestreets.util

import android.os.Build
import android.text.Html
import android.text.Spanned

fun fromHtml(string: String): Spanned {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        return Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY)
    else
        return fromHtmlPreNougat(string)
}

@Suppress("deprecation")
private fun fromHtmlPreNougat(string: String): Spanned {
    return Html.fromHtml(string)
}
