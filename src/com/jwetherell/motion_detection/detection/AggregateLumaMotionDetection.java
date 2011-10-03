package com.jwetherell.motion_detection.detection;

import android.util.Log;


/**
 * This class is used to process integer arrays containing luma data and detects motion using an aggregate map.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class AggregateLumaMotionDetection implements IMotionDetection {
	private static final String TAG = "AggregateLumaMotionDetection";

	//Specific settings
	private static final int mLeniency = 10; //Difference of aggregate map of luma values
	private static final int mDebugMode = 2; //State based debug
	private static final int mXBoxes = 10; //State based debug
	private static final int mYBoxes = 10; //State based debug

	private static int[] mPrevious = null;
	private static int mPreviousWidth;
	private static int mPreviousHeight;
	private static State mPreviousState = null;

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

		if (mPreviousState==null) {
			mPreviousState = new State(mPrevious, mPreviousWidth, mPreviousHeight);
			return false;
		}
		
		State state = new State(first, width, height);
		Comparer comparer = new Comparer(state, mPreviousState, mXBoxes, mYBoxes, mLeniency, mDebugMode);

		boolean different = comparer.isDifferent();
		String output = "isDifferent="+different;
		if (different) {
			Log.e(TAG, output);
			comparer.paintDifferences(first);
		} else {
			Log.d(TAG, output);
		}

		mPreviousState = state;
		
		return different;
	}

	/**
	 * Detect motion using aggregate luma values.
	 * {@inheritDoc}
	 */
	@Override
	public boolean detect(int[] luma, int width, int height) {
		if (luma==null) throw new NullPointerException();
		
		int[] original = luma.clone();
		
		// Create the "mPrevious" picture, the one that will be used to check the next frame against.
		if(mPrevious == null) {
			mPrevious = original;
			mPreviousWidth = width;
			mPreviousHeight = height;
			Log.i(TAG, "Creating background image");
		}

		long bDetection = System.currentTimeMillis();
		boolean motionDetected = isDifferent(luma, width, height);
		long aDetection = System.currentTimeMillis();
		Log.d(TAG, "Detection "+(aDetection-bDetection));
		
		// Replace the current image with the previous.
		mPrevious = original;
		mPreviousWidth = width;
		mPreviousHeight = height;

		return motionDetected;
	}
}
