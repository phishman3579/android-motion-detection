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
	
	//RGB Specific settings
	private static final int mRgbThreshold = 10000; //Number of different pixels
	private static final int mRgbPixelThreshold = 50; //Difference in pixel

	//HSL Specific settings
	private static final int mHslThreshold = 1000; //Number of different pixels
	private static final int mHslPixelThreshold = 1; //Difference in brightness
	
	private static int[] mPrevious = null;
	private static int mPreviousWidth = 0;
	private static int mPreviousHeight = 0;
	private static State mPreviousState = null;
	
	public static int[] getPrevious() {
		return ((mPrevious!=null)?mPrevious.clone():null);
	}

	protected static boolean isDifferentComparingState(int[] first, int width, int height) {
		if (first==null || mPrevious==null) return false;
		if (first.length != mPrevious.length) return true;
		if (mPreviousWidth != width || mPreviousHeight != height) return true;

		if (mPreviousState==null) mPreviousState = new State(mPrevious, mPreviousWidth, mPreviousHeight);
		State state = new State(first, width, height); 

		Comparer ic = new Comparer((width/10), (height/10), 25);
		ic.setDebugMode(1);
		Comparison c = ic.compare(state,mPreviousState);

		boolean different = c.isDifferent();
		String output = "isDifferent="+different;
		if (different) {
			Log.e(TAG, output);
			c.getChangeIndicator(first, width, height, ic);
		} else {
			Log.d(TAG, output);
		}

		mPreviousState = state.clone();
		
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

				int b1 = ImageProcessing.getBrightnessAtPoint(pix);
				int b2 = ImageProcessing.getBrightnessAtPoint(otherPix);
				
				if (Math.abs(b1 - b2) >= mHslPixelThreshold) {
					totDifferentPixels++;
					//Paint different pixel red
					first[ij] = Color.RED;
				}
			}
		}
		if (totDifferentPixels <= 0) totDifferentPixels = 1;
		boolean different = totDifferentPixels > mHslThreshold;
		
		int percent = 100/(size/totDifferentPixels);
		String output = "Number of different pixels: " + totDifferentPixels + "> " + percent + "%";
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

				if (Math.abs(pix - otherPix) >= mRgbPixelThreshold) {
					totDifferentPixels++;
					//Paint different pixel red
					first[ij] = Color.RED;
				}
			}
		}
		if (totDifferentPixels <= 0) totDifferentPixels = 1;
		boolean different = totDifferentPixels > mRgbThreshold;
		
		int percent = 100/(size/totDifferentPixels);
		String output = "Number of different pixels: " + totDifferentPixels + "> " + percent + "%";
		if (different) {
			Log.e(TAG, output);
		} else {
			Log.d(TAG, output);
		}

		return different;
	}
	
	public static boolean detect(int[] rgb, int width, int height) {
		if (rgb==null) return false;
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
		boolean motionDetected = false;
		if (USE_RGB) motionDetected = isDifferentComparingRGB(rgb, width, height);
		if (USE_HSL) motionDetected = isDifferentComparingHSL(rgb, width, height);
		if (USE_STATE) motionDetected = isDifferentComparingState(rgb, width, height);
		long aDetection = System.currentTimeMillis();
		Log.d(TAG, "Detection "+(aDetection-bDetection));
		
		// Replace the current image with the previous.
		mPrevious = original;
		mPreviousWidth = width;
		mPreviousHeight = height;

		return motionDetected;
	}
}