package net.cyclestreets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import net.cyclestreets.fragments.R;

public class WebPageFragment extends Fragment {
  public static MainNavDrawerActivity.PageInitialiser initialiser(final String url) {
    return new WebPageInitialiser(url);
  } // initialiser

  private static class WebPageInitialiser implements MainNavDrawerActivity.PageInitialiser {
    private final String url_;
    public WebPageInitialiser(final String url) { url_ = url; }

    public void initialise(Fragment page) {
      ((WebPageFragment)page).loadUrl(url_);
    } // initialise
  } // WebPageInitialiser

  //////////////////////////////////////////////////
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
