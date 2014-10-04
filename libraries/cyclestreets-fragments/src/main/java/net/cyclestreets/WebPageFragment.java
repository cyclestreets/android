package net.cyclestreets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import net.cyclestreets.fragments.R;

public class WebPageFragment extends Fragment {
  private String homePage_;

  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle saved) {
    final View webPage = inflater.inflate(R.layout.webpage, null);

    final WebView htmlView = (WebView)webPage.findViewById(R.id.html_view);
    htmlView.loadUrl(homePage_);

    return webPage;
  } // onCreateView

  public void loadUrl(final String url) {
    homePage_ = url;
  } // loadUrl
} // WebPageFragment
