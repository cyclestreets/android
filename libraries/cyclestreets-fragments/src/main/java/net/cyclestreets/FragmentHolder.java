package net.cyclestreets;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;

public abstract class FragmentHolder extends FragmentActivity {
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if(savedInstanceState == null)
      getSupportFragmentManager().beginTransaction().add(android.R.id.content,
                                                         fragment()).commit();
  } // onCreate

  protected abstract Fragment fragment();
} // FragmentHolder
