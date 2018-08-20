package net.cyclestreets;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.mikepenz.iconics.context.IconicsContextWrapper;

public abstract class FragmentHolder extends FragmentActivity {

  @Override
  protected void attachBaseContext(Context newBase) {
    // Allows the use of Material icon library, see https://github.com/mikepenz/Android-Iconics
    super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null)
      getSupportFragmentManager().beginTransaction().add(android.R.id.content,
                                                         fragment()).commit();
  }

  protected abstract Fragment fragment();
}
