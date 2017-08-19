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
package org.eclipse.che.selenium.miscellaneous;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.ConfigureClasspath;
import org.eclipse.che.selenium.pageobject.FindText;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class FindTextFeatureTest {

  private static final String PROJECT_NAME = NameGenerator.generate("FindTextFeature", 4);

  private static final String FIND_NOTHING = "(0 occurrence)";

  private static final String PR_1_EXPECTED_TEXT_1 =
      "(4 occurrences)\n"
          + "AppController.java\n"
          + "(/"
          + PROJECT_NAME
          + "/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java)\n"
          + "SayHello.java\n"
          + "(/"
          + PROJECT_NAME
          + "/my-lib/src/main/java/hello/SayHello.java)\n"
          + "SayHelloTest.java\n"
          + "(/"
          + PROJECT_NAME
          + "/my-lib/src/test/java/hello/SayHelloTest.java)\n"
          + "guess_num.jsp\n"
          + "(/"
          + PROJECT_NAME
          + "/my-webapp/src/main/webapp/WEB-INF/jsp/guess_num.jsp)";

  private static final String PR_2_EXPECTED_TEXT_1 = "(4 occurrences)";

  private static final String PR_3_PATH_1 =
      PROJECT_NAME + "/my-webapp/src/main/java/org/eclipse/qa/examples";
  private static final String PR_3_EXPECTED_TEXT_1 =
      "(1 occurrence)\n"
          + "AppController.java\n"
          + "(/"
          + PROJECT_NAME
          + "/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java)";

  private static final String PR_3_PATH_2 = "/" + PROJECT_NAME + "/my-lib";
  private static final String PR_3_EXPECTED_TEXT_2 =
      "(2 occurrences)\n"
          + "SayHello.java\n"
          + "(/"
          + PROJECT_NAME
          + "/my-lib/src/main/java/hello/SayHello.java)\n"
          + "SayHelloTest.java\n"
          + "(/"
          + PROJECT_NAME
          + "/my-lib/src/test/java/hello/SayHelloTest.java)";

  private static final String PR_4_EXPECTED_TEXT_1 =
      "(3 occurrences)\n"
          + "AppController.java\n"
          + "(/"
          + PROJECT_NAME
          + "/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java)\n"
          + "SayHello.java\n"
          + "(/"
          + PROJECT_NAME
          + "/my-lib/src/main/java/hello/SayHello.java)\n"
          + "SayHelloTest.java\n"
          + "(/"
          + PROJECT_NAME
          + "/my-lib/src/test/java/hello/SayHelloTest.java)";

  private static final String PR_4_EXPECTED_TEXT_2 =
      "(1 occurrence)\n"
          + "guess_num.jsp\n"
          + "(/"
          + PROJECT_NAME
          + "/my-webapp/src/main/webapp/WEB-INF/jsp/guess_num.jsp)";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private ConfigureClasspath configureClasspath;
  @Inject private MachineTerminal terminal;
  @Inject private FindText findText;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/java-multimodule");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkFindTextForm() {
    projectExplorer.waitProjectExplorer();
    terminal.waitTerminalTab();
    loader.waitOnClosed();

    // open main form from menu
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.waitSearchBtnMainFormIsDisabled();
    findText.closeFindTextMainForm();

    // open main form by keyboard
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.waitSearchBtnMainFormIsDisabled();
    findText.closeFindTextFormByEscape();
  }

  @Test(priority = 1)
  public void checkRecentlyCreatedFiles() throws Exception {
    String content = "FindTextFeatureTest";
    String fileNameCreatedFromAPI = "readme.api";
    String fileNameCreatedFromTerminal = "readme.con";

    projectExplorer.waitProjectExplorer();
    projectExplorer.selectItem(PROJECT_NAME);
    terminal.waitTerminalTab();
    terminal.selectTerminalTab();
    createFileInTerminal(fileNameCreatedFromTerminal);
    WaitUtils.sleepQuietly(15);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("pom.xml");
    findText.waitTextIntoFindField("pom.xml");
    findText.setAndWaitWholeWordCheckbox(false);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(fileNameCreatedFromTerminal);
    findText.selectItemInFindInfoPanel(fileNameCreatedFromTerminal);
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ENTER.toString());
    editor.waitActiveTabFileName(fileNameCreatedFromTerminal);

    projectExplorer.selectItem(PROJECT_NAME);
    createFileFromAPI(PROJECT_NAME, fileNameCreatedFromAPI, content);
    WaitUtils.sleepQuietly(15);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("Feature");
    findText.waitTextIntoFindField("Feature");
    findText.setAndWaitWholeWordCheckbox(false);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(fileNameCreatedFromAPI);
    findText.selectItemInFindInfoPanel(fileNameCreatedFromAPI);
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ENTER.toString());
    editor.waitActiveTabFileName(fileNameCreatedFromAPI);
  }

  @Test(priority = 2)
  public void checkFindTextBasic() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.waitSearchBtnMainFormIsDisabled();
    findText.typeTextIntoFindField("dddhhh");
    findText.waitTextIntoFindField("dddhhh");
    loader.waitOnClosed();
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(FIND_NOTHING);
    findText.clickHideBtnFindInfoPanel();
    findText.clickFindTab();
    findText.waitFindInfoPanelIsOpen();

    projectExplorer.selectItem(PROJECT_NAME);
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("String");
    findText.waitTextIntoFindField("String");
    findText.setAndWaitStateSearchRootCheckbox(false);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(PR_1_EXPECTED_TEXT_1);
    findText.selectItemInFindInfoPanel("guess_num.jsp");
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ENTER.toString());
    editor.waitActiveEditor();
    editor.waitActiveTabFileName("guess_num.jsp");
    editor.waitTextIntoEditor("String");
    findText.selectItemInFindInfoPanelByDoubleClick("SayHello.java");
    editor.waitActiveEditor();
    editor.waitActiveTabFileName("SayHello");
    editor.waitTextIntoEditor("String");
    findText.selectItemInFindInfoPanel("guess_num.jsp");
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ARROW_UP.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ENTER.toString());
    editor.waitActiveTabFileName("SayHelloTest");
    editor.closeAllTabs();
  }

  @Test(priority = 3)
  public void checkFindWholeWordOnly() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.selectItem(PROJECT_NAME);
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("uess");
    findText.waitTextIntoFindField("uess");
    findText.setAndWaitWholeWordCheckbox(false);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(PR_2_EXPECTED_TEXT_1);
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("uess");
    findText.waitTextIntoFindField("uess");
    findText.setAndWaitWholeWordCheckbox(true);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(FIND_NOTHING);
  }

  @Test(priority = 4)
  public void checkFindIntoSelectedPath() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.selectItem(PR_3_PATH_1);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("uess");
    findText.waitTextIntoFindField("uess");
    findText.setAndWaitStateSearchRootCheckbox(false);
    findText.waitPathIntoRootField("/" + PR_3_PATH_1);
    findText.clickOnSearchButtonMainForm();
    findText.waitExpectedTextInFindInfoPanel(PR_3_EXPECTED_TEXT_1);

    projectExplorer.selectItem(PROJECT_NAME);
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("hello");
    findText.waitTextIntoFindField("hello");
    findText.setAndWaitStateSearchRootCheckbox(true);
    findText.clickSearchDirectoryBtn();
    configureClasspath.waitSelectPathFormIsOpen();
    configureClasspath.openItemInSelectPathForm(PROJECT_NAME);
    configureClasspath.waitItemInSelectPathForm("my-lib");
    configureClasspath.waitItemInSelectPathForm("my-webapp");
    configureClasspath.selectItemInSelectPathForm("my-lib");
    configureClasspath.clickOkBtnSelectPathForm();
    findText.waitPathIntoRootField(PR_3_PATH_2);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(PR_3_EXPECTED_TEXT_2);
  }

  @Test(priority = 5)
  public void checkFindByFileMask() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("String");
    findText.waitTextIntoFindField("String");
    findText.setAndWaitFileMaskCheckbox(true);
    findText.typeTextIntoFileNameFilter("*.java");
    findText.waitTextIntoFileNameFilter("*.java");
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(PR_4_EXPECTED_TEXT_1);
    findText.waitExpectedTextIsNotPresentInFindInfoPanel(PR_4_EXPECTED_TEXT_2);

    projectExplorer.selectItem(PROJECT_NAME);
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("String");
    findText.waitTextIntoFindField("String");
    findText.setAndWaitFileMaskCheckbox(true);
    findText.typeTextIntoFileNameFilter("*.jsp");
    findText.waitTextIntoFileNameFilter("*.jsp");
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextIsNotPresentInFindInfoPanel(PR_4_EXPECTED_TEXT_1);
    findText.waitExpectedTextInFindInfoPanel(PR_4_EXPECTED_TEXT_2);
  }

  private void createFileFromAPI(String path, String fileName, String content) throws Exception {
    testProjectServiceClient.createFileInProject(workspace.getId(), path, fileName, content);
  }

  private void createFileInTerminal(String fileName) {
    terminal.typeIntoTerminal("cd " + PROJECT_NAME + Keys.ENTER);
    terminal.typeIntoTerminal("ls -als > " + fileName + Keys.ENTER);
    terminal.typeIntoTerminal("cat " + fileName + Keys.ENTER);
    terminal.typeIntoTerminal("ls" + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal(fileName);
  }
}
