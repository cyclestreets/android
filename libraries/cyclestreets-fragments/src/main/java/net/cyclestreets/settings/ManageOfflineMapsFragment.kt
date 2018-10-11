package net.cyclestreets.settings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import net.cyclestreets.content.OfflineMapDatabase
import net.cyclestreets.fragments.R

class ManageOfflineMapsFragment : Fragment() {
    private lateinit var offlineMapDb: OfflineMapDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        offlineMapDb = OfflineMapDatabase(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val mainView = inflater.inflate(R.layout.manage_offline_maps, container, false)
        val mapList = mainView.findViewById<LinearLayout>(R.id.offline_map_list)

        offlineMapDb.offlineMaps().forEach {
            val map = inflater.inflate(R.layout.offline_map, null)
            val commonInfo = map.findViewById<TextView>(R.id.offline_map_common_info)
            val localInfo = map.findViewById<TextView>(R.id.offline_map_local_info)
            val remoteInfo = map.findViewById<TextView>(R.id.offline_map_remote_info)

            commonInfo.text = it.name
            remoteInfo.text = "Last modified: ${it.lastModified}\nSize: ${it.sizeMb} MB"
            localInfo.text = "TODO: get info from local filesystem"
            mapList.addView(map)
        }
        return mainView
    }
}
