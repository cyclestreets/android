package net.cyclestreets;

import net.cyclestreets.fragments.R;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.mikepenz.iconics.context.IconicsContextWrapper;

public abstract class MainTabbedActivity extends Activity implements OnTabChangeListener, TabHost.TabContentFactory
{
  private TabHost tabHost_;
  private final Map<String, TabInfo> tabs_ = new HashMap<>();
  private TabInfo lastTab_;

  @Override
  protected void attachBaseContext(Context newBase) {
    // Allows the use of Material icon library, see https://github.com/mikepenz/Android-Iconics
    super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.maintabbedactivity);
    tabHost_ = (TabHost)findViewById(android.R.id.tabhost);
    tabHost_.setup();
    tabHost_.setOnTabChangedListener(this);

    addTabs(tabHost_);

    for (int i = 0; i != tabs_.size(); ++i) {
      final ViewGroup.LayoutParams layout = tabHost_.getTabWidget().getChildAt(i).getLayoutParams();
      layout.height = (int)(layout.height*0.66);
      tabHost_.getTabWidget().getChildAt(i).setLayoutParams(layout);
    }

    // start with route tab
    showMap();

    showWhatsNew();
  }

  protected abstract void addTabs(final TabHost tabHost);

  protected void setCurrentTab(final int tab) {
    tabHost_.setCurrentTab(tab);
  }

  public void showMap() {
    tabHost_.setCurrentTab(0);
  }

  public void showWhatsNew() {
    if (!CycleStreetsAppSupport.isNewVersion())
      return;

    final LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View whatsnewView = layoutInflater.inflate(R.layout.whatsnew, null);
    final WebView htmlView = (WebView)whatsnewView.findViewById(R.id.html_view);
    htmlView.loadUrl("file:///android_asset/whatsnew.html");

    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("What's New")
           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { }
            })
           .setView(whatsnewView)
           .show();
  }

  protected SharedPreferences prefs() {
    return getSharedPreferences("net.cyclestreets.CycleStreets", Context.MODE_PRIVATE);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Tab handling
  final class TabInfo  {
    private final String tag_;
    private final Class<? extends Fragment> clss_;
    private final Bundle args_;
    private Fragment fragment_;
    private boolean menuCreated_;

    TabInfo(final FragmentManager fm,
            final String t,
            final Class<? extends Fragment> fc,
            final Bundle a) {
      tag_ = t;
      clss_ = fc;
      args_ = a;
      menuCreated_ = false;

      // Check to see if we already have a fragment for this tab, probably
      // from a previously saved state.  If so, deactivate it, because our
      // initial state is that a tab isn't shown.

      fragment_ = fm.findFragmentByTag(tag_);
      if (fragment_ != null && !fragment_.isDetached()) {
        final FragmentTransaction ft = fm.beginTransaction();
        ft.detach(fragment_);
        ft.commit();
      }
    }

    void attach(final FragmentTransaction ft) {
      if (fragment_ == null) {
        fragment_ = Fragment.instantiate(MainTabbedActivity.this,
                                         clss_.getName(),
                                         args_);
        ft.add(R.id.realtabcontent, fragment_, tag_);
      }
      else
        ft.attach(fragment_);
    }

    void detach(final FragmentTransaction ft) {
      if (fragment_ == null)
        return;
      ft.detach(fragment_);
    }

    void onPrepareOptionsMenu(final Menu menu, final MenuInflater inflater) {
      if (!menuCreated_) {
        fragment_.onCreateOptionsMenu(menu, inflater);
        menuCreated_ = true;
      }
      fragment_.onPrepareOptionsMenu(menu);
    }

    boolean onOptionsItemSelected(final MenuItem item) {
      return fragment_.onOptionsItemSelected(item);
    }

    boolean onContextItemSelected(final MenuItem item) {
      return fragment_.onContextItemSelected(item);
    }

    boolean onBackPressed() {
      if (!(fragment_ instanceof Undoable))
        return false;
      return ((Undoable)fragment_).onBackPressed();
    }
  }  // class TabInfo

  @Override
  public View createTabContent(String tag) {
    final View v = new View(this);
    v.setMinimumWidth(0);
    v.setMinimumHeight(0);
    return v;
  }

  protected void addTab(final String tabId,
                        final int iconId,
                        final Class<? extends Fragment> fragClass) {
    final TabSpec tabSpec = tabHost_.newTabSpec(tabId);
    tabSpec.setIndicator("", getResources().getDrawable(iconId));
    tabSpec.setContent(this);

    final TabInfo info = new TabInfo(getFragmentManager(),
                                     tabId, fragClass, null);

    tabs_.put(tabId, info);
    tabHost_.addTab(tabSpec);
  }

  @Override
  public void onTabChanged(String tabId) {
    final TabInfo newTab = tabs_.get(tabId);
    if (lastTab_ == newTab)
      return;

    final FragmentTransaction ft = getFragmentManager().beginTransaction();
    if (lastTab_ != null)
      lastTab_.detach(ft);

    if (newTab != null)
      newTab.attach(ft);

    lastTab_ = newTab;
    ft.commit();
    getFragmentManager().executePendingTransactions();

    setTitle(applicationName() + " : " + tabId);
  }

  public String applicationName() {
    int stringId = getApplicationInfo().labelRes;
    return getString(stringId);
  }

  // pause/resume
  @Override
  protected void onPause() {
    super.onPause();

    final SharedPreferences.Editor edit = prefs().edit();
    edit.putString("TAB", tabHost_.getCurrentTabTag());
    edit.commit();
  }

  @Override
  protected void onResume() {
    final String tab = prefs().getString("TAB", "");
    tabHost_.setCurrentTabByTag(tab);

    super.onResume();
  }

  // menus
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    // fragments all share the same menu
    super.onCreateOptionsMenu(menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {
    // turn them all off
    for (int i = 0; i != menu.size(); ++i) {
      final MenuItem mi = menu.getItem(i);
      mi.setVisible(false);
    }

    // then let each fragment reenable
    lastTab_.onPrepareOptionsMenu(menu, getMenuInflater());
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (lastTab_.onOptionsItemSelected(item))
      return true;

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onContextItemSelected(final MenuItem item) {
    if (lastTab_.onContextItemSelected(item))
      return true;
    return super.onContextItemSelected(item);
  }

  // touch and buttons
  @Override
  public void onBackPressed() {
    if (lastTab_.onBackPressed())
      return;
    super.onBackPressed();
  }
}
