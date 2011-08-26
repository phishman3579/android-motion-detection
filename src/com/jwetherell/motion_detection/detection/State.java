package com.jwetherell.motion_detection.detection;

import com.jwetherell.motion_detection.image.ImageProcessing;

/**
 * This class is adapted from the web site below. It creates a state object based on the brightness of a RGB image
 * represented by an integer array.
 * http://mindmeat.blogspot.com/2008/11/java-image-comparison.html
 * 
 * @author Pat Cullen
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class State {
	private int[][] map;
	private int width;
	private int height;
	private int average;
	
	public State(int[] data, int width, int height) {
		this.width = width;
		this.height = height;
		
		// setup brightness map
		map = new int[height][width];
		
		// build map and stats
		average = 0;
		int ta = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0, xy = 0; x < width; x++, xy++) {
				ta = (int)(100*ImageProcessing.getBrightnessAtPoint(data[xy]));
				map[y][x] = ta;
				average += ta;
			}
		}
		average = (int)(average / (width * height));
	}

	public int[][] getMap() {
		return map;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
