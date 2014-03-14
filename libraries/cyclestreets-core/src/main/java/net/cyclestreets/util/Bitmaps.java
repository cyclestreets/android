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
	static private BitmapFactory.Options decodeOptions() 
	{ 
		final BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		decodeOptions.inPurgeable = true;
		decodeOptions.inSampleSize = 4;
		return decodeOptions;
	} // decodeOptions
	
	static public Bitmap loadFile(final String fileName)
	{
		return BitmapFactory.decodeFile(fileName, decodeOptions());
	} // loadFile

	static public Bitmap loadStream(final InputStream stream)
	{	
		Bitmap bm = null;
		try {
			// return BitmapFactory.decodeStream(inputStream);
			// Bug on slow connections, fixed in future release.
			bm = BitmapFactory.decodeStream(new FlushedInputStream(stream));
		} // try
		catch(Exception e) {
			// no matter
		} // catch
		finally {
			try {
				stream.close();
			} // try
			catch(IOException e) {
				// ah, well
			} // catch
		} // finally
		return bm;
	} // loadStream
	
	static public String resizePhoto(final String fileName)
	{
    if (fileName == null)
      return null;
    
		final BitmapFactory.Options options = bitmapBounds(fileName);

		int srcWidth = options.outWidth;

		final int desiredWidth = Math.min(640, srcWidth);

		// Calculate the correct inSampleSize/scale value. This helps 
		// reduce memory use. It should be a power of 2
		// from: http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
		int inSampleSize = 1;
		while(srcWidth / 2 > desiredWidth) {
		    srcWidth /= 2;
		    inSampleSize *= 2;
		} // while

		float desiredScale = (float)desiredWidth/srcWidth;

		// Decode with inSampleSize
		options.inJustDecodeBounds = false;
		options.inDither = false;
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
		} // try
		catch(IOException e) {
			return null;
		} // catch
	} // resizePhoto
	
	static private BitmapFactory.Options bitmapBounds(final String fileName)
	{
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(fileName, o);
		return o;
	} // bitmapBounds
	
	static private class FlushedInputStream extends FilterInputStream 
	{
	  public FlushedInputStream(final InputStream inputStream) 
	  {
	    super(inputStream);
	  } // FlushedInputStream

	  @Override
	  public long skip(long n) throws IOException 
	  {
	    long totalBytesSkipped = 0L;
	    while (totalBytesSkipped < n) 
	    {
	      long bytesSkipped = in.skip(n - totalBytesSkipped);
	      if (bytesSkipped == 0L) 
	      {
	        int b = read();
	        if (b < 0) 
	          break;  // we reached EOF
	        else 
	          bytesSkipped = 1; // we read one byte
	      } // if ...
	      totalBytesSkipped += bytesSkipped;
	    } // while ...
	    return totalBytesSkipped;
	  } // skip
  } // FlushedInputStream
} // class Bitmaps
