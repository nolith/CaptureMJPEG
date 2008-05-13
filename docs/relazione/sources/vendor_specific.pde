import it.lilik.capturemjpeg.*;

private CaptureMJPEG capture;
private PImage next_img = null;

void setup() {
  size(400, 300);
  background(0);
  capture = new CaptureMJPEG
    (this,
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
