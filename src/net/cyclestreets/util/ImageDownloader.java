package net.cyclestreets.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImageDownloader 
{
	static public void get(final String url, 
						   final ImageView imageView,
						   final WindowManager wm) 
	{
		final BitmapDownloaderTask task = new BitmapDownloaderTask(imageView, wm);
		task.execute(url);
	} // get

	//////////////////////////
	static class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> 
	{
		private final WeakReference<ImageView> imageViewReference;
		private final int height_;
		private final int width_;

		public BitmapDownloaderTask(final ImageView imageView,
									final WindowManager wm) 
		{
			imageViewReference = new WeakReference<ImageView>(imageView);
			height_ = wm.getDefaultDisplay().getHeight() / 10 * 4;
			width_ = wm.getDefaultDisplay().getWidth();
		} // BitmapDownloaderTask

		@Override
		protected Bitmap doInBackground(String... params) 
		{
			return downloadBitmap(params[0]);
		} // doInBackground

		@Override
		protected void onPostExecute(final Bitmap bitmap) 
		{
			if(isCancelled()) 
				return;
		  
			if(imageViewReference == null)
				return;
			
			final ImageView imageView = imageViewReference.get();
			if (imageView == null) 
				return;
			
			imageView.setImageBitmap(bitmap);
			imageView.setLayoutParams(new LinearLayout.LayoutParams(width_, height_));
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		} // onPostExecute

		private Bitmap downloadBitmap(final String url) 
		{
			final HttpClient client = new DefaultHttpClient();
			final HttpGet getRequest = new HttpGet(url);

			try 
			{
				HttpResponse response = client.execute(getRequest);
				final int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) 
					return null;

				final HttpEntity entity = response.getEntity();
				if (entity == null) 
					return null;
				
				InputStream inputStream = null;
				try 
				{
					inputStream = entity.getContent();
					// return BitmapFactory.decodeStream(inputStream);
					// Bug on slow connections, fixed in future release.
					return BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
		        } // try
				finally 
				{
					if(inputStream != null)
						inputStream.close();
					entity.consumeContent();
				} // finally
			} // try 
			catch (Exception e) 
			{
				getRequest.abort();
			} // catch
			return null;
		} // downloadBitmap
	} // BitmapDownloaderTask 

    static class FlushedInputStream extends FilterInputStream 
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
} // ImageDownloader
