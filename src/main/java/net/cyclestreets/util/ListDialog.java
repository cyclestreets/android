package net.cyclestreets.util;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ListDialog
{
	static public void showListDialog(final Context context, 
									  final String title,
									  final List<?> list,
									  final DialogInterface.OnClickListener clickListener)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        final CharSequence[] itemArray = new CharSequence[list.size()];
        for(int i = 0; i != list.size(); ++i)
        	itemArray[i] = list.get(i).toString();
        builder.setItems(itemArray, clickListener);	    

        builder.setTitle(title);
        final AlertDialog dlg = builder.create();
        dlg.show();
	} // createListDialog
} // class ListDialog
