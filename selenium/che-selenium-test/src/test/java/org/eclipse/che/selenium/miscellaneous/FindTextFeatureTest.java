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

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.EDIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.FIND;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.WEB_JAVA_PETCLINIC;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.ARROW_RIGHT;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.ConfigureClasspath;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.FindText;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.Assert;
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
  @Inject private FindText findText;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Wizard wizard;

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
    projectExplorer.selectItem(PROJECT_NAME);

    // Open the Find Text form from menu
    menu.runCommand(EDIT, FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.waitSearchBtnMainFormIsDisabled();
    findText.closeFindTextMainForm();

    // Open the Find Text form by keyboard
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.waitSearchBtnMainFormIsDisabled();
    findText.closeFindTextFormByEscape();
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
    projectExplorer.selectItem(PROJECT_NAME);

    //  Check that the Processes tab is opened
    if (!consoles.processesMainAreaIsOpen()) {
      consoles.clickOnProcessesTab();
    }

    // Create a file from terminal
    terminal.waitTerminalTab();
    terminal.selectTerminalTab();
    createFileInTerminal(fileNameCreatedFromTerminal);
    WaitUtils.sleepQuietly(LOAD_PAGE_TIMEOUT_SEC);

    // Check that created from terminal file found by "Filesystem" text
    menu.runCommand(EDIT, FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("Filesystem");
    findText.waitTextIntoFindField("Filesystem");
    findText.setAndWaitWholeWordCheckbox(false);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findText.waitExpectedTextInFindInfoPanel(asList(expectedText1.split("\n")));
    findText.selectItemInFindInfoPanel(
        format("/%s/readme.con", PROJECT_NAME),
        "1:   Filesystem 1K-blocks Used Available Use% Mounted on");
    findText.sendCommandByKeyboardInFindInfoPanel(ENTER.toString());
    editor.waitActiveTabFileName(fileNameCreatedFromTerminal);
    Assert.assertEquals(editor.getPositionOfLine(), 1);

    // Create a file from API
    createFileFromAPI(PROJECT_NAME, fileNameCreatedFromAPI, content);
    WaitUtils.sleepQuietly(LOAD_PAGE_TIMEOUT_SEC);

    // Check that created from API file found by "Feature" text
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(EDIT, FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("Feature");
    findText.waitTextIntoFindField("Feature");
    findText.setAndWaitWholeWordCheckbox(false);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findText.waitExpectedTextInFindInfoPanel(asList(expectedText2.split("\n")));
    findText.selectItemInFindInfoPanel(
        format("/%s/readme.api", PROJECT_NAME), "1:   FindTextFeatureTest");
    findText.sendCommandByKeyboardInFindInfoPanel(ENTER.toString());
    editor.waitActiveTabFileName(fileNameCreatedFromAPI);
    Assert.assertEquals(editor.getPositionOfLine(), 1);

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
    projectExplorer.selectItem(PROJECT_NAME);

    // Check that no occurrences found
    menu.runCommand(EDIT, FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.waitSearchBtnMainFormIsDisabled();
    findText.typeTextIntoFindField("dddhhh");
    findText.waitTextIntoFindField("dddhhh");
    loader.waitOnClosed();
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(findNothing);
    findText.clickHideBtnFindInfoPanel();
    findText.clickFindTab();
    findText.waitFindInfoPanelIsOpen();

    // Find files with 'String' text. Open 'guess_num.jsp' file and check cursor position
    projectExplorer.selectItem(PROJECT_NAME);
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("String");
    findText.waitTextIntoFindField("String");
    findText.setAndWaitStateSearchRootCheckbox(false);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(asList(expectedText.split("\n")));
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findText.selectItemInFindInfoPanel(
        pathToQuessNumFile,
        "25:    java.lang.String attempt = (java.lang.String)request.getAttribute(\"num\");");
    findText.sendCommandByKeyboardInFindInfoPanel(ENTER.toString());
    editor.waitActiveEditor();
    editor.waitActiveTabFileName("guess_num.jsp");
    editor.waitTextIntoEditor("String");
    Assert.assertEquals(editor.getPositionOfLine(), 25);

    // Check that the Find Info panel state restored
    findText.clickHideBtnFindInfoPanel();
    findText.clickFindTab();
    findText.waitFindInfoPanelIsOpen();

    // Open 'SayHello.java' file and check cursor position
    findText.selectItemInFindInfoPanel(
        pathToQuessNumFile,
        "25:    java.lang.String attempt = (java.lang.String)request.getAttribute(\"num\");");
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_DOWN.toString());
    findText.selectItemInFindInfoPanelByDoubleClick(
        pathToSayHelloFile, "20:    public String sayHello(String name)");
    editor.waitActiveEditor();
    editor.waitActiveTabFileName("SayHello");
    editor.waitTextIntoEditor("String");
    Assert.assertEquals(editor.getPositionOfLine(), 20);

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
    projectExplorer.selectItem(PROJECT_NAME);

    // Find text with whole world feature is disabled
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("uess");
    findText.waitTextIntoFindField("uess");
    findText.setAndWaitWholeWordCheckbox(false);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(asList(expectedText.split("\n")));

    // Find text with whole world feature is enabled
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(EDIT, FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("uess");
    findText.waitTextIntoFindField("uess");
    findText.setAndWaitWholeWordCheckbox(true);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel("No results found for\n'uess'\n");
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
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.selectItem(path1);

    menu.runCommand(EDIT, FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("uess");
    findText.waitTextIntoFindField("uess");
    findText.setAndWaitStateSearchRootCheckbox(false);
    findText.waitPathIntoRootField("/" + path1);
    findText.clickOnSearchButtonMainForm();
    findText.waitExpectedTextInFindInfoPanel(asList(expectedText1.split("\n")));

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
    findText.waitPathIntoRootField(path2);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(asList(expectedText2.split("\n")));
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
    projectExplorer.selectItem(PROJECT_NAME);

    // Find text with '*.java' file mask
    menu.runCommand(EDIT, FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("String");
    findText.waitTextIntoFindField("String");
    findText.setAndWaitFileMaskCheckbox(true);
    findText.typeTextIntoFileNameFilter("*.java");
    findText.waitTextIntoFileNameFilter("*.java");
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(asList(expectedText1.split("\n")));
    findText.waitExpectedTextIsNotPresentInFindInfoPanel(expectedText2);

    // Find text with '*.jsp' file mask
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
    findText.waitExpectedTextIsNotPresentInFindInfoPanel(expectedText1);
    findText.waitExpectedTextInFindInfoPanel(asList(expectedText2.split("\n")));
  }

  @Test
  public void checkTextResultsPagination() {
    int sumOfFoundOccurrences = 0;
    int sumOfFoundFiles = 0;
    String path1 = "/web-java-petclinic/pom.xml";
    String path2 = "/web-java-petclinic/src/main/resources/spring/mvc-view-config.xml";
    String path3 =
        "/web-java-petclinic/src/test/java/org/springframework/samples/petclinic/web/VisitsViewTests.java";
    String expectedText1 = "62:    <webjars-bootstrap.version>2.3.0</webjars-bootstrap.version>";
    String expectedText2 =
        "36:    <!-- Simple strategy: only path extension is taken into account -->";
    String expectedText3 = "19:   import static org.hamcrest.Matchers.containsString;";
    String resultsOnFirstPage =
        "125 occurrences found in 30 files (per page results) for 'Str'. Total file count - 63";
    String resultsOnSecondPage =
        "178 occurrences found in 30 files (per page results) for 'Str'. Total file count - 63";
    String resultsOnThirdPage =
        "10 occurrences found in 3 files (per page results) for 'Str'. Total file count - 63";

    // Import the web-java-petclinic project and find all occurrences of 'Str'
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectProjectAndCreate(WEB_JAVA_PETCLINIC, "web-java-petclinic");
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.selectItem("web-java-petclinic");
    projectExplorer.openItemByPath("web-java-petclinic");
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("Str");
    findText.waitTextIntoFindField("Str");
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();

    // Check move page buttons status on the first page
    Assert.assertTrue(findText.checkNextPageButtonIsEnabled());
    Assert.assertFalse(findText.checkPreviousPageButtonIsEnabled());
    sumOfFoundFiles += findText.getFoundFilesNumberOnPage();
    sumOfFoundOccurrences += findText.getFoundOccurrencesNumberOnPage();
    findText.clickOnNextPageButton();

    // Check move page buttons status on the second page
    Assert.assertTrue(findText.checkNextPageButtonIsEnabled());
    Assert.assertTrue(findText.checkPreviousPageButtonIsEnabled());
    sumOfFoundFiles += findText.getFoundFilesNumberOnPage();
    sumOfFoundOccurrences += findText.getFoundOccurrencesNumberOnPage();
    findText.clickOnNextPageButton();

    // Check move page buttons status on the third page
    Assert.assertFalse(findText.checkNextPageButtonIsEnabled());
    Assert.assertTrue(findText.checkPreviousPageButtonIsEnabled());
    sumOfFoundFiles += findText.getFoundFilesNumberOnPage();
    sumOfFoundOccurrences += findText.getFoundOccurrencesNumberOnPage();

    // Checking that sums of found files and occurrences correct
    Assert.assertEquals(sumOfFoundFiles, findText.getTotalFoundFilesNumber());
    Assert.assertEquals(sumOfFoundOccurrences, SUM_FOUND_OCCURRENCES);

    // Check results on the third page
    Assert.assertEquals(findText.getResults(), resultsOnThirdPage);
    findText.openFileNodeByDoubleClick(path3);
    findText.waitExpectedTextInFindInfoPanel(expectedText3);

    // Check results on the second page
    findText.clickOnPreviousPageButton();
    Assert.assertEquals(findText.getResults(), resultsOnSecondPage);
    findText.openFileNodeByDoubleClick(path2);
    findText.waitExpectedTextInFindInfoPanel(expectedText2);

    // Check results on the first page. Open a file and check cursor position
    findText.clickOnPreviousPageButton();
    Assert.assertEquals(findText.getResults(), resultsOnFirstPage);
    findText.openFileNodeByDoubleClick(path1);
    findText.waitExpectedTextInFindInfoPanel(expectedText1);
    findText.selectItemInFindInfoPanelByDoubleClick(path1, expectedText1);
    editor.waitActiveEditor();
    editor.waitActiveTabFileName("spring-petclinic");
    Assert.assertEquals(editor.getPositionOfLine(), 62);

    editor.closeAllTabsByContextMenu();
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
