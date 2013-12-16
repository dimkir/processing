package processing.app.elements.editorheader;


import processing.app.elements.sketch.Sketch;
import processing.app.elements.sketch.SketchCode;

/**
 * This is an attempt to abstract the tab displayed in header of PDE.
 * 
 * Unfortunately it is pretty tightly coupled to @link {@link Sketch} and 
 * @link {@link SketchCode}. And duplicates statuses of the both of them.
 * 
 * This status duplication should be removed, as it increases coupling 
 * between elements which should't be coupled. This way introduces 
 * complexity and thus undermines modularity and ability to test and
 * update components.
 * 
 * <p>
 * Implements Comparable, which will sort Tab's by their last visited time.
 * </p>
 *
 */
public class Tab implements Comparable {
  
  /**
   * This is enum which holds status of the tab.
   *
   */
  public enum ESelectedStatus { SELECTED, UNSELECTED } ;
  
  
  private ESelectedStatus mSelectedStatus = null;
  
  
  /**
   * Index of corresponding SC object within the sketch. 
   * 
   * It is annoying to actually have the state pointing to the index inside of Sketch object.
   * It doesn't seem very good practice to actually keep this status here.
   * 
   * It kinda introduces "logic" coupling between this class and @link {@link Sketch} and @link {@link SketchCode}
   * which is probably the worst one can do.
   */
  private int index;
  
  /**
   * Actual X of the left border of the Tab after layout.
   */
  int left;
  
  /**
   * Actual X or the right border of the Tab after layout.
   */
  int right;
  
  /**
   * Text of the label.
   */
  private String text;
  
  /**
   * Width of rendered text (set in @link {@link EditorHeader#paintComponent(java.awt.Graphics)})
   */
  private int textWidth;
  
  /**
   * Flag of whether this tab is in "collapsed" or "normal" state.
   */
  private boolean textVisible;
  
  /**
   * Holds copy of the last-visited property of the corresponding @link {@link SketchCode} object.
   * This is another example of logical coupling of this class to @link {@link SketchCode}.
   */
  private long lastVisited;

  
  /**
   * Index of the tab. Index is NOT the order in which tabs are displayed,
   * but it matches order of the SC objects inside of Sketch object. 
   * It is kinda very "long-shot" to keep this shit synchronized.
   * @param index
   */
  Tab(int index) {
    this.index = index;
  }

  /**
   * As tab may be in "compressed" state, then in this state
   * text is not visible.
   * @return
   */
  boolean isTextVisible(){
    return textVisible;
  }
  
  /**
   * Checks whether x lies within bounds of the tab (as layed out by the rendering process inside of
   * {@linkplain EditorHeader}.
   * 
   * @param x
   * @return
   */
  boolean contains(int x) {
    return x >= left && x <= right;
  }

  /**
   * Sort by the last time visited
   */
  public int compareTo(Object o) {
    Tab other = (Tab) o;
    // do this here to deal with situation where both are 0
    if (lastVisited == other.lastVisited) {
      return 0;
    }
    if (lastVisited == 0) {
      return -1;
    }
    if (other.lastVisited == 0) {
      return 1;
    }
    return (int) (lastVisited - other.lastVisited);
  }
  
  public String getText(){
    return text;
  }
  
  public int getIndex(){
    return index;
  }
  
  
  public int getTextWidth(){
    return textWidth;
  }
  
  /**
   * Returns selected status.
   * @return
   */
  public ESelectedStatus getSelectedStatus(){
    return mSelectedStatus;
  }
}