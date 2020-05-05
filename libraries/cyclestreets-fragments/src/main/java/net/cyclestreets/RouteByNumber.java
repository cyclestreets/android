package net.cyclestreets;

import net.cyclestreets.fragments.R;

import net.cyclestreets.routing.Route;
import net.cyclestreets.util.EditTextHistory;
import net.cyclestreets.util.MessageBox;
import net.cyclestreets.views.RouteType;

import android.app.AlertDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.AutoCompleteTextView;

public class RouteByNumber {
  public static void launch(@NonNull final Context context) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context)
            .setTitle(R.string.menu_route_by_number)
            .setMessage(R.string.routenumber_desc);

    final RouteByNumberCallbacks rbnc = new RouteByNumberCallbacks(context, builder);

    final AlertDialog ad = builder.create();
    ad.show();
    ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(android.R.style.TextAppearance_Large);

    rbnc.setDialog(ad);
  }

  private static class RouteByNumberCallbacks implements View.OnClickListener {
    private final Context context;
    private final AutoCompleteTextView numberText;
    private final RouteType routeType;
    private final EditTextHistory history;
    private AlertDialog ad;

    private RouteByNumberCallbacks(final Context context,
                                   final AlertDialog.Builder builder) {
      this.context = context;

      final View layout = View.inflate(context, R.layout.routenumber, null);
      builder
        .setView(layout)
        .setPositiveButton(R.string.load_route, MessageBox.NoAction);

      numberText = layout.findViewById(R.id.routeNumber);
      history = new EditTextHistory(context, "RouteNumber");
      numberText.setAdapter(history);

      routeType = layout.findViewById(R.id.routeType);
    }

    private void findRoute(long routeNumber) {
      final String routeType = this.routeType.selectedType();
      final int speed = CycleStreetsPreferences.speed();
      Route.FetchRoute(routeType, routeNumber, speed, context);
    }

    public void setDialog(final AlertDialog ad) {
      this.ad = ad;
      this.ad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
      final String entered = numberText.getText().toString();
      if (entered.length() == 0)
        return;

      try {
        history.addHistory(entered);
        long number = Long.parseLong(entered);
        findRoute(number);
        ad.dismiss();
      }
      catch (final NumberFormatException e) {
        // let's just swallow this, because hopefully it won't happen
      }
    }
  }
}
