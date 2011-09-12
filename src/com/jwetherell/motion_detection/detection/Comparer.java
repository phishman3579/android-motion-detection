package com.jwetherell.motion_detection.detection;

import android.graphics.Color;

/**
 * This class is adapted from the web site below. It is used to compare two State objects.
 * http://mindmeat.blogspot.com/2008/11/java-image-comparison.html
 * 
 * @author Pat Cullen
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class Comparer {
	private State state1 = null;
	private State state2 = null;
	private int xBoxes;
	private int yBoxes;
	private int xPixelsPerBox;
	private int yPixelsPerBox;
	private int leniency;
	private int debugMode; // 1: textual indication of change, 2: difference of factors
	
	private int[][] variance = null;
	private boolean different = false;

	public Comparer(State s1, State s2, int xBoxes, int yBoxes, int leniency) {		
		this.state1 = s1;
		this.state2 = s2;
		
		this.xBoxes = xBoxes;
		if (xBoxes > state1.getWidth()) xBoxes = state1.getWidth();
		
		this.yBoxes = yBoxes;
		if (yBoxes > state1.getHeight()) yBoxes = state1.getHeight();
		
		this.leniency = leniency;
		this.debugMode = 0;
		
		// how many points per box
		this.xPixelsPerBox = (int)(Math.floor(state1.getWidth() / xBoxes));
		if (xPixelsPerBox <= 0) xPixelsPerBox = 1;
		this.yPixelsPerBox = (int)(Math.floor(state1.getHeight() / yBoxes));
		if (yPixelsPerBox <= 0) yPixelsPerBox = 1;
		
		this.different = isDifferent(state1,state2);
	}

	// compare two images and populate the variance variable
	public boolean isDifferent(State s1, State s2) {
		if (s1==null || s2==null) 
			throw new NullPointerException();
		if (s1.getWidth()!=s2.getWidth() || s1.getHeight()!=s2.getHeight()) 
			throw new IllegalArgumentException();

		// Boxes
		this.variance = new int[yBoxes][xBoxes];
		
		// set to a different by default, if a change is found then flag non-match
		boolean different = false;
		// loop through whole image and compare individual blocks of images
		int b1 = 0;
		int b2 = 0;
		int diff = 0;
		for (int y = 0; y < yBoxes; y++) {
			for (int x = 0; x < xBoxes; x++) {
				b1 = aggregateMapArea(state1.getMap(), x, y);
				b2 = aggregateMapArea(state2.getMap(), x, y);
				diff = Math.abs(b1 - b2);
				variance[y][x] = diff;
				// the difference in a certain region has passed the threshold value
				if (diff > leniency) different = true;
			}
		}
		return different;
	}
	
	private int aggregateMapArea(int[] map, int xBox, int yBox) {
		if (map==null) throw new NullPointerException();

		int i = 0;
		int rowOffset = (yBox*yPixelsPerBox)*(yBoxes*yPixelsPerBox);
		int columnOffset = (xBox*xPixelsPerBox);
		int iy = 0;
		for (int y = 0; y < yPixelsPerBox; y++) {
			iy = (y*(yBoxes*yPixelsPerBox));
			for (int x = 0; x < xPixelsPerBox; x++) {
				i += map[rowOffset+columnOffset+iy+x];
			}
		}

		return (i/(xPixelsPerBox*yPixelsPerBox));
	}

	public void paintDifferences(int[] data) {
		if (data==null) throw new NullPointerException();

		int diff = 0;
		for (int y = 0; y < yBoxes; y++) {
			for (int x = 0; x < xBoxes; x++) {
				diff = variance[y][x];
				if (diff > leniency) paintRed(data, x, y);
			}
		}
	}
	
	private int paintRed(int[] data, int xBox, int yBox) {
		if (data==null) throw new NullPointerException();

		int i = 0;
		int rowOffset = (yBox*yPixelsPerBox)*(yBoxes*yPixelsPerBox);
		int columnOffset = (xBox*xPixelsPerBox);
		int iy = 0;
		for (int y = 0; y < yPixelsPerBox; y++) {
			iy = (y*(yBoxes*yPixelsPerBox));
			for (int x = 0; x < xPixelsPerBox; x++) {
				data[rowOffset+columnOffset+iy+x] = Color.RED;;
			}
		}

		return (i/(xPixelsPerBox*yPixelsPerBox));
	}
	 
	// want to see some stuff in the console as the comparison is happening?
	public void setDebugMode(int m) {
		this.debugMode = m;
	}

	public int getCompareX() {
		return xBoxes;
	}

	public int getCompareY() {
		return yBoxes;
	}

	public int getLeniency() {
		return leniency;
	}

	public int getDebugMode() {
		return debugMode;
	}

	public boolean isDifferent() {
		return different;
	}
	
	@Override
	public String toString() {
		int diff = 0;
		StringBuilder output = new StringBuilder();
		for (int y = 0; y < yBoxes; y++) {
			output.append('|');
			for (int x = 0; x < xBoxes; x++) {
				diff = variance[y][x];
				if (debugMode == 1) output.append((different ? 'X' : ' '));
				if (debugMode == 2) output.append(diff + (x < xBoxes - 1 ? "," : ""));
			}
			output.append("|\n");
		}
		return output.toString();
	}
}
