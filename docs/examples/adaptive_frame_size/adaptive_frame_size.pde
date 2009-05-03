/* 
   CaptureMJPEG - example
Adaptive frame size

We also provide a way to dynamically resize the applet according to the image size.
This is especially useful when you want to build a simple viewer without caring of 
the original image size.

(c) 2008 Alessio Caiazza, Cosimo Cecchi
*/

import it.lilik.capturemjpeg.*;

private CaptureMJPEG capture;
private PImage next_img = null;

void setup() {
  frame.setResizable (true);
  background(0);
  capture = new CaptureMJPEG (this,
                              "http://mynetworkcamera.foo/image?speed=20",
                              "user",
                              "password");
  capture.setAdaptFrameSize(true);
  capture.startCapture();
  frameRate(20);
}

void draw() {
  if (next_img != null) {
    image(next_img, 0, 0);
  }
}

void captureMJPEGEvent(PImage img) {
  next_img = img;
}

