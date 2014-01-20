package net.cyclestreets;

import net.cyclestreets.fragments.R;

import net.cyclestreets.api.Blog;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class BlogActivity extends Activity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.blog);

    final CheckBox notifications = (CheckBox)findViewById(R.id.blog_notifications);
    notifications.setChecked(CycleStreetsPreferences.blogNotifications());
    notifications.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
		{
      @Override
      public void onCheckedChanged(final CompoundButton button, final boolean checked)
      {
      CycleStreetsPreferences.setBlogNotifications(checked);
    } // onCheckedChanged
		});

    final WebView htmlView = (WebView)findViewById(R.id.html_view);
    htmlView.loadDataWithBaseURL(null, Blog.load().toHtml(), "text/html", "utf-8", null);
  } // onCreate
} // HtmlActivity
