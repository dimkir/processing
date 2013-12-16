/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  SketchCode - data class for a single file inside a sketch
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-11 Ben Fry and Casey Reas
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

package processing.app.elements.sketch;


// also coupled to Base



import java.io.File;
import java.io.IOException;


// why do we have this shit here?
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import processing.app.Base;
import processing.app.elements.statusline.EditorStatus;


/**
 * Represents a single tab of a sketch.
 * 
 * Backed by:
 * -file on the filesystem
 * -?Swing.text.Document
 * -String in memory containing the program
 * Which creates reasonable question:
 *  HOW DO WE KEEP THEM SYNCHED?
 * 
 * <p>
 * That's a very confusing abstraction: if it is "tab" in the editor, then it shoudl 
 * be coupled to editor. If it is "file of a sketch" it should be independent from the 
 * editor. 
 * In that way, both of the abstractions will follow SRP and have high level of cohesion
 * inside of them.
 * If we leave it now, it's more likely EditorTabWSketch as opposed to "sketch code".
 * </p>
 * 
 * <p>
 * I want some "self-sufficient" classes, which do not dependent on external states 
 * (or at least dependency on external states is reduced and localized).
 * 
 * Also I do not see any "general" error handling policy here.  Are we throwing 
 * checked exceptions? Are we throwing unchecked exceptions?
 * 
 * </p>
 * 
 */
public class SketchCode {
  /** Pretty name (no extension), not the full file name */
  private String prettyName;

  /** File object for where this code is located */
  private File file;

  /** Extension for this file (no dots, and in lowercase). */
  private String extension;

  /** Text of the program text for this tab */
  private String program;

  /** Last version of the program on disk. */
  private String savedProgram;

  /** Document object for this tab. Currently this is a SyntaxDocument. */
  private Document document;

  /** Last time this tab was visited */
  // TODO: FFS! This is even a package variable
  long visited;

  /**
   * Undo Manager for this tab, each tab keeps track of their own
   * Editor.undo will be set to this object when this code is the tab
   * that's currently the front.
   */
  private UndoManager undo = new UndoManager();

  /** What was on top of the undo stack when last saved. */
//  private UndoableEdit lastEdit;

  // saved positions from last time this tab was used
  // but it's funny that this is the state which belongs to EDITOR, not 
  // the sketch? 
  private int selectionStart;
  private int selectionStop;
  private int scrollPosition;

  /** This variable is  pure 'property' variable. But the getter is pretty popular */
  private boolean modified;

  /** name of .java file after preproc */
//  private String preprocName;
  /** where this code starts relative to the concat'd code */
  private int preprocOffset;


  /**
   * What is the "creation" policy of this object? 
   * Should it be path to existing file? Will it fail if the file doesn't exist? Or will
   * it create the file if it doesn't exist?
   * 
   * @throws Unfortunately this method swallows IOException. Will this sketch code 
   *          class become then invalid? Or it will be valid?
   * 
   * @param file
   * @param extension
   */
  public SketchCode(File file, String extension) {
    this.file = file;
    this.extension = extension;

    makePrettyName();

    try {
      load();
    } catch (IOException e) {
      System.err.println("Error while loading code " + file.getName());
      // so at this point, the SketchCode class will become invalid?
    }
  }


  // TODO: why not make it static?
  protected void makePrettyName() {
    prettyName = file.getName();
    int dot = prettyName.indexOf('.');
    prettyName = prettyName.substring(0, dot);
  }


  /**
   * It just returns name of the backing file.
   * <p>
   * Which version of the file does this return? Last saved?
   * </p>
   * 
   * @return
   */
  public File getFile() {
    return file;
  }


  /**
   * Returns whether backing file exists.
   * 
   * @return
   */
  protected boolean fileExists() {
    return file.exists();
  }


  /**
   * Returns if the backing file is read-only.
   * @return
   */
  protected boolean fileReadOnly() {
    return !file.canWrite();
  }

  /**
   * Attempts to delete the backing file.
   * @return true when file was deleted.
   */
  protected boolean deleteFile() {
    return file.delete();
  }


  /**
   * Attempts to rename backing file, updates pretty name
   * on success.
   * 
   * @param what new name
   * @param ext ??wtf is this? this is new setting of ext. which is error prone.
   * 
   * @return true when rename was successful.
   */
  protected boolean renameTo(File what, String ext) {
//    System.out.println("renaming " + file);
//    System.out.println("      to " + what);
    boolean success = file.renameTo(what);
    if (success) {
      this.file = what;  // necessary?
      this.extension = ext;
      makePrettyName();
    }
    return success;
  }


  /**
   * Attempts to save string buffer 'program' into dest file.
   * 
   * <p>
   * Under the hood uses Base.saveFile()  
   * which I don't know what properties have.
   * </p>
   * 
   * @param dest
   * @throws IOException when there's error saving.
   */
  public void copyTo(File dest) throws IOException {
    Base.saveFile(program, dest);
  }


  /**
   * Returns ONLY name of the backing file (without path).
   * 
   * @return
   */
  public String getFileName() {
    return file.getName();
  }


  /**
   * Getter for {@link #prettyName}.
   * 
   * @return
   */
  public String getPrettyName() {
    return prettyName;
  }


  /**
   * Getter for {@link #extension}.
   * @return
   */
  public String getExtension() {
    return extension;
  }


  public boolean isExtension(String what) {
    return extension.equals(what);
  }


  /** 
   * Gets contents of the {@link #program} buffer.
   *
   **/
  public String getProgram() {
    return program;
  }


  /**
   * Sets contents of the {@link #program} buffer.
   * 
   * @param replacement
   */
  public void setProgram(String replacement) {
    program = replacement;
  }


  /** get the last version saved of this tab */
  public String getSavedProgram() {
    return savedProgram;
  }


  /**
   * Gets count of lines in the {@link #program} buffer.
   * 
   * @return
   */
  public int getLineCount() {
    return Base.countLines(program);
  }


  /**
   * Setter for the {@link #modified} flag.
   * 
   * @param modified
   */
  public void setModified(boolean modified) {
    this.modified = modified;
  }


  /**
   * Getter for the {@link #modified} flag.
   * 
   * @return
   */
  public boolean isModified() {
    return modified;
  }


//  public void setPreprocName(String preprocName) {
//    this.preprocName = preprocName;
//  }
//
//
//  public String getPreprocName() {
//    return preprocName;
//  }


  public void setPreprocOffset(int preprocOffset) {
    this.preprocOffset = preprocOffset;
  }


  public int getPreprocOffset() {
    return preprocOffset;
  }


  public void addPreprocOffset(int extra) {
    preprocOffset += extra;
  }


  public Document getDocument() {
    return document;
  }


  public void setDocument(Document d) {
    document = d;
  }


  public UndoManager getUndo() {
    return undo;
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  // TODO these could probably be handled better, since it's a general state
  // issue that's read/write from only one location in Editor (on tab switch.)


  public int getSelectionStart() {
    return selectionStart;
  }


  public int getSelectionStop() {
    return selectionStop;
  }


  public int getScrollPosition() {
    return scrollPosition;
  }


  protected void setState(String p, int start, int stop, int pos) {
    program = p;
    selectionStart = start;
    selectionStop = stop;
    scrollPosition = pos;
  }


  /**
   * Returns time when the sketch was last visited.
   * 
   * <p>
   * ATMK Is used ONLY by the @link {@link EditorStatus} probably to sort
   * elements? 
   *
   * </p>
   * @return
   */
  public long lastVisited() {
    return visited;
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /**
   * Load this piece of code from a file.
   */
  public void load() throws IOException {
    program = Base.loadFile(file);

    // Remove NUL characters because they'll cause problems,
    // and their presence is very difficult to debug.
    // https://github.com/processing/processing/issues/1973
    if (program.indexOf('\0') != -1) {
      program = program.replaceAll("\0", "");
    }
    savedProgram = program;

    // This used to be the "Fix Encoding and Reload" warning, but since that
    // tool has been removed, it just rambles about text editors and encodings.
    if (program.indexOf('\uFFFD') != -1) {
      System.err.println(file.getName() + " contains unrecognized characters.");
      System.err.println("You should re-open " + file.getName() +
                         " with a text editor,");
      System.err.println("and re-save it in UTF-8 format. Otherwise, you can");
      System.err.println("delete the bad characters to get rid of this warning.");
      System.err.println();
    }

    setModified(false);
  }


  /**
   * Save this piece of code, regardless of whether the modified
   * flag is set or not.
   */
  public void save() throws IOException {
    // TODO re-enable history
    //history.record(s, SketchHistory.SAVE);

    Base.saveFile(program, file);
    savedProgram = program;
    setModified(false);
  }


  /**
   * Save this file to another location, used by Sketch.saveAs()
   */
  public void saveAs(File newFile) throws IOException {
    Base.saveFile(program, newFile);
    savedProgram = program;
    file = newFile;
    makePrettyName();
    setModified(false);
  }


  /**
   * Called when the sketch folder name/location has changed. Called when
   * renaming tab 0, the main code.
   */
  public void setFolder(File sketchFolder) {
    file = new File(sketchFolder, file.getName());
  }
}
