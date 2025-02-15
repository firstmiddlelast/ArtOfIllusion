/* Copyright (C) 2002-2013 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import artofillusion.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rtextarea.RTextScrollPane;

/** This class presents a user interface for entering scripts to be executed. */

public class ExecuteScriptWindow extends BFrame
{
  private LayoutWindow window;
  private RSyntaxTextArea scriptText;
  private BComboBox languageChoice;
  public static final String NEW_SCRIPT_NAME = Translate.text ("untitled");
  private String scriptPath;

  // QUESTION should this be static? It's actually the last directory used for scripts, 
  // is there a reason it is shared among all editing windows? 
  private static File scriptDir= new File(ArtOfIllusion.TOOL_SCRIPT_DIRECTORY);
  private String language;
  private final BButton save;
  private final BButton executeSelected;
  private final BButton executeToCursor;
  private static final int EDITORS_OFFSET = 32;
  private static ArrayList <String> openedScripts = new ArrayList<String> ();

    private javax.swing.filechooser.FileFilter scriptFileFilter;
    
    /**
     * Adds a script path to the recent scripts list. 
     * This uses a mapping to the current (now) timestamp ; the file paths are in the order of their timestamps, 
     * so the older is first. The list is truncated to RecentFiles.MAX_RECENT elements. 
     * @see artofillusion.RecentFiles#MAX_RECENT
     * @param filePath 
     */
    public static void addRecentScript(String filePath)
    {
      final Preferences pref = Preferences.userNodeForPackage(ExecuteScriptWindow.class);
      final String recentFiles[] = pref.get("recentFiles", "").split(File.pathSeparator);
      java.util.List<String> newRecentFiles = new ArrayList<String>();
      newRecentFiles.add (filePath);
      for (String recentFile : recentFiles) 
        if (!recentFile.equals(filePath)) // If the current file already has a timestamp it will be updated below
          newRecentFiles.add (recentFile);
      pref.put("recentFiles", String.join (File.pathSeparator, newRecentFiles.subList(0, java.lang.Math.min(newRecentFiles.size(), 10))));
    }
    
    public static String [] getRecentScripts()
    {
      final Preferences pref = Preferences.userNodeForPackage(ExecuteScriptWindow.class);
      final java.util.List<String> recentScripts = Arrays.asList (pref.get("recentFiles", "").split(File.pathSeparator));
      return recentScripts.toArray(new String [0]);
    } 

  /** This is used to track the "changed" status of the script being edited. */
  private class ScriptKeyListener implements KeyListener
  {

    @Override
    public void keyTyped(KeyEvent e)
    {
      save.setEnabled(true);        
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

  }
    
  /**
   * 
   * @param win
   * @param scriptAbsolutePath {@link ExecuteScriptWindow#NEW_SCRIPT_NAME} if this is a new script
   * @param scriptLanguage May be {@link ScriptRunner#UNKNOWN_LANGUAGE} if this is a new script
   */
  public ExecuteScriptWindow(LayoutWindow win, String scriptAbsolutePath, String scriptLanguage) throws IOException
  {
    super(scriptAbsolutePath);
    setScriptNameFromFile(scriptAbsolutePath);
    language = scriptLanguage;
    scriptPath = scriptAbsolutePath;
    // Get the extensions dynamically
    final java.util.List <String> extensions = new ArrayList <String>();
    for (String language : ScriptRunner.getLanguageNames ())
    {
      extensions.add (ScriptRunner.getFilenameExtension(language));
    }
    scriptFileFilter = new javax.swing.filechooser.FileNameExtensionFilter(
      "Script files", (String[]) extensions.toArray(new String [0]));

    BorderContainer content = new BorderContainer();
    setContent(content);
    window = win;
    String editorTextContent = "";
    if (scriptLanguage != ScriptRunner.UNKNOWN_LANGUAGE && scriptAbsolutePath.contains(".")) {
        editorTextContent = ArtOfIllusion.loadFile(new File (scriptAbsolutePath));
    }
    scriptText = new RSyntaxTextArea(editorTextContent, 25, 100);
    scriptText.addKeyListener(new ScriptKeyListener());
    SyntaxScheme scheme = scriptText.getSyntaxScheme();
    Style style = scheme.getStyle(SyntaxScheme.COMMENT_EOL);
    Style newStyle = new Style(style.foreground, style.background, style.font.deriveFont(Font.PLAIN));
    scheme.setStyle(SyntaxScheme.COMMENT_EOL, newStyle);
    scheme.setStyle(SyntaxScheme.COMMENT_MULTILINE, newStyle);

    scriptText.setAnimateBracketMatching(false);
    scriptText.setTabSize(2);
    scriptText.setCodeFoldingEnabled(true);
    content.add(new AWTWidget(new RTextScrollPane(scriptText)),
      BorderContainer.CENTER);
    languageChoice = new BComboBox(ScriptRunner.getLanguageNames());
    languageChoice.getComponent().setRenderer(new LanguageRenderer());
    BorderContainer tools = new BorderContainer ();
    content.add(tools, BorderContainer.NORTH);
    RowContainer buttons = new RowContainer();
    buttons.add(Translate.button("load", "...", this, "loadScript"));
    buttons.add(Translate.button("saveAs", "...", this, "saveScriptAs"));
    buttons.add(save = Translate.button("save", "", this, "saveScript"));
    save.setEnabled(false);

    tools.add(buttons, BorderContainer.WEST, new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE));

    // another center row for the "execute selected" and verious debugging items
    RowContainer debugTools = new RowContainer();
    debugTools.add(Translate.button("executeScript", this, "executeScript"));
    debugTools.add(executeToCursor = Translate.button("executeToCursor", this, "executeToCursor"));
    debugTools.add(executeSelected = Translate.button("executeSelected", this, "executeSelected"));
    
    tools.add(debugTools, BorderContainer.CENTER, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE));
    
    RowContainer languageRow = new RowContainer();
    languageRow.add(Translate.label("language"));
    languageRow.add(languageChoice);
    if (scriptLanguage != ScriptRunner.UNKNOWN_LANGUAGE)
    {
      languageChoice.setSelectedValue(scriptLanguage);
      languageChoice.setEnabled(false);
    }
    tools.add(languageRow, BorderContainer.EAST, new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE));
    //buttons.add(Translate.button("close", this, "closeWindow"));
    addEventLink(WindowClosingEvent.class, this, "closeWindow");
    languageChoice.addEventLink(ValueChangedEvent.class, this, "updateLanguage");
    scriptText.setCaretPosition(0);
    pack();
    updateLanguage();
    UIUtilities.centerWindow(this);
    // We add an offset to every window so one does not exactly hide the others
    int editorFrameOffset = EDITORS_OFFSET*openedScripts.size();
    setBounds(new Rectangle(this.getBounds().x + editorFrameOffset, this.getBounds().y + editorFrameOffset, 
            this.getBounds().width, this.getBounds().height));
    scriptText.requestFocus();
    setVisible(true);
    updateEditableStatus(NEW_SCRIPT_NAME, scriptAbsolutePath);
  }
  
  class LanguageRenderer extends JLabel implements ListCellRenderer  
  {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, 
            int index, boolean isSelected, boolean hasFocus)
    {
      String selectedLanguage = ((String)value);
      final ImageIcon languageIcon = new ImageIcon (getClass().getResource("/artofillusion/Icons/"
              + selectedLanguage + ".png"));
      if (isSelected)
      {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } 
      else
      {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setIcon (languageIcon);
      setText (selectedLanguage);
      return this;
    }
  }

  private void updateEditableStatus(String previousScriptAbsoluePath, String scriptAbsolutePath)
  {
    if (!previousScriptAbsoluePath.equals(scriptAbsolutePath))
    {    
      if (openedScripts.contains(scriptAbsolutePath))
      {
          scriptText.setEditable(false);
          scriptText.setEnabled(false);
          scriptText.setBackground(Color.LIGHT_GRAY);
          new BStandardDialog(null, new String [] {Translate.text("alreadyOpenedScript"),
              "This window is read-only : this script is open in other window(s) " + scriptAbsolutePath}, BStandardDialog.ERROR).showMessageDialog(this);
      }
      else
      {
          scriptText.setEditable(true);
          scriptText.setEnabled(true);
          scriptText.setBackground(Color.WHITE);
      }
      openedScripts.remove(previousScriptAbsoluePath);
      openedScripts.add(scriptAbsolutePath);
    }
  }

  /**
   * Make syntax highlighting match current scripting language 
   * {@link  ScriptRunner#Language}
   */
  private void updateLanguage()
  {
    scriptText.setSyntaxEditingStyle(
        ScriptRunner.Language.GROOVY.name.equalsIgnoreCase(language) ?
          SyntaxConstants.SYNTAX_STYLE_GROOVY : SyntaxConstants.SYNTAX_STYLE_JAVA);
  }

  private void closeWindow()
  {
    // Default action in the options dialog is "close anyway"
    int action = 1;
    // Warning message if the script hasn't been saved
    if (save.isEnabled())
    {
      action = new BStandardDialog(null, new String [] {Translate.text("unsavedChanges"),
        Translate.text("unsavedChangesPrompt")}, BStandardDialog.ERROR)
          .showOptionDialog(this, new String []{
            Translate.text("saveAndClose"), 
            Translate.text("discardChangesAndClose"), 
            Translate.text("cancelClosing")}, scriptPath);
    }
    // Action 0 is save and close
    if (action == 0)
    {
      saveScript();
    }
    // Action 2 is cancel closing
    if (action != 2)
    {
      dispose();
      openedScripts.remove(scriptPath);
    }
  }

  /** Prompt the user to load a script. */

  private void loadScript()
  {
    BFileChooser fc = new BFileChooser(BFileChooser.OPEN_FILE, Translate.text("selectScriptToLoad"));
    // Save the current program working directory
    File workingDir = fc.getDirectory();
    fc.setDirectory(scriptDir);
    fc.getComponent ().setFileFilter(scriptFileFilter);
    if (fc.showDialog(this))
    {
      scriptDir = fc.getDirectory();
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      File f = fc.getSelectedFile();
      try
      {
        scriptText.setText(ArtOfIllusion.loadFile(f));
        updateEditableStatus(scriptPath, fc.getSelectedFile().getAbsolutePath());
        scriptPath = fc.getSelectedFile().getAbsolutePath();
        scriptText.setCaretPosition(0);
        String filename = fc.getSelectedFile().getName();
        String fileLanguage = ScriptRunner.getLanguageForFilename(filename);
        if (fileLanguage != ScriptRunner.UNKNOWN_LANGUAGE)
        {
          language = fileLanguage;
          languageChoice.setSelectedValue(fileLanguage);
          languageChoice.setEnabled(false);
          setScriptNameFromFile(fc.getSelectedFile().getAbsolutePath());
          for (EditingWindow edWindow: ArtOfIllusion.getWindows())
          {
            if (edWindow instanceof LayoutWindow)
              ((LayoutWindow) edWindow).rebuildRecentScriptsMenu();
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          updateLanguage();
          // disable the "Save" button, 
          // to be re-enabled as soon as the text changes
          save.setEnabled(false);
        }
        else
        {
          new BStandardDialog(null, new String [] {Translate.text("errorReadingScript"),
            Translate.text("unsupportedFileExtension") + " : " + filename}, BStandardDialog.ERROR).showMessageDialog(this);
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        new BStandardDialog(null, new String [] {Translate.text("errorReadingScript"),
          ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(this);
      }
    }
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    // Restore program working directory for other filechoosers
    fc.setDirectory(workingDir);
  }

 /** Prompt the user to save a script. */

  private void saveScriptAs()
  {
    BFileChooser fc = new BFileChooser(BFileChooser.SAVE_FILE, Translate.text("saveScriptToFile"));
    // Save current program working directory
    File workingDir = fc.getDirectory();
    fc.setDirectory(scriptDir);
    if (language == ScriptRunner.UNKNOWN_LANGUAGE) 
      language = (String) languageChoice.getSelectedValue();
    fc.setSelectedFile(new File(scriptPath));
    fc.getComponent ().setFileFilter(scriptFileFilter);
    if (fc.showDialog(this))
    {
      scriptDir = fc.getDirectory();
  
      // Write the script to disk.
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      File f = fc.getSelectedFile();
      try
      {
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        out.write(scriptText.getText().toCharArray());
        out.close();
      }
      catch (Exception ex)
      {
        new BStandardDialog(null, new String [] {Translate.text("errorWritingScript"),
          ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(this);
      }
      // Now we have saved, we can't change the language
      languageChoice.setEnabled(false);
      updateEditableStatus(scriptPath, fc.getSelectedFile().getAbsolutePath());
      scriptPath = fc.getSelectedFile().getAbsolutePath();

      setScriptNameFromFile(fc.getSelectedFile().getAbsolutePath());
      // Update the Scripts menus in all windows.
      for (EditingWindow edWin : ArtOfIllusion.getWindows())
      {
         if (edWin instanceof LayoutWindow)
           ((LayoutWindow) edWin).rebuildScriptsMenu();
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    save.setEnabled(false);
    // Restore program working directory
    fc.setDirectory(workingDir);
 }

  /** Save the current script to its current file path, without user input. 
   */
  private void saveScript()
  {
     if (language == ScriptRunner.UNKNOWN_LANGUAGE) 
       language = (String) languageChoice.getSelectedValue();

    // Write the script to disk.
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    File f = new File (scriptPath);
    try
    {
      BufferedWriter out = new BufferedWriter(new FileWriter(f));
      out.write(scriptText.getText().toCharArray());
      out.close();
    }
    catch (Exception ex)
    {
      new BStandardDialog(null, new String [] {Translate.text("errorWritingScript"),
        scriptPath + (ex.getMessage() == null ? "" : ex.getMessage())}, BStandardDialog.ERROR).showMessageDialog(this);
    }
    // Now we have saved, we can't change the language
    languageChoice.setEnabled(false);
    save.setEnabled(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 }


  /** Set the script name based on the name of a file that was loaded or saved. 
   @param filePath NEW_SCRIPT_NAME or an absolute file path
   */
  private void setScriptNameFromFile(String filePath)
  {
    if (filePath != NEW_SCRIPT_NAME)
        addRecentScript(filePath);
    setTitle(filePath);
  }

  private void executeSelected()
  {
    executeText(scriptText.getSelectedText());
    window.updateImage();
    scriptText.requestFocus();
  }
  
  private void executeToCursor() 
  {
    final String substringAfterCaret = scriptText.getText().substring(scriptText.getCaretPosition());
    int charactersUntilEndOfLine = substringAfterCaret.indexOf("\n");
    if (charactersUntilEndOfLine == -1)
        charactersUntilEndOfLine = substringAfterCaret.length();
    final String textToEndOfCaretLine = scriptText.getText().substring(
            0, scriptText.getCaretPosition() + charactersUntilEndOfLine);
    executeText(textToEndOfCaretLine);
    window.updateImage();
    scriptText.requestFocus();
  }
  
  /** Execute the script. */
  private void executeScript()
  {
    executeText(scriptText.getText());
    window.updateImage();
    scriptText.requestFocus();
  }

  public void executeText(final String text)
  {
    try
    {
      String scriptLanguage = (language == ScriptRunner.UNKNOWN_LANGUAGE)? 
        (String) languageChoice.getSelectedValue():
        language;

      ToolScript script = ScriptRunner.parseToolScript(scriptLanguage, text);
      script.execute(window);
    }
    catch (Exception e)
    {
      int line = ScriptRunner.displayError(language, e);
      if (line > -1)
      {
        // Find the start of the line containing the error.
        int index = 0;
        for (int i = 0; i < line-1; i++)
        {
            int next = text.indexOf('\n', index);
            if (next == -1)
            {
                index = -1;
                break;
            }
            index = next+1;
        }
        if (index > -1)
          scriptText.setCaretPosition(index);
        scriptText.requestFocus();
      }
    }
  }
}
