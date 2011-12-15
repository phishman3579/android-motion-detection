package com.jwetherell.motion_detection.data;


/**
 * This class is used to store preferences on how to decode images and what to save.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class Preferences {	
    private Preferences() { }
    
	//Which motion detection to use
	public static boolean USE_RGB = true;
	public static boolean USE_LUMA = false;
	public static boolean USE_STATE = false;
	
	//Which photos to save
	public static boolean SAVE_PREVIOUS = false;
	public static boolean SAVE_ORIGINAL = false;
	public static boolean SAVE_CHANGES = true;
	
	//Time between saving photos
	public static int PICTURE_DELAY = 10000;
}
