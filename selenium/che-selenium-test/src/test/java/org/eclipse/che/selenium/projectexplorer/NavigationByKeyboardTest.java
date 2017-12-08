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
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author mmusienko
 * @author Andrey Chizhikov
 */
public class NavigationByKeyboardTest {

  private static final String PROJECT_NAME = NavigationByKeyboardTest.class.getSimpleName();

  private String nameFirstModule = "External Libraries";
  private String nameSecondModule = "org.eclipse.qa.examples";
  private final String PATH_TO_SECOND_MODULE =
      PROJECT_NAME + "/src/main/java/" + nameSecondModule.replace('.', '/');
  private final String EXPECTED_TEXT_IN_JAVA_FILE =
      "package org.eclipse.qa.examples;\n"
          + "\n"
          + "import java.util.Random;\n"
          + "\n"
          + "import org.springframework.web.servlet.ModelAndView;\n"
          + "import org.springframework.web.servlet.mvc.Controller;\n"
          + "\n"
          + "import javax.servlet.http.HttpServletRequest;\n"
          + "import javax.servlet.http.HttpServletResponse;\n"
          + "\n"
          + "public class AppController implements Controller {\n"
          + "    private static final String secretNum = Integer.toString(new Random().nextInt(10));\n"
          + "\n"
          + "    @Override\n"
          + "    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {\n"
          + "        String numGuessByUser = request.getParameter(\"numGuess\");\n"
          + "        String result = \"\";\n"
          + "        \n"
          + "        if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "            result = \"Congrats! The number is \" + secretNum;\n"
          + "        } \n"
          + "        \n"
          + "        else if (numGuessByUser != null) {\n"
          + "            result = \"Sorry, you failed. Try again later!\";\n"
          + "        }\n"
          + "\n"
          + "        ModelAndView view = new ModelAndView(\"guess_num\");\n"
          + "        view.addObject(\"num\", result);\n"
          + "        return view;\n"
          + "    }\n"
          + "}\n";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    consoles.closeProcessesArea();
  }

  @Test
  public void navigationByKeyboard() throws Exception {
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitVisibleItem(PROJECT_NAME + "/src");

    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.waitItemIsSelected(PROJECT_NAME + "/src");

    projectExplorer.sendToItemEnterKey();
    projectExplorer.waitItemInVisibleArea("main");
    projectExplorer.waitItemInVisibleArea("test");

    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.waitItemIsSelected(PROJECT_NAME + "/src/main");

    projectExplorer.sendToItemRightArrowKey();
    projectExplorer.waitItemInVisibleArea("java");
    projectExplorer.waitItemInVisibleArea("webapp");

    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.waitItemIsSelected(PROJECT_NAME + "/src/main/java");

    projectExplorer.sendToItemEnterKey();
    projectExplorer.waitItemInVisibleArea(nameSecondModule);
    projectExplorer.waitItemInVisibleArea("com.example");

    projectExplorer.selectItem(PATH_TO_SECOND_MODULE);
    projectExplorer.waitItemIsSelected(PATH_TO_SECOND_MODULE);

    projectExplorer.sendToItemRightArrowKey();
    checkItemsOfTheSecondModuleIsVisible();

    projectExplorer.sendToItemLeftArrowKey();
    checkItemsOfTheSecondModuleIsNotVisible();

    projectExplorer.sendToItemEnterKey();
    checkItemsOfTheSecondModuleIsVisible();

    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.waitItemIsSelected(PATH_TO_SECOND_MODULE + "/AppController.java");

    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(EXPECTED_TEXT_IN_JAVA_FILE);

    projectExplorer.selectVisibleItem(nameFirstModule);
    projectExplorer.sendToItemEnterKey();
    checkLibrariesIsVisible();

    projectExplorer.sendToItemLeftArrowKey();
    checkLibrariesIsNotVisible();

    projectExplorer.sendToItemRightArrowKey();
    checkLibrariesIsVisible();

    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    projectExplorer.waitItemInVisibleArea("META-INF");
    projectExplorer.waitItemInVisibleArea("javax.servlet");

    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemRightArrowKey();
    projectExplorer.waitItemInVisibleArea("MANIFEST.MF");
    projectExplorer.selectVisibleItem(nameFirstModule);

    projectExplorer.sendToItemEnterKey();
    checkServletInnerFilesIsNotVisible();
    checkLibrariesIsNotVisible();

    projectExplorer.sendToItemEnterKey();
    checkLibrariesIsVisible();
    checkServletInnerFilesIsNotVisible();
  }

  private void checkItemsOfTheSecondModuleIsNotVisible() {
    String modulePath = nameSecondModule.replace('.', '/');
    projectExplorer.waitItemIsNotPresentVisibleArea(
        PROJECT_NAME + "/src/main/java/" + modulePath + "/AppController.java");
    projectExplorer.waitItemIsNotPresentVisibleArea(
        PROJECT_NAME + "/src/main/java/" + modulePath + "/LessFile.less");
    projectExplorer.waitItemIsNotPresentVisibleArea(
        PROJECT_NAME + "/src/main/java/" + modulePath + "/another");
    projectExplorer.waitItemIsNotPresentVisibleArea(
        PROJECT_NAME + "/src/main/java/" + modulePath + "/sqlFile.sql");
  }

  private void checkItemsOfTheSecondModuleIsVisible() {
    String modulePath = nameSecondModule.replace('.', '/');
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/" + modulePath + "/AppController.java");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/" + modulePath + "/LessFile.less");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/" + modulePath + "/another");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/" + modulePath + "/sqlFile.sql");
  }

  private void checkLibrariesIsVisible() {
    projectExplorer.waitItemInVisibleArea("servlet-api-2.5.jar");
    projectExplorer.waitItemInVisibleArea("spring-asm-3.0.5.RELEASE.jar");
    projectExplorer.waitItemInVisibleArea("spring-beans-3.0.5.RELEASE.jar");
    projectExplorer.waitItemInVisibleArea("junit-4.12.jar");
  }

  private void checkLibrariesIsNotVisible() {
    projectExplorer.waitItemIsNotPresentVisibleArea("servlet-api-2.5.jar");
    projectExplorer.waitItemIsNotPresentVisibleArea("spring-asm-3.0.5.RELEASE.jar");
    projectExplorer.waitItemIsNotPresentVisibleArea("spring-beans-3.0.5.RELEASE.jar");
    projectExplorer.waitItemIsNotPresentVisibleArea("junit-4.12.jar");
  }

  private void checkServletInnerFilesIsNotVisible() {
    projectExplorer.waitItemIsNotPresentVisibleArea("META-INF");
    projectExplorer.waitItemIsNotPresentVisibleArea("javax.servlet");
    projectExplorer.waitItemIsNotPresentVisibleArea("MANIFEST.MF");
  }
}
