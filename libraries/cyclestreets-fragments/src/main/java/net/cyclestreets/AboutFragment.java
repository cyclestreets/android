package net.cyclestreets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.cyclestreets.fragments.R;

public class AboutFragment extends WebPageFragment {
  public AboutFragment() {
    super("file:///android_asset/credits.html", R.layout.about);
  } // AboutFragment

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container,
                           final Bundle savedInstanceState) {
    final View about = super.onCreateView(inflater, container, savedInstanceState);

    final TextView versionView = (TextView)about.findViewById(R.id.version_view);
    versionView.setText(CycleStreetsAppSupport.version());

    return about;
  } // onCreateView
} // AboutFragment
