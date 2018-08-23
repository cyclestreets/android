package net.cyclestreets

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

import net.cyclestreets.fragments.R

@SuppressLint("ValidFragment")
open class WebPageFragment : Fragment {
    private val homePage: String
    private val layout: Int

    protected constructor(url: String) {
        homePage = url
        this.layout = R.layout.webpage
    }

    protected constructor(url: String, layout: Int) {
        homePage = url
        this.layout = layout
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val webPage = inflater.inflate(layout, null)
        val htmlView = webPage.findViewById<WebView>(R.id.html_view)

        htmlView.webViewClient = FragmentViewClient(context!!, homePage)
        htmlView.settings.javaScriptEnabled = true
        htmlView.loadUrl(homePage)

        return webPage
    }

    private class FragmentViewClient(private val context: Context, private val homePage: String) : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return shouldOverrideUrlLoading(request.url.toString())
        }

        @Deprecated("")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return shouldOverrideUrlLoading(url)
        }

        private fun shouldOverrideUrlLoading(url: String): Boolean {
            if (url == homePage)
                return false

            // Otherwise, give the default behavior (open in browser)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            return true
        }
    }
}
