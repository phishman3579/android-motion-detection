package com.jwetherell.motion_detection;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jwetherell.motion_detection.data.GlobalData;
import com.jwetherell.motion_detection.data.Preferences;
import com.jwetherell.motion_detection.detection.AggregateLumaMotionDetection;
import com.jwetherell.motion_detection.detection.IMotionDetection;
import com.jwetherell.motion_detection.detection.LumaMotionDetection;
import com.jwetherell.motion_detection.detection.RgbMotionDetection;
import com.jwetherell.motion_detection.image.ImageProcessing;

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


/**
 * This class extends Activity to handle a picture preview, process the frame for motion, and then 
 * save the file to the SD card.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class MotionDetectionActivity extends SensorsActivity {
	private static final String TAG = "MotionDetectionActivity";

	private static SurfaceView preview = null;
	private static SurfaceHolder previewHolder = null;
	private static Camera camera = null;
	private static boolean inPreview = false;
	private static long mReferenceTime = 0;
	private static IMotionDetection detector = null;

	private static volatile AtomicBoolean processing = new AtomicBoolean(false);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		preview = (SurfaceView)findViewById(R.id.preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		if (Preferences.USE_RGB) {
			detector = new RgbMotionDetection();
		} else if (Preferences.USE_LUMA) {
			detector = new LumaMotionDetection();
		} else {
			//Using State based (aggregate map)
			detector = new AggregateLumaMotionDetection();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        camera.setPreviewCallback(null);
        if (inPreview) camera.stopPreview();
        inPreview = false;
        camera.release();
        camera = null;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onResume() {
		super.onResume();

		camera = Camera.open();
	}

	private PreviewCallback previewCallback = new PreviewCallback() {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onPreviewFrame(byte[] data, Camera cam) {
			if (data == null) return;
			Camera.Size size = cam.getParameters().getPreviewSize();
			if (size == null) return;

			if (!GlobalData.isPhoneInMotion()) {
			    DetectionThread thread = new DetectionThread(data,size.width,size.height);
			    thread.start();
			}
		}
	};

	private SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera.setPreviewDisplay(previewHolder);
				camera.setPreviewCallback(previewCallback);
			} catch (Throwable t) {
				Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
			}
		}

		/**
		 * {@inheritDoc}
		 */
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

		/**
		 * {@inheritDoc}
		 */
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

	private static final class DetectionThread extends Thread {
		private byte[] data;
		private int width;
		private int height;
		
		public DetectionThread(byte[] data, int width, int height) {
			this.data = data;
			this.width = width;
			this.height = height;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
	    public void run() {
			if (!processing.compareAndSet(false, true)) return;

			Log.d(TAG, "BEGIN PROCESSING...");
			try {
	        	//Previous frame
	        	int[] pre = null;
				if (Preferences.SAVE_PREVIOUS) pre = detector.getPrevious();
				
				//Current frame (with changes)
				long bConversion = System.currentTimeMillis();
				int[] img = null;
				if (Preferences.USE_RGB) {
					img = ImageProcessing.decodeYUV420SPtoRGB(data, width, height);
				} else {
					img = ImageProcessing.decodeYUV420SPtoLuma(data, width, height);
				}
				long aConversion = System.currentTimeMillis();
				Log.d(TAG, "Converstion="+(aConversion-bConversion));
				
				//Current frame (without changes)
				int[] org = null;
				if (Preferences.SAVE_ORIGINAL && img!=null) org = img.clone();
				
				if (img!=null && detector.detect(img, width, height)) {
					// The delay is necessary to avoid taking a picture while in the
					// middle of taking another. This problem can causes some phones
					// to reboot.
					long now = System.currentTimeMillis();
					if (now > (mReferenceTime + Preferences.PICTURE_DELAY)) {
						mReferenceTime = now;
						
						Bitmap previous = null;
						if (Preferences.SAVE_PREVIOUS && pre!=null) {
							if (Preferences.USE_RGB) previous = ImageProcessing.rgbToBitmap(pre, width, height);
							else previous = ImageProcessing.lumaToGreyscale(pre, width, height);
						}
						
						Bitmap original = null;
						if (Preferences.SAVE_ORIGINAL && org!=null) {
							if (Preferences.USE_RGB) original = ImageProcessing.rgbToBitmap(org, width, height);
							else original = ImageProcessing.lumaToGreyscale(org, width, height);
						}
						
						Bitmap bitmap = null;
						if (Preferences.SAVE_CHANGES && img!=null) {
							if (Preferences.USE_RGB) bitmap = ImageProcessing.rgbToBitmap(img, width, height);
							else bitmap = ImageProcessing.lumaToGreyscale(img, width, height);
						}
						
						Log.i(TAG,"Saving.. previous="+previous+" original="+original+" bitmap="+bitmap);
						Looper.prepare();
						new SavePhotoTask().execute(previous,original,bitmap);
					} else {
						Log.i(TAG, "Not taking picture because not enough time has passed since the creation of the Surface");
					}
				}
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            processing.set(false);
	        }
			Log.d(TAG, "END PROCESSING...");

			processing.set(false);
	    }
	};

	private static final class SavePhotoTask extends AsyncTask<Bitmap, Integer, Integer> {
		/**
		 * {@inheritDoc}
		 */
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