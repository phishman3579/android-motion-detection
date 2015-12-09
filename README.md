android-motion-detection
========================

Android code to detection motion from by comparing two pictures.

## Introduction

Android code to detection motion from by comparing two pictures. It comes with an Activity that initializes a camera and grabs two pictures and compares them.

* Created by Justin Wetherell
* For questions use: http://groups.google.com/forum/#!forum/android-motion-detection
* Google: http://code.google.com/p/android-motion-detection
* Github: http://github.com/phishman3579/android-motion-detection
* LinkedIn: http://www.linkedin.com/in/phishman3579
* E-mail: phishman3579@gmail.com
* Twitter: http://twitter.com/phishman3579

## Support me with a donation

<a href="https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=phishman3579%40gmail%2ecom&lc=US&item_name=Support%20open%20source&item_number=AndroidMotionDetection&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_SM%2egif%3aNonHosted" target="_new"><img border="0" alt="Donate to this project" src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif"></a>

## Details

You essentially have to override an onPreviewFrame(byte[] data, Camera cam) method and convert from the default YUV to RGB:

    int[] rgb = ImageProcessing.decodeYUV420SPtoRGB(data, width, height);

Create an object which you'll use for motion detection code:

    IMotionDetection detector = new RgbMotionDetection();

Call the detect() method passing in the parameters obtained above.

    boolean detected = detector.detect(rgb, width, height)

If the boolean "detected" variable is true then it has detected motion.

The RGB detection code is located in RgbMotionDetection.java class. The image processing code is located in ImageProcessing.java static class. The Activity to tie it all together is in MotionDetectionActivity.java.

I have created a MotionDetection class that detects motion comparing RGB values called RgbMotionDetection.java, a class that detects motion comparing Luminance values called LumaMotionDetection.java, and a class that detects motion comparing avergae Luminance values in regions called AggregateLumaMotionDetection.java.
