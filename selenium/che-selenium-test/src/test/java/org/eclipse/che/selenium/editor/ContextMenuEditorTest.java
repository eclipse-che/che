/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.editor;

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.CLOSE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.FIND;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.FORMAT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.NAVIGATE_FILE_STRUCTURE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.OPEN_DECLARATION;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.QUICK_DOC;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.QUICK_FIX;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.REDO;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.REFACTORING;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.REFACTORING_MOVE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.REFACTORING_RENAME;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.EditorContextMenu.UNDO;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.ERROR_MARKER;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.FileStructure;
import org.eclipse.che.selenium.pageobject.FindText;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class ContextMenuEditorTest {

  private static final String PROJECT_NAME =
      NameGenerator.generate(ContextMenuEditorTest.class.getSimpleName(), 4);
  private static final String PROJECT_NAME_2 = "2_ContextMenuEditor" + new Random().nextInt(999);
  private static final String FORMATTED_TEXT =
      "public class AppController implements Controller {\n"
          + "    private static final String secretNum = Integer.toString(new Random().nextInt(10));\n"
          + "\n"
          + "    @Override\n"
          + "    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {\n"
          + "        String numGuessByUser = request.getParameter(\"numGuess\");\n"
          + "        String result = \"\";\n"
          + "\n"
          + "        if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "            result = \"Congrats! The number is \" + secretNum;\n"
          + "        }\n"
          + "\n"
          + "        else if (numGuessByUser != null) {\n"
          + "            result = \"Sorry, you failed. Try again later!\";\n"
          + "        }\n"
          + "\n"
          + "        ModelAndView view = new ModelAndView(\"guess_num\");\n"
          + "        view.addObject(\"num\", result);\n"
          + "        return view;\n"
          + "    }\n"
          + "}\n";

  private static final String CHECK_TEXT = "// ** check **";

  private static final String QUICK_DOC_TEXT =
      "java.lang.Exception\n"
          + "\n"
          + "The class Exception and its subclasses are a form of Throwable that"
          + " indicates conditions that a reasonable application might want to catch.\n"
          + "The class Exception and any subclasses that are not also subclasses "
          + "of RuntimeException are checked exceptions. Checked exceptions need "
          + "to be declared in a method or constructor's throws clause if they can"
          + " be thrown by the execution of the method or constructor and propagate"
          + " outside the method or constructor boundary.\n"
          + "Since:\n"
          + "JDK1.0\n"
          + "Author:\n"
          + "Frank Yellin\n"
          + "See Also:\n"
          + "java.lang.Error\n"
          + "@jls\n"
          + "11.2 Compile-Time Checking of Exceptions";

  private String expectedTextBeforeDownloadSources = "";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private Refactor refactor;
  @Inject private FileStructure fileStructure;
  @Inject private FindText findText;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME_2,
        ProjectTemplates.MAVEN_SPRING);

    resource = ContextMenuEditorTest.class.getResource("expected-test-before-download-sources");
    List<String> expectedText =
        Files.readAllLines(Paths.get(resource.toURI()), Charset.forName("UTF-8"));
    expectedTextBeforeDownloadSources = Joiner.on('\n').join(expectedText);

    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitVisibleItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    consoles.closeProcessesArea();
  }

  @Test
  public void checkFormatContextMenu() {
    projectExplorer.waitVisibleItem(PROJECT_NAME);
    projectExplorer.scrollToItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActive();
    loader.waitOnClosed();
    editor.openContextMenuInEditor();
    editor.clickOnItemInContextMenu(FORMAT);
    editor.waitTextIntoEditor(FORMATTED_TEXT);
    editor.waitContextMenuIsNotPresent();
  }

  @Test(priority = 1)
  public void checkUndoRedo() {
    projectExplorer.waitItem(PROJECT_NAME);
    editor.waitActive();
    editor.setCursorToLine(2);
    editor.waitActive();
    editor.typeTextIntoEditor(CHECK_TEXT);
    editor.waitTextIntoEditor(CHECK_TEXT);
    loader.waitOnClosed();
    editor.openContextMenuInEditor();
    editor.clickOnItemInContextMenu(UNDO);
    editor.setCursorToLine(2);
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(CHECK_TEXT);
    editor.openContextMenuInEditor();
    editor.clickOnItemInContextMenu(REDO);
    editor.setCursorToLine(2);
    editor.waitActive();
    editor.waitTextIntoEditor(CHECK_TEXT);
    editor.waitContextMenuIsNotPresent();
  }

  @Test(priority = 2)
  public void checkClose() {
    projectExplorer.waitItem(PROJECT_NAME);
    loader.waitOnClosed();
    editor.waitActive();
    editor.openContextMenuInEditor();
    loader.waitOnClosed();
    editor.clickOnItemInContextMenu(CLOSE);
    editor.waitContextMenuIsNotPresent();
    editor.waitTabIsNotPresent("AppController");
  }

  @Test(priority = 3)
  public void checkQuickDocumentation() {
    projectExplorer.waitItem(PROJECT_NAME_2);
    projectExplorer.openItemByPath(
        PROJECT_NAME_2 + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActive();
    editor.goToCursorPositionVisible(25, 105);
    loader.waitOnClosed();
    editor.openContextMenuOnElementInEditor("Exception");
    editor.clickOnItemInContextMenu(QUICK_DOC);
    editor.waitContextMenuIsNotPresent();
    editor.waitJavaDocPopUpOpened();
    editor.checkTextToBePresentInJavaDocPopUp(QUICK_DOC_TEXT);
    editor.selectTabByName("AppController");
    editor.waitJavaDocPopUpClosed();
  }

  @Test(priority = 4)
  public void checkQuickFix() {
    projectExplorer.waitItem(PROJECT_NAME_2);
    projectExplorer.openItemByPath(
        PROJECT_NAME_2 + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActive();
    editor.setCursorToLine(28);
    editor.typeTextIntoEditor("String s = 5;");
    editor.waitTextIntoEditor("String s = 5;");
    editor.waitMarkerInPosition(ERROR_MARKER, 28);
    editor.openContextMenuOnElementInEditor("5");
    editor.clickOnItemInContextMenu(QUICK_FIX);
    editor.waitContextMenuIsNotPresent();
    editor.waitTextIntoFixErrorProposition("Change type of 's' to 'int'");
    editor.selectFirstItemIntoFixErrorPropByEnter();
    editor.setCursorToLine(28);
    editor.waitTextIntoEditor("int s = 5;");
    editor.waitMarkerDisappears(ERROR_MARKER, 28);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
  }

  @Test(priority = 5)
  public void checkOpenDeclaration() {
    projectExplorer.waitItem(PROJECT_NAME_2);
    editor.goToCursorPositionVisible(25, 13);
    editor.openContextMenuOnElementInEditor(" ModelAndView");
    editor.clickOnItemInContextMenu(OPEN_DECLARATION);
    editor.waitContextMenuIsNotPresent();
    editor.waitTabIsPresent("ModelAndView");
    editor.waitTextIntoEditor(expectedTextBeforeDownloadSources);
    editor.closeFileByNameWithSaving("ModelAndView");
  }

  @Test(priority = 6)
  public void checkRefactoring() {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/com/example/Test1.java");
    editor.goToCursorPositionVisible(13, 15);
    editor.openContextMenuOnElementInEditor("Test1");
    editor.clickOnItemInContextMenu(REFACTORING);
    editor.clickOnItemInContextMenu(REFACTORING_MOVE);
    editor.waitContextMenuIsNotPresent();
    refactor.waitMoveItemFormIsOpen();
    refactor.clickOnExpandIconTree(PROJECT_NAME);
    loader.waitOnClosed();
    refactor.clickOnExpandIconTree("/src/main/java");
    loader.waitOnClosed();
    refactor.chooseDestinationForItem("org.eclipse.qa.examples");
    refactor.clickOkButtonRefactorForm();
    refactor.waitMoveItemFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/Test1.java");
    editor.goToCursorPositionVisible(13, 15);
    editor.openContextMenuOnElementInEditor("Test1");
    editor.clickOnItemInContextMenu(REFACTORING);
    editor.clickOnItemInContextMenu(REFACTORING_RENAME);
    editor.waitContextMenuIsNotPresent();
    editor.typeTextIntoEditor("Zclass");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    loader.waitOnClosed();
    editor.waitTextIntoEditor("public class Zclass");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/Zclass.java");
  }

  @Test(priority = 7)
  public void checkNaviFileStructure() {
    projectExplorer.waitItem(PROJECT_NAME_2);
    projectExplorer.openItemByPath(
        PROJECT_NAME_2 + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.openContextMenuInEditor();
    editor.clickOnItemInContextMenu(NAVIGATE_FILE_STRUCTURE);
    editor.waitContextMenuIsNotPresent();
    fileStructure.waitFileStructureFormIsOpen("AppController");
    loader.waitOnClosed();
    fileStructure.waitExpectedTextInFileStructure(
        "handleRequest(HttpServletRequest, HttpServletResponse) : ModelAndView");
    loader.waitOnClosed();
    fileStructure.selectItemInFileStructure(
        "handleRequest(HttpServletRequest, HttpServletResponse) : ModelAndView");
    fileStructure.selectItemInFileStructureByDoubleClick(
        "handleRequest(HttpServletRequest, HttpServletResponse) : ModelAndView");
    fileStructure.waitFileStructureFormIsClosed();
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.waitTextElementsActiveLine("handleRequest");
    editor.waitSpecifiedValueForLineAndChar(25, 25);
  }

  @Test(priority = 8)
  public void checkFind() {
    projectExplorer.waitItem(PROJECT_NAME_2);
    projectExplorer.openItemByPath(
        PROJECT_NAME_2 + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.openContextMenuInEditor();
    editor.clickOnItemInContextMenu(FIND);
    editor.waitContextMenuIsNotPresent();
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("class");
    findText.waitTextIntoFindField("class");
    loader.waitOnClosed();
    findText.waitPathIntoRootField("/" + PROJECT_NAME_2 + "/src/main/java/org/eclipse/qa/examples");
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel("AppController.java");
  }
}
