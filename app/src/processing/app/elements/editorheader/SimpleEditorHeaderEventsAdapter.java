package processing.app.elements.editorheader;

import java.awt.event.MouseEvent;
import java.io.PrintStream;

import processing.app.elements.sketch.Sketch;


/**
 * Offers default implementation for the events provided by the EditorHeader.
 * 
 * <p>
 * You can override any of them, if you want to customize.
 * </p>
 * 
 * @author Dimitry Kireyenkov
 *
 */
public class SimpleEditorHeaderEventsAdapter implements IEditorHeaderEvents {


  private PrintStream ps;
  
  
  /**
   * Initializes with given console stream.
   * 
   * Can be used when trying to 
   * 
   * @param console NOT NULL console. (?or should I allow null?)
   */
  public SimpleEditorHeaderEventsAdapter(PrintStream console) {
     this.ps = console;
  }
  
  private void log(String s){
    ps.append(s);
    ps.append('\n');
  }
  
  private void log(String prefix, String suffix){
    ps.append(prefix);
    ps.append("\t");
    ps.append(suffix);
    ps.append("\n");
  }
  


  @Override
  public void onTabClicked(Tab tab, Sketch sketch, int tabIndex,
                           EditorHeader editorHeader) {
    // TODO Auto-generated method stub
    log("onTabClicked()", editorHeader.toString());
    
  }


  @Override
  public void onRequestedDelete(EditorHeader eh) {
    // TODO Auto-generated method stub
    log("onRequestedDelete()", eh.toString());
  }


  @Override
  public void onMouseExitedHeader(MouseEvent evt, EditorHeader eh) {
    // TODO Auto-generated method stub
    log("onMouseExitedHeader()", eh.toString());
    
  }


  @Override
  public void onHoverOverCollapsedTab(Tab tab, EditorHeader eh) {
    // TODO Auto-generated method stub
    log("onHoverOverCollapsedTab()", eh.toString());
  }


  @Override
  public void onSelectedNewMenuElement(EditorHeader eh) {
    // TODO Auto-generated method stub
    log("onSelectedNewMenuElement()", eh.toString());
    
  }


  @Override
  public void onSelectedRename(EditorHeader eh) {
    // TODO Auto-generated method stub
    log("onSelectedRename()", eh.toString());
    
  }
  


}
