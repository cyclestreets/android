package net.cyclestreets.util

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import net.cyclestreets.view.R

object TurnIcons {
    private lateinit var mapping: Map<String, Pair<Int, Drawable>>

    @JvmStatic
    fun initialise(context: Context) {
        val res = context.resources
        mapping = hashMapOf(
            "straight on" to getDrawable(res, R.drawable.straight_on),
            "bear left" to getDrawable(res, R.drawable.bear_left),
            "turn left" to getDrawable(res, R.drawable.turn_left),
            "sharp left" to getDrawable(res, R.drawable.sharp_left),
            "bear right" to getDrawable(res, R.drawable.bear_right),
            "turn right" to getDrawable(res, R.drawable.turn_right),
            "sharp right" to getDrawable(res, R.drawable.sharp_right),
            "double-back" to getDrawable(res, R.drawable.double_back),
            "join roundabout" to getDrawable(res, R.drawable.roundabout),
            "first exit" to getDrawable(res, R.drawable.first_exit),
            "second exit" to getDrawable(res, R.drawable.second_exit),
            "third exit" to getDrawable(res, R.drawable.third_exit),
            "waymark" to getDrawable(res, R.drawable.waymark),
            "default" to getDrawable(res, R.drawable.ic_launcher)
        )
    }

    @JvmStatic
    fun icon(turn: String): Drawable {
        return mappingFor(turn).second
    }

    @JvmStatic
    fun iconId(turn: String): Int {
        return mappingFor(turn).first
    }

    private fun mappingFor(turn: String): Pair<Int, Drawable> {
        return mapping[turn.toLowerCase()] ?: mapping["default"]!!
    }

    private fun getDrawable(res: Resources, iconId: Int): Pair<Int, Drawable> {
        return Pair(iconId, ResourcesCompat.getDrawable(res, iconId, null)!!)
    }
}
