package processing.app.elements.editorheader.examples;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import processing.app.Base;
import processing.app.Mode;
import processing.app.elements.editorheader.EditorHeader;
import processing.app.elements.editorheader.SimpleEditorHeaderEventsAdapter;
import processing.app.elements.editorheader.SimpleSettingsAdapter;
import processing.app.elements.sketch.Sketch;
import processing.mode.java.JavaMode;

/**
 * This is test class which attempts to construct 
 * JFrame with our PDE component {@link EditorHeader}.
 * 
 * @author Dimitry Kireyenkov
 *
 */
public class SimpleFrame extends JFrame {
  
   public static final String C_SKETCH_PATH = "c:\\Users\\Ernesto Guevara\\Desktop\\processing\\!12 December\\MyFontFun";
   public static final String C_JAVA_MODE_FOLDER_PATH = "c:\\Users\\Ernesto Guevara\\Desktop\\ProcessingBin\\processing-2.1 64bit\\modes\\java";
  
   /**
    * This class attempts to initialize the components to show the thing.
    * 
    * @throws RuntimeException when there was an error initializing.
    *         As this is a class which will only be used for testing
    *         and it will be always on the very top of calling stack,
    *         it's ok that we throw plain RTEX.
    */
   SimpleFrame(){
     
       File modeFolder = new File(C_JAVA_MODE_FOLDER_PATH);
      // TODO: how to get the mode?
     // TODO: how to get the sketch instance?
       Mode mode;
      try {
        mode = new JavaMode(new Base(new String[] {}), modeFolder);

     
       Sketch sketch = new Sketch(C_SKETCH_PATH, mode);
     
       
       EditorHeader eh = new EditorHeader(sketch, new SimpleSettingsAdapter(mode));
       
       eh.setOnHeaderEvent(new SimpleEditorHeaderEventsAdapter(System.out));
       
       add(eh);
       
       pack();
       
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }       
      
   }
   
   
   public static void main(String[] args){
     
     SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        
         setupUEH();

         new SimpleFrame().setVisible(true); 
      }
    });
     
   } // main()

   
   /**
    * Sets up UEH.
    */
   static void  setupUEH(){
     // our policy UEH policy is simple: user thown direct RTEXes may propagage up the stack
     // but the method which throws it, knows that this throwing plain RTEXes is a fatal
     // error which will halt the thread.
     Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      
      @Override
      public void uncaughtException(Thread t, Throwable e) {
         e.printStackTrace();
      }
    });     
   }
   
}
