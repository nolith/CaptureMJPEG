import it.lilik.capturemjpeg.*;

private CaptureMJPEG capture;
private PImage img = null;


void setup() {
  size(320,240);

  background(0);
  capture = new CaptureMJPEG(this,
                new SonyURL("XXX.XXX.XXX.XXX")
                  .setFPS(20).getURL(),
                "xxxxxxxxx",
                "xxxxxxxxx");

  capture.startCapture();
  frameRate(5);
}

void draw() {
  img = capture.getImage();
  if (img != null) {

    img.filter(GRAY);
    image(img, 0, 0);

    // Create an opaque image of the same 
    //  size as the original
    PImage edgeImg=new PImage(img.width/2, img.height);
    edgeImg.copy(img, 0, 0, img.width/2, img.height,
                  0, 0, edgeImg.width, edgeImg.height);

    edgeImg.filter(BLUR);
    // Draw the new image
    image(edgeImg, img.width/2, 0); 
  }
}



