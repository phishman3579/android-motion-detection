package com.jwetherell.motion_detection.detection;

import android.graphics.Color;

/**
 * This class is adapted from the web site below. It is used to indicate the variance in two state objects.
 * http://mindmeat.blogspot.com/2008/11/java-image-comparison.html
 * 
 * @author Pat Cullen
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class Comparison {
	private State s1;
	private State s2;
	private int[][] variance;
	private int width; 
	private int height; 
	private boolean different = false;

	public Comparison(State s1, State s2, int[][] variance, boolean different) {
		if (variance==null || s1==null || s2==null) throw new NullPointerException();
		
		this.s1 = s1;
		this.s2 = s2;
		this.different = different;
		
		this.variance = variance;
		this.height = variance.length;
		this.width = variance[0].length;
	}

	public void getChangeIndicator(int[] data, int width, int height, Comparer comparer) {
		if (data==null || comparer==null) throw new NullPointerException();
		
		int xBoxes = comparer.getCompareX();
		if (xBoxes > s1.getWidth()) xBoxes = s1.getWidth();
		int yBoxes = comparer.getCompareY();
		if (yBoxes > s1.getHeight()) yBoxes = s1.getHeight();

		int xPixelsPerBox = (int)(Math.floor(s1.getWidth() / xBoxes));
		if (xPixelsPerBox <= 0) xPixelsPerBox = 1;
		int yPixelsPerBox = (int)(Math.floor(s1.getHeight() / yBoxes));
		if (yPixelsPerBox <= 0) yPixelsPerBox = 1;
		
		int tx = 0;
		int ty = 0;
		for (int y = 0; y < yBoxes; y++) {
			ty = (int)(y * yPixelsPerBox);
			for (int x = 0; x < xBoxes; x++) {
				tx = (int)(x * xPixelsPerBox);
				if (variance[y][x] > comparer.getLeniency()) {
					paintArea(data,tx,ty,xPixelsPerBox,yPixelsPerBox);
				}
			}
		}
	}

	private static void paintArea(int[] data, int ox, int oy, int w, int h) {
		if (data==null) throw new NullPointerException();

		int ty = 0;
		int tx = 0;
		for (int y = 1; y <= h; y++) {
			ty = oy*y;
			for (int x = 0; x < w; x++) {
				tx = ox+x;
				data[ty+tx] = Color.RED;
			}
		}
	}
	
	public int[][] getVariance() {
		return variance;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public boolean isDifferent() {
		return different;
	}
	
	public State getState1() {
		return s1;
	}
	
	public State getState2() {
		return s2;
	}
}
