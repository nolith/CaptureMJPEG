import it.lilik.capturemjpeg.*;

private CaptureMJPEG capture;
private PImage next_img = null;

void setup() {
  size(400, 300);
  background(0);
  capture = new CaptureMJPEG
 	(this,
         "http://mycamera.foo/image?speed=20"),
         "user",
         "password");
  // or this if you don't need auth
  // capture = new CaptureMJPEG
  // (this, "http://mycamera.foo/image?speed=20");

  capture.startCapture();
  frameRate(20);
}

void draw() {
  if (next_img != null) {
    image(next_img, 0, 0);
  }
}

// callback method
void captureMJPEGEvent(PImage img) {
  next_img = img;
}
