package net.cyclestreets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.cyclestreets.fragments.R;

@SuppressLint("ValidFragment")
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
  private int layout_ = R.layout.webpage;

  public WebPageFragment() {
    homePage_ = null;
  } // WebPageFragment

  protected WebPageFragment(final String url) {
    homePage_ = url;
  } // WebPageFragment

  protected WebPageFragment(final String url,
                            final int layout) {
    this(url);
    layout_ = layout;
  } // WebPageFragment

  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle saved) {
    final View webPage = inflater.inflate(layout_, null);

    final WebView htmlView = (WebView)webPage.findViewById(R.id.html_view);
    if (homePage_ != null) {
      htmlView.setWebViewClient(new FragmentViewClient(getActivity(), homePage_));
      htmlView.loadUrl(homePage_);
    } // if ...

    return webPage;
  } // onCreateView

  public void loadUrl(final String url) {
    homePage_ = url;
  } // loadUrl

  private static class FragmentViewClient extends WebViewClient {
    private final Context context_;
    private String homePage_;

    public FragmentViewClient(final Context context,
                              final String homePage) {
      context_ = context;
      homePage_ = homePage;
    } // FragmentViewClient

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (url.equals(homePage_))
        return false;

      // Otherwise, give the default behavior (open in browser)
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      context_.startActivity(intent);
      return true;
    }
  }
} // WebPageFragment
