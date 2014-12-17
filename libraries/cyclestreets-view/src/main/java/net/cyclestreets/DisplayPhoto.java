package net.cyclestreets;

import net.cyclestreets.api.Photo;
import net.cyclestreets.view.R;
import net.cyclestreets.util.ImageDownloader;

import android.app.AlertDialog;
import android.content.Context;
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
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    final String title = String.format("%s #%d", photo.hasVideos() ? "Video" : "Photo", photo.id());
    builder.setTitle(title);

    final View layout = loadView(photo, context);
    builder.setView(layout);

    final AlertDialog ad = builder.create();
    ad.show();
  } // launch

  private static View loadView(final Photo photo, final Context context) {
    final View layout = (photo.hasVideos()) ? loadVideoView(photo, context) : loadPhotoView(photo, context);

    final TextView text = (TextView)layout.findViewById(R.id.caption);
    text.setText(photo.caption());

    return layout;
  } // loadView

  private static View loadVideoView(final Photo photo, final Context context) {
    final View layout = View.inflate(context, R.layout.showvideo, null);
    final String bestUrl = videoUrl(photo);

    final VideoView vv = (VideoView)layout.findViewById(R.id.video);
    sizeView(vv, context);

    final MediaController mc = new MediaController(context);
    mc.setAnchorView(vv);
    mc.setMediaPlayer(vv);
    vv.setMediaController(mc);

    final Uri uri = Uri.parse(bestUrl);
    vv.setVideoURI(uri);
    vv.start();

    return layout;
  } // loadVideoView

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
        ? device_height / 10 * 4
        : device_height / 10 * 6;
    final int width = device_width;
    final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)v.getLayoutParams();
    v.setLayoutParams(new LinearLayout.LayoutParams(width, height));
  } // sizeView
} // DisplayPhoto
