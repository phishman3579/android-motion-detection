package com.jwetherell.motion_detection.detection;

public class EdgeDetector {
	// value used to flatten
	private double flat;
	// value used to grow
	private double ceil;
	
	// kernels for use in edge detection process
	private double [][] k1;
	private double [][] k2;
	private double [][] k3;
	private double [][] k4;

	public EdgeDetector(double flatFactor, double ceilingFactor) {
		flat = flatFactor;
		ceil = ceilingFactor;
		k1 = new double[][] { {flat,ceil,flat}, {flat,ceil,flat}, {flat,ceil,flat} };
		k2 = new double[][] { {flat,flat,flat}, {ceil,ceil,ceil}, {flat,flat,flat} };
		k3 = new double[][] { {flat,flat,ceil}, {flat,ceil,flat}, {ceil,flat,flat} };
		k4 = new double[][] { {ceil,flat,flat}, {flat,ceil,flat}, {flat,flat,ceil}};
	}
	
	public EdgeDetector() {
		this(-0.1667, 0.3333);
	}
	
	public int[][] process(int[][] map) {
		// mapped is a new array that contains the kernel manipulated values.
		int[][] mapped = new int[map.length][map[0].length];
		// run through every value in the map
		for (int i = 0; i < map.length; i++)
			for (int j = 0; j < map[0].length; j++) {
				// we want to get 4 values - 1 for each kernel
				double t1 = kernelValue(k1, map, j, i);
				double t2 = kernelValue(k2, map, j, i);
				double t3 = kernelValue(k3, map, j, i);
				double t4 = kernelValue(k4, map, j, i);
				mapped[i][j] = (int)((t1 + t2 + t3 + t4) );
			}
		return mapped;
	}
	
	private static double kernelValue(double[][] k, int[][] map, int x, int y) {
		// loop through the kernel and multiply the kernel value against the respective map value
		double temp = 0;
		int vmx, vmy;
		for (int i = 0; i < 3; i++) {
			vmy = y + i - 1;
			if (vmy < 0) vmy = 0; else if (vmy >= map.length) vmy = map.length - 1;
			for (int j = 0; j < 3; j++) {
				vmx = x + j - 1; // the middle point in the kernel maps to the [x,y] point in map
				if (vmx < 0) vmx = 0; else if (vmx >= map[0].length) vmx = map[0].length - 1;
				temp += map[vmy][vmx] * k[i][j];
			}
		}
		return temp;
	}
}
