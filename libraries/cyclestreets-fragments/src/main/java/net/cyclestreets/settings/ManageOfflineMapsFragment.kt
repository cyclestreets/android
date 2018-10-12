package net.cyclestreets.settings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

import net.cyclestreets.content.OfflineMapDatabase
import net.cyclestreets.fragments.R
import net.cyclestreets.offline.OfflineMap
import net.cyclestreets.offline.OfflineMapDownloadTask
import net.cyclestreets.util.AsyncDelete
import java.io.File
import java.lang.ref.WeakReference

// TODO: get info from local filesystem
private val localOfflineMaps: Map<String, OfflineMap> = mapOf(
    "Andorra" to OfflineMap("Andorra", "/made/up-file", "2018-09-17", 1)
)

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

        offlineMapDb.offlineMaps().forEach { m ->
            val map = inflater.inflate(R.layout.offline_map, null)
            val commonInfo = map.findViewById<TextView>(R.id.offline_map_common_info)
            val localInfo = map.findViewById<TextView>(R.id.offline_map_local_info)
            val localDelete = map.findViewById<TextView>(R.id.offline_map_local_delete)
            val remoteInfo = map.findViewById<TextView>(R.id.offline_map_remote_info)
            val remoteDownload = map.findViewById<TextView>(R.id.offline_map_remote_download)
            val progressBar = map.findViewById<ProgressBar>(R.id.offline_map_download_progress)

            val localCopy: OfflineMap? = localOfflineMaps.get(m.name)

            commonInfo.text = m.name

            remoteInfo.text = "Last modified: ${m.lastModified}\nSize: ${m.sizeMb} MB"
            //            remoteDownload.text = ClickableSpan() <- todo: use this model instead
            remoteDownload.setOnClickListener { _ -> download(m, remoteDownload, progressBar, localInfo, localDelete) }

            localCopy?.let {
                localInfo.text = "Last modified: ${it.lastModified}\nSize: ${it.sizeMb} MB"
                localDelete.visibility = VISIBLE
                localDelete.setOnClickListener { _ -> deleteLocalCopy(localCopy.url, localInfo, localDelete) }
            }
            mapList.addView(map)
        }
        return mainView
    }

    private fun download(m: OfflineMap, remoteDownload: TextView, progressBar: ProgressBar,
                         localInfo: TextView, localDelete: TextView) {
        val task = OfflineMapDownloadTask(
            m.url,
            File("/tmp/file"),
            WeakReference(progressBar),
            WeakReference(localInfo),
            WeakReference(localDelete)
        )
        remoteDownload.text = "Downloading... click to cancel"
        remoteDownload.setOnClickListener { _ -> task.cancel(false) }
        task.execute()
    }

    private fun deleteLocalCopy(filePath: String, localInfo: TextView, localDelete: TextView) {
        AsyncDelete().execute(File(filePath))
        localInfo.text = "None"
        localDelete.visibility = INVISIBLE
    }

}
