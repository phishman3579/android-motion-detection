package com.jwetherell.motion_detection.detection;

import com.jwetherell.motion_detection.image.ImageProcessing;

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
