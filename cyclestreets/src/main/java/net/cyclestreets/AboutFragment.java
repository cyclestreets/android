package net.cyclestreets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

public class AboutFragment extends Fragment {
  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle savedInstanceState) {
    final View about = inflater.inflate(R.layout.about, container, false);

    final WebView htmlView = (WebView)about.findViewById(R.id.html_view);
    htmlView.loadUrl("file:///android_asset/credits.html");

    final TextView versionView = (TextView)about.findViewById(R.id.version_view);
    versionView.setText(versionName());

    return about;
  } // onCreateView

  private String versionName() {
    return CycleStreetsAppSupport.version();
  } // versionName
} // AboutFragment
