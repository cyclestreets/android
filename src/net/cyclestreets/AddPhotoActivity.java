package net.cyclestreets;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class AddPhotoActivity extends Activity implements SurfaceHolder.Callback,
	View.OnClickListener, Camera.PictureCallback {
	protected Camera camera;
	protected SurfaceView surface;
	protected SurfaceHolder holder;
	
	// Lifecycle methods
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	surface = new SurfaceView(this);
    	surface.setOnClickListener(this);
    	holder = surface.getHolder();
    	holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	holder.addCallback(this);
    	setContentView(surface);
	}
	
	// SurfaceHolder.Callback methods
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        camera = Camera.open();
        try {
           camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            camera.release();
            camera = null;
        	throw new RuntimeException(e);
        }
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
        // set preview size
		Camera.Parameters params = camera.getParameters();

		// needs API level 5
//        List<Size> sizes = params.getSupportedPreviewSizes();
//        Size optimalSize = getOptimalPreviewSize(sizes, w, h);
        params.setPreviewSize(width, height);

        camera.setParameters(params);
        camera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// release camera
		camera.stopPreview();
		camera.release();
        camera = null;
	}
	
    protected Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    // View.OnClickListener methods
	@Override
	public void onClick(View view) {
		camera.takePicture(null, null, this);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d(getClass().getSimpleName(), "picture taken!");
		Intent intent = new Intent(this, PhotoTakenActivity.class);
		intent.putExtra(CycleStreets.EXTRA_PHOTO, data);
		startActivity(intent);
	}
}
