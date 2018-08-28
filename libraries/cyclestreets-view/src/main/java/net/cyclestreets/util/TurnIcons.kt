package net.cyclestreets.util

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat
import net.cyclestreets.view.R

import net.cyclestreets.util.Turn.*

object TurnIcons {
    private lateinit var mapping: Map<Turn, Pair<Int, Drawable>>

    @JvmStatic
    fun initialise(context: Context) {
        val res = context.resources
        mapping = hashMapOf(
            STRAIGHT_ON to getDrawable(res, R.drawable.straight_on),
            BEAR_LEFT to getDrawable(res, R.drawable.bear_left),
            TURN_LEFT to getDrawable(res, R.drawable.turn_left),
            SHARP_LEFT to getDrawable(res, R.drawable.sharp_left),
            BEAR_RIGHT to getDrawable(res, R.drawable.bear_right),
            TURN_RIGHT to getDrawable(res, R.drawable.turn_right),
            SHARP_RIGHT to getDrawable(res, R.drawable.sharp_right),
            LEFT_RIGHT to getDrawable(res, R.drawable.left_right),
            RIGHT_LEFT to getDrawable(res, R.drawable.right_left),
            BEAR_LEFT_RIGHT to getDrawable(res, R.drawable.bear_left_right),
            BEAR_RIGHT_LEFT to getDrawable(res, R.drawable.bear_right_left),
            DOUBLE_BACK to getDrawable(res, R.drawable.double_back),
            JOIN_ROUNDABOUT to getDrawable(res, R.drawable.roundabout),
            FIRST_EXIT to getDrawable(res, R.drawable.first_exit),
            SECOND_EXIT to getDrawable(res, R.drawable.second_exit),
            THIRD_EXIT to getDrawable(res, R.drawable.third_exit),
            WAYMARK to getDrawable(res, R.drawable.waymark),
            DEFAULT to getDrawable(res, R.drawable.ic_launcher)
        )
    }

    @JvmStatic
    fun icon(turn: Turn): Drawable {
        return mappingFor(turn).second
    }

    @JvmStatic
    fun iconId(turn: Turn): Int {
        return mappingFor(turn).first
    }

    private fun mappingFor(turn: Turn): Pair<Int, Drawable> {
        return mapping[turn] ?: mapping[DEFAULT]!!
    }

    private fun getDrawable(res: Resources, iconId: Int): Pair<Int, Drawable> {
        return Pair(iconId, ResourcesCompat.getDrawable(res, iconId, null)!!)
    }
}
