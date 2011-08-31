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
		if (s1.getWidth()!=s2.getWidth() || s1.getHeight()!=s2.getHeight()) return null;
		
		// number of boxes
		int xBoxes = comparex;
		if (xBoxes > s1.getWidth()) xBoxes = s1.getWidth();
		int yBoxes = comparey;
		if (yBoxes > s1.getHeight()) yBoxes = s1.getHeight();
		
		// how many points per box
		int xPixelsPerBox = (int)(Math.floor(s1.getWidth() / xBoxes));
		if (xPixelsPerBox <= 0) xPixelsPerBox = 1;
		int yPixelsPerBox = (int)(Math.floor(s1.getHeight() / yBoxes));
		if (yPixelsPerBox <= 0) yPixelsPerBox = 1;
		
		// Boxes
		int[][] variance = new int[yBoxes][xBoxes];
		
		// set to a different by default, if a change is found then flag non-match
		boolean different = false;
		// loop through whole image and compare individual blocks of images
		int ty = 0;
		int tx = 0;
		int b1 = 0;
		int b2 = 0;
		int diff = 0;
		for (int y = 0; y < yBoxes; y++) {
			StringBuilder output = new StringBuilder();
			if (debugMode > 0) output.append("|");
			ty = y*yPixelsPerBox;
			for (int x = 0; x < xBoxes; x++) {
				tx = x*xPixelsPerBox;
				b1 = aggregateMapArea(s1.getMap(), tx, ty, xPixelsPerBox, yPixelsPerBox);
				b2 = aggregateMapArea(s2.getMap(), tx, ty, xPixelsPerBox, yPixelsPerBox);
				diff = Math.abs(b1 - b2);
				variance[y][x] = diff;
				// the difference in a certain region has passed the threshold value
				if (diff > leniency) different = true;
				if (debugMode == 1) output.append((different ? "X" : " "));
				if (debugMode == 2) output.append(diff + (x < xBoxes - 1 ? "," : ""));
			}
			if (debugMode > 0) {
				output.append("|");
				Log.d(TAG, output.toString());
			}
		}
		return (new Comparison(s1, s2, variance, different));
	}
	
	private static int aggregateMapArea(int[] map, int ox, int oy, int w, int h) {
		if (map==null) throw new NullPointerException();

		int t = 0;
		int ty = 0;
		int tx = 0;
		for (int y = 0; y < h; y++) {
			ty = oy+y;
			for (int x = 0; x < w; x++) {
				tx = ox+x;
				t += map[ty+tx];
			}
		}
		return (t/(w*h));
	}

	public int getCompareX() {
		return comparex;
	}

	public int getCompareY() {
		return comparey;
	}

	public int getLeniency() {
		return leniency;
	}

	public int getDebugMode() {
		return debugMode;
	}
}
