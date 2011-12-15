#1/bin/bash

javadoc -author -linkoffline http://d.android.com/reference file:/usr/local/android-sdk-linux_86/docs/reference -classpath /usr/local/android-sdk-linux_86/platforms/android-4/android.jar -d javadocs/ -sourcepath `find . -name *.java`
