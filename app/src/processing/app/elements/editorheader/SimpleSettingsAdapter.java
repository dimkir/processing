package processing.app.elements.editorheader;

import java.awt.Color;
import java.awt.Font;

import processing.app.Base;
import processing.app.Mode;
import processing.app.Preferences;
import processing.app.Toolkit;


/**
 * This is implementation of the SettingsAdapter,
 * which takes seetings of Mode and Preferences.
 * 
 * This way this adapter is coupled to Mode and Preferences, 
 * but EditorHeader aren't.
 * 
 * @author Dimitry Kireyenkov
 *
 */
public class SimpleSettingsAdapter extends SettingsAdapter {
  
  private Color bgColor;
  private boolean hiding;
  private Color hideColor;
  private Color[] textColor;
  private Color textColorSelected;
  private Color textColorUnselected;
  private Font font;
  private Color tabColorSelected;
  private Color tabColorUnselected;
  private Color modifiedColor;

  public SimpleSettingsAdapter(Mode mode) {
    bgColor = mode.getColor("header.bgcolor");
    hiding = Preferences.getBoolean("buttons.hide.image");
    
    hideColor = mode.getColor("buttons.hide.color"); 
    
    textColorSelected = mode.getColor("header.text.selected.color");
    
    textColorUnselected = mode.getColor("header.text.unselected.color");
    
    font = mode.getFont("header.text.font");
    
    tabColorSelected = mode.getColor("header.tab.selected.color");   
    tabColorUnselected = mode.getColor("header.tab.unselected.color");
    
    modifiedColor = mode.getColor("editor.selection.color");
    
  }
  
  
  public Color getBgColor() {
    return bgColor;
  }

  public boolean getHiding() {
    return hiding;
  }

  public Color getHideColor() {
    return hideColor;
  }

  public Color getTextSelectedColor() {
    return textColorSelected;
  }

  public Color getTextUnselectedColor() {
    return textColorUnselected;
  }

  public Font getFont() {
    return font;
  }

  public Color getTabSelectedColor() {
    return tabColorSelected;
  }

  public Color getTabUnselectedColor() {
    return tabColorUnselected;
  }
  

  public Color getModifiedOrSelectionColor() {
    return modifiedColor;
  }


  @Override
  public int getGridSize() {
    return Preferences.GRID_SIZE;
  }


  @Override
  public int getMinimumWidth() {
    return 300;
  }


  @Override
  public int getMaximumWidth() {
    return 3000;
  }


  @Override
  public int getGUISpacing() {
    return Preferences.GUI_SMALL; // just as it was originally in EditorStatus
  }


  @Override
  public boolean isHiResRetina() {
    return Toolkit.highResDisplay();
  }


  @Override
  public boolean useAntialiasing() {
    // Oracle Java looks better with anti-aliasing turned on
    // don't anti-alias text in retina mode w/ Apple Java
    return Base.isUsableOracleJava();
  }


  @Override
  public int getContractedTabWidth() {
    return 10;
  }
}
