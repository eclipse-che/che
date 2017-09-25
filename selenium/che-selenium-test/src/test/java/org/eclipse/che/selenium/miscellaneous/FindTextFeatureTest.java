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

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
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
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class FindTextFeatureTest {

  private static final String PROJECT_NAME = NameGenerator.generate("FindTextFeature", 4);

  private static final String FIND_NOTHING = "Found occurrences of 'dddhhh' (0 occurrence)";

  private static final String PR_1_EXPECTED_TEXT_1 =
      format(
          "Found occurrences of 'Filesystem' (1 occurrence)\n"
              + "/%s/readme.con\n"
              + "(1 occurrence of 'Filesystem' found)\n"
              + "1:   Filesystem 1K-blocks Used Available Use%% Mounted on",
          PROJECT_NAME);
  private static final String PR_1_EXPECTED_TEXT_2 =
      format(
          "Found occurrences of 'Feature' (1 occurrence)\n"
              + "/%s/readme.api\n"
              + "(1 occurrence of 'Feature' found)\n"
              + "1:   FindTextFeatureTest",
          PROJECT_NAME);

  private static final String PR_2_EXPECTED_TEXT_1 =
      format(
          "Found occurrences of 'String' (9 occurrences)\n"
              + "/%1$s/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java\n"
              + "(4 occurrences of 'String' found)\n"
              + "22:    private static final String secretNum = Integer.toString(new Random().nextInt(10));\n"
              + "22:    private static final String secretNum = Integer.toString(new Random().nextInt(10));\n"
              + "26:    String numGuessByUser = request.getParameter(\"numGuess\");\n"
              + "27:    String result = \"\";\n"
              + "/%1$s/my-lib/src/main/java/hello/SayHello.java\n"
              + "(2 occurrences of 'String' found)\n"
              + "20:    public String sayHello(String name)\n"
              + "20:    public String sayHello(String name)\n"
              + "/%1$s/my-lib/src/test/java/hello/SayHelloTest.java\n"
              + "(1 occurrence of 'String' found)\n"
              + "27:    public SayHelloTest(String testName)\n"
              + "/%1$s/my-webapp/src/main/webapp/WEB-INF/jsp/guess_num.jsp\n"
              + "(2 occurrences of 'String' found)\n"
              + "25:    java.lang.String attempt = (java.lang.String)request.getAttribute(\"num\");\n"
              + "25:    java.lang.String attempt = (java.lang.String)request.getAttribute(\"num\");",
          PROJECT_NAME);

  private static final String PR_3_EXPECTED_TEXT_1 =
      format(
          "Found occurrences of 'uess' (10 occurrences)\n"
              + "/%1$s/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java\n"
              + "(6 occurrences of 'uess' found)\n"
              + "26:    String numGuessByUser = request.getParameter(\"numGuess\");\n"
              + "26:    String numGuessByUser = request.getParameter(\"numGuess\");\n"
              + "29:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
              + "29:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
              + "33:    else if (numGuessByUser != null) {\n"
              + "37:    ModelAndView view = new ModelAndView(\"guess_num\");\n"
              + "/%1$s/my-webapp/src/main/webapp/WEB-INF/jsp/guess_num.jsp\n"
              + "(2 occurrences of 'uess' found)\n"
              + "17:    <form method=\"post\" action=\"guess\">\n"
              + "18:    <input type=text size=\"5\" name=\"numGuess\" >\n"
              + "/%1$s/my-webapp/src/main/webapp/index.jsp\n"
              + "(1 occurrence of 'uess' found)\n"
              + "14:    response.sendRedirect(\"spring/guess\");\n"
              + "/%1$s/my-webapp/src/main/webapp/WEB-INF/spring-servlet.xml\n"
              + "(1 occurrence of 'uess' found)\n"
              + "15:    <bean name=\"/guess\" class=\"projects.debugStepInto.src.main.java.java.org.eclipse.qa.examples.AppController\"></bean>",
          PROJECT_NAME);

  private static final String PR_4_PATH_1 =
      PROJECT_NAME + "/my-webapp/src/main/java/org/eclipse/qa/examples";
  private static final String PR_4_EXPECTED_TEXT_1 =
      format(
          "Found occurrences of 'uess' (6 occurrences)\n"
              + "/%1$s/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java\n"
              + "(6 occurrences of 'uess' found)\n"
              + "26:    String numGuessByUser = request.getParameter(\"numGuess\");\n"
              + "26:    String numGuessByUser = request.getParameter(\"numGuess\");\n"
              + "29:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
              + "29:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
              + "33:    else if (numGuessByUser != null) {\n"
              + "37:    ModelAndView view = new ModelAndView(\"guess_num\");",
          PROJECT_NAME);

  private static final String PR_4_PATH_2 = format("/%s/my-lib", PROJECT_NAME);
  private static final String PR_4_EXPECTED_TEXT_2 =
      format(
          "Found occurrences of 'hello' (15 occurrences)\n"
              + "/%1$s/my-lib/src/main/java/hello/SayHello.java\n"
              + "(5 occurrences of 'hello' found)\n"
              + "11:   package hello;\n"
              + "14:    * Hello world!\n"
              + "18:   public class SayHello\n"
              + "20:    public String sayHello(String name)\n"
              + "22:    return \"Hello, \" + name;\n"
              + "/%1$s/my-lib/src/test/java/hello/SayHelloTest.java\n"
              + "(10 occurrences of 'hello' found)\n"
              + "11:   package hello;\n"
              + "20:   public class SayHelloTest extends TestCase\n"
              + "27:    public SayHelloTest(String testName)\n"
              + "37:    return new TestSuite(SayHelloTest.class);\n"
              + "43:    public void testSayHello()\n"
              + "45:    SayHello sayHello = new SayHello();\n"
              + "45:    SayHello sayHello = new SayHello();\n"
              + "45:    SayHello sayHello = new SayHello();\n"
              + "46:    assertTrue(\"Hello, codenvy\".equals(sayHello.sayHello(\"codenvy\")));\n"
              + "46:    assertTrue(\"Hello, codenvy\".equals(sayHello.sayHello(\"codenvy\")));",
          PROJECT_NAME);

  private static final String PR_5_EXPECTED_TEXT_1 =
      format(
          "Found occurrences of 'String' (7 occurrences)\n"
              + "/%1$s/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java\n"
              + "(4 occurrences of 'String' found)\n"
              + "22:    private static final String secretNum = Integer.toString(new Random().nextInt(10));\n"
              + "22:    private static final String secretNum = Integer.toString(new Random().nextInt(10));\n"
              + "26:    String numGuessByUser = request.getParameter(\"numGuess\");\n"
              + "27:    String result = \"\";\n"
              + "/%1$s/my-lib/src/main/java/hello/SayHello.java\n"
              + "(2 occurrences of 'String' found)\n"
              + "20:    public String sayHello(String name)\n"
              + "20:    public String sayHello(String name)\n"
              + "/%1$s/my-lib/src/test/java/hello/SayHelloTest.java\n"
              + "(1 occurrence of 'String' found)\n"
              + "27:    public SayHelloTest(String testName)",
          PROJECT_NAME);

  private static final String PR_5_EXPECTED_TEXT_2 =
      format(
          "Found occurrences of 'String' (2 occurrences)\n"
              + "/%s/my-webapp/src/main/webapp/WEB-INF/jsp/guess_num.jsp\n"
              + "(2 occurrences of 'String' found)\n"
              + "25:    java.lang.String attempt = (java.lang.String)request.getAttribute(\"num\");\n"
              + "25:    java.lang.String attempt = (java.lang.String)request.getAttribute(\"num\");",
          PROJECT_NAME);

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
  @Inject private Consoles consoles;

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
    String content = "FindTextFeatureTest";
    String fileNameCreatedFromAPI = "readme.api";
    String fileNameCreatedFromTerminal = "readme.con";

    projectExplorer.waitProjectExplorer();
    projectExplorer.selectItem(PROJECT_NAME);
    if (!consoles.processesMainAreaIsOpen()) {
      consoles.clickOnProcessesTab();
    }
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
    findText.waitExpectedTextInFindInfoPanel(PR_1_EXPECTED_TEXT_1);
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
    findText.waitExpectedTextInFindInfoPanel(PR_1_EXPECTED_TEXT_2);
    findText.selectItemInFindInfoPanel(
        format("/%s/readme.api", PROJECT_NAME), "1:   FindTextFeatureTest");
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ENTER.toString());
    editor.waitActiveTabFileName(fileNameCreatedFromAPI);
    Assert.assertEquals(editor.getPositionOfLine(), 1);
  }

  @Test
  public void checkFindTextBasic() {
    String pathToQuessNumFile =
        format("/%s/my-webapp/src/main/webapp/WEB-INF/jsp/guess_num.jsp", PROJECT_NAME);
    String pathToSayHelloFile =
        format("/%s/my-lib/src/main/java/hello/SayHello.java", PROJECT_NAME);
    String pathToAppControllerFile =
        format(
            "/%s/my-webapp/src/main/java/org/eclipse/qa/examples/AppController.java", PROJECT_NAME);
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
    findText.waitExpectedTextInFindInfoPanel(PR_2_EXPECTED_TEXT_1);
    findText.selectItemInFindInfoPanel(
        pathToQuessNumFile,
        "25:    java.lang.String attempt = (java.lang.String)request.getAttribute(\"num\");");
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ENTER.toString());
    editor.waitActiveEditor();
    editor.waitActiveTabFileName("guess_num.jsp");
    editor.waitTextIntoEditor("String");
    Assert.assertEquals(editor.getPositionOfLine(), 25);
    findText.selectItemInFindInfoPanelByDoubleClick(
        pathToSayHelloFile, "20:    public String sayHello(String name)");
    editor.waitActiveEditor();
    editor.waitActiveTabFileName("SayHello");
    editor.waitTextIntoEditor("String");
    Assert.assertEquals(editor.getPositionOfLine(), 20);
    findText.selectItemInFindInfoPanel(
        pathToAppControllerFile,
        "26:    String numGuessByUser = request.getParameter(\"numGuess\");");
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ARROW_UP.toString());
    findText.sendCommandByKeyboardInFindInfoPanel(Keys.ENTER.toString());
    editor.waitActiveTabFileName("AppController");
    Assert.assertEquals(editor.getPositionOfLine(), 22);
    editor.closeAllTabsByContextMenu();
  }

  @Test
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
    findText.waitExpectedTextInFindInfoPanel(PR_3_EXPECTED_TEXT_1);
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("uess");
    findText.waitTextIntoFindField("uess");
    findText.setAndWaitWholeWordCheckbox(true);
    findText.waitPathIntoRootField("/" + PROJECT_NAME);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel("Found occurrences of 'uess' (0 occurrence)");
  }

  @Test
  public void checkFindIntoSelectedPath() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.selectItem(PR_4_PATH_1);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.FIND);
    findText.waitFindTextMainFormIsOpen();
    findText.typeTextIntoFindField("uess");
    findText.waitTextIntoFindField("uess");
    findText.setAndWaitStateSearchRootCheckbox(false);
    findText.waitPathIntoRootField("/" + PR_4_PATH_1);
    findText.clickOnSearchButtonMainForm();
    findText.waitExpectedTextInFindInfoPanel(PR_4_EXPECTED_TEXT_1);

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
    findText.waitPathIntoRootField(PR_4_PATH_2);
    findText.clickOnSearchButtonMainForm();
    findText.waitFindInfoPanelIsOpen();
    findText.waitExpectedTextInFindInfoPanel(PR_4_EXPECTED_TEXT_2);
  }

  @Test
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
    findText.waitExpectedTextInFindInfoPanel(PR_5_EXPECTED_TEXT_1);
    findText.waitExpectedTextIsNotPresentInFindInfoPanel(PR_5_EXPECTED_TEXT_2);

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
    findText.waitExpectedTextIsNotPresentInFindInfoPanel(PR_5_EXPECTED_TEXT_1);
    findText.waitExpectedTextInFindInfoPanel(PR_5_EXPECTED_TEXT_2);
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
