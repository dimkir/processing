package processing.app.elements.editorheader;

import java.awt.Color;
import java.awt.Font;

import processing.app.elements.statusline.EditorStatus;


/**
 * This class abstracts settings (like colors) of the EditorHeader.
 * 
 * This class is here to remove direct coupling of EditorHeader to Mode  and Preferences. 
 * 
 * <p>
 * It is important that all "loading" is done in constructor and that
 * implementation of abstract getters is very lightweight. (as they may be called frequently [eg on 
 * every redraw])
 * </p>
 * 
 * @author Dimitry Kireyenkov
 *
 */
public abstract class SettingsAdapter {

  abstract public Color getBgColor();

  abstract public boolean getHiding();

  abstract public Color getHideColor();

  abstract public Color getTextSelectedColor();

  abstract public Color getTextUnselectedColor();

  abstract public Font getFont();

  abstract public Color getTabSelectedColor();

  abstract public Color getTabUnselectedColor() ;

  abstract public Color getModifiedOrSelectionColor();
  
  abstract public int getGridSize();
  
  abstract public int getMinimumWidth();
  
  abstract public int getMaximumWidth();
  
  /**
   * This one was rather meant for @link {@link EditorStatus} and
   * rather got here by mistake.
   * 
   * @return
   */
  abstract public int getGUISpacing();
  
  abstract public boolean isHiResRetina();
  
  abstract public boolean useAntialiasing();
  
  abstract public int getContractedTabWidth();
}
