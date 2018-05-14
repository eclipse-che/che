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
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.PYTHON;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_PYTHON3_SIMPLE;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
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
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.Keys;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckMainFeatureForPythonLanguageTest {
  private static final String PROJECT_NAME = "console-python3-simple";
  private static final String WORKSPACE_NAME = generate("python", 4);
  private static final String LS_INIT_MESSAGE =
      "Initialized Language Server org.eclipse.che.plugin.python.languageserver on project file:///projects/console-python3-simple";
  private static final String PYTHON_CLASS =
      "class MyClass:\n"
          + "\tvar = 1\n"
          + "variable = \"bla\"\n"
          + "\n"
          + "def function(self):\n"
          + "\tprint(\"This is a message inside the class.\")";

  private static final String PYTHON_METHOD = "def add(a, b):\n" + "return a + b";

  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Dashboard dashboard;
  @Inject private NewWorkspace newWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private MachineTerminal terminal;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestProjectServiceClient testProjectServiceClient;
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
    editor.waitTabIsPresent("main.py");

    consoles.selectProcessByTabName("dev-machine");
    System.out.println(consoles.getVisibleTextFromCommandConsole());
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkErrorMessages() {
    editor.selectTabByName("main.py");
    editor.waitAllMarkersInvisibility(ERROR);

    //     editor.goToPosition(1, 3);
    editor.typeTextIntoEditor("p");
    editor.waitMarkerInPosition(ERROR, 1);
    // editor.goToPosition(1, 3);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    // TODO remove added text
    editor.waitAllMarkersInvisibility(ERROR);
  }

  @Test(priority = 2)
  public void checkErrorMessages2() {
    editor.selectTabByName("main.py");
    editor.deleteAllContent();
    editor.typeTextIntoEditor(PYTHON_CLASS);
    editor.waitAllMarkersInvisibility(ERROR);
    editor.typeTextIntoEditor("\n");

    editor.waitMarkerInPosition(WARNING, 7);
    // TODO check for "W293 blank line contains whitespace" message in WARNING marker

  }

  @Test(priority = 3)
  public void checkAutocomplete() {
    editor.typeTextIntoEditor("\n");
    editor.typeTextIntoEditor("myobjectx = MyClass()");
    editor.typeTextIntoEditor("\n");
    editor.typeTextIntoEditor("myobjectx.");
    editor.launchAutocomplete();
    editor.waitTextIntoAutocompleteContainer("function");
    editor.waitTextIntoAutocompleteContainer("var");
    editor.waitTextIntoAutocompleteContainer("variable");
    // TODO check autocomplete
    editor.removeLineAndAllAfterIt(9);
    //    editor.enterAutocompleteProposal("Build() ");
  }

  @Test(priority = 3)
  public void checkFindDefinition() {
    createFile("myModule.py");

    editor.typeTextIntoEditor(PYTHON_METHOD);
    editor.clickOnCloseFileIcon("myModule.py");

    editor.selectTabByName("main.py");
    editor.setCursorToLine(9);
    editor.typeTextIntoEditor("var2 = myModule.add(100, 200)");

    editor.goToCursorPositionVisible(9, 26);
    menu.runCommand(Assistant.ASSISTANT, Assistant.FIND_DEFINITION);
    editor.waitTabIsPresent("myModule.py");
    editor.clickOnCloseFileIcon("myModule.py");

    // TODO repeat steps with F4 button
    editor.goToCursorPositionVisible(9, 26);
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitTabIsPresent("myModule.py");
  }

  private void createFile(String fileName) {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(fileName);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    editor.waitTabIsPresent(fileName);
  }
}
