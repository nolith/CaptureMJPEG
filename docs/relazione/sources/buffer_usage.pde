import it.lilik.capturemjpeg.*;

private CaptureMJPEG capture;
private PImage next_img = null;

void setup() {
  size (400,300);
  capture = new CaptureMJPEG
    (this,
     "http://mynetworkcamera.foo/image?speed=20"),
     "user",
     "password");
  capture.startCapture();
  frameRate(20);
}
                
void draw() {
  next_img = capture.getImage();
  if (next_img != null) {
    image(next_img, 0, 0);
  }
}               

void stop()
{
  capture.dispose();
}

