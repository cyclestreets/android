package net.cyclestreets.iconics

import android.content.Context
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon

import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import net.cyclestreets.util.Logging

private val TAG = Logging.getTag(IconicsHelper::class.java)

object IconicsHelper {
    // Derive Context from the inflater, and then create the IconicsDrawable.
    fun drawable(inflater: Any, iconId: IIcon, colorFunction: (Context) -> Int): IconicsDrawable? {
        getContext(inflater)?.apply {
            return IconicsDrawable(this).icon(iconId).color(colorFunction(this))
        }
        return null
    }

    // Derive Context from the inflater, and then delegate to the Iconics inflater.
    fun inflate(inflater: MenuInflater, menuId: Int, menu: Menu) {
        inflate(inflater, menuId, menu, true)
    }

    // Derive Context from the inflater, and then delegate to the Iconics inflater.
    fun inflate(inflater: MenuInflater, menuId: Int, menu: Menu, checkSubMenus: Boolean) {
        val context = getContext(inflater)

        if (context != null) {
            IconicsMenuInflaterUtil.inflate(inflater, context, menuId, menu, checkSubMenus)
        } else {
            // In the worst case (e.g. on Google implementation change), we fall back to the default
            // inflater; we'll lose the icons but won't fall over.
            inflater.inflate(menuId, menu)
        }
    }

    // Use reflection to derive the Context from a MenuInflater or LayoutInflater.
    //
    // In some fragment transitions, the menu inflation is performed before the fragment's context
    // is initialised, so we can't just do a `getContext()`; the internal `mContext` field is used
    // in this way by the native inflater.inflate(), so we should be safe.
    private fun getContext(inflater: Any): Context? {
        return try {
            val f = inflater.javaClass.getDeclaredField("mContext")
            f.isAccessible = true
            f.get(inflater) as Context
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "IllegalAccessException: Failed to find mContext on ${inflater.javaClass.canonicalName}")
            null
        } catch (e: NoSuchFieldException) {
            Log.e(TAG, "NoSuchFieldException: Failed to find mContext on ${inflater.javaClass.canonicalName}")
            null
        }
    }
}
