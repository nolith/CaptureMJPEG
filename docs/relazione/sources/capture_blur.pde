import processing.video.*;

// Variable for capture device
Capture img;


void setup() {
  size(320, 240, P3D);
  frameRate(5);

  colorMode(RGB, 255, 255, 255, 100);

  img = new Capture(this, width, height, 12);
  
  background(0);
}


void draw() { 
  if (img.available()) {
    img.read();
  
    img.filter(GRAY);
    image(img, 0, 0);

    PImage edgeImg=new PImage(img.width/2, img.height);
    edgeImg.copy(img, 0, 0, img.width/2, img.height,
                  0, 0, edgeImg.width, edgeImg.height);
    edgeImg.filter(BLUR);
    // Draw the new image
    image(edgeImg, img.width/2, 0);
  }
}
