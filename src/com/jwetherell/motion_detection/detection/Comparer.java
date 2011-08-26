package com.jwetherell.motion_detection.detection;

import android.util.Log;

/**
 * This class is adapted from the web site below. It is used to compare two State objects.
 * http://mindmeat.blogspot.com/2008/11/java-image-comparison.html
 * 
 * @author Pat Cullen
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class Comparer {
	private static final String TAG = "Comparer";
	
	private int comparex;
	private int comparey;
	private int leniency;
	private int debugMode; // 1: textual indication of change, 2: difference of factors

	public Comparer(int comparex, int comparey, int leniency) {
		this.comparex = comparex;
		this.comparey = comparey;
		this.leniency = leniency;
		this.debugMode = 0;
	}
 
	// want to see some stuff in the console as the comparison is happening?
	public void setDebugMode(int m) {
		this.debugMode = m;
	}
	
	// compare two images.
	public Comparison compare(State s1, State s2) {
		if (s1==null || s2==null) return null;
		
		int cx = comparex;
		if (cx > s1.getWidth()) cx = s1.getWidth();
		int cy = comparey;
		if (cy > s1.getHeight()) cy = s1.getHeight();
		
		// how many points per section
		int bx = (int)(Math.floor(s1.getWidth() / cx));
		if (bx <= 0) bx = 1;
		int by = (int)(Math.floor(s1.getHeight() / cy));
		if (by <= 0) by = 1;
		int[][] variance = new int[cy][cx];
		
		// set to a different by default, if a change is found then flag non-match
		boolean different = false;
		// loop through whole image and compare individual blocks of images
		int ty = 0;
		for (int y = 0; y < cy; y++) {
			StringBuilder output = new StringBuilder();
			if (debugMode > 0) output.append("|");
			ty = y*by;
			for (int x = 0; x < cx; x++) {
				int b1 = aggregateMapArea(s1.getMap(), x*bx, ty, bx, by);
				int b2 = aggregateMapArea(s2.getMap(), x*bx, ty, bx, by);
				int diff = Math.abs(b1 - b2);
				variance[y][x] = diff;
				// the difference in a certain region has passed the threshold value
				if (diff > leniency) different = true;
				if (debugMode == 1) output.append((diff > leniency ? "X" : " "));
				if (debugMode == 2) output.append(diff + (x < cx - 1 ? "," : ""));
			}
			if (debugMode > 0) {
				output.append("|");
				Log.d(TAG, output.toString());
			}
		}
		return (new Comparison(s1, s2, variance, different));
	}
	
	private static int aggregateMapArea(int[][] map, int ox, int oy, int w, int h) {
		if (map==null) return Integer.MIN_VALUE;
		
		int t = 0;
		for (int i = 0; i < h; i++) {
			int ty = oy+i;
			for (int j = 0; j < w; j++) t += map[ty][ox+j];
		}
		return (int)(t/(w*h));
	}

	public int getComparex() {
		return comparex;
	}

	public int getComparey() {
		return comparey;
	}

	public int getLeniency() {
		return leniency;
	}

	public int getDebugMode() {
		return debugMode;
	}
}
