package net.cyclestreets.util

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import net.cyclestreets.view.R

import net.cyclestreets.util.Turn.*

object TurnIcons {
    private lateinit var mapping: Map<Turn, Pair<Int, Drawable>>

    @JvmStatic
    fun initialise(context: Context) {
        val res = context.resources
        mapping = hashMapOf(
            STRAIGHT_ON to getDrawable(res, R.drawable.lr_straight_on),
            BEAR_LEFT to getDrawable(res, R.drawable.lr_bear_left),
            TURN_LEFT to getDrawable(res, R.drawable.lr_turn_left),
            SHARP_LEFT to getDrawable(res, R.drawable.lr_sharp_left),
            BEAR_RIGHT to getDrawable(res, R.drawable.lr_bear_right),
            TURN_RIGHT to getDrawable(res, R.drawable.lr_turn_right),
            SHARP_RIGHT to getDrawable(res, R.drawable.lr_sharp_right),
            TURN_LEFT_THEN_RIGHT to getDrawable(res, R.drawable.lr_turn_left_then_right),
            TURN_RIGHT_THEN_LEFT to getDrawable(res, R.drawable.lr_turn_right_then_left),
            BEAR_LEFT_THEN_RIGHT to getDrawable(res, R.drawable.lr_bear_left_then_right),
            BEAR_RIGHT_THEN_LEFT to getDrawable(res, R.drawable.lr_bear_right_then_left),
            DOUBLE_BACK to getDrawable(res, R.drawable.lr_double_back_right),
            FIRST_EXIT to getDrawable(res, R.drawable.lr_first_exit),
            SECOND_EXIT to getDrawable(res, R.drawable.lr_second_exit),
            THIRD_EXIT to getDrawable(res, R.drawable.lr_third_exit),
            WAYMARK to getDrawable(res, R.drawable.lr_waymark),
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
