package net.cyclestreets.addphoto

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Spinner
import android.widget.TextView

import net.cyclestreets.api.PhotomapCategory

internal class CategoryAdapter(context: Context,
                               private val categories: List<PhotomapCategory>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return categories.size
    }

    override fun getItem(position: Int): String {
        val c = categories[position]
        return c.name
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val id = if (parent is Spinner) android.R.layout.simple_spinner_item else android.R.layout.simple_spinner_dropdown_item
        val tv = inflater.inflate(id, parent, false) as TextView
        tv.text = getItem(position)
        return tv
    }
}
