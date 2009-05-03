/* 
   CaptureMJPEG - example
Usage of vendor-specific URL builders

Note that when using vendor-specific URL builder, you must provide only 
the host name to the constructor (without "http://").

(c) 2008 Alessio Caiazza, Cosimo Cecchi
*/
import it.lilik.capturemjpeg.*;

private CaptureMJPEG capture;
private PImage next_img = null;

void setup() {
  size(400, 300);
  background(0);
  capture = new CaptureMJPEG (this,
                              new SonyURL("mynetworkcamera.foo")
                              .setFPS(20).getURL(),
                            //new AxisURL("mynetworkcamera.foo")
                            //.setDesiredFPS(20).getURL(),
                              "user",
                              "password");
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

