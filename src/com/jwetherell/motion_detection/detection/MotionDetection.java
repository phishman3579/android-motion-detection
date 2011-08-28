package com.jwetherell.motion_detection.detection;

import com.jwetherell.motion_detection.image.ImageProcessing;

import android.graphics.Color;
import android.util.Log;

/**
 * This abstract class is used to process integer arrays containing RGB data and detects motion.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class MotionDetection {
	private static final String TAG = "MotionDetection";
	private static final boolean USE_RGB = true;
	private static final boolean USE_HSL = false;
	private static final boolean USE_STATE = false;
	private static final int mThreshold = 10000; //Number of different pixels
	private static final int mPixelThreshold = 50; //Difference in pixel

	private static int[] mPrevious = null;
	private static int mPreviousWidth = 0;
	private static int mPreviousHeight = 0;
	private static State mPreviousStatus = null;
	
	public static int[] getPrevious() {
		return (mPrevious!=null)?mPrevious.clone():null;
	}

	protected static boolean isDifferentComparingState(int[] first, int width, int height) {
		if (first==null || mPrevious==null) return false;
		if (first.length != mPrevious.length) return true;
		if (mPreviousWidth != width || mPreviousHeight != height) return true;

		if (mPreviousStatus==null) {
			mPreviousStatus = new State(mPrevious, width, height);
			return false;
		}

		State state = new State(first, (width/10), (height/10)); 
		Comparer ic = new Comparer(20, 20, 0);
		ic.setDebugMode(1);
		Comparison c = ic.compare(mPreviousStatus, state);
		
		mPreviousStatus = new State(mPrevious, width, height);
		
		boolean different = c.isDifferent();
		String output = "isDifferent="+different;
		if (different) {
			Log.e(TAG, output);
		} else {
			Log.d(TAG, output);
		}

		return different;
	}
	
	protected static boolean isDifferentComparingRGB(int[] first, int width, int height) {
		if (first==null || mPrevious==null) return false;
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
	
	protected static boolean isDifferentComparingHSL(int[] first, int width, int height) {
		if (first==null || mPrevious==null) return false;
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

				float b1 = ImageProcessing.getBrightnessAtPoint(pix);
				float b2 = ImageProcessing.getBrightnessAtPoint(otherPix);
				
				if (Math.abs(b1 - b2) >= mPixelThreshold) {
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

	public static boolean detect(int[] data, int width, int height) {
		if (data==null) return false;
		int[] original = data.clone();
		
		// Create the "mPrevious" picture, the one that will be used to check the next frame against.
		if(mPrevious == null) {
			mPrevious = original;
			mPreviousWidth = width;
			mPreviousHeight = height;
			Log.i(TAG, "Creating background image");
			return false;
		}

		boolean motionDetected = false;
		if (USE_RGB) motionDetected = isDifferentComparingRGB(data, width, height);
		if (USE_HSL) motionDetected = isDifferentComparingHSL(data, width, height);
		if (USE_STATE) motionDetected = isDifferentComparingState(data, width, height);

		// Replace the current image with the previous.
		mPrevious = original;
		mPreviousWidth = width;
		mPreviousHeight = height;

		return motionDetected;
	}
}