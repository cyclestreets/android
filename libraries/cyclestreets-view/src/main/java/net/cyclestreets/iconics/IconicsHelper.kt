package net.cyclestreets.iconics

import android.content.Context
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon

import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import net.cyclestreets.util.Logging


private val TAG = Logging.getTag(IconicsHelper::class.java)


object IconicsHelper {

    fun materialIcon(context: Context, iconId: IIcon, color: Int? = null, size: Int = 24): IconicsDrawable {
        return materialIcons(context, listOf(iconId), color, size).first()
    }

    fun materialIcons(context: Context, iconIds: List<IIcon>, color: Int? = null, size: Int = 24): List<IconicsDrawable> {
        return iconIds.map {
            iconId -> IconicsDrawable(context, iconId)
                .apply {
                    sizeDp = size
                    color?.let { this.colorInt = color}
                }
        }
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

    // Derive the Context from a MenuInflater (using reflection).
    //
    // In some fragment transitions, menu inflation is performed before the fragment's context
    // is initialised, so we can't just do a `getContext()`; the internal `mContext` field is used
    // in this scope by the native inflater.inflate(), so we should be safe.
    private fun getContext(inflater: MenuInflater): Context? {
        return try {
            val f = inflater.javaClass.getDeclaredField("mContext")
            f.isAccessible = true
            f.get(inflater) as Context
        } catch (e: IllegalAccessException) {
            Log.w(TAG, "IllegalAccessException: Failed to find mContext on ${inflater.javaClass.canonicalName}")
            null
        } catch (e: NoSuchFieldException) {
            Log.w(TAG, "NoSuchFieldException: Failed to find mContext on ${inflater.javaClass.canonicalName}")
            null
        }
    }
}
