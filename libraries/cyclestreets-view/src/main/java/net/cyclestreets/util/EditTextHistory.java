package net.cyclestreets.util;

import net.cyclestreets.view.R;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EditTextHistory extends ArrayAdapter<String> {
  private static final String PREFS_KEY = "net.cyclestreets.api.EditTextAdapter";
  private static final String LAST_WRITTEN = "lastWritten";
  private static final int MAX_HISTORY = 20;
  private static final int AdapterViewId = R.layout.texthistory;
  
  private final LayoutInflater inflater_;
  private final SharedPreferences prefs_;

  public EditTextHistory(final Context context, final String name) {
    super(context, AdapterViewId);
    inflater_ = LayoutInflater.from(context);
    prefs_ = context.getSharedPreferences(PREFS_KEY + "-" + name, Application.MODE_PRIVATE);
    
    loadHistory();
  } // EditTextHistory
  
  private void loadHistory() {
    for(int c = 0; c != MAX_HISTORY; ++c) {
      final String e = prefs_.getString(Integer.toString(c), "");
      if(e.length() != 0)
        add(e);
    } // for ...
  } // loadHistory
  
  public void addHistory(final String n) {
    if(n == null || n.length() == 0)
      return;
    
    int lastWritten = prefs_.getInt(LAST_WRITTEN, -1);    
    ++lastWritten;
    if(lastWritten == MAX_HISTORY)
      lastWritten = 0;
    
    final SharedPreferences.Editor edit = prefs_.edit();
    edit.putString(Integer.toString(lastWritten), n);
    edit.putInt(LAST_WRITTEN, lastWritten);
    edit.commit();
  } // addHistory

  @Override
  public View getView(final int position, 
                      final View convertView, 
                      final ViewGroup parent) {
    final TextView row = (TextView)inflater_.inflate(AdapterViewId, parent, false);
    final String s = getItem(position);
  
    row.setText(s);

    return row;
  } // getView
} // EditTextHistory 
