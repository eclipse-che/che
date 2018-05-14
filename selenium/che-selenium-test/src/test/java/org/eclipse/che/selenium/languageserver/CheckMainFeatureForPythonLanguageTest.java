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
package org.eclipse.che.selenium.languageserver;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_DEFINITION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.FILE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.PYTHON;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_PYTHON3_SIMPLE;
import static org.openqa.selenium.Keys.F4;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.Keys;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckMainFeatureForPythonLanguageTest {
  private static final String PROJECT_NAME = "console-python3-simple";
  private static final String WORKSPACE_NAME = generate("python", 4);
  private static final String PYTHON_FILE_NAME = "main.py";
  private static final String PYTHON_MODULE_NAME = "myModule";
  private static final String LS_INIT_MESSAGE =
      "Initialized Language Server org.eclipse.che.plugin.python.languageserver on project file:///projects/console-python3-simple";
  private static final String PYTHON_CLASS =
      "class MyClass:\n"
          + "\tvar = 1\n"
          + "variable = \"bla\"\n"
          + "\n"
          + "def function(self):\n"
          + "\tprint(\"This is a message inside the class.\")";

  private static final String PYTHON_METHOD = "def add(a, b):\n return a + b";

  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Dashboard dashboard;
  @Inject private NewWorkspace newWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private ToastLoader toastLoader;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private AskForValueDialog askForValueDialog;;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkLaunchingCodeserver() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();

    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(PYTHON.getId());
    newWorkspace.typeWorkspaceName(WORKSPACE_NAME);
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(CONSOLE_PYTHON3_SIMPLE);
    projectSourcePage.clickOnAddProjectButton();
    newWorkspace.clickOnCreateButtonAndOpenInIDE();

    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    toastLoader.waitToastLoaderAndClickStartButton();
    ide.waitOpenedWorkspaceIsReadyToUse();

    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/main.py");
    editor.waitTabIsPresent(PYTHON_FILE_NAME);

    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkErrorMessages() {
    editor.selectTabByName(PYTHON_FILE_NAME);
    editor.deleteAllContent();
    editor.typeTextIntoEditor("");
    editor.waitAllMarkersInvisibility(ERROR);

    editor.typeTextIntoEditor("p");
    editor.waitMarkerInPosition(ERROR, 1);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.waitAllMarkersInvisibility(ERROR);
  }

  @Test(priority = 2)
  public void checkErrorMessages2() {
    editor.selectTabByName(PYTHON_FILE_NAME);
    editor.deleteAllContent();
    editor.typeTextIntoEditor(PYTHON_CLASS);
    editor.waitAllMarkersInvisibility(ERROR);
    editor.typeTextIntoEditor("\n");

    editor.waitMarkerInPosition(WARNING, editor.getPositionVisible()); // 7
    // TODO check for "W293 blank line contains whitespace" message in WARNING marker

  }

  @Test(priority = 2)
  public void checkPythonLsAutocomplete() {
    editor.selectTabByName(PYTHON_FILE_NAME);
    editor.deleteAllContent();
    editor.typeTextIntoEditor(PYTHON_CLASS);

    editor.typeTextIntoEditor("\n");
    editor.typeTextIntoEditor("myobjectx = MyClass()");
    editor.typeTextIntoEditor("\n");
    editor.typeTextIntoEditor("myobjectx.");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("function");
    editor.waitTextIntoAutocompleteContainer("var");
    editor.waitTextIntoAutocompleteContainer("variable");
    // TODO check autocomplete
    editor.removeLineAndAllAfterIt(editor.getPositionVisible()); // 8
    //    editor.enterAutocompleteProposal("Build() ");
  }

  @Test(priority = 2)
  public void checkPythonLsFindDefinition() {
    createFile(PYTHON_MODULE_NAME);

    editor.selectTabByName(PYTHON_MODULE_NAME);
    editor.typeTextIntoEditor(PYTHON_METHOD);
    editor.clickOnCloseFileIcon(PYTHON_MODULE_NAME);

    editor.selectTabByName(PYTHON_FILE_NAME);
    editor.deleteAllContent();
    // editor.setCursorToLine(9);
    editor.typeTextIntoEditor("import myModule\n");
    editor.typeTextIntoEditor(PYTHON_CLASS);
    editor.typeTextIntoEditor("\nvar2 = myModule.add(100, 200)");

    editor.goToCursorPositionVisible(editor.getPositionVisible(), 27);
    menu.runCommand(Assistant.ASSISTANT, FIND_DEFINITION);
    editor.waitTabIsPresent(PYTHON_MODULE_NAME);
    editor.clickOnCloseFileIcon(PYTHON_MODULE_NAME);

    // TODO repeat steps with F4 button
    editor.goToCursorPositionVisible(editor.getPositionVisible(), 27);
    editor.typeTextIntoEditor(F4.toString());
    editor.waitTabIsPresent(PYTHON_MODULE_NAME);
  }

  private void createFile(String fileName) {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(PROJECT, NEW, FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(fileName);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    editor.waitTabIsPresent(fileName);
  }
}
