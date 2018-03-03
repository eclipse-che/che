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
package org.eclipse.che.selenium.miscellaneous;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.EDIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.FIND;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.WEB_JAVA_PETCLINIC;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.ARROW_RIGHT;
import static org.openqa.selenium.Keys.ENTER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.ConfigureClasspath;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.FindText;
import org.eclipse.che.selenium.pageobject.FindText.SearchFileResult;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.PanelSelector;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class FindTextFeatureTest {

  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final int SUM_FOUND_OCCURRENCES = 313;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private ConfigureClasspath configureClasspath;
  @Inject private MachineTerminal terminal;
  @Inject private FindText findTextPage;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;
  @Inject private PanelSelector panelSelector;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Wizard wizard;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/java-multimodule");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);

    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
  }

  @Test
  public void checkFindTextForm() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    // Open the Find Text form from menu
    menu.runCommand(EDIT, FIND);
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.waitSearchBtnMainFormIsDisabled();
    findTextPage.closeFindTextMainForm();

    // Open the Find Text form by keyboard
    findTextPage.launchFindFormByKeyboard();
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.waitSearchBtnMainFormIsDisabled();
    findTextPage.closeFindTextFormByEscape();
  }

  @Test
  public void checkRecentlyCreatedFiles() throws Exception {
    String expectedText1 =
        format(
            "1 occurrence found in 1 file (per page results) for 'Filesystem'. Total file count - 1\n"
                + "/%s/readme.con\n"
                + "(1 occurrence of 'Filesystem' found)\n"
                + "1:   Filesystem 1K-blocks Used Available Use%% Mounted on",
            PROJECT_NAME);
    String expectedText2 =
        format(
            "1 occurrence found in 1 file (per page results) for 'Feature'. Total file count - 1\n"
                + "/%s/readme.api\n"
                + "(1 occurrence of 'Feature' found)\n"
                + "1:   FindTextFeatureTest",
            PROJECT_NAME);
    String content = "FindTextFeatureTest";
    String fileNameCreatedFromAPI = "readme.api";
    String fileNameCreatedFromTerminal = "readme.con";

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    //  Check that the Processes tab is opened
    if (!consoles.processesMainAreaIsOpen()) {
      panelSelector.selectPanelTypeFromPanelSelector(PanelSelector.PanelTypes.LEFT_BOTTOM);
    }
    consoles.clickOnProcessesButton();

    // Create a file from terminal
    terminal.waitTerminalTab();
    terminal.selectTerminalTab();
    createFileInTerminal(fileNameCreatedFromTerminal);
    WaitUtils.sleepQuietly(LOAD_PAGE_TIMEOUT_SEC);

    // Check that created from terminal file found by "Filesystem" text
    menu.runCommand(EDIT, FIND);
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.typeTextIntoFindField("Filesystem");
    findTextPage.waitTextIntoFindField("Filesystem");
    findTextPage.setAndWaitWholeWordCheckbox(false);
    findTextPage.waitPathIntoRootField("/" + PROJECT_NAME);
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitFindInfoPanelIsOpen();
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findTextPage.waitExpectedTextInFindInfoPanel(asList(expectedText1.split("\n")));
    findTextPage.selectItemInFindInfoPanel(
        format("/%s/readme.con", PROJECT_NAME),
        "1:   Filesystem 1K-blocks Used Available Use% Mounted on");
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ENTER.toString());
    editor.waitActiveTabFileName(fileNameCreatedFromTerminal);
    assertEquals(editor.getPositionVisible(), 1);

    // Create a file from API
    createFileFromAPI(PROJECT_NAME, fileNameCreatedFromAPI, content);
    WaitUtils.sleepQuietly(LOAD_PAGE_TIMEOUT_SEC);

    // Check that created from API file found by "Feature" text
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(EDIT, FIND);
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.typeTextIntoFindField("Feature");
    findTextPage.waitTextIntoFindField("Feature");
    findTextPage.setAndWaitWholeWordCheckbox(false);
    findTextPage.waitPathIntoRootField("/" + PROJECT_NAME);
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitFindInfoPanelIsOpen();
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findTextPage.waitExpectedTextInFindInfoPanel(asList(expectedText2.split("\n")));
    findTextPage.selectItemInFindInfoPanel(
        format("/%s/readme.api", PROJECT_NAME), "1:   FindTextFeatureTest");
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ENTER.toString());
    editor.waitActiveTabFileName(fileNameCreatedFromAPI);
    assertEquals(editor.getPositionVisible(), 1);

    editor.closeAllTabsByContextMenu();
  }

  @Test
  public void checkFindTextBasic() {
    String expectedText =
        format(
            "9 occurrences found in 4 files (per page results) for 'String'. Total file count - 4\n"
                + "/%1$s/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java\n"
                + "(4 occurrences of 'String' found)\n"
                + "/%1$s/my-webapp/src/main/webapp/WEB-INF/jsp/guess_num.jsp\n"
                + "(2 occurrences of 'String' found)\n"
                + "/%1$s/my-lib/src/test/java/hello/SayHelloTest.java\n"
                + "(1 occurrence of 'String' found)\n"
                + "/%1$s/my-lib/src/main/java/hello/SayHello.java\n"
                + "(2 occurrences of 'String' found)",
            PROJECT_NAME);
    String findNothing = "No results found for\n'dddhhh'\n";
    String pathToQuessNumFile =
        format("/%s/my-webapp/src/main/webapp/WEB-INF/jsp/guess_num.jsp", PROJECT_NAME);
    String pathToSayHelloFile =
        format("/%s/my-lib/src/main/java/hello/SayHello.java", PROJECT_NAME);

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    // Check that no occurrences found
    menu.runCommand(EDIT, FIND);
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.waitSearchBtnMainFormIsDisabled();
    findTextPage.typeTextIntoFindField("dddhhh");
    findTextPage.waitTextIntoFindField("dddhhh");
    loader.waitOnClosed();
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitFindInfoPanelIsOpen();
    findTextPage.waitExpectedTextInFindInfoPanel(findNothing);

    // check find info panel when switch to processes panel
    consoles.clickOnProcessesButton();
    findTextPage.waitFindInfoPanelIsClosed();
    findTextPage.clickFindTextButton();
    findTextPage.waitFindInfoPanelIsOpen();
    consoles.closeProcessesArea();
    findTextPage.waitFindInfoPanelIsClosed();

    // Find files with 'String' text. Open 'guess_num.jsp' file and check cursor position
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    findTextPage.launchFindFormByKeyboard();
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.typeTextIntoFindField("String");
    findTextPage.waitTextIntoFindField("String");
    findTextPage.setAndWaitStateSearchRootCheckbox(false);
    findTextPage.waitPathIntoRootField("/" + PROJECT_NAME);
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitFindInfoPanelIsOpen();
    findTextPage.waitExpectedTextInFindInfoPanel(asList(expectedText.split("\n")));
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findTextPage.selectItemInFindInfoPanel(
        pathToQuessNumFile,
        "25:    java.lang.String attempt = (java.lang.String)request.getAttribute(\"num\");");
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ENTER.toString());
    editor.waitActive();
    editor.waitActiveTabFileName("guess_num.jsp");
    editor.waitTextIntoEditor("String");
    assertEquals(editor.getPositionVisible(), 25);

    // Check that the Find Info panel state restored
    consoles.closeProcessesArea();
    findTextPage.waitFindInfoPanelIsClosed();
    panelSelector.selectPanelTypeFromPanelSelector(PanelSelector.PanelTypes.LEFT_BOTTOM);
    findTextPage.waitFindInfoPanelIsOpen();

    // Open 'SayHello.java' file and check cursor position
    findTextPage.selectItemInFindInfoPanel(
        pathToQuessNumFile,
        "25:    java.lang.String attempt = (java.lang.String)request.getAttribute(\"num\");");
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findTextPage.selectItemInFindInfoPanelByDoubleClick(
        pathToSayHelloFile, "20:    public String sayHello(String name)");
    editor.waitActive();
    editor.waitActiveTabFileName("SayHello");
    editor.waitTextIntoEditor("String");
    assertEquals(editor.getPositionVisible(), 20);

    editor.closeAllTabsByContextMenu();
  }

  @Test
  public void checkFindWholeWordOnly() {
    String expectedText =
        format(
            "10 occurrences found in 4 files (per page results) for 'uess'. Total file count - 4\n"
                + "/%1$s/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java\n"
                + "(6 occurrences of 'uess' found)\n"
                + "/%1$s/my-webapp/src/main/webapp/index.jsp\n"
                + "(1 occurrence of 'uess' found)\n"
                + "/%1$s/my-webapp/src/main/webapp/WEB-INF/jsp/guess_num.jsp\n"
                + "(2 occurrences of 'uess' found)\n"
                + "/%1$s/my-webapp/src/main/webapp/WEB-INF/spring-servlet.xml\n"
                + "(1 occurrence of 'uess' found)",
            PROJECT_NAME);

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    // Find text with whole world feature is disabled
    findTextPage.launchFindFormByKeyboard();
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.typeTextIntoFindField("uess");
    findTextPage.waitTextIntoFindField("uess");
    findTextPage.setAndWaitWholeWordCheckbox(false);
    findTextPage.waitPathIntoRootField("/" + PROJECT_NAME);
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitFindInfoPanelIsOpen();
    findTextPage.waitExpectedTextInFindInfoPanel(asList(expectedText.split("\n")));

    // Find text with whole world feature is enabled
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(EDIT, FIND);
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.typeTextIntoFindField("uess");
    findTextPage.waitTextIntoFindField("uess");
    findTextPage.setAndWaitWholeWordCheckbox(true);
    findTextPage.waitPathIntoRootField("/" + PROJECT_NAME);
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitFindInfoPanelIsOpen();
    findTextPage.waitExpectedTextInFindInfoPanel("No results found for\n'uess'\n");
  }

  @Test
  public void checkFindIntoSelectedPath() {
    String path1 = PROJECT_NAME + "/my-webapp/src/main/java/org/eclipse/qa/examples";
    String expectedText1 =
        format(
            "6 occurrences found in 1 file (per page results) for 'uess'. Total file count - 1\n"
                + "/%1$s/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java\n"
                + "(6 occurrences of 'uess' found)",
            PROJECT_NAME);
    String path2 = format("/%s/my-lib", PROJECT_NAME);
    String expectedText2 =
        format(
            "15 occurrences found in 2 files (per page results) for 'hello'. Total file count - 2\n"
                + "/%1$s/my-lib/src/test/java/hello/SayHelloTest.java\n"
                + "(10 occurrences of 'hello' found)\n"
                + "/%1$s/my-lib/src/main/java/hello/SayHello.java\n"
                + "(5 occurrences of 'hello' found)",
            PROJECT_NAME);

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitAndSelectItem(path1);

    menu.runCommand(EDIT, FIND);
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.typeTextIntoFindField("uess");
    findTextPage.waitTextIntoFindField("uess");
    findTextPage.setAndWaitStateSearchRootCheckbox(false);
    findTextPage.waitPathIntoRootField("/" + path1);
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitExpectedTextInFindInfoPanel(asList(expectedText1.split("\n")));

    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    findTextPage.launchFindFormByKeyboard();
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.typeTextIntoFindField("hello");
    findTextPage.waitTextIntoFindField("hello");
    findTextPage.setAndWaitStateSearchRootCheckbox(true);
    findTextPage.clickSearchDirectoryBtn();
    configureClasspath.waitSelectPathFormIsOpen();
    configureClasspath.openItemInSelectPathForm(PROJECT_NAME);
    configureClasspath.waitItemInSelectPathForm("my-lib");
    configureClasspath.waitItemInSelectPathForm("my-webapp");
    configureClasspath.selectItemInSelectPathForm("my-lib");
    configureClasspath.clickOkBtnSelectPathForm();
    findTextPage.waitPathIntoRootField(path2);
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitFindInfoPanelIsOpen();
    findTextPage.waitExpectedTextInFindInfoPanel(asList(expectedText2.split("\n")));
  }

  @Test
  public void checkFindByFileMask() {
    String expectedText1 =
        format(
            "7 occurrences found in 3 files (per page results) for 'String'. Total file count - 3\n"
                + "/%1$s/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java\n"
                + "(4 occurrences of 'String' found)\n"
                + "/%1$s/my-lib/src/test/java/hello/SayHelloTest.java\n"
                + "(1 occurrence of 'String' found)\n"
                + "/%1$s/my-lib/src/main/java/hello/SayHello.java\n"
                + "(2 occurrences of 'String' found)",
            PROJECT_NAME);
    String expectedText2 =
        format(
            "2 occurrences found in 1 file (per page results) for 'String'. Total file count - 1\n"
                + "/%s/my-webapp/src/main/webapp/WEB-INF/jsp/guess_num.jsp\n"
                + "(2 occurrences of 'String' found)",
            PROJECT_NAME);

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    // Find text with '*.java' file mask
    menu.runCommand(EDIT, FIND);
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.typeTextIntoFindField("String");
    findTextPage.waitTextIntoFindField("String");
    findTextPage.setAndWaitFileMaskCheckbox(true);
    findTextPage.typeTextIntoFileNameFilter("*.java");
    findTextPage.waitTextIntoFileNameFilter("*.java");
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitFindInfoPanelIsOpen();
    findTextPage.waitExpectedTextInFindInfoPanel(asList(expectedText1.split("\n")));
    findTextPage.waitExpectedTextIsNotPresentInFindInfoPanel(expectedText2);

    // Find text with '*.jsp' file mask
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    findTextPage.launchFindFormByKeyboard();
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.typeTextIntoFindField("String");
    findTextPage.waitTextIntoFindField("String");
    findTextPage.setAndWaitFileMaskCheckbox(true);
    findTextPage.typeTextIntoFileNameFilter("*.jsp");
    findTextPage.waitTextIntoFileNameFilter("*.jsp");
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitFindInfoPanelIsOpen();
    findTextPage.waitExpectedTextIsNotPresentInFindInfoPanel(expectedText1);
    findTextPage.waitExpectedTextInFindInfoPanel(asList(expectedText2.split("\n")));
  }

  @Test
  public void checkTextResultsPagination() {
    SearchFileResult searchFileResult;
    int sumOfFoundOccurrences = 0;
    int sumOfFoundFiles = 0;

    // Import the web-java-petclinic project and find all occurrences of 'Str'
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectProjectAndCreate(WEB_JAVA_PETCLINIC, "web-java-petclinic");
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.waitItem("web-java-petclinic");
    projectExplorer.waitAndSelectItem("web-java-petclinic");

    findTextPage.launchFindFormByKeyboard();
    findTextPage.waitFindTextMainFormIsOpen();
    findTextPage.typeTextIntoFindField("Str");
    findTextPage.waitTextIntoFindField("Str");
    findTextPage.clickOnSearchButtonMainForm();
    findTextPage.waitFindInfoPanelIsOpen();

    // Check move page buttons status on the first page
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    assertTrue(findTextPage.checkNextPageButtonIsEnabled());
    assertFalse(findTextPage.checkPreviousPageButtonIsEnabled());
    searchFileResult = findTextPage.getResults();
    sumOfFoundFiles += searchFileResult.getFoundFilesOnPage();
    sumOfFoundOccurrences += searchFileResult.getFoundOccurrencesOnPage();
    findTextPage.clickOnNextPageButton();

    // Check move page buttons status on the second page
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    assertTrue(findTextPage.checkNextPageButtonIsEnabled());
    assertTrue(findTextPage.checkPreviousPageButtonIsEnabled());
    searchFileResult = findTextPage.getResults();
    sumOfFoundFiles += searchFileResult.getFoundFilesOnPage();
    sumOfFoundOccurrences += searchFileResult.getFoundOccurrencesOnPage();
    findTextPage.clickOnNextPageButton();

    // Check move page buttons status on the third page
    findTextPage.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    assertFalse(findTextPage.checkNextPageButtonIsEnabled());
    assertTrue(findTextPage.checkPreviousPageButtonIsEnabled());
    searchFileResult = findTextPage.getResults();
    sumOfFoundFiles += searchFileResult.getFoundFilesOnPage();
    sumOfFoundOccurrences += searchFileResult.getFoundOccurrencesOnPage();

    // Checking that sums of found files and occurrences correct
    assertEquals(sumOfFoundFiles, findTextPage.getResults().getTotalNumberFoundFiles());
    assertEquals(sumOfFoundOccurrences, SUM_FOUND_OCCURRENCES);
  }

  private void createFileFromAPI(String path, String fileName, String content) throws Exception {
    testProjectServiceClient.createFileInProject(workspace.getId(), path, fileName, content);
  }

  private void createFileInTerminal(String fileName) {
    terminal.typeIntoTerminal("cd " + PROJECT_NAME + ENTER);
    terminal.typeIntoTerminal("df > " + fileName + ENTER);
    terminal.typeIntoTerminal("cat " + fileName + ENTER);
    terminal.typeIntoTerminal("ls" + ENTER);
    terminal.waitExpectedTextIntoTerminal(fileName);
  }
}
