/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
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

import java.awt.FileDialog;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import processing.app.Base;
import processing.app.Editor;
import processing.app.Mode;
import processing.app.Preferences;
import processing.core.PApplet;




/**
 * Stores information about files in the current sketch.
 * 
 * <p>
 * I wonder how exactly this works? The easiest way to figure it out, is probably
 * to try to reuse this sketch.
 * 
 * 
 * What are the bounds of this abstraction? Is there clear line between
 * which states belong to sketch and which states belong to Editor? It seems to me
 * that Editor/Sketch are too tightly coupled and some more of "loose coupling" may
 * be introduced.
 * </p>
 * 
 */
public class Sketch {
  
  /**
   * Why should Sketch hold the reference to Editor? 
   * Sketch should only care about it's own status and 
   * should be NOT coupled to the editor.
   */
  private Editor editor;
  
  /**
   * How is mode reference used here?
   * Can we extract some?
   */
  private Mode mode;

  
  /** main pde file for this sketch. */
  private File primaryFile;

  /**
   * Name of sketch, which is the name of main file
   * (without .pde or .java extension)
   */
  private String name;

  /** true if any of the files have been modified. */
  private boolean modified;

  /** folder that contains this sketch */
  private File folder;

  /** data folder location for this sketch (may not exist yet) */
  private File dataFolder;

  /** code folder location for this sketch (may not exist yet) */
  private File codeFolder;

  /**
   * ?
   */
  private SketchCode current;
  
  /**
   * ?
   */
  private int currentIndex;
  /**
   * Number of sketchCode objects (tabs) in the current sketch. Note that this
   * will be the same as code.length, because the getCode() method returns
   * just the code[] array, rather than a copy of it, or an array that's been
   * resized to just the relevant files themselves.
   * http://dev.processing.org/bugs/show_bug.cgi?id=940
   */
  private int codeCount;
  private SketchCode[] code;

  /** Moved out of Editor and into here for cleaner access. */
  private boolean untitled;

//  /** Class path determined during build. */
//  private String classPath;
//
//  /**
//   * This is *not* the "Processing" libraries path, this is the Java libraries
//   * path, as in java.library.path=BlahBlah, which identifies search paths for
//   * DLLs or JNILIBs. (It's Java's LD_LIBRARY_PATH, for you UNIX fans.)
//   */
//  private String javaLibraryPath;
//
//  /**
//   * List of library folders, set up in the preprocess() method.
//   */
//  private ArrayList<Library> importedLibraries;
//  //private ArrayList<File> importedLibraries;

  /**
   * Most recent, default build path. This will contain the .java files that
   * have been preprocessed, as well as any .class files that were compiled.
   */
//  private File buildFolder;


  /**
   * Used by the command-line version to create a sketch object.
   * @param path location of the main .pde file
   * @param mode what flavor of sketch we're dealing with.
   * <p>
   * ZZZ Is it failable? What happens when there's an error?
   * 
   * It should be failable in case path is pointing to the non-exisitng 
   * directory?
   * Or it will just create by default empty one?
   * </p>
   */
  public Sketch(String path, Mode mode) {
    this.editor = null;
    this.mode = mode;
    load(path);
  }


  /**
   * path is location of the main .pde file, because this is also
   * simplest to use when opening the file from the finder/explorer.
   */
  public Sketch(String path, Editor editor) throws IOException {
    this.editor = editor;
    this.mode = editor.getMode();
    load(path);
  }


  /**
   * Attempts to load a sketch Loads file.
   * What's the successful factor.
   * 
   * @throws SketchRtException when something went wrong. 
   * 
   * @param path
   */
  protected void load(String path) {
    primaryFile = new File(path);
    // get the name of the sketch by chopping .pde or .java
    // off of the main file name
    String mainFilename = primaryFile.getName();
    int suffixLength = mode.getDefaultExtension().length() + 1;
    name = mainFilename.substring(0, mainFilename.length() - suffixLength);
    folder = new File(new File(path).getParent());
    load();
  }


  /**
   * Build the list of files.
   * <P>
   * Generally this is only done once, rather than
   * each time a change is made, because otherwise it gets to be
   * a nightmare to keep track of what files went where, because
   * not all the data will be saved to disk.
   * <P>
   * This also gets called when the main sketch file is renamed,
   * because the sketch has to be reloaded from a different folder.
   * <P>
   * Another exception is when an external editor is in use,
   * in which case the load happens each time "run" is hit.
   */
  protected void load() {
    codeFolder = new File(folder, "code");
    dataFolder = new File(folder, "data");

    // get list of files in the sketch folder
    String list[] = folder.list();

    // reset these because load() may be called after an
    // external editor event. (fix for 0099)
    codeCount = 0;

    code = new SketchCode[list.length];

    String[] extensions = mode.getExtensions();

    for (String filename : list) {
      // Ignoring the dot prefix files is especially important to avoid files
      // with the ._ prefix on Mac OS X. (You'll see this with Mac files on
      // non-HFS drives, i.e. a thumb drive formatted FAT32.)
      if (filename.startsWith(".")) continue;

      // Don't let some wacko name a directory blah.pde or bling.java.
      if (new File(folder, filename).isDirectory()) continue;

      // figure out the name without any extension
      String base = filename;
      // now strip off the .pde and .java extensions
      for (String extension : extensions) {
        if (base.toLowerCase().endsWith("." + extension)) {
          base = base.substring(0, base.length() - (extension.length() + 1));

          // Don't allow people to use files with invalid names, since on load,
          // it would be otherwise possible to sneak in nasty filenames. [0116]
          if (Sketch.isSanitaryName(base)) {
            code[codeCount++] =
              new SketchCode(new File(folder, filename), extension);
          }
        }
      }
    }
    // Remove any code that wasn't proper
    code = (SketchCode[]) PApplet.subset(code, 0, codeCount);

    // move the main class to the first tab
    // start at 1, if it's at zero, don't bother
    for (int i = 1; i < codeCount; i++) {
      //if (code[i].file.getName().equals(mainFilename)) {
      if (code[i].getFile().equals(primaryFile)) {
        SketchCode temp = code[0];
        code[0] = code[i];
        code[i] = temp;
        break;
      }
    }

    // sort the entries at the top
    sortCode();

    // set the main file to be the current tab
    if (editor != null) {
      setCurrentCode(0);
    }
  }


  /**
   * Reload the current sketch. Used to update the text area when
   * an external editor is in use.
   * 
   * ZZZ in case I have called this reload method, how is the Editor is supposed
   *    to get notified about the event?
   */
  public void reload() {
    // set current to null so that the tab gets updated
    // http://dev.processing.org/bugs/show_bug.cgi?id=515
    current = null;
    // nuke previous files and settings
    load();
  }


  /**
   * Replace what with what?
   * 
   * <p>
   * It is not really failable, but if there's no match,
   * then it will silently not-replace anything.
   * </p>
   * 
   * @param newCode an initialized {@link SketchCode} object
   *        whose {@link SketchCode#getFileName()} method will be 
   *        used to find amongst current sketches sketchcode with same filename.
   * <p>
   * Taking into account that 
   * </p>
   * 
   * @param newCode
   */
  protected void replaceCode(SketchCode newCode) {
    for (int i = 0; i < codeCount; i++) {
      if (code[i].getFileName().equals(newCode.getFileName())) {
        code[i] = newCode;
        break;
      }
    }
  }


  /**
   * Inserts initializes sketch code object into 
   * the sketch.
   * 
   * <p>
   * What happens if we're inserting SC with the same {@link SketchCode#getFileName()} name
   * as already in the thing.
   * </p>
   * 
   * @param newCode
   */
  protected void insertCode(SketchCode newCode) {
    // make sure the user didn't hide the sketch folder
    ensureExistence();

    // add file to the code/codeCount list, resort the list
    //if (codeCount == code.length) {
    code = (SketchCode[]) PApplet.append(code, newCode);
    codeCount++;
    //}
    //code[codeCount++] = newCode;
  }


  protected void sortCode() {
    // cheap-ass sort of the rest of the files
    // it's a dumb, slow sort, but there shouldn't be more than ~5 files
    for (int i = 1; i < codeCount; i++) {
      int who = i;
      for (int j = i + 1; j < codeCount; j++) {
        if (code[j].getFileName().compareTo(code[who].getFileName()) < 0) {
          who = j;  // this guy is earlier in the alphabet
        }
      }
      if (who != i) {  // swap with someone if changes made
        SketchCode temp = code[who];
        code[who] = code[i];
        code[i] = temp;
      }
    }
  }


  boolean renamingCode;

  /**
   * Handler for the New Code menu option.
   * 
   * ZZZ what does it mean "handle new code"? Add new tab?
   */
  public void handleNewCode() {
    // make sure the user didn't hide the sketch folder
    ensureExistence();

    // if read-only, give an error
    if (isReadOnly()) {
      // if the files are read-only, need to first do a "save as".
      Base.showMessage("Sketch is Read-Only",
                       "Some files are marked \"read-only\", so you'll\n" +
                       "need to re-save the sketch in another location,\n" +
                       "and try again.");
      return;
    }

    renamingCode = false;
    
    // TODO: WTF?? Why are we directly calling to the EDITOR??
    editor.status.edit("Name for new file:", "");
  }


  /**
   * Handler for the Rename Code menu option.
   *
   * Isn't it better to actually try to introduce preconditions
   * (renamed non empty etc) and in case they're not met, simply throw an exception
   * 
   * Attempts to rename (smth) and aborts if sketch isn't saved.
   * 
   * So WHAT is it going to rename? A sketch, a tab, a current tab? a random tab?
   * 
   * Condition to rename means that sketch cannot be untitled and should be saved.
   * 
   * Basically it doesn't even rename, it simply checks for valid
   * renaming conditions and after that just asks StatusLine to request
   * name and assumes that status line will call upon completion the necessary
   * renaming method.
   * 
   * <ul>
   * <li>Makes sure sketch folder exists
   * <li>Verifies sketch isn't untitled.
   * <li>Verifies sketch
   * </ul>
   * 
   * Can we elaborate on this one? 
   * How does it know which element to rename?
   * 
   */
  public void handleRenameCode() {
    // make sure the user didn't hide the sketch folder
    ensureExistence();

    if (currentIndex == 0 && isUntitled()) {
      Base.showMessage("Sketch is Untitled",
                       "How about saving the sketch first \n" +
                       "before trying to rename it?");
      return;
    }

    if (isModified()) {
      Base.showMessage("Save", "Please save the sketch before renaming.");
      return;
    }

    // if read-only, give an error
    if (isReadOnly()) {
      // if the files are read-only, need to first do a "save as".
      Base.showMessage("Sketch is Read-Only",
                       "Some files are marked \"read-only\", so you'll\n" +
                       "need to re-save the sketch in another location,\n" +
                       "and try again.");
      return;
    }

    // ask for new name of file (internal to window)
    // TODO maybe just popup a text area?
    renamingCode = true;
    String prompt = (currentIndex == 0) ?
      "New name for sketch:" : "New name for file:";
    String oldName = (current.isExtension("pde")) ?
      current.getPrettyName() : current.getFileName();
      // TODO: WTF again this crappy coupling.
    editor.status.edit(prompt, oldName);
  }


  /**
   * This is a very fancy method, which is called when?
   * 
   * 
   * This is called upon return from entering a new file name.
   * (that is, from either newCode or renameCode after the prompt)
   * This code is almost identical for both the newCode and renameCode
   * cases, so they're kept merged except for right in the middle
   * where they diverge.
   * 
   * @param newName is the name for ?
   */
  protected void nameCode(String newName) {
    // make sure the user didn't hide the sketch folder
    ensureExistence();

    // Add the extension here, this simplifies some of the logic below.
    if (newName.indexOf('.') == -1) {
      newName += "." + mode.getDefaultExtension();
    }

    // if renaming to the same thing as before, just ignore.
    // also ignoring case here, because i don't want to write
    // a bunch of special stuff for each platform
    // (osx is case insensitive but preserving, windows insensitive,
    // *nix is sensitive and preserving.. argh)
    if (renamingCode) {
      if (newName.equalsIgnoreCase(current.getFileName())) {
        // exit quietly for the 'rename' case.
        // if it's a 'new' then an error will occur down below
        return;
      }
    }

    newName = newName.trim();
    if (newName.equals("")) return;

    int dot = newName.indexOf('.');
    if (dot == 0) {
      Base.showWarning("Problem with rename",
                       "The name cannot start with a period.", null);
      return;
    }

    String newExtension = newName.substring(dot+1).toLowerCase();
    if (!mode.validExtension(newExtension)) {
      Base.showWarning("Problem with rename",
                       "\"." + newExtension + "\"" +
                       "is not a valid extension.", null);
      return;
    }

    // Don't let the user create the main tab as a .java file instead of .pde
    if (!mode.isDefaultExtension(newExtension)) {
      if (renamingCode) {  // If creating a new tab, don't show this error
        if (current == code[0]) {  // If this is the main tab, disallow
          Base.showWarning("Problem with rename",
                           "The first tab cannot be a ." + newExtension + " file.\n" +
                           "(It may be time for your to graduate to a\n" +
                           "\"real\" programming environment, hotshot.)", null);
          return;
        }
      }
    }

    // dots are allowed for the .pde and .java, but not in the name
    // make sure the user didn't name things poo.time.pde
    // or something like that (nothing against poo time)
    String shortName = newName.substring(0, dot);
    String sanitaryName = Sketch.sanitizeName(shortName);
    if (!shortName.equals(sanitaryName)) {
      newName = sanitaryName + "." + newExtension;
    }

    // If changing the extension of a file from .pde to .java, then it's ok.
    // http://code.google.com/p/processing/issues/detail?id=776
    // A regression introduced by Florian's bug report (below) years earlier.
    if (!(renamingCode && sanitaryName.equals(current.getPrettyName()))) {
      // Make sure no .pde *and* no .java files with the same name already exist
      // http://processing.org/bugs/bugzilla/543.html
      for (SketchCode c : code) {
        if (sanitaryName.equalsIgnoreCase(c.getPrettyName())) {
          Base.showMessage("Nope",
                           "A file named \"" + c.getFileName() + "\" already exists at\n" +
                             "\"" + folder.getAbsolutePath() + "\"");
          return;
        }
      }
    }

    File newFile = new File(folder, newName);

    if (renamingCode) {
      if (currentIndex == 0) {
        // get the new folder name/location
        String folderName = newName.substring(0, newName.indexOf('.'));
        File newFolder = new File(folder.getParentFile(), folderName);
        if (newFolder.exists()) {
          Base.showWarning("Cannot Rename",
                           "Sorry, a sketch (or folder) named " +
                           "\"" + newName + "\" already exists.", null);
          return;
        }

        // renaming the containing sketch folder
        boolean success = folder.renameTo(newFolder);
        if (!success) {
          Base.showWarning("Error", "Could not rename the sketch folder.", null);
          return;
        }
        // let this guy know where he's living (at least for a split second)
        current.setFolder(newFolder);
        // folder will be set to newFolder by updateInternal()

        // unfortunately this can't be a "save as" because that
        // only copies the sketch files and the data folder
        // however this *will* first save the sketch, then rename

        // moved this further up in the process (before prompting for the name)
//        if (isModified()) {
//          Base.showMessage("Save", "Please save the sketch before renaming.");
//          return;
//        }

        // This isn't changing folders, just changes the name
        newFile = new File(newFolder, newName);
        if (!current.renameTo(newFile, newExtension)) {
          Base.showWarning("Error",
                           "Could not rename \"" + current.getFileName() +
                           "\" to \"" + newFile.getName() + "\"", null);
          return;
        }

        // Tell each code file the good news about their new home.
        // current.renameTo() above already took care of the main tab.
        for (int i = 1; i < codeCount; i++) {
          code[i].setFolder(newFolder);
        }
        // Update internal state to reflect the new location
        updateInternal(sanitaryName, newFolder);

//        File newMainFile = new File(newFolder, newName + ".pde");
//        String newMainFilePath = newMainFile.getAbsolutePath();
//
//        // having saved everything and renamed the folder and the main .pde,
//        // use the editor to re-open the sketch to re-init state
//        // (unfortunately this will kill positions for carets etc)
//        editor.handleOpenUnchecked(newMainFilePath,
//                                   currentIndex,
//                                   editor.getSelectionStart(),
//                                   editor.getSelectionStop(),
//                                   editor.getScrollPosition());
//
//        // get the changes into the sketchbook menu
//        // (re-enabled in 0115 to fix bug #332)
//        editor.base.rebuildSketchbookMenusAsync();

      } else {  // else if something besides code[0]
        if (!current.renameTo(newFile, newExtension)) {
          Base.showWarning("Error",
                           "Could not rename \"" + current.getFileName() +
                           "\" to \"" + newFile.getName() + "\"", null);
          return;
        }
      }

    } else {  // not renaming, creating a new file
      try {
        if (!newFile.createNewFile()) {
          // Already checking for IOException, so make our own.
          throw new IOException("createNewFile() returned false");
        }
      } catch (IOException e) {
        Base.showWarning("Error",
                         "Could not create the file \"" + newFile + "\"\n" +
                         "in \"" + folder.getAbsolutePath() + "\"", e);
        return;
      }
      SketchCode newCode = new SketchCode(newFile, newExtension);
      //System.out.println("new code is named " + newCode.getPrettyName() + " " + newCode.getFile());
      insertCode(newCode);
    }

    // sort the entries
    sortCode();

    // set the new guy as current
    setCurrentCode(newName);

    // update the tabs
    editor.header.rebuild();  // this shouldn't be happening from inside the sketch
                              // the Editor should be notified of important sketch events (namely marked with call to editor in our example)
                              // and when this event occurs, the Editor should decide himself which parts 
                              // of itself it needs to update. Maybe not only header.
  }


  /**
   * Remove a piece of code from the sketch and from the disk.
   */
  public void handleDeleteCode() {
    // make sure the user didn't hide the sketch folder
    ensureExistence();

    // if read-only, give an error
    if (isReadOnly()) {
      // if the files are read-only, need to first do a "save as".
      Base.showMessage("Sketch is Read-Only",
                       "Some files are marked \"read-only\", so you'll\n" +
                       "need to re-save the sketch in another location,\n" +
                       "and try again.");
      return;
    }

    // don't allow if untitled
    if (currentIndex == 0 && isUntitled()) {  
      Base.showMessage("Cannot Delete",
                       "You can't delete a sketch that has not been saved.");
      return;
    }
    
    // confirm deletion with user, yes/no
    Object[] options = { "OK", "Cancel" };
    String prompt = (currentIndex == 0) ?
      "Are you sure you want to delete this sketch?" :
      "Are you sure you want to delete \"" + current.getPrettyName() + "\"?";
    int result = JOptionPane.showOptionDialog(editor,
                                              prompt,
                                              "Delete",
                                              JOptionPane.YES_NO_OPTION,
                                              JOptionPane.QUESTION_MESSAGE,
                                              null,
                                              options,
                                              options[0]);
    if (result == JOptionPane.YES_OPTION) {
      if (currentIndex == 0) {
        // need to unset all the modified flags, otherwise tries
        // to do a save on the handleNew()

        // delete the entire sketch
        Base.removeDir(folder);

        // get the changes into the sketchbook menu
        //sketchbook.rebuildMenus();

        // make a new sketch, and i think this will rebuild the sketch menu
        //editor.handleNewUnchecked();
        //editor.handleClose2();
        editor.base.handleClose(editor, false);

      } else {
        // delete the file
        if (!current.deleteFile()) {
          Base.showMessage("Couldn't do it",
                           "Could not delete \"" +
                           current.getFileName() + "\".");
          return;
        }

        // remove code from the list
        removeCode(current);

        // just set current tab to the main tab
        setCurrentCode(0);

        // update the tabs
        editor.header.repaint();
      }
    }
  }


  protected void removeCode(SketchCode which) {
    // remove it from the internal list of files
    // resort internal list of files
    for (int i = 0; i < codeCount; i++) {
      if (code[i] == which) {
        for (int j = i; j < codeCount-1; j++) {
          code[j] = code[j+1];
        }
        codeCount--;
        code = (SketchCode[]) PApplet.shorten(code);
        return;
      }
    }
    System.err.println("removeCode: internal error.. could not find code");
  }


  /**
   * Move to the previous tab.
   */
  public void handlePrevCode() {
    int prev = currentIndex - 1;
    if (prev < 0) prev = codeCount-1;
    setCurrentCode(prev);
  }


  /**
   * Move to the next tab.
   */
  public void handleNextCode() {
    setCurrentCode((currentIndex + 1) % codeCount);
  }


  /**
   * Sets the modified value for the code in the frontmost tab.
   */
  public void setModified(boolean state) {
    //System.out.println("setting modified to " + state);
    //new Exception().printStackTrace(System.out);
    if (current.isModified() != state) {
      current.setModified(state);
      calcModified();
    }
  }


  protected void calcModified() {
    modified = false;
    for (int i = 0; i < codeCount; i++) {
      if (code[i].isModified()) {
        modified = true;
        break;
      }
    }
    editor.header.repaint();

    if (Base.isMacOS()) {
      // http://developer.apple.com/qa/qa2001/qa1146.html
      Object modifiedParam = modified ? Boolean.TRUE : Boolean.FALSE;
      editor.getRootPane().putClientProperty("windowModified", modifiedParam);
    }
  }


  public boolean isModified() {
    return modified;
  }


  /**
   * This forces sketch to fetch text from editor and 
   * put it to sketch.
   * 
   * I wonder who's caller of this method?
   * 
   * Save all code in the current sketch. This just forces the files to save
   * in place, so if it's an untitled (un-saved) sketch, saveAs() should be
   * called instead. (This is handled inside Editor.handleSave()).
   */
  public boolean save() throws IOException {
    // make sure the user didn't hide the sketch folder
    ensureExistence();

    // first get the contents of the editor text area
//    if (current.isModified()) {
    current.setProgram(editor.getText());
//    }

    // don't do anything if not actually modified
    //if (!modified) return false;

    if (isReadOnly()) {
      // if the files are read-only, need to first do a "save as".
      Base.showMessage("Sketch is read-only",
                       "Some files are marked \"read-only\", so you'll\n" +
                       "need to re-save this sketch to another location.");
      // if the user cancels, give up on the save()
      if (!saveAs()) return false;
    }

    for (int i = 0; i < codeCount; i++) {
      if (code[i].isModified()) code[i].save();
    }
    calcModified();
    return true;
  }


  /**
   * Handles 'Save As' for a sketch.
   * <P>
   * This basically just duplicates the current sketch folder to
   * a new location, and then calls 'Save'. (needs to take the current
   * state of the open files and save them to the new folder..
   * but not save over the old versions for the old sketch..)
   * <P>
   * Also removes the previously-generated .class and .jar files,
   * because they can cause trouble.
   */
  protected boolean saveAs() throws IOException {
    String newParentDir = null;
    String newName = null;
    // TODO rewrite this to use shared version from PApplet
    final String PROMPT = "Save sketch folder as...";
    if (Preferences.getBoolean("chooser.files.native")) {
      // get new name for folder
      FileDialog fd = new FileDialog(editor, PROMPT, FileDialog.SAVE);
      if (isReadOnly() || isUntitled()) {
        // default to the sketchbook folder
        fd.setDirectory(Preferences.get("sketchbook.path"));
      } else {
        // default to the parent folder of where this was
        fd.setDirectory(folder.getParent());
      }
      String oldName = folder.getName();
      fd.setFile(oldName);
      fd.setVisible(true);
      newParentDir = fd.getDirectory();
      newName = fd.getFile();
    } else {
      JFileChooser fc = new JFileChooser();
      fc.setDialogTitle(PROMPT);
      if (isReadOnly() || isUntitled()) {
        // default to the sketchbook folder
        fc.setCurrentDirectory(new File(Preferences.get("sketchbook.path")));
      } else {
        // default to the parent folder of where this was
        fc.setCurrentDirectory(folder.getParentFile());
      }
      // can't do this, will try to save into itself by default
      //fc.setSelectedFile(folder);
      int result = fc.showSaveDialog(editor);
      if (result == JFileChooser.APPROVE_OPTION) {
        File selection = fc.getSelectedFile();
        newParentDir = selection.getParent();
        newName = selection.getName();
      }
    }

    // user canceled selection
    if (newName == null) return false;

    // check on the sanity of the name
    String sanitaryName = Sketch.checkName(newName);
    File newFolder = new File(newParentDir, sanitaryName);
    if (!sanitaryName.equals(newName) && newFolder.exists()) {
      Base.showMessage("Cannot Save",
                       "A sketch with the cleaned name\n" +
                       "“" + sanitaryName + "” already exists.");
      return false;
    }
    newName = sanitaryName;

//    String newPath = newFolder.getAbsolutePath();
//    String oldPath = folder.getAbsolutePath();

//    if (newPath.equals(oldPath)) {
//      return false;  // Can't save a sketch over itself
//    }

    // make sure there doesn't exist a tab with that name already
    // but ignore this situation for the first tab, since it's probably being
    // resaved (with the same name) to another location/folder.
    for (int i = 1; i < codeCount; i++) {
      if (newName.equalsIgnoreCase(code[i].getPrettyName())) {
        Base.showMessage("Nope",
                         "You can't save the sketch as \"" + newName + "\"\n" +
                         "because the sketch already has a tab with that name.");
        return false;
      }
    }

    // check if the paths are identical
    if (newFolder.equals(folder)) {
      // just use "save" here instead, because the user will have received a
      // message (from the operating system) about "do you want to replace?"
      return save();
    }

    // check to see if the user is trying to save this sketch inside itself
    try {
      String newPath = newFolder.getCanonicalPath() + File.separator;
      String oldPath = folder.getCanonicalPath() + File.separator;

      if (newPath.indexOf(oldPath) == 0) {
        Base.showWarning("How very Borges of you",
                         "You cannot save the sketch into a folder\n" +
                         "inside itself. This would go on forever.", null);
        return false;
      }
    } catch (IOException e) { }

    // if the new folder already exists, then first remove its contents before
    // copying everything over (user will have already been warned).
    if (newFolder.exists()) {
      Base.removeDir(newFolder);
    }
    // in fact, you can't do this on Windows because the file dialog
    // will instead put you inside the folder, but it happens on OS X a lot.

    // now make a fresh copy of the folder
    newFolder.mkdirs();

    // grab the contents of the current tab before saving
    // first get the contents of the editor text area
    if (current.isModified()) {
      current.setProgram(editor.getText());
    }

    File[] copyItems = folder.listFiles(new FileFilter() {
      public boolean accept(File file) {
        String name = file.getName();
        // just in case the OS likes to return these as if they're legit
        if (name.equals(".") || name.equals("..")) {
          return false;
        }
        // list of files/folders to be ignored during "save as"
        for (String ignorable : mode.getIgnorable()) {
          if (name.equals(ignorable)) {
            return false;
          }
        }
        // ignore the extensions for code, since that'll be copied below
        for (String ext : mode.getExtensions()) {
          if (name.endsWith(ext)) {
            return false;
          }
        }
        // don't do screen captures, since there might be thousands. kind of
        // a hack, but seems harmless. hm, where have i heard that before...
        if (name.startsWith("screen-")) {
          return false;
        }
        return true;
      }
    });
    // now copy over the items that make sense
    for (File copyable : copyItems) {
      if (copyable.isDirectory()) {
        Base.copyDir(copyable, new File(newFolder, copyable.getName()));
      } else {
        Base.copyFile(copyable, new File(newFolder, copyable.getName()));
      }
    }

    // save the other tabs to their new location
    for (int i = 1; i < codeCount; i++) {
      File newFile = new File(newFolder, code[i].getFileName());
      code[i].saveAs(newFile);
    }

    // While the old path to the main .pde is still set, remove the entry from
    // the Recent menu so that it's not sticking around after the rename.
    // If untitled, it won't be in the menu, so there's no point.
    if (!isUntitled()) {
      editor.removeRecent();
    }

    // save the main tab with its new name
    File newFile = new File(newFolder, newName + ".pde");
    code[0].saveAs(newFile);

    updateInternal(newName, newFolder);

    // Make sure that it's not an untitled sketch
    setUntitled(false);

    // Add this sketch back using the new name
    editor.addRecent();

    // let Editor know that the save was successful
    return true;
  }


  /**
   * Update internal state for new sketch name or folder location.
   */
  protected void updateInternal(String sketchName, File sketchFolder) {
    // reset all the state information for the sketch object

//  String oldPath = getMainFilePath();
    primaryFile = code[0].getFile();
//    String newPath = getMainFilePath();
//    editor.base.renameRecent(oldPath, newPath);

    name = sketchName;
    folder = sketchFolder;
    codeFolder = new File(folder, "code");
    dataFolder = new File(folder, "data");

    // set the main file to be the current tab
    //setCurrentCode(0);
    // nah, this might just annoy people

    // Name changed, rebuild the sketch menus
    calcModified();
//    System.out.println("modified is now " + modified);
    editor.updateTitle();
    editor.base.rebuildSketchbookMenus();
//    editor.header.rebuild();
  }


  /**
   * Prompt the user for a new file to the sketch, then call the
   * other addFile() function to actually add it.
   */
  public void handleAddFile() {
    // make sure the user didn't hide the sketch folder
    ensureExistence();

    // if read-only, give an error
    if (isReadOnly()) {
      // if the files are read-only, need to first do a "save as".
      Base.showMessage("Sketch is Read-Only",
                       "Some files are marked \"read-only\", so you'll\n" +
                       "need to re-save the sketch in another location,\n" +
                       "and try again.");
      return;
    }

    // get a dialog, select a file to add to the sketch
    String prompt =
      "Select an image or other data file to copy to your sketch";
    //FileDialog fd = new FileDialog(new Frame(), prompt, FileDialog.LOAD);
    FileDialog fd = new FileDialog(editor, prompt, FileDialog.LOAD);
    fd.setVisible(true);

    String directory = fd.getDirectory();
    String filename = fd.getFile();
    if (filename == null) return;

    // copy the file into the folder. if people would rather
    // it move instead of copy, they can do it by hand
    File sourceFile = new File(directory, filename);

    // now do the work of adding the file
    boolean result = addFile(sourceFile);

    if (result) {
      editor.statusNotice("One file added to the sketch.");
    }
  }


  /**
   * Add a file to the sketch.
   * 
   * <p/>
   * .pde or .java files will be added to the sketch folder. <br/>
   * .jar, .class, .dll, .jnilib, and .so files will all
   * be added to the "code" folder. <br/>
   * All other files will be added to the "data" folder.
   * <p/>
   * If they don't exist already, the "code" or "data" folder
   * will be created.
   * <p/>
   * 
   * @param sourceFile does this file has to be an existing file?
   *                  and it doesn't really have to be a source file.
   * 
   * @throws What is the error handling policy?
   * @return true if successful.
   */
  public boolean addFile(File sourceFile) {
    String filename = sourceFile.getName();
    File destFile = null;
    String codeExtension = null;
    boolean replacement = false;

    // if the file appears to be code related, drop it
    // into the code folder, instead of the data folder
    if (filename.toLowerCase().endsWith(".class") ||
        filename.toLowerCase().endsWith(".jar") ||
        filename.toLowerCase().endsWith(".dll") ||
        filename.toLowerCase().endsWith(".jnilib") ||
        filename.toLowerCase().endsWith(".so")) {

      //if (!codeFolder.exists()) codeFolder.mkdirs();
      prepareCodeFolder();
      destFile = new File(codeFolder, filename);

    } else {
      for (String extension : mode.getExtensions()) {
        String lower = filename.toLowerCase();
        if (lower.endsWith("." + extension)) {
          destFile = new File(this.folder, filename);
          codeExtension = extension;
        }
      }
      if (codeExtension == null) {
        prepareDataFolder();
        destFile = new File(dataFolder, filename);
      }
    }

    // check whether this file already exists
    if (destFile.exists()) {
      Object[] options = { "OK", "Cancel" };
      String prompt = "Replace the existing version of " + filename + "?";
      
      // TODO: it asssumes that we're running Swing application.
      int result = JOptionPane.showOptionDialog(editor,
                                                prompt,
                                                "Replace",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE,
                                                null,
                                                options,
                                                options[0]);
      if (result == JOptionPane.YES_OPTION) {
        replacement = true;
      } else {
        return false;
      }
    }

    // If it's a replacement, delete the old file first,
    // otherwise case changes will not be preserved.
    // http://dev.processing.org/bugs/show_bug.cgi?id=969
    if (replacement) {
      boolean muchSuccess = destFile.delete();
      if (!muchSuccess) {
        Base.showWarning("Error adding file",
                         "Could not delete the existing '" +
                         filename + "' file.", null);
        return false;
      }
    }

    // make sure they aren't the same file
    if ((codeExtension == null) && sourceFile.equals(destFile)) {
      Base.showWarning("You can't fool me",
                       "This file has already been copied to the\n" +
                       "location from which where you're trying to add it.\n" +
                       "I ain't not doin nuthin'.", null);
      return false;
    }

    // in case the user is "adding" the code in an attempt
    // to update the sketch's tabs
    if (!sourceFile.equals(destFile)) {
      try {
        Base.copyFile(sourceFile, destFile);

      } catch (IOException e) {
        Base.showWarning("Error adding file",
                         "Could not add '" + filename + "' to the sketch.", e);
        return false;
      }
    }

    if (codeExtension != null) {
      SketchCode newCode = new SketchCode(destFile, codeExtension);

      if (replacement) {
        replaceCode(newCode);

      } else {
        insertCode(newCode);
        sortCode();
      }
      setCurrentCode(filename);
      editor.header.repaint();
      if (isUntitled()) {  // TODO probably not necessary? problematic?
        // Mark the new code as modified so that the sketch is saved
        current.setModified(true);
      }

    } else {
      if (isUntitled()) {  // TODO probably not necessary? problematic?
        // If a file has been added, mark the main code as modified so
        // that the sketch is properly saved.
        code[0].setModified(true);
      }
    }
    return true;
  }


  /**
   * Change what file is currently being edited. Changes the current tab index.
   * <OL>
   * <LI> store the String for the text of the current file.
   * <LI> retrieve the String for the text of the new file.
   * <LI> change the text that's visible in the text area
   * </OL>
   */
  public void setCurrentCode(int which) {
//    // for the tab sizing
//    if (current != null) {
//      current.visited = System.currentTimeMillis();
//      System.out.println(current.visited);
//    }
    // if current is null, then this is the first setCurrent(0)
    if ((currentIndex == which) && (current != null)) {
      return;
    }

    // get the text currently being edited
    if (current != null) {
      current.setState(editor.getText(),
                       editor.getSelectionStart(),
                       editor.getSelectionStop(),
                       editor.getScrollPosition());
    }

    current = code[which];
    currentIndex = which;
    current.visited = System.currentTimeMillis();

    editor.setCode(current);
//    editor.header.rebuild();
    editor.header.repaint();
  }


  /**
   * Sets current code to supplied filename.
   * 
   * Internal helper function to set the current tab based on a name.
   * 
   * @param findName the file name (OR pretty name) to be shown (should be VALID, existing)
   * @throws SketchRtException when cannot find the element.
   */
  protected void setCurrentCode(String findName) {
    for (int i = 0; i < codeCount; i++) {
      if (findName.equals(code[i].getFileName()) ||
          findName.equals(code[i].getPrettyName())) {
        setCurrentCode(i);
        return;
      }
    }
    throw new SketchRtException("Cannot find file: " + findName);
  }


  /**
   * Create a temporary folder that includes the sketch's name in its title.
   */
  public File makeTempFolder() {
    try {
      File buildFolder = Base.createTempFolder(name, "temp", null);
//      if (buildFolder.mkdirs()) {
      return buildFolder;

//      } else {
//        Base.showWarning("Build folder bad",
//                         "Could not create a place to build the sketch.", null);
//      }
    } catch (IOException e) {
      Base.showWarning("Build folder bad",
                       "Could not find a place to build the sketch.", e);
    }
    return null;
  }


  /**
   * When running from the editor, take care of preparations before running
   * a build or an export. Also erases and/or creates 'targetFolder' if it's
   * not null, and if preferences say to do so when exporting.
   * @param targetFolder is something like applet, application, android...
   */
  /*
  public void prepareBuild(File targetFolder) throws SketchException {
    // make sure the user didn't hide the sketch folder
    ensureExistence();

    // don't do from the command line
    if (editor != null) {
      // make sure any edits have been stored
      current.setProgram(editor.getText());

      // if an external editor is being used, need to grab the
      // latest version of the code from the file.
      if (Preferences.getBoolean("editor.external")) {
        // set current to null so that the tab gets updated
        // http://dev.processing.org/bugs/show_bug.cgi?id=515
        current = null;
        // nuke previous files and settings
        load();
      }
    }

    if (targetFolder != null) {
      // Nuke the old applet/application folder because it can cause trouble
      if (Preferences.getBoolean("export.delete_target_folder")) {
        System.out.println("temporarily skipping deletion of " + targetFolder);
//        Base.removeDir(targetFolder);
//        targetFolder.renameTo(dest);
      }
      // Create a fresh output folder (needed before preproc is run next)
      targetFolder.mkdirs();
    }
  }
  */


  /**
   * Make sure the sketch hasn't been moved or deleted by some
   * nefarious user. If they did, try to re-create it and save.
   * Only checks to see if the main folder is still around,
   * but not its contents.
   * 
   * <p>
   * What's the error-handling policy here? Catch-all?
   * </p>
   */
  public void ensureExistence() {
    if (!folder.exists()) {
      // Disaster recovery, try to salvage what's there already.
      Base.showWarning("Sketch Disappeared",
                       "The sketch folder has disappeared.\n " +
                       "Will attempt to re-save in the same location,\n" +
                       "but anything besides the code will be lost.", null);
      try {
        folder.mkdirs();
        modified = true;

        for (int i = 0; i < codeCount; i++) {
          code[i].save();  // this will force a save
        }
        calcModified();

      } catch (Exception e) {
        Base.showWarning("Could not re-save sketch",
                         "Could not properly re-save the sketch. " +
                         "You may be in trouble at this point,\n" +
                         "and it might be time to copy and paste " +
                         "your code to another text editor.", e);
      }
    }
  }


  /**
   * Returns true if this is a read-only sketch. Used for the
   * examples directory, or when sketches are loaded from read-only
   * volumes or folders without appropriate permissions.
   */
  public boolean isReadOnly() {
    String apath = folder.getAbsolutePath();
    Mode mode = editor.getMode();
    if (apath.startsWith(mode.getExamplesFolder().getAbsolutePath()) ||
        apath.startsWith(mode.getLibrariesFolder().getAbsolutePath())) {
      return true;

      // canWrite() doesn't work on directories
      //} else if (!folder.canWrite()) {
    } else {
      // check to see if each modified code file can be written to
      for (int i = 0; i < codeCount; i++) {
        if (code[i].isModified() &&
            code[i].fileReadOnly() &&
            code[i].fileExists()) {
          //System.err.println("found a read-only file " + code[i].file);
          return true;
        }
      }
      //return true;
    }
    return false;
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  // Additional accessors added in 0136 because of package work.
  // These will also be helpful for tool developers.


  /**
   * Returns the name of this sketch. (The pretty name of the main tab.)
   */
  public String getName() {
    return name;
  }


  /**
   * Returns a File object for the main .pde file for this sketch.
   */
  public File getMainFile() {
    return primaryFile;
  }


  /**
   * Returns path to the main .pde file for this sketch.
   */
  public String getMainFilePath() {
    return primaryFile.getAbsolutePath();
    //return code[0].file.getAbsolutePath();
  }


  /**
   * Returns the sketch folder.
   */
  public File getFolder() {
    return folder;
  }


  /**
   * Returns the location of the sketch's data folder. (It may not exist yet.)
   */
  public File getDataFolder() {
    return dataFolder;
  }


  public boolean hasDataFolder() {
    return dataFolder.exists();
  }


  /**
   * Create the data folder if it does not exist already. As a convenience,
   * it also returns the data folder, since it's likely about to be used.
   */
  public File prepareDataFolder() {
    if (!dataFolder.exists()) {
      dataFolder.mkdirs();
    }
    return dataFolder;
  }


  /**
   * Returns the location of the sketch's code folder. (It may not exist yet.)
   */
  public File getCodeFolder() {
    return codeFolder;
  }


  public boolean hasCodeFolder() {
    return (codeFolder != null) && codeFolder.exists();
  }


  /**
   * Create the code folder if it does not exist already. As a convenience,
   * it also returns the code folder, since it's likely about to be used.
   */
  public File prepareCodeFolder() {
    if (!codeFolder.exists()) {
      codeFolder.mkdirs();
    }
    return codeFolder;
  }


//  public String getClassPath() {
//    return classPath;
//  }


//  public String getLibraryPath() {
//    return javaLibraryPath;
//  }


  public SketchCode[] getCode() {
    return code;
  }


  public int getCodeCount() {
    return codeCount;
  }


  public SketchCode getCode(int index) {
    return code[index];
  }


  public int getCodeIndex(SketchCode who) {
    for (int i = 0; i < codeCount; i++) {
      if (who == code[i]) {
        return i;
      }
    }
    return -1;
  }


  /**
   * Returns "current" sketch code.
   * <p>
   * It is very annoying that this object actually has this state of "current code".
   * I don't really like it. I believe that this state should be the state of the editor or some
   * entity which will be performing responsibility of "controller" for the Editor and other shit.
   * This state should only be in once place.
   * </p>
   * @return
   */
  public SketchCode getCurrentCode() {
    return current;
  }


  public int getCurrentCodeIndex() {
    return currentIndex;
  }


  public String getMainProgram() {
    return getCode(0).getProgram();
  }


  public void setUntitled(boolean untitled) {
//    editor.untitled = u;
    this.untitled = untitled;
    editor.updateTitle();
  }


  public boolean isUntitled() {
//    return editor.untitled;
    return untitled;
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /**
   * Convert to sanitized name and alert the user
   * if changes were made.
   */
  static public String checkName(String origName) {
    String newName = sanitizeName(origName);

    if (!newName.equals(origName)) {
      String msg =
        "The sketch name had to be modified. Sketch names can only consist\n" +
        "of ASCII characters and numbers (but cannot start with a number).\n" +
        "They should also be less than 64 characters long.";
      System.out.println(msg);
    }
    return newName;
  }


  /**
   * Return true if the name is valid for a Processing sketch.
   */
  static public boolean isSanitaryName(String name) {
    if (name.toLowerCase().endsWith(".pde")) {
      name = name.substring(0, name.length() - 4);
    }
    return sanitizeName(name).equals(name);
  }


  static final boolean asciiLetter(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }


  /**
   * Produce a sanitized name that fits our standards for likely to work.
   * <p/>
   * Java classes have a wider range of names that are technically allowed
   * (supposedly any Unicode name) than what we support. The reason for
   * going more narrow is to avoid situations with text encodings and
   * converting during the process of moving files between operating
   * systems, i.e. uploading from a Windows machine to a Linux server,
   * or reading a FAT32 partition in OS X and using a thumb drive.
   * <p/>
   * This helper function replaces everything but A-Z, a-z, and 0-9 with
   * underscores. Also disallows starting the sketch name with a digit
   * or underscore.
   * <p/>
   * In Processing 2.0, sketches can no longer begin with an underscore,
   * because these aren't valid class names on Android.
   */
  static public String sanitizeName(String origName) {
    char orig[] = origName.toCharArray();
    StringBuffer buffer = new StringBuffer();

    // Can't lead with a digit (or anything besides a letter), so prefix with
    // "sketch_". In 1.x this prefixed with an underscore, but those get shaved
    // off later, since you can't start a sketch name with underscore anymore.
    if (!asciiLetter(orig[0])) {
      buffer.append("sketch_");
    }
//    for (int i = 0; i < orig.length; i++) {
    for (char c : orig) {
      if (asciiLetter(c) || (c >= '0' && c <= '9')) {
        buffer.append(c);

      } else {
        // Tempting to only add if prev char is not underscore, but that
        // might be more confusing if lots of chars are converted and the
        // result is a very short string thats nothing like the original.
        buffer.append('_');
      }
    }
    // Let's not be ridiculous about the length of filenames.
    // in fact, Mac OS 9 can handle 255 chars, though it can't really
    // deal with filenames longer than 31 chars in the Finder.
    // Limiting to that for sketches would mean setting the
    // upper-bound on the character limit here to 25 characters
    // (to handle the base name + ".class")
    if (buffer.length() > 63) {
      buffer.setLength(63);
    }
    // Remove underscores from the beginning, these seem to be a reserved
    // thing on Android, plus it sometimes causes trouble elsewhere.
    int underscore = 0;
    while (underscore < buffer.length() && buffer.charAt(underscore) == '_') {
      underscore++;
    }
    if (underscore == buffer.length()) {
      return "bad_sketch_name_please_fix";

    } else if (underscore != 0) {
      return buffer.substring(underscore);
    }
    return buffer.toString();
  }


  public Mode getMode() {
    return mode;
  }
  
  /**
   * This is listener for all the sketch events.
   * 
   */
  private ISketchEvents mSketchEventsListener;
  
  protected boolean isListenerMissing(){
    return mSketchEventsListener == null;
  }
  
  public void setOnSketchEventListener(ISketchEvents events){
      mSketchEventsListener = events;
  }
  
  protected void fireOnFileAddedEvent(SketchCode sc){
      if ( isListenerMissing() ){ return; }
      mSketchEventsListener.onFileAdded(sc, this);
  }
  
}