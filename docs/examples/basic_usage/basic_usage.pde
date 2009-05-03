/* 
   CaptureMJPEG - example
Basic usage

If you use the generic URL parser, you have to specify the full path of
your video camera, including "http://".

(c) 2008 Alessio Caiazza, Cosimo Cecchi
*/
import it.lilik.capturemjpeg.*;

private CaptureMJPEG capture;
private PImage next_img = null;

void setup() {
  size(400, 300);
  background(0);
  capture = new CaptureMJPEG (this,
                              "http://mynetworkcamera.foo/image?speed=20",
                              "user",
                              "password");
  // or this if you don't need auth
  // capture = new CaptureMJPEG(this, "http://mynetworkcamera.foo/image?speed=20");

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

