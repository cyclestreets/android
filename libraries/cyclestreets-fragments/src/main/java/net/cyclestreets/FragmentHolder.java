package net.cyclestreets;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public abstract class FragmentHolder extends ActionBarActivity {
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if(savedInstanceState == null)
      getSupportFragmentManager().beginTransaction().add(android.R.id.content,
                                                         fragment()).commit();
  } // onCreate

  protected abstract Fragment fragment();
} // FragmentHolder
