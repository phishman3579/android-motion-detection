package com.jwetherell.motion_detection.detection;


/**
 * This class is adapted from the web site below. It creates a state object based on the brightness of a RGB image
 * represented by an integer array.
 * http://mindmeat.blogspot.com/2008/11/java-image-comparison.html
 * 
 * @author Pat Cullen
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class State {
	private int[] map = null;
	private int width;
	private int height;
	private int average;

	public State(int[] data, int width, int height) {
		if (data==null) throw new NullPointerException();
		
		this.map = data.clone();
		this.width = width;
		this.height = height;

		// build map and stats
		this.average = 0;
		for (int y = 0, xy=0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++, xy++) {
				this.average += data[xy];
			}
		}
		this.average = (this.average / (this.width * this.height));
	}

	public int[] getMap() {
		return map;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		output.append("h="+height+" w="+width+"\n");
		for (int y = 0, xy = 0; y < height; y++) {
			output.append('|');
			for (int x = 0;x < width; x++, xy++) {
				output.append(map[xy]);
				output.append('|');
			}
			output.append("\n");
		}
		return output.toString();
	}
}
