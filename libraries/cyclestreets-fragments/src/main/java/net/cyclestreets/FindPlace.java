package net.cyclestreets;

import net.cyclestreets.fragments.R;

import net.cyclestreets.api.GeoPlace;

import net.cyclestreets.util.MessageBox;
import net.cyclestreets.views.PlaceView;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;

public class FindPlace {
  public interface Listener {
    void onPlaceFound(final IGeoPoint place);
  }

  public static void launch(final Context context,
                            final BoundingBoxE6 boundingBox,
                            final FindPlace.Listener listener) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(R.string.menu_find_place);

    final FindPlaceCallbacks fpcb = new FindPlaceCallbacks(context, builder, boundingBox, listener);

    final AlertDialog ad = builder.create();
    ad.show();
    ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(context, android.R.style.TextAppearance_Large);

    fpcb.setDialog(ad);
  } // launch

  private static class FindPlaceCallbacks implements View.OnClickListener, PlaceView.OnResolveListener {
    private final Context context_;
    private final PlaceView place_;
    private final Listener listener_;
    private AlertDialog ad_;

    public FindPlaceCallbacks(final Context context,
                              final AlertDialog.Builder builder,
                              final BoundingBoxE6 boundingBox,
                              final Listener listener) {
      context_ = context;

      final View layout = View.inflate(context, R.layout.findplace, null);
      builder.setView(layout);

      builder.setPositiveButton(R.string.btn_find_place, MessageBox.NoAction);

      place_ = (PlaceView) layout.findViewById(R.id.place);
      place_.setBounds(boundingBox);

      listener_ = listener;
    } // onCreate

    public void setDialog(final AlertDialog ad) {
      ad_ = ad;
      ad_.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this);
    } // setDialog

    private void placeSelected(final GeoPlace place) {
      if (place == null || place.coord() == null)
        return;

      place_.addHistory(place);

      listener_.onPlaceFound(place.coord());
      ad_.dismiss();
    } // placeSelected

    @Override
    public void onClick(final View view) {
      final String from = place_.getText();
      if (from.length() == 0) {
        Toast.makeText(context_, R.string.lbl_choose_place, Toast.LENGTH_LONG).show();
        return;
      } // if ...

      place_.geoPlace(this);
    } // onClick

    @Override
    public void onResolve(final GeoPlace place) {
      placeSelected(place);
    } // onResolve
  } // class FindPlaceCallback
} // class FindPlace
