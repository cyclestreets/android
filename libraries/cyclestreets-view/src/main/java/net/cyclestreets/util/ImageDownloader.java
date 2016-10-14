package net.cyclestreets.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageDownloader {

  private static OkHttpClient client = new OkHttpClient.Builder().build();

  // Prevent instantiation as this is class only contains static methods
  private ImageDownloader() {}

  public static void get(final String url,
                         final ImageView imageView) {
    final BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
    task.execute(url);
  } // get

  //////////////////////////
  private static class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;

    public BitmapDownloaderTask(final ImageView imageView) {
      imageViewReference = new WeakReference<>(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
      return downloadBitmap(params[0]);
    }

    @Override
    protected void onPostExecute(final Bitmap bitmap) {
      if (isCancelled())
        return;

      if (bitmap == null)
        return;

      if (imageViewReference == null)
        return;
      
      final ImageView imageView = imageViewReference.get();
      if (imageView == null) 
        return;

      final WindowManager wm = (WindowManager)imageView.getContext().getSystemService(Context.WINDOW_SERVICE);
      final int device_height = wm.getDefaultDisplay().getHeight();
      final int device_width = wm.getDefaultDisplay().getWidth();
      final int height = (device_height > device_width)
          ? device_height / 10 * 5
          : device_height / 10 * 6;
      final int width = imageView.getWidth();
      imageView.setLayoutParams(new LinearLayout.LayoutParams(width, height));

      final int imageWidth = bitmap.getWidth();
      final int imageHeight = bitmap.getHeight();
      final float aspect = (float)imageWidth/(float)imageHeight;

      if (aspect > 1) {
        // landscape, so adjust height
        final int viewHeight = imageView.getHeight();
        int newViewHeight = (int)(viewHeight/aspect);
        imageView.setMaxHeight(newViewHeight);
      }

      imageView.setAnimation(null);

      imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
      imageView.setImageBitmap(bitmap);
    }

    private static Bitmap downloadBitmap(final String url) {

      Request request = new Request.Builder()
              .url(url)
              .header("User-Agent", "CycleStreets Android/1.0")
              .build();
      try {
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
          return null;
        }
        final InputStream inputStream = response.body().byteStream();
        return Bitmaps.loadStream(inputStream);
      } catch (IOException e) {
        // Uh-oh
        return null;
      }
    }
  }
}
