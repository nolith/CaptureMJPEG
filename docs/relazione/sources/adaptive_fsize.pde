import it.lilik.capturemjpeg.*;

private CaptureMJPEG capture;
private PImage next_img = null;

void setup() {
  frame.setResizable (true);
  background(0);
  capture = new CaptureMJPEG
    (this,
     "http://mynetworkcamera.foo/image?speed=20"),
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

