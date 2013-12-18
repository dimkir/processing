package processing.app.elements.statusline.example;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import processing.app.elements.editorheader.EditorHeader;
import processing.app.elements.editorheader.SimpleSettingsAdapter;
import processing.app.elements.statusline.EditorStatus;

public class SimpleFrame extends JFrame {
  
   SimpleFrame(){
     
       add(new EditorStatus(null));
       
       add(new EditorHeader(sketch, new SimpleSettingsAdapter(mode)));
       
       pack();
      
   }
   
   
   public static void main(String[] args){
     
     SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        // TODO Auto-generated method stub
          new SimpleFrame().setVisible(true); 
      }
    });
     
   } // main()

}
