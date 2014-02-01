package net.cyclestreets.util;

import net.cyclestreets.view.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Dialog 
{
  private static class CycleStreetsProgressDialog extends ProgressDialog
  {
    public CycleStreetsProgressDialog(final Context context, final String message) 
    {
      super(context);
      setMessage(message);
      setIndeterminate(true);
      setCancelable(false);
    } // CycleStreetsProgressDialog
    
    @Override
    public void dismiss() 
    {
      try {
        super.dismiss();
      } // try
      catch(final IllegalArgumentException e) {
        // suppress
      } // catch
    } // dismiss
  } // class CycleStreetsProgressDialog
  
	static public ProgressDialog createProgressDialog(final Context context,
	                                                  final int messageId)
	{
		return createProgressDialog(context, context.getString(messageId));
	} // createProgressDialog
	
	static public ProgressDialog createProgressDialog(final Context context,
	                                                  final String message)
	{
		final ProgressDialog progress = new CycleStreetsProgressDialog(context, message);
		return progress;
	} // createProgressDialog
	
	public interface UpdatedTextListener
	{
		public void updatedText(final String updated);
	} // interface UpdatedTextListener
	
	static public void editTextDialog(final Context context, 
									                  final String initialText,
									                  final String buttonText,
									                  final UpdatedTextListener listener)
	{
    final View layout = View.inflate(context, R.layout.edittextdialog, null);
	  final EditText textBox = ((EditText)layout.findViewById(R.id.edit_text));
	  textBox.setText(initialText);

	  final AlertDialog.Builder builder = newBuilder(context);
	  builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
	  	@Override
			public void onClick(DialogInterface dialog, int which) {
				final String t = textBox.getText().toString().trim();
				listener.updatedText(t);
			} // onClick
		});
	  builder.setView(layout);

	  show(builder);
	} // editTextDialog
	
	static public void listViewDialog(final Context context,
	                                  final ListAdapter adapter,
	                                  final DialogInterface.OnClickListener yesAction)
	{
	  listViewDialog(context, adapter, yesAction, MessageBox.NoAction);
	} // listViewDialog

	
	static public void listViewDialog(final Context context,
	                                  final ListAdapter adapter,
	                                  final DialogInterface.OnClickListener yesAction,
	                                  final DialogInterface.OnClickListener noAction)
	{
    final View layout = View.inflate(context, R.layout.listdialog, null);
    final ListView listView = ((ListView)layout.findViewById(R.id.list_view));
    listView.setAdapter(adapter);
    
    final AlertDialog.Builder builder = newBuilder(context);
    builder.setPositiveButton("OK", yesAction);
    builder.setNegativeButton("Cancel", noAction);
    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        noAction.onClick(dialog, -1);
      } // onCancel
    });
    builder.setView(layout);
    
    show(builder);
	} // listViewDialog
	
	static AlertDialog.Builder newBuilder(final View parent)
	{
		return newBuilder(parent.getContext());
	} // newBuilder
	
	static AlertDialog.Builder newBuilder(final Context context)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(applicationName(context));
		return builder;
	} // newBuilder

  private static String applicationName(final Context context) {
    int stringId = context.getApplicationInfo().labelRes;
    return context.getString(stringId);
  } // applicationName

  static void show(final AlertDialog.Builder builder)
	{
		final AlertDialog ad = builder.create();
		ad.show();
	} // show
} // class Dialog
