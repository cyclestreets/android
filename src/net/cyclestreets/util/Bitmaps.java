package net.cyclestreets.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Bitmaps 
{
	static public BitmapFactory.Options DecodeOptions() { return decodeOptions_; }
	
	static public Bitmap loadFile(final String fileName)
	{
		return BitmapFactory.decodeFile(fileName, decodeOptions_);
	} // loadFile
	
	static public Bitmap loadStream(final InputStream stream)
	{	
		Bitmap bm = null;
		try {
			// return BitmapFactory.decodeStream(inputStream);
			// Bug on slow connections, fixed in future release.
			bm = BitmapFactory.decodeStream(new FlushedInputStream(stream), null, decodeOptions_);
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
	
	static private final BitmapFactory.Options decodeOptions_;
	static 
	{
		decodeOptions_ = new BitmapFactory.Options();
    	decodeOptions_.inPurgeable = true;
    	decodeOptions_.inSampleSize = 4;
	} // static

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
