package com.jwetherell.motion_detection.image;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;


public class ImageProcessing {
	public static final int A = 0;
	public static final int R = 1;
	public static final int G = 2;
	public static final int B = 3;
	
	public static final int H = 0;
	public static final int S = 1;
	public static final int L = 2;

	//Get RGB from Integer
	public static float[] getARGB(int pixel) {
    	int a = (pixel >> 24) & 0xff;
    	int r = (pixel >> 16) & 0xff;
    	int g = (pixel >> 8) & 0xff;
    	int b = (pixel) & 0xff;
    	return (new float[]{a,r,g,b});
	}
	
	//Get HSL from RGB
	public static float[] convertToHSL(int r, int g, int b) {
		float minComponent = Math.min(r, Math.min(g, b));
        float maxComponent = Math.max(r, Math.max(g, b));
        float range = maxComponent - minComponent;
        float[] HSL = new float[3];
        
        HSL[L] = maxComponent;

        if(range == 0) { // Monochrome image
        	HSL[H] = HSL[S] = 0;
        } else {
            HSL[S] = 	(HSL[L] > 0.5) ? 
		    				range / (2 - maxComponent - minComponent) 
		    			: 
		    				range / (maxComponent + minComponent);
    				
            float red = r;
            float green = g;
            float blue = b;
            red /= 255;
            green /= 255;
            blue /= 255;
            
            if(r == maxComponent) {
            	HSL[H] = (blue - green) / range + (green < blue ? 6 : 0);
            } else if(g == maxComponent) {
            	HSL[H] = (blue - red) / range + 2;
            } else if(b == maxComponent) {
            	HSL[H] = (red - green) / range + 4;
            }
        }
        HSL[H] /= 6;
        
		return HSL;
	}

	public static float getBrightnessAtPoint(int pixel) {
		//Get RGB from Integer
    	int r = (pixel >> 16) & 0xff;
    	int g = (pixel >> 8) & 0xff;
    	int b = (pixel) & 0xff;
    	
    	//Convert RGB to HSL
		float minComponent = Math.min(r, Math.min(g, b));
        float maxComponent = Math.max(r, Math.max(g, b));
        float range = maxComponent - minComponent;
        float h=0,l=0;
        
        l = maxComponent;

        if(range == 0) { // Monochrome image
        	h = 0;
        } else {
            float red = r/255;
            float green = g/255;
            float blue = b/255;
            
            if(r == maxComponent) {
            	h = (blue - green) / range + (green < blue ? 6 : 0);
            } else if(g == maxComponent) {
            	h = (blue - red) / range + 2;
            } else if(b == maxComponent) {
            	h = (red - green) / range + 4;
            }
        }
        h /= 6;

        //Convert the HSL into a single "brightness" representation
		return (float)(l * 0.5 + ((h / 360) * 50));
	}

	public static int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {
		final int frameSize = width * height;
		int[] rgb = new int[frameSize];

	    for (int j = 0, yp = 0; j < height; j++) {
	        int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
	        for (int i = 0; i < width; i++, yp++) {
	            int y = (0xff & ((int) yuv420sp[yp])) - 16;
	            if (y < 0) y = 0;
	            if ((i & 1) == 0) {
	                v = (0xff & yuv420sp[uvp++]) - 128;
	                u = (0xff & yuv420sp[uvp++]) - 128;
	            }
	            int y1192 = 1192 * y;
	            int r = (y1192 + 1634 * v);
	            int g = (y1192 - 833 * v - 400 * u);
	            int b = (y1192 + 2066 * u);

	            if (r < 0) r = 0; else if (r > 262143) r = 262143;
	            if (g < 0) g = 0; else if (g > 262143) g = 262143;
	            if (b < 0) b = 0; else if (b > 262143) b = 262143;

	            rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
	        }
	    }
	    return rgb;
	}

	public static Bitmap rgbToBitmap(int[] rgb, int width, int height) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		bitmap.setPixels(rgb, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	public static Bitmap rotate(Bitmap bmp, int degrees) {
        //getting scales of the image  
        int width = bmp.getWidth();  
        int height = bmp.getHeight();  

        //Creating a Matrix and rotating it to 90 degrees   
        Matrix matrix = new Matrix();  
        matrix.postRotate(degrees);  

        //Getting the rotated Bitmap  
        Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream); 
        return rotatedBmp;
	}
	
	public static byte[] rotate(byte[] data, int degrees) {
		//Convert the byte data into a Bitmap
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);  

        //Getting the rotated Bitmap  
        Bitmap rotatedBmp = rotate(bmp,degrees);
        
        //Get the byte array from the Bitmap
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream); 
        return stream.toByteArray();
	}
}
