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
		this.s1 = s1;
		this.s2 = s2;
		this.different = different;
		
		if (variance==null) return;
		this.variance = variance;
		this.height = variance.length;
		this.width = variance[0].length;
	}

	public void getChangeIndicator(int[] data, int width, int height, Comparer comparer) {
		int bx = (width / this.width);
		int by = (height / this.height);

		int tx = 0;
		int ty = 0;
		for (int y = 0; y < by; y++) {
			ty = (int)(y * by);
			for (int x = 0; x < bx; x++) {
				tx = (int)(x * bx);
				if (variance[y][x] > comparer.getLeniency()) {
					paintArea(data,tx,ty,bx,by);
				}
			}
		}
	}

	private static void paintArea(int[] data, int ox, int oy, int w, int h) {
		if (data==null) return;

		int ty = 0;
		for (int y = 1; y <= h; y++) {
			ty = oy*y;
			for (int x = 0, xy=ty; x < w; x++, xy++) {
				data[xy] = Color.RED;
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
