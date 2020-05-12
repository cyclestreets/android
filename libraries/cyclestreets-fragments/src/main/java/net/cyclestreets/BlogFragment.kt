package net.cyclestreets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

private const val CYCLE_STREETS_BLOG_URL = "https://www.cyclestreets.org/news/"

class BlogFragment : WebPageFragment(CYCLE_STREETS_BLOG_URL) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        BlogState.markBlogAsRead(requireActivity())
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}
