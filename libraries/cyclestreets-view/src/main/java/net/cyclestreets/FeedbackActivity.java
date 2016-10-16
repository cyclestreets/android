package net.cyclestreets;

import net.cyclestreets.api.Result;
import net.cyclestreets.util.Dialog;
import net.cyclestreets.view.R;
import net.cyclestreets.api.Feedback;
import net.cyclestreets.routing.Route;
import net.cyclestreets.util.MessageBox;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FeedbackActivity extends Activity implements TextWatcher, OnClickListener {
  private Button upload_;
  
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.routefeedback);

    upload_ = (Button)findViewById(R.id.upload);
    
    upload_.setEnabled(false);
    upload_.setOnClickListener(this);
    
    setText(R.id.name, CycleStreetsPreferences.name());
    setText(R.id.email, CycleStreetsPreferences.email());
    
    textView(R.id.comments).addTextChangedListener(this);
  }
  
  private TextView textView(final int id) {
    return (TextView)findViewById(id);
  }
  
  private void setText(final int id, final String value) {
    textView(id).setText(value);
  }
  
  private String text(final int id) {
    return textView(id).getText().toString();
  }

  @Override
  public void afterTextChanged(Editable s) {}

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    upload_.setEnabled(s.length() != 0);
  }

  @Override
  public void onClick(View v) {
    FeedbackTask task = new FeedbackTask(this, Route.journey().itinerary(), text(R.id.comments),
                                         text(R.id.name), text(R.id.email));
    task.execute();
  }

  private void messageBox(Result result) {
    MessageBox.OKAndFinish(this.getCurrentFocus(), result.message(), this, result.ok());
  }

  private class FeedbackTask extends AsyncTask<Object, Void, Result> {
    private final int itinerary;
    private final String comments;
    private final String name;
    private final String email;
    private final ProgressDialog progress;

    public FeedbackTask(Context context,
                        int itinerary,
                        String comments,
                        String name,
                        String email) {
      this.itinerary = itinerary;
      this.comments = comments;
      this.name = name;
      this.email = email;

      this.progress = Dialog.createProgressDialog(context, R.string.feedback_sending);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      progress.show();
    }

    @Override
    protected Result doInBackground(final Object... params) {
      return Feedback.send(itinerary, comments, name, email);
    }

    @Override
    protected void onPostExecute(final Result result) {
      progress.dismiss();
      messageBox(result);
    }
  }
}
