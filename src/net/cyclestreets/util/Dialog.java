package net.cyclestreets.util;

import net.cyclestreets.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

public class Dialog 
{
	static public ProgressDialog createProgressDialog(final Context context,
	                                                  final int messageId)
	{
		return createProgressDialog(context, context.getString(messageId));
	} // createProgressDialog
	
	static public ProgressDialog createProgressDialog(final Context context,
	                                                  final String message)
	{
		final ProgressDialog progress = new ProgressDialog(context);
		progress.setMessage(message);
		progress.setIndeterminate(true);
		progress.setCancelable(false);
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

	
	static AlertDialog.Builder newBuilder(final View parent)
	{
		return newBuilder(parent.getContext());
	} // newBuilder
	
	static AlertDialog.Builder newBuilder(final Context context)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("CycleStreets");
		return builder;
	} // newBuilder
	
	static void show(final AlertDialog.Builder builder)
	{
		final AlertDialog ad = builder.create();
		ad.show();
	} // show
} // class Dialog
