/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.editor;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.TabAction;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class SplitEditorFeatureTest {

  private static final String PROJECT_NAME =
      NameGenerator.generate(SplitEditorFeatureTest.class.getSimpleName(), 4);
  private static final String PATH_TEXT_FILE = PROJECT_NAME + "/README.md";
  private static final String PATH_JAVA_FILE =
      PROJECT_NAME + "/src/main/java/org/eclipse/che/examples/GreetingController.java";
  private static final String NAME_JAVA_CLASS = "GreetingController";
  private static final String NEW_NAME = "NewName";
  private static final String NEW_NAME_JAVA = "NewName.java";
  private static final String NEW_NAME_TXT_FILE = "NewREADME";

  private static final String TEXT = "some text";

  @Inject private TestWorkspace workspace;
  @Inject private TestUser defaultTestUser;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private Menu menu;
  @Inject private Refactor refactor;
  @Inject private Wizard wizard;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
    createProject(PROJECT_NAME);
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_JAVA_FILE);
    loader.waitOnClosed();
  }

  @Test
  public void checkSplitEditorWindow() {
    editor.waitActiveEditor();
    editor.openContextMenuForTabByName(NAME_JAVA_CLASS);
    editor.runActionForTabFromContextMenu(TabAction.SPIT_HORISONTALLY);

    editor.waitCountTabsWithProvidedName(2, NAME_JAVA_CLASS);

    editor.selectTabByIndexEditorWindowAndOpenMenu(0, NAME_JAVA_CLASS);
    editor.runActionForTabFromContextMenu(TabAction.SPLIT_VERTICALLY);
    editor.waitCountTabsWithProvidedName(3, NAME_JAVA_CLASS);

    editor.selectTabByIndexEditorWindow(1, NAME_JAVA_CLASS);
    editor.waitActiveEditor();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.UP.toString());
    editor.typeTextIntoEditor(TEXT);

    selectSplittedTabAndWaitExpectedText(0, NAME_JAVA_CLASS, TEXT);

    selectSplittedTabAndWaitExpectedText(1, NAME_JAVA_CLASS, TEXT);

    selectSplittedTabAndWaitExpectedText(2, NAME_JAVA_CLASS, TEXT);
  }

  @Test(priority = 1)
  public void checkFocusInCurrentWindow() {
    editor.selectTabByIndexEditorWindow(1, NAME_JAVA_CLASS);
    projectExplorer.openItemByPath(PATH_TEXT_FILE);
    Assert.assertTrue(editor.tabIsPresentOnce("README.md"));
  }

  @Test(priority = 2)
  public void checkRefactoring() {
    editor.selectTabByIndexEditorWindow(2, NAME_JAVA_CLASS);
    editor.waitActiveEditor();
    projectExplorer.selectItem(PATH_JAVA_FILE);

    projectExplorer.launchRefactorByKeyboard();
    refactor.typeAndWaitNewName(NEW_NAME_JAVA);
    refactor.sendKeysIntoField(Keys.SPACE.toString());
    refactor.sendKeysIntoField(Keys.BACK_SPACE.toString());
    refactor.clickOkButtonRefactorForm();
    editor.waitActiveEditor();

    editor.selectTabByIndexEditorWindow(1, NEW_NAME);
    editor.waitActiveEditor();

    editor.selectTabByIndexEditorWindow(0, NEW_NAME);
    editor.waitActiveEditor();
    editor.setCursorToLine(2);
    editor.typeTextIntoEditor("//" + TEXT);
    editor.waitTextIntoEditor("//" + TEXT);

    selectSplittedTabAndWaitExpectedText(1, NEW_NAME, "//" + TEXT);

    selectSplittedTabAndWaitExpectedText(2, NEW_NAME, "//" + TEXT);
  }

  @Test(priority = 3)
  public void checkContentAfterRenameFile() {
    editor.selectTabByIndexEditorWindow(0, NEW_NAME);
    projectExplorer.openItemByPath(PATH_TEXT_FILE);
    editor.waitActiveEditor();
    editor.selectTabByIndexEditorWindow(2, NEW_NAME);
    projectExplorer.openItemByPath(PATH_TEXT_FILE);
    editor.waitActiveEditor();
    renameFile(PATH_TEXT_FILE);
    editor.selectTabByIndexEditorWindow(0, NEW_NAME_TXT_FILE);
    editor.waitActiveEditor();
    editor.setCursorToLine(3);
    editor.typeTextIntoEditor("***" + TEXT);
    editor.waitTextIntoEditor("***" + TEXT);

    selectSplittedTabAndWaitExpectedText(1, NEW_NAME_TXT_FILE, "***" + TEXT);

    selectSplittedTabAndWaitExpectedText(2, NEW_NAME_TXT_FILE, "***" + TEXT);
  }

  private void createProject(String projectName) {
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    wizard.waitCreateProjectWizardForm();
    wizard.typeProjectNameOnWizard(projectName);
    wizard.selectSample(Wizard.SamplesName.WEB_JAVA_SPRING);
    wizard.clickCreateButton();
    loader.waitOnClosed();
    wizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
    loader.waitOnClosed();
  }

  private void renameFile(String pathToFile) {
    projectExplorer.selectItem(pathToFile);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.RENAME);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.clearInput();
    askForValueDialog.typeAndWaitText(NEW_NAME_TXT_FILE);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
  }

  private void selectSplittedTabAndWaitExpectedText(
      int tabIndex, String tabName, String expectedText) {
    editor.selectTabByIndexEditorWindow(tabIndex, tabName);
    editor.waitActiveEditor();
    editor.waitTextInDefinedSplitEditor(tabIndex + 1, LOAD_PAGE_TIMEOUT_SEC, expectedText);
  }

  private void selectSplittedTabAndWaitTextIsNotPresent(int tabIndex, String tabName, String text) {
    editor.selectTabByIndexEditorWindow(tabIndex, tabName);
    editor.waitActiveEditor();
    editor.waitTextIsNotPresentInDefinedSplitEditor(tabIndex + 1, LOAD_PAGE_TIMEOUT_SEC, text);
  }
}
