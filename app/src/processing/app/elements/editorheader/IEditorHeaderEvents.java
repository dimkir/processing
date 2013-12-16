package processing.app.elements.editorheader;

import java.awt.event.MouseEvent;

import processing.app.elements.sketch.Sketch;

/**
 * This header declares all the things.
 * @author Dimitry Kireyenkov
 *
 */
public interface IEditorHeaderEvents {

  /**
   * Is called when tab is clicked in editor-header.
   * 
   * @param tab
   * @param sketch in case sketch is set, this specifies sketche
   * @param tabIndex specifies index of the {@link SketchCode} element inside of sketch.
   * @param editorHeader
   * 
   */
  public void onTabClicked(Tab tab, int tabIndex, EditorHeader editorHeader);
  
  /**
   * This callback is called when in menu selected item to delete.
   * 
   *  <p>
   *  We don't have Sketch or SketchCode as parameters,
   *  as @link {@link EditorHeader} isn't coupled to those classes.
   *  </p>
   *  
   */
  public void onRequestedDelete(EditorHeader eh);
  
  /**
   * When mouse exits header.
   * @param evt
   */
  public void onMouseExitedHeader(MouseEvent evt, EditorHeader eh);
  
  
  /**
   * Triggered when mouse is hovering over tab which is in "collapsed" state.
   * 
   * <p>
   * In the original PDE this functionality is used to show in the status bar
   * the name of the file on tab, which is hovered over.
   * </p>
   * 
   * @param tab
   */
  public void onHoverOverCollapsedTab(Tab tab, EditorHeader eh);

  /**
   * Triggered when user selects in the menu "new tab" event.
   */
  public void onSelectedNewMenuElement(EditorHeader eh);

  /**
   * Triggered when user selects "rename" item in the Header's menu.
   */
  public void onSelectedRename(EditorHeader eh);
  
}
