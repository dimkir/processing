// Bad-print shader. Adapted from rom Fluxus Shader Library:
// http://www.pawfal.org/index.php?page=FluxusHardwareShaderLibrary

import controlP5.*;

ControlP5 controlP5;

PShader badPrint;

boolean enabled = true;
float scaleR = 1.0, scaleG = 1.0, scaleB = 1.0;
float offsetR = 0.2, offsetG = 0.2, offsetB = 0.2; 
float registerR = 0.2, registerG = 0.2, registerB = 0.2;
float sizeR = 0.1, sizeG = 0.2, sizeB = 0.1;

public void setup() {
  size(800, 800, P3D);

  noStroke();
  fill(204);

  badPrint = loadShader("BadPrintFrag.glsl", "BadPrintVert.glsl");
  
  sphereDetail(60);
  
  controlP5 = new ControlP5(this);
  controlP5.addToggle("enabled").linebreak();
  controlP5.addSlider("scaleR", 0, 1);
  controlP5.addSlider("scaleG", 0, 1);
  controlP5.addSlider("scaleB", 0, 1).linebreak();  
  controlP5.addSlider("offsetR", 0, 1);
  controlP5.addSlider("offsetG", 0, 1);
  controlP5.addSlider("offsetB", 0, 1).linebreak();  
  controlP5.addSlider("registerR", 0, 1);
  controlP5.addSlider("registerG", 0, 1);
  controlP5.addSlider("registerB", 0, 1).linebreak();
  controlP5.addSlider("sizeR", 0, 1);
  controlP5.addSlider("sizeG", 0, 1);
  controlP5.addSlider("sizeB", 0, 1).linebreak();  
}

public void draw() {
  background(0);

  if (enabled) {  
    shader(badPrint);
   
    badPrint.set("Scale", scaleR, scaleG, scaleB);
    badPrint.set("Offset", offsetR, offsetG, offsetB);
    badPrint.set("Register", registerR, registerG, registerB);
    badPrint.set("Size", sizeR, sizeG, sizeB);
  } else {
    resetShader(); 
  }
    
  noStroke(); 
  pointLight(204, 204, 204, 1000, 1000, 1000);
  
  pushMatrix();
  translate(width/2, height/2); 
  rotateX(frameCount * 0.01);
  rotateY(frameCount * 0.01); 
  sphere(200); 
  popMatrix();
  
  hint(DISABLE_DEPTH_TEST);
  noLights();
  resetShader();
  controlP5.draw();
  hint(ENABLE_DEPTH_TEST);
}