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
import android.widget.TextView;
import android.widget.VideoView;

public final class DisplayPhoto {
  public static void launch(final Photo photo, final Context context) {
    DisplayDialog dd = (photo.hasVideos()) ?
      new VideoDisplay(photo, context) :
      new PhotoDisplay(photo, context);

    dd.show();
  } // launch

  private DisplayPhoto() { }

  //////////////////////////////////////////
  private static class VideoDisplay extends DisplayDialog implements MediaPlayer.OnPreparedListener {
    private VideoView vv_;
    private ProgressDialog pd_;

    VideoDisplay(final Photo photo, final Context context) {
      super(photo, context);
    } // VideoDisplay

    @Override
    protected String title() { return String.format("Video #%d", photo_.id()); }
    @Override
    protected View loadLayout() {
      final View layout = View.inflate(context_, R.layout.showvideo, null);
      vv_ = (VideoView)layout.findViewById(R.id.video);
      return layout;
    } // loadLayout

    @Override
    protected void postShowSetup(AlertDialog dialog) {
      final String bestUrl = videoUrl(photo_);

      sizeView(vv_, dialog.getContext());

      final Uri uri = Uri.parse(bestUrl);
      vv_.setVideoURI(uri);
      vv_.setZOrderOnTop(true);
      vv_.requestFocus();
      vv_.start();

      pd_ = new ProgressDialog(dialog.getContext());
      pd_.setMessage("Loading video ...");
      pd_.show();

      vv_.setOnPreparedListener(this);
    } // postShowSetup

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
      pd_.dismiss();

      //final MediaController mc = new MediaController(ad.getContext());
      //mc.setAnchorView(vv_);
      //mc.setMediaPlayer(vv_);
      //vv_.setMediaController(mc);
      //mc.show();
    } // onPrepared

    private static String videoUrl(final Photo photo) {
      for (String format : new String[]{ "mp4", "mov", "3gp" }) {
        Photo.Video v = photo.video(format);
        if (v != null)
          return v.url();
      } // for ...
      return null;
    } // videoUrl
  } // VideoDisplay

  private static class PhotoDisplay extends DisplayDialog {
    private ImageView iv_;

    PhotoDisplay(final Photo photo, final Context context) {
      super(photo, context);
    } // PhotoDisplay

    @Override
    protected String title() { return String.format("Photo #%d", photo_.id()); }
    @Override
    protected View loadLayout() {
      final View layout = View.inflate(context_, R.layout.showphoto, null);
      iv_ = (ImageView)layout.findViewById(R.id.photo);

      sizeView(iv_, context_);
      iv_.startAnimation(AnimationUtils.loadAnimation(context_, R.anim.spinner));

      final String thumbnailUrl = photo_.thumbnailUrl();
      ImageDownloader.get(thumbnailUrl, iv_);

      return layout;
    } // loadLayout

    @Override
    protected void preShowSetup(AlertDialog.Builder builder) {
      builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
          final Bitmap photo = ((BitmapDrawable)iv_.getDrawable()).getBitmap();
          photo.recycle();
        } // onCancel
      });
    } // preShowSetup
  } // class PhotoDisplay

  private abstract static class DisplayDialog {
    protected final Photo photo_;
    protected final Context context_;

    protected DisplayDialog(final Photo photo, final Context context) {
      photo_ = photo;
      context_ = context;
    } // Display

    public void show() {
      final AlertDialog.Builder builder = new AlertDialog.Builder(context_);
      builder.setTitle(title());

      final View layout = loadLayout();
      builder.setView(layout);

      final TextView text = (TextView)layout.findViewById(R.id.caption);
      text.setText(photo_.caption());

      preShowSetup(builder);

      final AlertDialog ad = builder.create();
      ad.show();

      postShowSetup(ad);
    } // show

    protected abstract String title();
    protected abstract View loadLayout();
    
    protected void preShowSetup(AlertDialog.Builder builder) { }
    protected void postShowSetup(AlertDialog dialog) { }

    protected static void sizeView(final View v, final Context context) {
      final WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
      final int device_height = wm.getDefaultDisplay().getHeight();
      final int device_width = wm.getDefaultDisplay().getWidth();
      final int height = (device_height > device_width)
          ? device_height / 10 * 5
          : device_height / 10 * 7;
      final int width = device_width;
      v.setLayoutParams(new LinearLayout.LayoutParams(width, height));
    } // sizeView
  } // DisplayDialog
} // DisplayPhoto
