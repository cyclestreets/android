package net.cyclestreets.util;

import android.view.Menu;
import android.view.MenuItem;

public class MenuHelper
{
  static public MenuItem createMenuItem(final Menu menu, 
                                        final int itemId,
                                        final int order,
                                        final int iconId)
  {
    final MenuItem item = menu.findItem(itemId);
    if(item != null)
      return item;
    final MenuItem newItem = menu.add(0, itemId, order, itemId);
    if(iconId != 0)
      newItem.setIcon(iconId);
    return newItem;
  } // createMenuItem

  static public MenuItem createMenuItem(final Menu menu, final int itemId)
  {
    final MenuItem item = menu.findItem(itemId);
    if(item != null)
      return item;
    return menu.add(0, itemId, Menu.NONE, itemId);
  } // createMenuItem
  
  static public MenuItem enableMenuItem(final Menu menu, final int itemId, final boolean enabled)
  {
    final MenuItem mi = menu.findItem(itemId);
    if (mi != null) {
      mi.setVisible(true);
      mi.setEnabled(enabled);
    } // if ...
    return mi;
  } // enableMenuItem
  
  static public MenuItem showMenuItem(final Menu menu, final int itemId, final boolean show)
  {
    final MenuItem mi = menu.findItem(itemId);
    if (mi != null)
      mi.setVisible(show);
    return mi;
  } // showMenuItem
} // MenuHelper
