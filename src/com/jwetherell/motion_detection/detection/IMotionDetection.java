package com.jwetherell.motion_detection.detection;


/**
 * This interface is used to represent a class that can detect motion
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public interface IMotionDetection {

	public int[] getPrevious();

	public boolean detect(int[] data, int width, int height);
}
