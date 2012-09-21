package net.cyclestreets;

import android.view.Menu;
import android.view.MenuItem;

class FragmentHelper
{
  static void createMenuItem(final Menu menu, 
                             final int itemId,
                             final int order,
                             final int iconId)
  {
    if(menu.findItem(itemId) != null)
      return;
    menu.add(0, itemId, order, itemId).setIcon(iconId);
  } // createMenuItem
  
  static void enableMenuItem(final Menu menu, final int itemId, final boolean enabled)
  {
    final MenuItem mi = menu.findItem(itemId);
    mi.setVisible(true);
    mi.setEnabled(enabled);
  } // enableMenuItem
  
  static void showMenuItem(final Menu menu, final int itemId, final boolean show)
  {
    final MenuItem mi = menu.findItem(itemId);
    mi.setVisible(show);
  } // showMenuItem
} // FragmentHelper
