package net.cyclestreets;

import net.cyclestreets.api.Photo;
import net.cyclestreets.util.Screen;
import net.cyclestreets.view.R;
import net.cyclestreets.util.ImageDownloader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.lang.ref.WeakReference;

public final class DisplayPhoto {
  public static void launch(final Photo photo, final Context context) {
    ImageDisplay dd = (photo.hasVideos()) ?
      videoDisplay(photo, context) :
      photoDisplay(photo, context);

    dd.show();
  } // launch

  private DisplayPhoto() { }

  //////////////////////////////////////////
  private static ImageDisplay videoDisplay(
      final Photo photo,
      final Context context) {
    if (Screen.isSmall(context))
      return new ExternalVideoPlayer(photo, context);
    return new VideoDisplay(photo, context);
  } // VideoDisplay

  private static class VideoDisplay
      extends DisplayDialog
      implements MediaPlayer.OnPreparedListener {
    private VideoView vv_;
    private VideoControllerView controller_;
    private ProgressDialog pd_;

    VideoDisplay(final Photo photo, final Context context) {
      super(photo, context);
    } // VideoDisplay

    @Override
    protected String title() { return String.format("Video #%d", photo_.id()); }
    @Override
    protected String caption() { return photo_.caption().replace('\n', ' '); }
    @Override
    protected View loadLayout() {
      final View layout = View.inflate(context_, R.layout.showvideo, null);
      controller_ = new VideoControllerView(layout.findViewById(R.id.videocontroller));
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

      controller_.setMediaPlayer(vv_);
      controller_.show();

      int vw = vv_.getWidth();
      int cw = controller_.getWidth();

      if (vw >= cw) {
        // need to resize
        float scale = cw/(float)vw;
        int newwidth = (int)(vw * scale);
        int newheight = (int)(vv_.getHeight() * scale);

        vv_.setLayoutParams(new LinearLayout.LayoutParams(newwidth, newheight));
      }
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

  private static class ExternalVideoPlayer implements ImageDisplay {
    protected final Photo photo_;
    protected final Context context_;

    ExternalVideoPlayer(final Photo photo, final Context context) {
      photo_ = photo;
      context_ = context;
    } // VideoDisplay

    public void show() {
      final String videoUrl = videoUrl(photo_);
      if (videoUrl == null)
        return;

      final Intent player = new Intent(Intent.ACTION_VIEW);
      player.setDataAndType(Uri.parse(videoUrl), "video/*");
      context_.startActivity(player);
    } // show

    private static String videoUrl(final Photo photo) {
      for (String format : new String[]{ "mp4", "mov", "3gp" }) {
        Photo.Video v = photo.video(format);
        if (v != null)
          return v.url();
      } // for ...
      return null;
    } // videoUrl
  } // class ExternalVideoPlayer

  /////////////////////////////////////////////////////////////////////
  private static ImageDisplay photoDisplay(
      final Photo photo,
      final Context context) {
  return new PhotoDisplay(photo, context);
  } // photoDisplay

  private static class PhotoDisplay extends DisplayDialog {
    private ImageView iv_;

    PhotoDisplay(final Photo photo, final Context context) {
      super(photo, context);
    } // PhotoDisplay

    @Override
    protected String title() { return String.format("Photo #%d", photo_.id()); }
    @Override
    protected String caption() { return photo_.caption(); }
    @Override
    protected View loadLayout() {
      final View layout = View.inflate(context_, R.layout.showphoto, null);
      iv_ = (ImageView)layout.findViewById(R.id.photo);

      //sizeView(iv_, context_);
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

  ///////////////////////////////////////////////////////////
  private interface ImageDisplay {
    void show();
  }

  private abstract static class DisplayDialog implements ImageDisplay, View.OnTouchListener, GestureDetector.OnGestureListener {
    protected final Photo photo_;
    protected final Context context_;
    private final GestureDetector gd_;
    private AlertDialog ad_;

    protected DisplayDialog(final Photo photo, final Context context) {
      photo_ = photo;
      context_ = context;
      gd_ = new GestureDetector(context_, this);
    } // Display

    public void show() {
      final AlertDialog.Builder builder = new AlertDialog.Builder(context_);
      builder.setTitle(title());

      final View layout = loadLayout();
      builder.setView(layout);

      final TextView text = (TextView)layout.findViewById(R.id.caption);
      text.setText(caption());

      preShowSetup(builder);

      ad_ = builder.create();
      ad_.show();

      postShowSetup(ad_);

      layout.setOnTouchListener(this);
    } // show

    @Override
    public boolean onTouch(View view, MotionEvent event) {
      return gd_.onTouchEvent(event);
    } // onTouch

    @Override public boolean onDown(MotionEvent motionEvent) { return false; }
    @Override public void onShowPress(MotionEvent motionEvent) { }
    @Override public boolean onSingleTapUp(MotionEvent motionEvent) { return false; }
    @Override public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) { return false; }
    @Override public void onLongPress(MotionEvent motionEvent) { }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
      ad_.cancel();
      return true;
    } // onFling

    protected abstract String title();
    protected abstract String caption();
    protected abstract View loadLayout();
    
    protected void preShowSetup(AlertDialog.Builder builder) { }
    protected void postShowSetup(AlertDialog dialog) { }

    protected static void sizeView(final View v, final Context context) {
      final WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
      final int device_height = wm.getDefaultDisplay().getHeight();
      final int device_width = wm.getDefaultDisplay().getWidth();
      final int height = (device_height > device_width)
          ? device_height / 10 * 5
          : device_height / 10 * 6;
      final int width = device_width;
      v.setLayoutParams(new LinearLayout.LayoutParams(width, height));
    } // sizeView
  } // DisplayDialog

  /////////////////////////////////////////////////////////////////////////////////////
  private static class VideoControllerView {
    private MediaController.MediaPlayerControl videoPlayer_;
    private View controllerView_;
    private SeekBar seekBar_;
    private TextView timeElapsed_;
    private TextView endTime_;
    private boolean isDragging_;
    private static final int SHOW_PROGRESS = 2;
    private ImageButton pauseBtn_;
    private ImageButton ffBtn_;
    private ImageButton rewBtn_;
    private Handler msgHandler_ = new MessageHandler(this);

    public VideoControllerView(final View controllerView) {
      controllerView_ = controllerView;
      initControllerView(controllerView_);
    } // VideoControllerView

    public int getWidth() { return  controllerView_.getWidth(); }

    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
      videoPlayer_ = player;
      updatePausePlay();
    } // setMediaPlayer

    private void initControllerView(View v) {
      pauseBtn_ = (ImageButton)v.findViewById(R.id.pause);
      pauseBtn_.setOnClickListener(pauseListener_);

      ffBtn_ = (ImageButton)v.findViewById(R.id.ffwd);
      ffBtn_.setOnClickListener(ffListener_);

      rewBtn_ = (ImageButton)v.findViewById(R.id.rew);
      rewBtn_.setOnClickListener(rewListener_);

      seekBar_ = (SeekBar)v.findViewById(R.id.mediacontroller_progress);
      seekBar_.setOnSeekBarChangeListener(seekListener_);
      seekBar_.setMax(1000);

      timeElapsed_ = (TextView)v.findViewById(R.id.time_current);
      endTime_ = (TextView)v.findViewById(R.id.time);
    } // initControllerView

    private void disableUnsupportedButtons() {
      if (videoPlayer_ == null)
        return;

      try {
        if (pauseBtn_ != null && !videoPlayer_.canPause())
          pauseBtn_.setEnabled(false);
        if (rewBtn_ != null && !videoPlayer_.canSeekBackward())
          rewBtn_.setEnabled(false);
        if (ffBtn_ != null && !videoPlayer_.canSeekForward())
          ffBtn_.setEnabled(false);
      } catch (IncompatibleClassChangeError ex) {
      }
    }

    /**
     * Show the controller on screen. .
     */
    public void show() {
      if (!isShowing()) {
        setProgress();
        pauseBtn_.requestFocus();
        disableUnsupportedButtons();
      } // if ...

      controllerView_.setVisibility(View.VISIBLE);
      updatePausePlay();
      msgHandler_.sendEmptyMessage(SHOW_PROGRESS);
    } // show

    private boolean isShowing() {
      return (controllerView_.getVisibility() == View.VISIBLE);
    } // isShowing

    public void hide() {
      controllerView_.setVisibility(View.GONE);
    } // hide

    private String formatTime(int timeMs) {
      int totalSeconds = timeMs / 1000;

      int seconds = totalSeconds % 60;
      int minutes = (totalSeconds / 60) % 60;
      int hours   = totalSeconds / 3600;

      if (hours > 0)
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
      else
        return String.format("%02d:%02d", minutes, seconds);
    } // formatTime

    private int setProgress() {
      if (videoPlayer_ == null || isDragging_)
        return 0;

      int position = videoPlayer_.getCurrentPosition();
      int duration = videoPlayer_.getDuration();
      if (duration > 0) {
        long pos = 1000L * position / duration;
        seekBar_.setProgress((int)pos);
      } // if ...
      int percent = videoPlayer_.getBufferPercentage();
      seekBar_.setSecondaryProgress(percent * 10);

      timeElapsed_.setText(formatTime(position));
      endTime_.setText(formatTime(duration));

      return position;
    } // setProgress

    private View.OnClickListener pauseListener_ = new View.OnClickListener() {
      public void onClick(View v) { doPauseResume(); }
    };

    public void updatePausePlay() {
      pauseBtn_.setImageResource(videoPlayer_.isPlaying() ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
    } // updatePausePlay

    private void doPauseResume() {
      if (videoPlayer_.isPlaying())
        videoPlayer_.pause();
      else
        videoPlayer_.start();
      updatePausePlay();
    } // doPauseResume

    private SeekBar.OnSeekBarChangeListener seekListener_ = new SeekBar.OnSeekBarChangeListener() {
      public void onStartTrackingTouch(SeekBar bar) {
        isDragging_ = true;

        // By removing these pending progress messages we make sure
        // that a) we won't update the progress while the user adjusts
        // the seekbar and b) once the user is done dragging the thumb
        // we will post one of these messages to the queue again and
        // this ensures that there will be exactly one message queued up.
        msgHandler_.removeMessages(SHOW_PROGRESS);
      } // onStartTrackingTouch

      public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
        if (!fromuser)
          return;

        long duration = videoPlayer_.getDuration();
        long newposition = (duration * progress) / 1000L;
        videoPlayer_.seekTo((int)newposition);
        timeElapsed_.setText(formatTime((int) newposition));
      } // onProgressChanged

      public void onStopTrackingTouch(SeekBar bar) {
        isDragging_ = false;
        setProgress();
        updatePausePlay();
        // Ensure that progress is properly updated in the future,
        // the call to show() does not guarantee this because it is a
        // no-op if we are already showing.
        msgHandler_.sendEmptyMessage(SHOW_PROGRESS);
      } // onStopTrackingTouch
    };

    private View.OnClickListener rewListener_ = new View.OnClickListener() {
      public void onClick(View v) {
        int pos = videoPlayer_.getCurrentPosition();
        pos -= 5000; // milliseconds
        videoPlayer_.seekTo(pos);
        setProgress();
      }
    };

    private View.OnClickListener ffListener_ = new View.OnClickListener() {
      public void onClick(View v) {
        int pos = videoPlayer_.getCurrentPosition();
        pos += 15000; // milliseconds
        videoPlayer_.seekTo(pos);
        setProgress();
      }
    };

    private static class MessageHandler extends Handler {
      private final WeakReference<VideoControllerView> view_;

      MessageHandler(VideoControllerView view) {
        view_ = new WeakReference<>(view);
      }

      @Override
      public void handleMessage(Message msg) {
        VideoControllerView view = view_.get();
        if (view == null || view.videoPlayer_ == null)
          return;

        if (msg.what == SHOW_PROGRESS) {
            int pos = view.setProgress();
            if (!view.isDragging_ && view.isShowing() && view.videoPlayer_.isPlaying()) {
              msg = obtainMessage(SHOW_PROGRESS);
              sendMessageDelayed(msg, 1000 - (pos % 1000));
            }
            view.updatePausePlay();
        } // if ...
      } // handleMessage
    } // class MessageHandler
  } // class VideoViewController
} // DisplayPhoto


