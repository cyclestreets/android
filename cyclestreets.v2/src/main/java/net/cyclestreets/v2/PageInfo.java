package net.cyclestreets.v2;

import android.support.v4.app.Fragment;

public class PageInfo {
  private String title_;
  private Class<? extends Fragment> fragClass_;
  private Fragment fragment_;

  public PageInfo(final String title, final Class<? extends Fragment> fragClass) {
    title_ = title;
    fragClass_ = fragClass;
  } // PageInfo

  public String title() { return title_; }
  public Fragment fragment() {
    try {
      if (fragment_ == null)
        fragment_ = fragClass_.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } // try
    return fragment_;
  } // fragment

  @Override
  public String toString() { return title_; }
} // PageInfo