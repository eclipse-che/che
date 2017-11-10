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
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.ARROW_RIGHT;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
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
import org.eclipse.che.selenium.pageobject.PanelSelector;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.Wizard.SamplesName;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class FindTextFeatureTest {

  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);

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
  @Inject private PanelSelector panelSelector;
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
    if (!consoles.processesMainAreaIsOpen()) {
      panelSelector.selectPanelTypeFromPanelSelector(PanelSelector.PanelTypes.LEFT_BOTTOM);
    }
    consoles.clickOnProcessesButton();
    terminal.waitTerminalTab();
    terminal.selectTerminalTab();
    createFileInTerminal(fileNameCreatedFromTerminal);
    WaitUtils.sleepQuietly(TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
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
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ENTER.toString());
    editor.waitActiveTabFileName(fileNameCreatedFromTerminal);
    Assert.assertEquals(editor.getPositionOfLine(), 1);

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
    findText.sendCommandByKeyboardInFindInfoPanel(ARROW_RIGHT.toString());
    findText.waitExpectedTextInFindInfoPanel(asList(expectedText2.split("\n")));
    findText.selectItemInFindInfoPanel(
        format("/%s/readme.api", PROJECT_NAME), "1:   FindTextFeatureTest");
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ENTER.toString());
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
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.waitSearchBtnMainFormIsDisabled();
    findText.typeTextIntoFindField("dddhhh");
    findText.waitTextIntoFindField("dddhhh");
    loader.waitOnClosed();
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(findNothing);

    // check find info panel when switch to processes panel
    consoles.clickOnProcessesButton();
    findText.waitFindInfoPanelIsClosed();
    findText.clickFindTextButton();
    findText.waitFindInfoPanelIsOpen();
    consoles.closeProcessesArea();
    findText.waitFindInfoPanelIsClosed();

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
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ENTER.toString());
    editor.waitActiveEditor();
    editor.waitActiveTabFileName("guess_num.jsp");
    editor.waitTextIntoEditor("String");
    Assert.assertEquals(editor.getPositionOfLine(), 25);
    consoles.closeProcessesArea();
    findText.waitFindInfoPanelIsClosed();
    panelSelector.selectPanelTypeFromPanelSelector(PanelSelector.PanelTypes.LEFT_BOTTOM);
    findText.waitFindInfoPanelIsOpen();
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
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("uess");
    findText.waitTextIntoFindField("uess");
    findText.setAndWaitWholeWordCheckbox(false);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(asList(expectedText.split("\n")));
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
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
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
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
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
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

    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    wizard.selectProjectAndCreate(SamplesName.WEB_JAVA_PETCLINIC, "web-java-petclinic");
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.selectItem("web-java-petclinic");
    projectExplorer.openItemByPath("web-java-petclinic");
    findText.launchFindFormByKeyboard();
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("Str");
    findText.waitTextIntoFindField("Str");
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();

    // check results, open a file and check cursor position
    Assert.assertEquals(findText.getResults(), resultsOnFirstPage);
    findText.openFileNodeByDoubleClick(path1);
    findText.waitExpectedTextInFindInfoPanel(expectedText1);
    findText.selectItemInFindInfoPanelByDoubleClick(path1, expectedText1);
    editor.waitActiveEditor();
    editor.waitActiveTabFileName("spring-petclinic");
    Assert.assertEquals(editor.getPositionOfLine(), 62);

    // check that the previous page button is disabled on the first page and click on the next page
    // button
    Assert.assertFalse(findText.checkPreviousPageButtonIsEnabled());
    findText.clickOnNextPageButton();

    // check results on second page and the previous page button is enabled
    Assert.assertEquals(findText.getResults(), resultsOnSecondPage);
    Assert.assertTrue(findText.checkPreviousPageButtonIsEnabled());
    findText.openFileNodeByDoubleClick(path2);
    findText.waitExpectedTextInFindInfoPanel(expectedText2);
    findText.clickOnNextPageButton();

    // check results on third page and that the next page button is disabled
    Assert.assertEquals(findText.getResults(), resultsOnThirdPage);
    Assert.assertFalse(findText.checkNextPageButtonIsEnabled());
    findText.openFileNodeByDoubleClick(path3);
    findText.waitExpectedTextInFindInfoPanel(expectedText3);
    findText.clickOnPreviousPageButton();

    Assert.assertEquals(findText.getResults(), resultsOnSecondPage);
    Assert.assertTrue(findText.checkNextPageButtonIsEnabled());
    findText.clickOnPreviousPageButton();

    Assert.assertEquals(findText.getResults(), resultsOnFirstPage);
    Assert.assertFalse(findText.checkPreviousPageButtonIsEnabled());

    editor.closeAllTabsByContextMenu();
  }

  private void createFileFromAPI(String path, String fileName, String content) throws Exception {
    testProjectServiceClient.createFileInProject(workspace.getId(), path, fileName, content);
  }

  private void createFileInTerminal(String fileName) {
    terminal.typeIntoTerminal("cd " + PROJECT_NAME + Keys.ENTER);
    terminal.typeIntoTerminal("df > " + fileName + Keys.ENTER);
    terminal.typeIntoTerminal("cat " + fileName + Keys.ENTER);
    terminal.typeIntoTerminal("ls" + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal(fileName);
  }
}
