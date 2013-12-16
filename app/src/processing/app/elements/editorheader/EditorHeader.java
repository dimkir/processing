/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-13 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.app.elements.editorheader;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import processing.app.Base;
import processing.app.Mode;
import processing.app.Toolkit;
import processing.app.elements.editorheader.Tab.ESelectedStatus;
import processing.app.elements.sketch.Sketch;
import processing.app.elements.sketch.SketchCode;


/**
 * Sketch tabs at the top of the editor window.
 * 
 * <p>
 * Manages representing sketches tabs, as well as offers rightclick menu and context operations.
 * </p>
 */
public class EditorHeader extends JComponent {

  /**
   *  standard UI sizing (OS-specific, but generally consistent)
   */
  private static final int SCROLLBAR_WIDTH = 16;

  /**
   *  amount of space on the left edge before the tabs start
   */
  private static final int MARGIN_WIDTH = 6;
  
  
  /**
   *  distance from the righthand side of a tab to the drop-down arrow
   */
  private static final int ARROW_GAP_WIDTH = 8;
  
  /**
   *  indent x/y for notch on the tab
   */
  private static final int NOTCH = 0;

  
  /**
   * how far to raise the tab from the bottom of this Component
   */
  private static final int TAB_HEIGHT = 25;
  
  /**
   *  line that continues across all of the tabs for the current one
   */
  private static final int TAB_STRETCH = 3;

  /**
   *  amount of extra space between individual tabs
   */
  private static final int TAB_BETWEEN = 2;

  /**
   *  amount of margin on the left/right for the text on the tab
   */
  private static final int TEXT_MARGIN = 10;
  
  /**
   *  Width of the tab when no text visible.

   *  <p>
   *  (total tab width will be this plus TEXT_MARGIN*2)
   *  </p>
   */

  
  /// =====================================================================
  /// ==================   visualization parameters  ======================
  /// =====================================================================
  
//  private Color bgColor;
//  private boolean hiding;
//  private Color hideColor;
//  
//  private Color textColor[] = new Color[2];
//  private Color tabColor[] = new Color[2];
//  private Color modifiedColor;
//  
//  private Font font;
//FontMetrics metrics;
  
  /**
   * ??
   * When is it initialized?
   * It is initialized in {@link #paintComponent(Graphics)}
   * and used one more time in {@link #placeTabs(int, int, Graphics2D)}
   */
  private int fontAscent;  
  
  /**
   * Image for tab arrow.
   * 
   * Is loaded in {@link #loadImages(Mode)}
   */
  private static Image tabArrow;  

  /// =====================================================================
  ///       ^^^^^^^^^^^^^  visualization parameters  ^^^^^^^^^^^^^         
  /// =====================================================================
  
  
  /**
   * This instance holds references to the settings needed by the EditorHeader.
   */
  private SettingsAdapter sadapter; 
  
  /**
   * I want to remove this variable.
   */
  //private Editor editor;

  
  // ===========================================================================
  // ===========       supos                                     ===============
  // ===========================================================================
  /**
   * Tabs which represent the filenames?
   * 
   * ?Tabs of the header.
   * When is this created? At class construction time 
   * it is initialized as empty array.
   * 
   * why two arrays?
   */
  private Tab[] tabs = new Tab[0];
  
  /**
   * Looks like this is list of tabs,
   * in the stepping order.
   * The quantity of Tabs is 1 less than the quantity of the tabs.
   * 
   * It starts uninitialized ??
   */
  private Tab[] visitOrder;



  private JMenu menu;
  private JPopupMenu popup;

  /**
   * ?? what is this var
   */
  private int menuLeft;
  
  /**
   * What is this variable?
   */
  private int menuRight;

  //

  private static final String STATUS[] = { "unsel", "sel" };
  private static final int UNSELECTED = 0;
  private static final int SELECTED = 1;

//  static final String WHERE[] = { "left", "mid", "right" }; //, "menu" };
//  static final int LEFT = 0;
//  static final int MIDDLE = 1;
//  static final int RIGHT = 2;
//  static final int MENU = 3;

  private static final int PIECE_WIDTH = 4;
  private static final int PIECE_HEIGHT = 33;
  private Image[][] pieces;

  private static final int ARROW_WIDTH = 14;
  private static final int ARROW_HEIGHT = 14;
  


  
  /**
   * This is offscreen buffer, which
   * we use in {@link #paintComponent(Graphics)}
   * to render all the imagery. 
   * 
   * Later as the last call in {@link #paintComponent(Graphics)}
   * this offscreen buffer is rendered to actual screen surface.
   * 
   */
  private Image offscreen;
  
  /**
   *  Dimensions we last rendered the component.
   *  Mostly this one is used to detect change in the dimensions,
   *  so that we may resize the image if necessary.
   *  
   *  <p>
   *  Keep in mind that dimensions of the component
   *  may be smaller than the dimensions of the offscreen
   *  buffer. And only when the dimensions of the offscreen
   *  are not enough to fit newly enlarged component, we
   *  will recreate the offscreen buffer.
   *  </p>
   *  
   *  But do we really need these variables? Can we just get by with {@link #imageW} and 
   *  {@link #imageH}?
   *   
   *  Only used in {@link #paintComponent(Graphics)}
   */
  private int sizeW, sizeH;
  
  /**
   * Contains the actual dimensions of the offscreen buffer.
   * They're set when we create the image. 
   * 
   *  Which imageW?
   *  Only used in {@link #paintComponent(Graphics)}
   */
  private int imageW, imageH;

  /**
   * I wonder what is THIS used for?
   * This is the notice which is kinda used to be set in the Editor. 
   */
//  private String lastNoticeName;
  
  /**
   * This is the sketch for which we are displaying tabs.
   * 
   * <p>
   * Note that when it's NULL, we have no sketches loaded.
   */
  private Sketch mSketch;
  
  // TODO: need to rename variable into ehEvents or smth more
  // close to the purspose of containing listener for ALL events.
  private IEditorHeaderEvents mOnTabClickedListener;

  /**
   * Incuim, Just trivial setter for the sketch (and calls to {@link #rebuild()})
   * 
   * What may happen, is that we need to add methods to start listening for sketch events.
   * 
   * @param sketch
   */
  public void setSketch(Sketch sketch){
    // TODO: when Sketch can provide events, we should add listeners to the events (like adding/deleting/etc).
    mSketch = sketch;
    // TODO: should here be rebuild.
    rebuild();
  }

  
  /**
   * Initializes header with given sketch and mode.
   * 
   * <p>
   * Can parameters be NULL?
   * </p>
   * 
   * @param sketch in theory it should be able to render itself without any sketch loaded, and sketch
   *        may be added later.
   * @param settingsAdapter NOT NULL.
   * 
   * @throws IllegalArgumentException when invalid settings adapter is supplied. (null)
   * 
   */
  public EditorHeader(Sketch sketch, SettingsAdapter settingsAdapter) {
    verifyNotNullOrThrow(settingsAdapter, "settingsAdapter");
    
    sadapter = settingsAdapter;
    
    setSketch(sketch);
    
    loadImages();

    addMouseListener(new MouseAdapter() {
      
      /**
       * On mouse pressed we try to figure out on which tab the mouse click
       * happened and then the header tells Editor (via Sketch) to actually
       * active this sketchcode (tab) in the editor.
       * 
       * I think it would be better to fire an event "on tabpressed" or smth like that.
       */
        public void mousePressed(MouseEvent e) {
          int x = e.getX();
          int y = e.getY();

          if ((x > menuLeft) && (x < menuRight)) {
            
            menu.getPopupMenu().show(EditorHeader.this, x, y);
            // TODO: verify if this fix worked:  replaces with the call to getPopupMenu()
//            popup.show(EditorHeader.this, x, y);

          } else {
//            Sketch sketch = editor.getSketch();
            
//            for (int i = 0; i < sketch.getCodeCount(); i++) {
//              if ((x > tabLeft[i]) && (x < tabRight[i])) {
//                sketch.setCurrentCode(i);
//                repaint();
//              }
//            }
            
            Tab tb = findTabContainingX(x);
            if ( tb != null ){
              fireTabClicked(tb);
              repaint();
            }
            
          }
        }

        /**
         * And when mouse is exited? What do we do?
         */
        public void mouseExited(MouseEvent e) {
          // only clear if it's been set
//          if (lastNoticeName != null) {
//            // only clear if it's the same as what we set it to
//            editor.clearNotice(lastNoticeName);
//            lastNoticeName = null;
              fireMouseExitedHeader(e);
          }
        
    });




    addMouseMotionListener(new MouseMotionAdapter() {
        /**
         * When tab is "collapsed" it's text is not visible,
         * thus when we hover on the thing, we can actually set
         * to status bar name of the item.
         */
        public void mouseMoved(MouseEvent e) {
          int x = e.getX();
          Tab tb = findTabContainingX(x);
          if ( tb != null && !tb.isTextVisible() ){
            fireHoverOverCollapsedTab(tb);
          }
        }
      });
  }


  /**
   * Loops over tabs and tries to find 
   * one containing x.
   * 
   * @param x 
   * @return NULL in case can't find tab containing x.
   */
  protected Tab findTabContainingX(int x) {
    for (Tab tab : tabs) {
      if (tab.contains(x)) {
         return tab;
      }
    }   
    return null;
  }


  /**
   * When we hover over "collapsed" tab.
   * 
   * @param tab
   */
  protected void fireHoverOverCollapsedTab(Tab tab) {
    if ( !verifyAndLogListenerAvailable() ){  return;   }
    
    mOnTabClickedListener.onHoverOverCollapsedTab(tab, this);
    
  }
  
  
  protected void fireSelectedDelete(){
    if ( !verifyAndLogListenerAvailable() ){  return;   }

    mOnTabClickedListener.onRequestedDelete(this);
  }

  /**
   * Fires when the mouse is out of the header.
   * @param e
   */
  protected void fireMouseExitedHeader(MouseEvent e) {
    if ( !verifyAndLogListenerAvailable() ){  return;   }
    
    mOnTabClickedListener.onMouseExitedHeader(e, this);
  
  }  




  /**
   * Fires tab clicked event, in case we have registered listener.
   * 
   * @param tab
   */
  private void fireTabClicked(Tab tab) {
    if ( !verifyAndLogListenerAvailable() ){  return;   }
    
    mOnTabClickedListener.onTabClicked(tab, tab.getIndex(), EditorHeader.this);
  }

  /**
   * Verifies if the callback is available and as well logs 
   * moments when it's not.
   * 
   * @return true when available
   *        false when NOT available.
   */
  private boolean verifyAndLogListenerAvailable(){
    if ( mOnTabClickedListener == null ) {
      log("fireTabClicked() was called, but no listener is registered.");
      return false;
    }
    return true;
    
  }
  
  /**
   * Just delegate for logging. 
   * 
   * @param string
   */
  private void log(String string) {
    // TODO implement proper logging.
    System.out.println(string);
  }

  /**
   * Sets event listener for all EditorHeader events.
   * @param listener
   */
  public void setOnHeaderEvent(IEditorHeaderEvents listener){
     mOnTabClickedListener = listener;
  }
  

//  protected String tabFile(int status, int where) {
//    return "theme/tab-" + STATUS[status] + "-" + WHERE[where];
//  }


  /**
   * Just loads colors for the mode (from mode and Preferences) and
   * as well as loads {@link #tabArrow} image.
   * 
   */
  private void loadImages() {
    
//    Mode mode = editor.getMode();
//    int res = Toolkit.isRetina() ? 2 : 1;
//    String suffix = "-2x.png";  // wishful thinking
//    // Some modes may not have a 2x version. If a mode doesn't have a 1x
//    // version, this will cause an error... they should always have 1x.
//    if (res == 2) {
//      if (!mode.getContentFile(tabFile(0, 0) + suffix).exists()) {
//        res = 1;
//      }
//    }
//    if (res == 1) {
//      suffix = ".png";
//      if (!mode.getContentFile(tabFile(0, 0) + suffix).exists()) {
//        suffix = ".gif";
//      }
//    }
//
//    pieces = new Image[STATUS.length][WHERE.length];
//    for (int status = 0; status < STATUS.length; status++) {
//      for (int where = 0; where < WHERE.length; where++) {
//        //String filename = "theme/tab-" + STATUS[i] + "-" + WHERE[j] + ".gif";
//        pieces[status][where] = mode.loadImage(tabFile(status, where) + suffix);
//      }
//    }

    if (tabArrow == null) {
      String suffix = sadapter.isHiResRetina() ? "-2x.png" : ".png";
      tabArrow = Toolkit.getLibImage("tab-arrow" + suffix);
    }

//    bgColor = sadapter.getBgColor();
//    hiding = sadapter.getHiding();
//    
//    hideColor = sadapter.getHideColor();
//
//    textColor[SELECTED] = sadapter.getTextSelectedColor();
//    textColor[UNSELECTED] = sadapter.getTextUnselectedColor();
//
//    font = sadapter.getFont();
//    tabColor[SELECTED] = sadapter.getTabSelectedColor();
//    tabColor[UNSELECTED] = sadapter.getTabUnselectedColor();
//
//    modifiedColor = sadapter.getModifiedOrSelectionColor();
  }

  /**
   * Kinda draws component.
   * Only if there's {@link #mSketch} set.
   * 
   * <p>
   * PRECONDITIONS:
   * assumes that mSketch is NOT null.
   * 
   * </p>
   * 
   * @param screen shouldn't be NULL, but I guess it MAY NOT be null.
   * 
   */
  public void paintComponent(Graphics screen) {
    if (screen == null) return;
    
    if (mSketch == null) {
      // shouldn't we draw some kind of placeholder instead?
      // TODO: add drawing of placeholder.
      return;  // ??
    }

    
    offscreen = setupOrUpdateOffscreenBuffer(offscreen, getSize());
    // checking and setting some dimensions?

    Graphics g = offscreen.getGraphics();
    g.setFont(sadapter.getFont());  // need to set this each time through
//    metrics = g.getFontMetrics();
//    fontAscent = metrics.getAscent();
    if (fontAscent == 0) {
      fontAscent = (int) Toolkit.getAscent(g);
    }

    Graphics2D g2 = (Graphics2D) g;

    if (sadapter.isHiResRetina()) {
      // scale everything 2x, will be scaled down when drawn to the screen
      g2.scale(2, 2);
      if (sadapter.useAntialiasing() ) {

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      }
    } else {

      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // set the background for the offscreen
    g.setColor(sadapter.getHiding() ? sadapter.getHideColor() : sadapter.getBgColor() );
    g.fillRect(0, 0, imageW, imageH);

    if (!sadapter.getHiding() ) {
      // TODO: fix the situation with this background redrawal.
      // redraws background of mode or smth.
      // honestly I do not really understand what is this line doing.
      //editor.getMode().drawBackground(g, sadapter.getGridSize() );
      log("We were supposed to redraw background of mode or smth. But i commented out that line.");
    }

    // this one recreates tabs. 
    // This is shit approach, mainly because we poll sketch upon every repaint.
    // it kinda works because it works.
    
    // here we recreate tab objects. 
    // and because of unknown reason create empty visitOrder array
    // 
    if (tabs.length != mSketch.getCodeCount()) {
      tabs = new Tab[mSketch.getCodeCount()];
      for (int i = 0; i < tabs.length; i++) {
        tabs[i] = new Tab(i);
      }
      visitOrder = new Tab[mSketch.getCodeCount() - 1];
    }

    // sets up some kind of coordinates and dimensions.
    int leftover =
      ARROW_GAP_WIDTH + ARROW_WIDTH + MARGIN_WIDTH; // + SCROLLBAR_WIDTH;
    int tabMax = getWidth() - leftover;

    // reset all tab positions
    // That's the trick here: if it is working or not working.
    for (Tab tab : tabs) {
      SketchCode code = mSketch.getCode(tab.getIndex());
      tab.textVisible = true;
      tab.lastVisited = code.lastVisited();

      // hide extensions for .pde files (or whatever else is the norm elsewhere
      
      // TODO: this should be decoupled from this element.
      
      boolean hide = editor.getMode().hideExtension(code.getExtension());
//      String codeName = hide ? code.getPrettyName() : code.getFileName();
      // if modified, add the li'l glyph next to the name
//      tab.text = "  " + codeName + (code.isModified() ? " \u00A7" : "  ");
//      tab.text = "  " + codeName + "  ";
      tab.text = hide ? code.getPrettyName() : code.getFileName();
      
      tab.textWidth = (int)
        sadapter.getFont().getStringBounds(tab.getText(), g2.getFontRenderContext()).getWidth();
    }

    // make sure everything can fit
    if (!placeTabs(MARGIN_WIDTH, tabMax, null)) {
      //System.arraycopy(tabs, 0, visitOrder, 0, tabs.length);
      // always show the tab with the sketch's name
//      System.arraycopy(tabs, 1, visitOrder, 0, tabs.length - 1);
      int index = 0;
      // stock the array backwards so that the rightmost tabs are closed by default
      for (int i = tabs.length - 1; i > 0; --i) {
        visitOrder[index++] = tabs[i];
      }
      Arrays.sort(visitOrder);  // sort on when visited
//      for (int i = 0; i < visitOrder.length; i++) {
//        System.out.println(visitOrder[i].index + " " + visitOrder[i].text);
//      }
//      System.out.println();

      // Keep shrinking the tabs one-by-one until things fit properly
      for (int i = 0; i < visitOrder.length; i++) {
        tabs[visitOrder[i].index].textVisible = false;
        if (placeTabs(MARGIN_WIDTH, tabMax, null)) {
          break;
        }
      }
    }// if ( !placetabs... )

    // now actually draw the tabs
    placeTabs(MARGIN_WIDTH, tabMax, g2);

    // draw the dropdown menu target
    menuLeft = tabs[tabs.length - 1].right + ARROW_GAP_WIDTH;
    menuRight = menuLeft + ARROW_WIDTH;
    int arrowY = (getHeight() - TAB_HEIGHT - TAB_STRETCH) + (TAB_HEIGHT - ARROW_HEIGHT)/2;
    g.drawImage(tabArrow, menuLeft, arrowY,
                ARROW_WIDTH, ARROW_HEIGHT, null);
//    g.drawImage(pieces[popup.isVisible() ? SELECTED : UNSELECTED][MENU],
//                menuLeft, 0, null);

    screen.drawImage(offscreen, 0, 0, imageW, imageH, null);
  }


  /**
   * On first run sets up offscreen buffer, on further runs just makes sure
   * that buffer will fit the current dimensions of the component.
   * 
   * @param offscreen NULL in case we need to create new object (eg. on first run).
   *        or existing offscreen which will be enlarged may it be necessary.
   *                  
   * @param size current dimension of the component. Mind that it may be
   *        less or larger than the last dimensions.
   * @return
   */
  private Image setupOrUpdateOffscreenBuffer(Image offscreen, Dimension size) {
    if ((size.width != sizeW) || (size.height != sizeH)) {
      // component has been resized (or this is first time we call #paintComponent())

      if ((size.width > imageW) || (size.height > imageH)) {
        // nix the image and recreate, it's too small
        offscreen = null;

      } else {
        // if the image is larger than necessary, no need to change
        sizeW = size.width;
        sizeH = size.height;
      }
    }

    // creates off-screen surface (according to whether
    // we are on RetinaScreen or not.
    if (offscreen == null) {
      sizeW = size.width;
      sizeH = size.height;
      imageW = sizeW;
      imageH = sizeH;
      if (sadapter.isHiResRetina()) {
        offscreen = createImage(imageW*2, imageH*2);
      } else {
        offscreen = createImage(imageW, imageH);
      }
    }
    return offscreen;
  }


  /**
   * Lays out tabs and draws them to the graphics.
   * 
   * 
   * @precondition assumes that {@link #tabs} and {@link Sketch#getCodeCount()} are the same length.
   * As it only uses sketch to figure out whether the tab is selected or not,
   * why don't we keep the selected status of the tab, inside of the actual tab?
   *  
   * @param left looks like minim coordinate it should be drawing
   * @param right looks like maximum coordinate it can take
   * @param g may be NULL if we want to avoid actual drawing, but just want to layout tabs.
   * @return true if we managed to fit them, false when didn't manage.
   */
  private static boolean placeTabs(int left, int right, Graphics2D g, Tab[] tabs, SettingsAdapter sadapter) 
  {
    
    int x = left;

    final int bottom = getHeight() - TAB_STRETCH;
    final int top = bottom - TAB_HEIGHT;
    
    GeneralPath path = null;
    
    // why does it have to 
    // recreate tabs again?
    
    // we iterate over all SCs
    for (int i = 0; i < tabs.length ; i++) {
//      SketchCode code = mSketch.getCode(i);
      Tab tab = tabs[i];

//      int pieceCount = 2 + (tab.textWidth / PIECE_WIDTH);
//      if (tab.textVisible == false) {
//        pieceCount = 4;
//      }
//      int pieceWidth = pieceCount * PIECE_WIDTH;

//      int state = (code == mSketch.getCurrentCode()) ? SELECTED : UNSELECTED;
      ESelectedStatus status = tab.getSelectedStatus();
//      int state = ( status == ESelectedStatus.SELECTED) ? SELECTED : UNSELECTED;
      
      if (g != null) {
        // What are we drawing here?
        //g.drawImage(pieces[state][LEFT], x, 0, PIECE_WIDTH, PIECE_HEIGHT, null);
        path = new GeneralPath();
        path.moveTo(x, bottom);
        path.lineTo(x, top + NOTCH);
        path.lineTo(x + NOTCH, top);
      }
      
      tab.left = x;
      x += TEXT_MARGIN;
//      x += PIECE_WIDTH;

//      int contentLeft = x;
//      for (int j = 0; j < pieceCount; j++) {
//        if (g != null) {
//          g.drawImage(pieces[state][MIDDLE], x, 0, PIECE_WIDTH, PIECE_HEIGHT, null);
//        }
//        x += PIECE_WIDTH;
//      }
//      if (g != null) {
      int drawWidth = tab.isTextVisible() ? tab.getTextWidth() : sadapter.getContractedTabWidth();
        x += drawWidth + TEXT_MARGIN;
//        path.moveTo(x, top);
//      }
      tab.right = x;

      if (g != null) {
        path.lineTo(x - NOTCH, top);
        path.lineTo(x, top + NOTCH);
        path.lineTo(x, bottom);
        path.closePath();
//        g.setColor(tabColor[state]);
        g.setColor(( status == ESelectedStatus.SELECTED )? sadapter.getTabSelectedColor() : sadapter.getTabUnselectedColor());
//        g.setColor(tabColor[state]);
        g.fill(path);
        // have to draw an extra outline to make things line up on retina
        g.draw(path);
        //g.drawImage(pieces[state][RIGHT], x, 0, PIECE_WIDTH, PIECE_HEIGHT, null);

        if (tab.isTextVisible()) {
          int textLeft = tab.left + ((tab.right - tab.left) - tab.getTextWidth()) / 2;
          g.setColor( status == ESelectedStatus.SELECTED ? sadapter.getTextSelectedColor() : sadapter.getTextUnselectedColor());
//          g.setColor(textColor[state]);
//          int baseline = (int) Math.ceil((sizeH + fontAscent) / 2.0);
          //int baseline = bottom - (TAB_HEIGHT - fontAscent)/2;
          int tabHeight = TAB_HEIGHT; //bottom - top;
          int baseline = top + (tabHeight + fontAscent) / 2;
          //g.drawString(sketch.code[i].name, textLeft, baseline);
          g.drawString(tab.getText(), textLeft, baseline);
//          g.drawLine(tab.left, baseline-fontAscent, tab.right, baseline-fontAscent);
//          g.drawLine(tab.left, baseline, tab.right, baseline);
        }
      
        // This variable is weird.
        if (code.isModified()) {
          g.setColor(sadapter.getModifiedOrSelectionColor());
          g.drawLine(tab.left + NOTCH, top, tab.right - NOTCH, top);
        }
      }

//      if (g != null) {
//        g.drawImage(pieces[state][RIGHT], x, 0, PIECE_WIDTH, PIECE_HEIGHT, null);
//      }
//      x += PIECE_WIDTH - 1;  // overlap by 1 pixel
      x += TAB_BETWEEN;
    }
    
    // Draw this last because of half-pixel overlaps on retina displays
    if (g != null) {
      g.setColor(tabColor[SELECTED]);
      g.fillRect(0, bottom, getWidth(), TAB_STRETCH);
    }
    
    return x <= right;
  }


  /**
   * Called when a new sketch is opened.
   */
  public void rebuild() {
    //System.out.println("rebuilding editor header");
    rebuildMenu();
    repaint();
  }


  /**
   * What the hell does this method do?
   * 
   * Looks like it is populating the context menu
   * with the names of the tabs from {@link #tabs} .
   * 
   */
  public void rebuildMenu() {
    //System.out.println("rebuilding");
    // empties existing menu or creates new
    menu = emptyOrSetupNewMenu(menu, this);
    JMenuItem item;

    // maybe this shouldn't have a command key anyways..
    // since we're not trying to make this a full ide..
    //item = Editor.newJMenuItem("New", 'T');

    /*
    item = Editor.newJMenuItem("Previous", KeyEvent.VK_PAGE_UP);
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.out.println("prev");
        }
      });
    if (editor.sketch != null) {
      item.setEnabled(editor.sketch.codeCount > 1);
    }
    menu.add(item);

    item = Editor.newJMenuItem("Next", KeyEvent.VK_PAGE_DOWN);
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.out.println("ext");
        }
      });
    if (editor.sketch != null) {
      item.setEnabled(editor.sketch.codeCount > 1);
    }
    menu.add(item);

    menu.addSeparator();
    */

    //item = new JMenuItem("New Tab");
    item = Toolkit.newJMenuItemShift("New Tab", 'N');
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // TODO: need to remove this coupling to editor from here.
          fireSelectedNewTab();
        }
      });
    menu.add(item);

    item = new JMenuItem("Rename");
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          
          // TODO: how the hell can Sketch#handleRenameCode() know
          // which tab to rename?
          fireSelectedRenameTab();

          /*
          // this is already being called by nameCode(), the second stage of rename
          if (editor.sketch.current == editor.sketch.code[0]) {
            editor.sketchbook.rebuildMenus();
          }
          */
        }
      });
    menu.add(item);

    item = new JMenuItem("Delete");
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          
          fireSelectedDelete();
          Sketch sketch = editor.getSketch();
          if (!Base.isMacOS() &&  // ok on OS X
              editor.base.editors.size() == 1 &&  // mmm! accessor
              sketch.getCurrentCodeIndex() == 0) {
              Base.showWarning("Yeah, no." ,
                               "You can't delete the last tab " +
                               "of the last open sketch.", null);
          } else {
            editor.getSketch().handleDeleteCode();
          }
        }

      });
    menu.add(item);

    menu.addSeparator();

    //  KeyEvent.VK_LEFT and VK_RIGHT will make Windows beep

    item = new JMenuItem("Previous Tab");
    KeyStroke ctrlAltLeft =
      KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Toolkit.SHORTCUT_ALT_KEY_MASK);
    item.setAccelerator(ctrlAltLeft);
    // this didn't want to work consistently
    /*
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          editor.sketch.prevCode();
        }
      });
    */
    menu.add(item);

    item = new JMenuItem("Next Tab");
    KeyStroke ctrlAltRight =
      KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Toolkit.SHORTCUT_ALT_KEY_MASK);
    item.setAccelerator(ctrlAltRight);
    /*
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          editor.sketch.nextCode();
        }
      });
    */
    menu.add(item);

    addJumperElements(menu);
    // this adds some "jumper" elements.
    // which is just a list of tabs

  }

  /**
   * Simply adds names of tabs into the menu.
   * 
   * This way the menu can contain elements, which can be clicked and "selected tab" event
   * will be called.
   * 
   * @param menu valid initialized menu, to which we will add separateor and items.
   */
  private void addJumperElements(JMenu menu) {
    // TODO Auto-generated method stub
    if ( tabs.length < 1 ){
      return;
    }
    menu.addSeparator();

    // listener for action
    ActionListener jumpListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // TODO: need to replace with some index.
        int scIndex = Integer.parseInt(e.getActionCommand());
        fireTabClicked(tabs[scIndex]);
      }
    };    
    
    for(Tab tb : tabs){
      JMenuItem item = new JMenuItem(tb.getText());
      item.setActionCommand(String.valueOf(tb.getIndex()));
      item.addActionListener(jumpListener);
      menu.add(item);
    }

  }


  /**
   * 
   */
  protected void fireSelectedRenameTab() {
    if ( !verifyAndLogListenerAvailable() ){  return;   }
    
    mOnTabClickedListener.onSelectedRename(this);
    
  }


  protected void fireSelectedNewTab() {
    // TODO Auto-generated method stub
    if ( !verifyAndLogListenerAvailable() ){  return;   }
    mOnTabClickedListener.onSelectedNewMenuElement(this);
  }


  /**
   * When menu doesn't exist, will set up the new one and
   * attach Popup to the given component 
   * 
   * When menu does exit, will simply clear it.
   * 
   * @param menu NULL when new menu should be created and popup attached, 
   *        valid menu if it simply needs to be cleared.
   *        
   * @param comp reference to component which will receive the popup
   * @return valid clear menu, which is attached via popup to the component.
   */
  private static JMenu emptyOrSetupNewMenu(JMenu menu, JComponent comp) {
    if (menu != null) {
      menu.removeAll();

    } else {
      menu = new JMenu();
      JPopupMenu popup = menu.getPopupMenu();
      comp.add(popup);
      popup.setLightWeightPopupEnabled(true);

      /*
      popup.addPopupMenuListener(new PopupMenuListener() {
          public void popupMenuCanceled(PopupMenuEvent e) {
            // on redraw, the isVisible() will get checked.
            // actually, a repaint may be fired anyway, so this
            // may be redundant.
            repaint();
          }

          public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
          public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
        });
      */
    }   
    return menu;
  }



  public void deselectMenu() {
    repaint();
  }


  public Dimension getPreferredSize() {
    return getMinimumSize();
  }


  public Dimension getMinimumSize() {
//    if (Base.isMacOS()) {
    return new Dimension(sadapter.getMinimumWidth(), sadapter.getGridSize() );
//    }
//    return new Dimension(300, Preferences.GRID_SIZE - 1);
  }


  public Dimension getMaximumSize() {
//    if (Base.isMacOS()) {
    return new Dimension(sadapter.getMaximumWidth(), sadapter.getGridSize());
//    }
//    return new Dimension(3000, Preferences.GRID_SIZE - 1);
  }


  // . . . . . . . . . . . . . . .     UTILITIES          . . . . . . . . . . . . .
  // . . . . . . . . . . . . . . .     UTILITIES          . . . . . . . . . . . . .
  // . . . . . . . . . . . . . . .     UTILITIES          . . . . . . . . . . . . .

  /**
   * Utility method, which simply checks whether the param is 
   * null and throws IllegalArgumentException then
   * @param param
   * @param paramName
   * @throws IllegalArgumentException when param is null.
   */
  private static void verifyNotNullOrThrow(Object param,
                                    String paramName) {
    if ( param == null ){
      throw new IllegalArgumentException("Parameter " + paramName + " cannot be null.");
    }
    
  }
  
  
  
  @Override
  public String toString() {
    // TODO: make better to string.
    return "EditorHeader: {" + tabs.length + " tabs contains }";
  }
}
