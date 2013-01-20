package net.cyclestreets;

import net.cyclestreets.api.Feedback;
import net.cyclestreets.planned.Route;
import net.cyclestreets.util.MessageBox;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FeedbackActivity extends Activity implements TextWatcher, OnClickListener 
{
	private Button upload_;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.routefeedback);

		upload_ = (Button)findViewById(R.id.upload);
		
		upload_.setEnabled(false);
		upload_.setOnClickListener(this);
		
		setText(R.id.name, CycleStreetsPreferences.name());
		setText(R.id.email, CycleStreetsPreferences.email());
		
		textView(R.id.comments).addTextChangedListener(this);
	} // onCreate
	
	private TextView textView(final int id)
	{
		return (TextView)findViewById(id);
	} // textView
	
	private void setText(final int id, final String value)
	{
		textView(id).setText(value);
	} // setText
	
	private String text(final int id)
	{
		return textView(id).getText().toString();
	} // getText

	@Override
	public void afterTextChanged(Editable s) {	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) 
	{
		upload_.setEnabled(s.length() != 0);
	} //onTextChanged

	@Override
	public void onClick(View v) 
	{
		try { 
			final Feedback.Result result = Feedback.send(Route.itinerary(), 
              			                               text(R.id.comments), 
              			                               text(R.id.name),
              			                               text(R.id.email));
			MessageBox.OKAndFinish(this.getCurrentFocus(), result.message(), this, result.ok());
		}
		catch(Exception e) {
			MessageBox.OK(v, "There was a problem sending your comments:\n" + e.getMessage());
		}
	} // onClick

} // FeedbackActivity
