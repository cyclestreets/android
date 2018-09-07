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
import android.graphics.Point;
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
  }

  private DisplayPhoto() { }

  //////////////////////////////////////////
  private static ImageDisplay videoDisplay(
      final Photo photo,
      final Context context) {
    if (Screen.isSmall(context))
      return new ExternalVideoPlayer(photo, context);
    return new VideoDisplay(photo, context);
  }

  private static class ExternalVideoPlayer implements ImageDisplay {
    protected final Photo photo_;
    protected final Context context_;

    ExternalVideoPlayer(final Photo photo, final Context context) {
      photo_ = photo;
      context_ = context;
    }

    public void show() {
      final String videoUrl = videoUrl(photo_);
      if (videoUrl == null)
        return;

      final Intent player = new Intent(Intent.ACTION_VIEW);
      player.setDataAndType(Uri.parse(videoUrl), "video/*");
      context_.startActivity(player);
    }

    private static String videoUrl(final Photo photo) {
      for (String format : new String[]{ "mp4", "mov", "3gp" }) {
        Photo.Video v = photo.video(format);
        if (v != null)
          return v.url();
      }
      return null;
    }
  }

  /////////////////////////////////////////////////////////////////////
  private static ImageDisplay photoDisplay(
      final Photo photo,
      final Context context) {
  return new PhotoDisplay(photo, context);
  }



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
    }

    public int getWidth() { return  controllerView_.getWidth(); }

    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
      videoPlayer_ = player;
      updatePausePlay();
    }

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
    }

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
      }

      controllerView_.setVisibility(View.VISIBLE);
      updatePausePlay();
      msgHandler_.sendEmptyMessage(SHOW_PROGRESS);
    }

    private boolean isShowing() {
      return (controllerView_.getVisibility() == View.VISIBLE);
    }

    public void hide() {
      controllerView_.setVisibility(View.GONE);
    }

    private String formatTime(int timeMs) {
      int totalSeconds = timeMs / 1000;

      int seconds = totalSeconds % 60;
      int minutes = (totalSeconds / 60) % 60;
      int hours   = totalSeconds / 3600;

      if (hours > 0)
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
      else
        return String.format("%02d:%02d", minutes, seconds);
    }

    private int setProgress() {
      if (videoPlayer_ == null || isDragging_)
        return 0;

      int position = videoPlayer_.getCurrentPosition();
      int duration = videoPlayer_.getDuration();
      if (duration > 0) {
        long pos = 1000L * position / duration;
        seekBar_.setProgress((int)pos);
      }
      int percent = videoPlayer_.getBufferPercentage();
      seekBar_.setSecondaryProgress(percent * 10);

      timeElapsed_.setText(formatTime(position));
      endTime_.setText(formatTime(duration));

      return position;
    }

    private View.OnClickListener pauseListener_ = new View.OnClickListener() {
      public void onClick(View v) { doPauseResume(); }
    };

    public void updatePausePlay() {
      pauseBtn_.setImageResource(videoPlayer_.isPlaying() ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
    }

    private void doPauseResume() {
      if (videoPlayer_.isPlaying())
        videoPlayer_.pause();
      else
        videoPlayer_.start();
      updatePausePlay();
    }

    private SeekBar.OnSeekBarChangeListener seekListener_ = new SeekBar.OnSeekBarChangeListener() {
      public void onStartTrackingTouch(SeekBar bar) {
        isDragging_ = true;

        // By removing these pending progress messages we make sure
        // that a) we won't update the progress while the user adjusts
        // the seekbar and b) once the user is done dragging the thumb
        // we will post one of these messages to the queue again and
        // this ensures that there will be exactly one message queued up.
        msgHandler_.removeMessages(SHOW_PROGRESS);
      }

      public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
        if (!fromuser)
          return;

        long duration = videoPlayer_.getDuration();
        long newposition = (duration * progress) / 1000L;
        videoPlayer_.seekTo((int)newposition);
        timeElapsed_.setText(formatTime((int) newposition));
      }

      public void onStopTrackingTouch(SeekBar bar) {
        isDragging_ = false;
        setProgress();
        updatePausePlay();
        // Ensure that progress is properly updated in the future,
        // the call to show() does not guarantee this because it is a
        // no-op if we are already showing.
        msgHandler_.sendEmptyMessage(SHOW_PROGRESS);
      }
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
        }
      }
    }
  }
}

