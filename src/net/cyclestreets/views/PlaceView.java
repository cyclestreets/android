package net.cyclestreets.views;

import net.cyclestreets.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class PlaceView extends LinearLayout
					   implements OnClickListener
{
	final Context context_;
	final PlaceAutoCompleteTextView textView_;
	final ImageButton button_;
	
	public PlaceView(final Context context) 
	{
		this(context, null);
	} // PlaceView
	
	public PlaceView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		context_ = context;
		
		setOrientation(HORIZONTAL);
		
		final LayoutInflater inflator = LayoutInflater.from(context);
		inflator.inflate(R.layout.placetextview, this);

		textView_ = (PlaceAutoCompleteTextView)findViewById(R.id.placeBox);
		button_ = (ImageButton)findViewById(R.id.optionsBtn);
		
		button_.setOnClickListener(this);
	} // PlaceView

	@Override
	public void onClick(final View v) 
	{
	} // onClick
} // class PlaceView
