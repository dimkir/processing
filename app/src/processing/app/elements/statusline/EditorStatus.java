/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-10 Ben Fry and Casey Reas
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

package processing.app.elements.statusline;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import processing.app.Base;
import processing.app.Editor;
import processing.app.Mode;
import processing.app.Preferences;
import processing.app.Toolkit;


//TODO: **** need to decouple this class from static coupling to Preferences

/**
 * Panel just below the editing area that contains status messages.
 * 
 * This is basically status line with ability to show yes/no buttons and ask for text input.
 * 
 * <p>
 * What's the mechanics of this status line? 
 * -it can change bg color.
 * </p>
 */
public class EditorStatus extends JPanel {
  private   Color[] bgcolor;
  private   Color[] fgcolor;

  private static final int NOTICE = 0;
  private static final int ERR    = 1;
  //static final int PROMPT = 2;
  //static final int EDIT   = 3;
  private static final int EDIT   = 2;

  private static final int YES    = 1;
  private static final int NO     = 2;
  private static final int CANCEL = 3;
  private static final int OK     = 4;

  private static final String NO_MESSAGE = "";

  // TODO: need to decouple this method from this variable.
  private Editor editor;

  private int statusLineMode;
  private String message;

  private Font font;
  private FontMetrics metrics;
  private int ascent;

  private Image offscreen;
  private int sizeW, sizeH;

  private JButton cancelButton;
  private JButton okButton;
  private JTextField editField;

  private int response;

  /**
   * @deprecated is used by unused components.
   */
  private boolean mIndeterminate;
  
  /**
   * @deprecated is used by unused components.
   */
  private Thread mThread;


  /**
   * Initializes components.
   * 
   * @param editor ?? as we're tightly coupled to Editor, in the constructor we pass the parameter of the editor.
   */
  public EditorStatus(Editor editor) {
    this.editor = editor;
    empty();
    updateMode();
  }

  /**
   * As each mode has it's own color, this one
   * is updating colors of the EditorStatus to teh colors of the mode.
   */
  public void updateMode() {
    Mode mode = editor.getMode();
    bgcolor = new Color[] {
      mode.getColor("status.notice.bgcolor"),
      mode.getColor("status.error.bgcolor"),
      mode.getColor("status.edit.bgcolor")
    };

    fgcolor = new Color[] {
      mode.getColor("status.notice.fgcolor"),
      mode.getColor("status.error.fgcolor"),
      mode.getColor("status.edit.fgcolor")
    };

    font = mode.getFont("status.font");
    metrics = null;
  }

  /**
   * Sets status line to NOTICE mode and sets message to NO_MESSAGE.
   * Also calls {@link #repaint()}
   */
  public void empty() {
    statusLineMode = NOTICE;
    message = NO_MESSAGE;
    repaint();
  }


  /**
   * Sets status line into NOTICE mode and sets message to the 
   * value of message parameter.
   * Also forces {@link #repaint()} 
   * 
   * @param message
   */
  public void notice(String message) {
    statusLineMode = NOTICE;
    this.message = message;
    repaint();
  }


  public void unnotice(String unmessage) {
    if (message.equals(unmessage)) empty();
  }


  public void error(String message) {
    statusLineMode = ERR;
    this.message = message;
    repaint();
  }


  /**
   * Basically shows filename input prompt within the status line.
   * The completion of the input, is wired to {@link Sketch#nameCode()} or smth like this.
   * 
   * @param message
   * @param dflt
   */
  public void edit(String message, String dflt) {
    statusLineMode = EDIT;
    this.message = message;

    response = 0;
    okButton.setVisible(true);
    cancelButton.setVisible(true);
    editField.setVisible(true);
    editField.setText(dflt);
    editField.selectAll();
    editField.requestFocusInWindow();

    repaint();
  }

  
  /**
   * Hides all "edit-related" controls and returns focus to editors textarea.
   * Also resets to "empty" state the control.
   */
  public void unedit() {
    okButton.setVisible(false);
    cancelButton.setVisible(false);
    editField.setVisible(false);
    // TODO: need to figure out how to solve this one? See below considerations.
    // honesly unedit should't really play with the focus
    // There should be an event, which says "edit-finished" and it may be "successful" or "not",
    // but the caller will be the one who knows that and will be the one who
    // will be deciding to which control it should return focus after "edit" is finished.
    editor.textarea.requestFocusInWindow();
    empty();
  }


  /**
   * WTF is this?? How can we be repainting 
   * from another thread?
   * @deprecated it looks to be used by unused caller methods.
   */
  public void startIndeterminate() {
    mIndeterminate = true;
    mThread = new Thread() {
      public void run() {
        while (Thread.currentThread() == mThread) {
          repaint();
          try {
            Thread.sleep(1000 / 10);
          } catch (InterruptedException e) { }
        }
      }
    };
    mThread.start();
  }


  /**
   * 
   * @deprecated called by unused caller.
   */
  public void stopIndeterminate() {
    mIndeterminate = false;
    mThread = null;
    repaint();
  }


  public void paintComponent(Graphics screen) {
    // TODO: it's weird that initialization of the parts of the component are made in paintComponent()
    if (okButton == null) setup();

    Dimension size = getSize();
    if ((size.width != sizeW) || (size.height != sizeH)) {
      // component has been resized
      offscreen = null;
    }

    if (offscreen == null) {
      sizeW = size.width;
      sizeH = size.height;
      setButtonBounds();
      if (Toolkit.highResDisplay()) {
        offscreen = createImage(sizeW*2, sizeH*2);
      } else {
        offscreen = createImage(sizeW, sizeH);
      }
    }

    Graphics g = offscreen.getGraphics();

    Graphics2D g2 = (Graphics2D) g;
    if (Toolkit.highResDisplay()) {
      g2.scale(2, 2);
      if (Base.isUsableOracleJava()) {
        // Oracle Java looks better with anti-aliasing turned on
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      }
    } else {
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    g.setFont(font);
    if (metrics == null) {
      metrics = g.getFontMetrics();
      ascent = metrics.getAscent();
    }

    //setBackground(bgcolor[mode]);  // does nothing

    g.setColor(bgcolor[statusLineMode]);
    g.fillRect(0, 0, sizeW, sizeH);

    g.setColor(fgcolor[statusLineMode]);
    g.setFont(font); // needs to be set each time on osx
    // TODO: need to remove coupling with Preferences
    g.drawString(message, Preferences.GUI_SMALL, (sizeH + ascent) / 2);

    if (mIndeterminate) {
      int x = cancelButton.getX();
      int w = cancelButton.getWidth();
      int y = getHeight() / 3;
      int h = getHeight() / 3;
//      int y = cancelButton.getY();
//      int h = cancelButton.getHeight();
//      g.setColor(fgcolor[mode]);
//      g.setColor(Color.DARK_GRAY);
      g.setColor(new Color(0x80000000, true));
      g.drawRect(x, y, w, h);
      for (int i = 0; i < 10; i++) {
        int r = (int) (x + Math.random() * w);
        g.drawLine(r, y, r, y+h);
      }
    }

    screen.drawImage(offscreen, 0, 0, sizeW, sizeH, null);
  }


  /**
   * Creates buttons and JTextField and hooks up listeners.
   * 
   */
  protected void setup() {
    if (okButton == null) {
      cancelButton = new JButton(Preferences.PROMPT_CANCEL);
      okButton = new JButton(Preferences.PROMPT_OK);

      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (statusLineMode == EDIT) {
            unedit();
            //editor.toolbar.clear();
          }
        }
      });

      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // answering to rename/new code question
          if (statusLineMode == EDIT) {  // this if() isn't (shouldn't be?) necessary
            String answer = editField.getText();
            editor.getSketch().nameCode(answer);
            unedit();
          }
        }
      });

      // !@#(* aqua ui #($*(( that turtle-neck wearing #(** (#$@)(
      // os9 seems to work if bg of component is set, but x still a bastard
      if (Base.isMacOS()) {
        //yesButton.setBackground(bgcolor[EDIT]);
        //noButton.setBackground(bgcolor[EDIT]);
        cancelButton.setBackground(bgcolor[EDIT]);
        okButton.setBackground(bgcolor[EDIT]);
      }
      setLayout(null);

      add(cancelButton);
      add(okButton);

      cancelButton.setVisible(false);
      okButton.setVisible(false);

      editField = new JTextField();
      // disabling, was not in use
      //editField.addActionListener(this);

      //if (Base.platform != Base.MACOSX) {
      editField.addKeyListener(new KeyAdapter() {

          // Grab ESC with keyPressed, because it's not making it to keyTyped
          public void keyPressed(KeyEvent event) {
            if (event.getKeyChar() == KeyEvent.VK_ESCAPE) {
              unedit();
              //editor.toolbar.clear();
              event.consume();
            }
          }

          // use keyTyped to catch when the feller is actually
          // added to the text field. with keyTyped, as opposed to
          // keyPressed, the keyCode will be zero, even if it's
          // enter or backspace or whatever, so the keychar should
          // be used instead. grr.
          public void keyTyped(KeyEvent event) {
            //System.out.println("got event " + event);
            int c = event.getKeyChar();

            if (c == KeyEvent.VK_ENTER) {  // accept the input
              String answer = editField.getText();
              editor.getSketch().nameCode(answer);
              unedit();
              event.consume();

              // easier to test the affirmative case than the negative
            } else if ((c == KeyEvent.VK_BACK_SPACE) ||
                       (c == KeyEvent.VK_DELETE) ||
                       (c == KeyEvent.VK_RIGHT) ||
                       (c == KeyEvent.VK_LEFT) ||
                       (c == KeyEvent.VK_UP) ||
                       (c == KeyEvent.VK_DOWN) ||
                       (c == KeyEvent.VK_HOME) ||
                       (c == KeyEvent.VK_END) ||
                       (c == KeyEvent.VK_SHIFT)) {
              // these events are ignored

              /*
            } else if (c == KeyEvent.VK_ESCAPE) {
              unedit();
              editor.toolbar.clear();
              event.consume();
              */

            } else if (c == KeyEvent.VK_SPACE) {
              String t = editField.getText();
              int start = editField.getSelectionStart();
              int end = editField.getSelectionEnd();
              editField.setText(t.substring(0, start) + "_" +
                                t.substring(end));
              editField.setCaretPosition(start+1);
              event.consume();

            } else if ((c == '_') || (c == '.') ||  // allow .pde and .java
                       ((c >= 'A') && (c <= 'Z')) ||
                       ((c >= 'a') && (c <= 'z'))) {
              // these are ok, allow them through

            } else if ((c >= '0') && (c <= '9')) {
              // getCaretPosition == 0 means that it's the first char
              // and the field is empty.
              // getSelectionStart means that it *will be* the first
              // char, because the selection is about to be replaced
              // with whatever is typed.
              if ((editField.getCaretPosition() == 0) ||
                  (editField.getSelectionStart() == 0)) {
                // number not allowed as first digit
                //System.out.println("bad number bad");
                event.consume();
              }
            } else {
              event.consume();
              //System.out.println("code is " + code + "  char = " + c);
            }
            //System.out.println("code is " + code + "  char = " + c);
          }
        });
      add(editField);
      editField.setVisible(false);
    }
  }


  /**
   * Wtf? 
   * Why do we need to set button bounds?
   */
  protected void setButtonBounds() {
    int top = (sizeH - Preferences.BUTTON_HEIGHT) / 2;
    int eachButton = Preferences.GUI_SMALL + Preferences.BUTTON_WIDTH;

    int cancelLeft = sizeW      - eachButton;
    int noLeft     = cancelLeft - eachButton;
    int yesLeft    = noLeft     - eachButton;

    //yesButton.setLocation(yesLeft, top);
    //noButton.setLocation(noLeft, top);
    cancelButton.setLocation(cancelLeft, top);
    okButton.setLocation(noLeft, top);

    //yesButton.setSize(Preferences.BUTTON_WIDTH, Preferences.BUTTON_HEIGHT);
    //noButton.setSize(Preferences.BUTTON_WIDTH, Preferences.BUTTON_HEIGHT);
    cancelButton.setSize(Preferences.BUTTON_WIDTH, Preferences.BUTTON_HEIGHT);
    okButton.setSize(Preferences.BUTTON_WIDTH, Preferences.BUTTON_HEIGHT);

    // edit field height is awkward, and very different between mac and pc,
    // so use at least the preferred height for now.
    int editWidth = 2*Preferences.BUTTON_WIDTH;
    int editHeight = editField.getPreferredSize().height;
    int editTop = (1 + sizeH - editHeight) / 2;  // add 1 for ceil
    editField.setBounds(yesLeft - Preferences.BUTTON_WIDTH, editTop,
                        editWidth, editHeight);
  }


  public Dimension getPreferredSize() {
    return getMinimumSize();
  }


  public Dimension getMinimumSize() {
    return new Dimension(300, Preferences.GRID_SIZE);
  }


  public Dimension getMaximumSize() {
    return new Dimension(3000, Preferences.GRID_SIZE);
  }


  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == cancelButton) {
      if (statusLineMode == EDIT) unedit();
      //editor.toolbar.clear();

    } else if (e.getSource() == okButton) {
      // answering to rename/new code question
      if (statusLineMode == EDIT) {  // this if() isn't (shouldn't be?) necessary
        String answer = editField.getText();
        
        // OMG, this is a nightmare!!!!!
        editor.getSketch().nameCode(answer);
        unedit();
      }
    }
  }
}
