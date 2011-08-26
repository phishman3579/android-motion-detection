package com.jwetherell.motion_detection.detection;

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
		this.variance = variance;
		this.different = different;
		this.height = variance.length;
		this.width = variance[0].length;
	}
 
	/*
	public BufferedImage getChangeIndicator(BufferedImage cx, Comparer comparer) {
		// setup change display image
		Graphics2D gc = cx.createGraphics();
		gc.setColor(Color.RED);

		float bx = (cx.getWidth() / width);
		float by = (cx.getHeight() / height);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (variance[y][x] > comparer.leniency)
					gc.drawRect((int)(x * bx), (int)(y * by), (int)bx, (int)by);
			}
		}
		return cx;
	}
	*/
	
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
