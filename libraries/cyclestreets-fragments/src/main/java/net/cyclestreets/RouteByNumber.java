package net.cyclestreets;

import net.cyclestreets.fragments.R;

import net.cyclestreets.routing.Route;
import net.cyclestreets.util.EditTextHistory;
import net.cyclestreets.util.MessageBox;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;

public class RouteByNumber {
  public static void launch(final Context context) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(R.string.ic_menu_route_number);

    final RouteByNumberCallbacks rbnc = new RouteByNumberCallbacks(context, builder);

    final AlertDialog ad = builder.create();
    ad.show();

    rbnc.setDialog(ad);
  } // launch

  private static class RouteByNumberCallbacks
      implements View.OnClickListener {
    private final Context context_;
    private final AutoCompleteTextView numberText_;
    private final RadioGroup routeTypeGroup;
    private final EditTextHistory history_;
    private AlertDialog ad_;

    public RouteByNumberCallbacks(final Context context,
                                  final AlertDialog.Builder builder) {
      context_ = context;

      final View layout = View.inflate(context, R.layout.routenumber, null);
      builder.setView(layout);

      builder.setPositiveButton(R.string.go, MessageBox.NoAction);

      numberText_ = (AutoCompleteTextView)layout.findViewById(R.id.routeNumber);
      history_ = new EditTextHistory(context, "RouteNumber");
      numberText_.setAdapter(history_);

      routeTypeGroup = (RadioGroup) layout.findViewById(R.id.routeTypeGroup);
      routeTypeGroup.check(RouteTypeMapper.idFromName(CycleStreetsPreferences.routeType()));
    } // RouteByNumberCallbacks

    private void findRoute(long routeNumber) {
      final String routeType = RouteTypeMapper.nameFromId(routeTypeGroup.getCheckedRadioButtonId());
      final int speed = CycleStreetsPreferences.speed();
      Route.FetchRoute(routeType, routeNumber, speed, context_);
    } // findRoute

    public void setDialog(final AlertDialog ad) {
      ad_ = ad;
      ad_.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
    } // setDialog

    @Override
    public void onClick(final View view) {
      final String entered = numberText_.getText().toString();
      if (entered.length() == 0)
        return;

      try {
        history_.addHistory(entered);
        long number = Long.parseLong(entered);
        findRoute(number);
        ad_.dismiss();
      } //try
      catch (final NumberFormatException e) {
        // let's just swallow this, because hopefully it won't happen
      } // catch
    } // onClick
  } // class RouteByNumberCallbacks
} // RouteByNumber
