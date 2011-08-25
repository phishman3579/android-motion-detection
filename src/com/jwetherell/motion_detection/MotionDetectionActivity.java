package com.jwetherell.motion_detection;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jwetherell.motion_detection.detection.MotionDetection;
import com.jwetherell.motion_detection.image.ImageProcessing;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class MotionDetectionActivity extends Activity {
	private static final String TAG = "MotionDetectionActivity";
	private static final boolean SAVE_PREVIOUS = true;
	private static final boolean SAVE_ORIGINAL = true;
	private static final boolean SAVE_CHANGES = true;
	private static final int PICTURE_DELAY = 10000;

	private static SurfaceView preview = null;
	private static SurfaceHolder previewHolder = null;
	private static Camera camera = null;
	private static boolean inPreview = false;
	private static long mReferenceTime = 0;
	
	private static volatile AtomicBoolean processing = new AtomicBoolean(false);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		preview = (SurfaceView)findViewById(R.id.preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onResume() {
		super.onResume();

		camera = Camera.open();
	}

	@Override
	public void onPause() {
		super.onPause();

		camera.setPreviewCallback(null);
		if (inPreview) camera.stopPreview();
		inPreview = false;
		camera.release();
		camera = null;
	}

	private PreviewCallback previewCallback = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera cam) {
			if (data == null) return;
			Camera.Size size = cam.getParameters().getPreviewSize();
			if (size == null) return;

			if (!processing.compareAndSet(false, true)) return;
			DetectionThread thread = new DetectionThread(data,size.width,size.height);
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			processing.set(false);
		}
	};

	private SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera.setPreviewDisplay(previewHolder);
				camera.setPreviewCallback(previewCallback);
			} catch (Throwable t) {
				Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size size = getBestPreviewSize(width, height, parameters);
			if (size!=null) {
				parameters.setPreviewSize(size.width, size.height);
				Log.d(TAG, "Using width="+size.width+" height="+size.height);
			}
			camera.setParameters(parameters);
			camera.startPreview();
			inPreview=true;
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// Ignore
		}
	};

	private static Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
		Camera.Size result=null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width<=width && size.height<=height) {
				if (result==null) {
					result=size;
				} else {
					int resultArea=result.width*result.height;
					int newArea=size.width*size.height;

					if (newArea>resultArea) result=size;
				}
			}
		}

		return result;
	}
	
	private class DetectionThread extends Thread {
		private byte[] data;
		private int width;
		private int height;
		
		public DetectionThread(byte[] data, int width, int height) {
			this.data = data;
			this.width = width;
			this.height = height;
		}
		
	    @Override
	    public void run() {
			Looper.prepare();
			Log.d(TAG, "BEGIN PROCESSING...");
	        try {
	        	//Previous frame
	        	int[] pre = null;
				if (SAVE_PREVIOUS) pre = MotionDetection.getPrevious();
				
				//Current frame (with changes)
				int[] rgb = ImageProcessing.decodeYUV420SP(data, width, height);
				
				//Current frame (without changes)
				int[] org = null;
				if (SAVE_ORIGINAL) org = rgb.clone();
				
				if (MotionDetection.detect(rgb, width, height)) {
					// The delay is necessary to avoid taking a picture while in the
					// middle of taking another. This problem can causes some phones
					// to reboot.
					long now = System.currentTimeMillis();
					if (now > (mReferenceTime + PICTURE_DELAY)) {
						mReferenceTime = now;
						
						Bitmap previous = null;
						if (SAVE_PREVIOUS) previous = ImageProcessing.rgbToBitmap(pre, width, height);
						
						Bitmap original = null;
						if (SAVE_ORIGINAL) original = ImageProcessing.rgbToBitmap(org, width, height);
						
						Bitmap bitmap = null;
						if (SAVE_CHANGES) bitmap = ImageProcessing.rgbToBitmap(rgb, width, height);
						
						Log.i(TAG,"Saving.. previous="+previous+" original="+original+" bitmap="+bitmap);
						new SavePhotoTask().execute(previous,original,bitmap);
					} else {
						Log.i(TAG, "Not taking picture because not enough time has passed since the creation of the Surface");
					}
				}
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
			Log.d(TAG, "END PROCESSING...");
	    }
	};

	private class SavePhotoTask extends AsyncTask<Bitmap, Integer, Integer> {
		@Override
		protected Integer doInBackground(Bitmap... data) {
			for (int i=0; i<data.length; i++) {
				Bitmap bitmap = data[i];
				String name = String.valueOf(System.currentTimeMillis());
				if (bitmap!=null) save(name,bitmap);
			}
			return 1;
		}
		
		private void save(String name, Bitmap bitmap) {
			File photo=new File(Environment.getExternalStorageDirectory(), name+".jpg");
			if (photo.exists()) photo.delete();

			try {
				FileOutputStream fos=new FileOutputStream(photo.getPath());
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.close();
			} catch (java.io.IOException e) {
				Log.e("PictureDemo", "Exception in photoCallback", e);
			}
		}
	}
}