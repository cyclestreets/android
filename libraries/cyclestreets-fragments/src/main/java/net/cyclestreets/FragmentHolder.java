package net.cyclestreets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public abstract class FragmentHolder extends FragmentActivity {
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null)
      getSupportFragmentManager().beginTransaction().add(android.R.id.content,
                                                         fragment()).commit();
  }

  protected abstract Fragment fragment();
}
