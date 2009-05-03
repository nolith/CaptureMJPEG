/* 
   CaptureMJPEG - example
Usage of the buffer facility

In other examples, you can see that we're defining a captureMJPEGEvent() function.
This function is called from within CaptureMJPEG every time a new image is ready to be displayed.
You may prefer to avoid defining this function and choose yourself when to display the images.
CaptureMJPEG has an internal circular buffer to store ready images for this purpose.

(c) 2008 Alessio Caiazza, Cosimo Cecchi
*/
import it.lilik.capturemjpeg.*;

private CaptureMJPEG capture;
private PImage next_img = null;

void setup() {
  size (400,300);
  capture = new CaptureMJPEG (this,
                              "http://mynetworkcamera.foo/image?speed=20",
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

