package com.jwetherell.motion_detection.detection;

import android.graphics.Color;
import android.util.Log;


/**
 * This class is used to process integer arrays containing RGB data and detects motion.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class RgbMotionDetection implements IMotionDetection {
	private static final String TAG = "RgbMotionDetection";

	//Specific settings
	private static final int mPixelThreshold = 50; //Difference in pixel (RGB)
	private static final int mThreshold = 10000; //Number of different pixels (RGB)

	private static int[] mPrevious = null;
	private static int mPreviousWidth = 0;
	private static int mPreviousHeight = 0;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] getPrevious() {
		return ((mPrevious!=null)?mPrevious.clone():null);
	}

	protected static boolean isDifferent(int[] first, int width, int height) {
		if (first==null) throw new NullPointerException();
		
		if (mPrevious==null) return false;
		if (first.length != mPrevious.length) return true;
		if (mPreviousWidth != width || mPreviousHeight != height) return true;

		int totDifferentPixels = 0;
		int size = height * width;

		for (int i = 0, ij=0; i < height; i++) {
			for (int j = 0; j < width; j++, ij++) {
				int pix = (0xff & ((int)first[ij]));
				int otherPix = (0xff & ((int)mPrevious[ij]));

				//Catch any pixels that are out of range
				if (pix < 0) pix = 0;
				if (pix > 255) pix = 255;
				if (otherPix < 0) otherPix = 0;
				if (otherPix > 255) otherPix = 255;

				if (Math.abs(pix - otherPix) >= mPixelThreshold) {
					totDifferentPixels++;
					//Paint different pixel red
					first[ij] = Color.RED;
				}
			}
		}
		if (totDifferentPixels <= 0) totDifferentPixels = 1;
		boolean different = totDifferentPixels > mThreshold;
		
		int percent = 100/(size/totDifferentPixels);
		String output = "Number of different pixels: " + totDifferentPixels + "> " + percent + "%";
		if (different) {
			Log.e(TAG, output);
		} else {
			Log.d(TAG, output);
		}

		return different;
	}

	/**
	 * Detect motion comparing RGB pixel values.
	 * {@inheritDoc}
	 */
	@Override
	public boolean detect(int[] rgb, int width, int height) {
		if (rgb==null) throw new NullPointerException();
		
		int[] original = rgb.clone();
		
		// Create the "mPrevious" picture, the one that will be used to check the next frame against.
		if(mPrevious == null) {
			mPrevious = original;
			mPreviousWidth = width;
			mPreviousHeight = height;
			Log.i(TAG, "Creating background image");
			return false;
		}

		long bDetection = System.currentTimeMillis();
		boolean motionDetected = isDifferent(rgb, width, height);
		long aDetection = System.currentTimeMillis();
		Log.d(TAG, "Detection "+(aDetection-bDetection));
		
		// Replace the current image with the previous.
		mPrevious = original;
		mPreviousWidth = width;
		mPreviousHeight = height;

		return motionDetected;
	}
}
