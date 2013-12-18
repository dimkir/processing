package processing.app.elements.sketch.example;

import java.io.File;
import java.io.IOException;

import processing.app.Editor;
import processing.app.elements.sketch.Sketch;

public class TrySketch {
    public static void main(String[] args){
      
        Editor ed = null;
        try {
          
          
          Sketch sketch = new Sketch(args[0] , ed);
          sketch.addFile(new File("somefile.pde"));
          
          
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        
      
    }
}
