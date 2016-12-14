package net.cyclestreets;

import android.app.Fragment;
import android.os.Bundle;
import android.app.Activity;

public abstract class FragmentHolder extends Activity {
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if(savedInstanceState == null)
      getFragmentManager().beginTransaction().add(android.R.id.content,
                                                         fragment()).commit();
  } // onCreate

  protected abstract Fragment fragment();
} // FragmentHolder
