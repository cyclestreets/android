package net.cyclestreets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.cyclestreets.fragments.R;

@SuppressLint("ValidFragment")
public class WebPageFragment extends Fragment {
  public static MainNavDrawerActivity.PageInitialiser initialiser(final String url) {
    return initialiser(url, null);
  }
  public static MainNavDrawerActivity.PageInitialiser initialiser(final String url, final String postLoadJs) {
    return new WebPageInitialiser(url, postLoadJs);
  }

  private static class WebPageInitialiser implements MainNavDrawerActivity.PageInitialiser {
    private final String url_;
    private final String customiser_;
    public WebPageInitialiser(final String url) { this(url, null); }
    public WebPageInitialiser(final String url, final String customiser) {
      url_ = url;
      customiser_ = customiser;
    }

    public void initialise(Fragment page) {
      ((WebPageFragment)page).loadUrl(url_, customiser_);
    }
  }

  //////////////////////////////////////////////////
  private String homePage_;
  private String postLoadJs_;
  private int layout_ = R.layout.webpage;

  public WebPageFragment() {
    homePage_ = null;
  }

  protected WebPageFragment(final String url) {
    homePage_ = url;
  }

  protected WebPageFragment(final String url,
                            final int layout) {
    this(url);
    layout_ = layout;
  }

  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle saved) {
    final View webPage = inflater.inflate(layout_, null);

    final WebView htmlView = (WebView)webPage.findViewById(R.id.html_view);
    if (homePage_ != null) {
      htmlView.setWebViewClient(new FragmentViewClient(getActivity(), homePage_, postLoadJs_));
      htmlView.getSettings().setJavaScriptEnabled(true);
      htmlView.loadUrl(homePage_);
    }

    return webPage;
  }

  public void loadUrl(final String url,
                      final String customiser) {
    homePage_ = url;
    postLoadJs_ = customiser;
  }

  private static class FragmentViewClient extends WebViewClient {
    private final Context context_;
    private String homePage_;
    private String postLoadJs_;

    public FragmentViewClient(final Context context,
                              final String homePage,
                              final String postLoadJs) {
      context_ = context;
      homePage_ = homePage;
      postLoadJs_ = postLoadJs;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
      return shouldOverrideUrlLoading(request.getUrl().toString());
    }

    @Override
    @Deprecated
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return shouldOverrideUrlLoading(url);
    }

    private boolean shouldOverrideUrlLoading(String url) {
      if (url.equals(homePage_))
        return false;

      // Otherwise, give the default behavior (open in browser)
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      context_.startActivity(intent);
      return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      if (url.equals(homePage_) && postLoadJs_ != null)
          view.loadUrl("javascript:(function() { " +
              postLoadJs_ +
          "})()");
    }
  }
}
