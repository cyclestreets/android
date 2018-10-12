//package net.cyclestreets
//
//import android.os.Bundle
//import android.renderscript.RenderScript
//import android.support.v4.app.Fragment
//import net.cyclestreets.offline.OfflineMap
//import net.cyclestreets.offline.offlineMaps
//
//import com.tonyodev.fetch2.Request
//import com.tonyodev.fetch2.FetchConfiguration
//import com.tonyodev.fetch2.Fetch
//
//class OfflineMapDownloadFragment : Fragment() {
//
//    override fun onCreate(savedInstance: Bundle?) {
//        super.onCreate(savedInstance)
//
//        // need to persist the Fetch beyond restarts, probably... or warn users that things will fail
//        val fetchConfiguration = FetchConfiguration.Builder(context!!)
//                .setDownloadConcurrentLimit(3)
//                .build()
//        val fetch = Fetch.getInstance(fetchConfiguration);
//
//        val mapToGet: OfflineMap = offlineMaps[3]
//
//        val request: Request = Request(url, file);
//        request.setPriority(RenderScript.Priority.HIGH);
//        request.setNetworkType(NetworkType.ALL);
//        request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG");
//
////        fetch.enqueue(request, updatedRequest -> {
////            //Request was successfully enqueued for download.
////        }, error -> {
////            //An error occurred enqueuing the request.
////        });
//
//    }
//}