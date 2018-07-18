package net.cyclestreets.util;

import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class Bitmaps
{
  private static BitmapFactory.Options decodeOptions() {
    final BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
    decodeOptions.inSampleSize = 4;
    return decodeOptions;
  }

  public static Bitmap loadFile(final String fileName) {
    return BitmapFactory.decodeFile(fileName, decodeOptions());
  }

  public static Bitmap loadStream(final InputStream stream) {
    Bitmap bm = null;
    try {
      // return BitmapFactory.decodeStream(inputStream);
      // Bug on slow connections, fixed in future release.
      bm = BitmapFactory.decodeStream(new FlushedInputStream(stream));
    }
    catch (Exception e) {
      // no matter
    }
    finally {
      try {
        stream.close();
      }
      catch (IOException e) {
        // ah, well
      }
    }
    return bm;
  }

  public static String resizePhoto(final String fileName) {
    if (fileName == null)
      return null;

    final BitmapFactory.Options options = bitmapBounds(fileName);

    int srcWidth = options.outWidth;

    final int desiredWidth = Math.min(640, srcWidth);

    // Calculate the correct inSampleSize/scale value. This helps
    // reduce memory use. It should be a power of 2
    // from: http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
    int inSampleSize = 1;
    while (srcWidth / 2 > desiredWidth) {
        srcWidth /= 2;
        inSampleSize *= 2;
    }

    float desiredScale = (float)desiredWidth/srcWidth;

    // Decode with inSampleSize
    options.inJustDecodeBounds = false;
    options.inSampleSize = inSampleSize;
    options.inScaled = false;
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

    Bitmap sampledSrcBitmap = BitmapFactory.decodeFile(fileName, options);

    // Resize
    final Matrix matrix = new Matrix();
    matrix.postScale(desiredScale, desiredScale);
    Bitmap scaledBitmap = Bitmap.createBitmap(sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);
    sampledSrcBitmap.recycle();
    sampledSrcBitmap = null;

    // Save
    try {
      final String smallFileName = fileName + "-small";
      final FileOutputStream out = new FileOutputStream(smallFileName);
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      scaledBitmap.recycle();
      scaledBitmap = null;
      return smallFileName;
    }
    catch (IOException e) {
      return null;
    }
  }

  private static BitmapFactory.Options bitmapBounds(final String fileName) {
    final BitmapFactory.Options o = new BitmapFactory.Options();
    o.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(fileName, o);
    return o;
  }

  private static class FlushedInputStream extends FilterInputStream  {
    public FlushedInputStream(final InputStream inputStream) {
      super(inputStream);
    }

    @Override
    public long skip(long n) throws IOException  {
      long totalBytesSkipped = 0L;
      while (totalBytesSkipped < n) {
        long bytesSkipped = in.skip(n - totalBytesSkipped);
        if (bytesSkipped == 0L) {
          int b = read();
          if (b < 0)
            break;  // we reached EOF
          else
            bytesSkipped = 1; // we read one byte
        }
        totalBytesSkipped += bytesSkipped;
      }
      return totalBytesSkipped;
    }
  }
}
