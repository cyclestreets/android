package net.cyclestreets;

import net.cyclestreets.api.Photo;
import net.cyclestreets.view.R;
import net.cyclestreets.util.ImageDownloader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class DisplayPhoto {
  public static void launch(final Photo photo, final Context context) {
    if (photo.hasVideos())
      launchVideo(photo, context);
    else
      launchPhoto(photo, context);
  } // launch

  private static void launchVideo(final Photo photo, final Context context) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    final String title = String.format("Video #%d", photo.id());
    builder.setTitle(title);

    final View layout = View.inflate(context, R.layout.showvideo, null);
    builder.setView(layout);

    final TextView text = (TextView)layout.findViewById(R.id.caption);
    text.setText(photo.caption());

    final AlertDialog ad = builder.create();
    ad.show();

    // start video
    final String bestUrl = videoUrl(photo);

    final VideoView vv = (VideoView)layout.findViewById(R.id.video);
    sizeView(vv, ad.getContext());

    final Uri uri = Uri.parse(bestUrl);
    vv.setVideoURI(uri);
    vv.setZOrderOnTop(true);
    vv.requestFocus();
    vv.start();

    final ProgressDialog pd = new ProgressDialog(ad.getContext());
    pd.setMessage("Loading video ...");
    pd.show();

    vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mediaPlayer) {
        pd.dismiss();

       final MediaController mc = new MediaController(ad.getContext());
       mc.setAnchorView(vv);
       mc.setMediaPlayer(vv);
       vv.setMediaController(mc);
       mc.show();
      }
    });
  } // launchVideo

  private static void launchPhoto(final Photo photo, final Context context) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    final String title = String.format("Photo #%d", photo.id());
    builder.setTitle(title);

    final View layout = loadPhotoView(photo, context);
    builder.setView(layout);

    final TextView text = (TextView)layout.findViewById(R.id.caption);
    text.setText(photo.caption());

    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialogInterface) {
        final ImageView iv = (ImageView)layout.findViewById(R.id.photo);
        if (iv == null)
          return;
        final Bitmap photo = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        photo.recycle();
      } // onCancel
    });

    final AlertDialog ad = builder.create();
    ad.show();
  } // launchPhoto

  private static String videoUrl(final Photo photo) {
    for (String format : new String[]{ "mp4", "mov", "3gp" }) {
      Photo.Video v = photo.video(format);
      if (v != null)
        return v.url();
    } // for ...
    return null;
  } // videoUrl

  private static View loadPhotoView(final Photo photo, final Context context) {
    final View layout = View.inflate(context, R.layout.showphoto, null);
    final ImageView iv = (ImageView)layout.findViewById(R.id.photo);

    sizeView(iv, context);
    iv.startAnimation(AnimationUtils.loadAnimation(context, R.anim.spinner));

    final String thumbnailUrl = photo.thumbnailUrl();
    ImageDownloader.get(thumbnailUrl, iv);

    return layout;
  } // loadPhotoView

  private static void sizeView(final View v, final Context context) {
    final WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    final int device_height = wm.getDefaultDisplay().getHeight();
    final int device_width = wm.getDefaultDisplay().getWidth();
    final int height = (device_height > device_width)
        ? device_height / 10 * 5
        : device_height / 10 * 7;
    final int width = device_width;
    v.setLayoutParams(new LinearLayout.LayoutParams(width, height));
  } // sizeView
} // DisplayPhoto
