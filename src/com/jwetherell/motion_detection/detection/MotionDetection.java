package com.jwetherell.motion_detection.detection;

import com.jwetherell.motion_detection.Globals;

import android.graphics.Color;
import android.util.Log;


/**
 * This abstract class is used to process integer arrays containing RGB data and detects motion.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class MotionDetection {
	private static final String TAG = "MotionDetection";

	//Specific settings
	private static final int mPixelThreshold = 50; //Difference in pixel (RGB & LUMA)
	private static final int mThreshold = 10000; //Number of different pixels (RGB & LUMA)
	private static final int mLeniency = 100; //Difference of aggregate map (State)
	private static final int mDebugMode = 2; //State based debug (State)

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

		Comparer ic = new Comparer((width/50), (height/50), mLeniency);
		ic.setDebugMode(mDebugMode);
		Comparison c = ic.compare(state,mPreviousState);

		boolean different = c.isDifferent();
		String output = "isDifferent="+different;
		if (different) {
			Log.e(TAG, output);
			c.getChangeIndicator(first, width, height, ic);
		} else {
			Log.d(TAG, output);
		}

		mPreviousState = state;
		
		return different;
	}

	protected static boolean isDifferentComparingLuminescence(int[] first, int width, int height) {
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
		if (Globals.USE_RGB) motionDetected = isDifferentComparingRGB(rgb, width, height);
		if (Globals.USE_LUMA) motionDetected = isDifferentComparingLuminescence(rgb, width, height);
		if (Globals.USE_STATE) motionDetected = isDifferentComparingState(rgb, width, height);
		long aDetection = System.currentTimeMillis();
		Log.d(TAG, "Detection "+(aDetection-bDetection));
		
		// Replace the current image with the previous.
		mPrevious = original;
		mPreviousWidth = width;
		mPreviousHeight = height;

		return motionDetected;
	}
}