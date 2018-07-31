/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.assistant;

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.OrganizeImports;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class OrganizeImportsTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(OrganizeImportsTest.class.getSimpleName(), 4);
  private static final String SOURCE_FOLDER = "src/main/java";
  private static final String PATH_TO_CLASS_IN_SPRING_PACKAGE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/" + "AppController.java";
  private static final String TEST_FILE_NAME = "TestClass.java";
  private static final String PATH_TO_A_PACKAGE = PROJECT_NAME + "/src/main/java/a";
  private static final String PATH_TO_CLASS_IN_A_PACKAGE =
      PROJECT_NAME + "/src/main/java/a/TestClass.java";
  private static final String PATH_TO_B_PACKAGE = PROJECT_NAME + "/src/main/java/b";
  private static final String PATH_TO_CLASS_IN_B_PACKAGE =
      PROJECT_NAME + "/src/main/java/b/TestClass.java";
  private static final String NAME_OF_A_PACKAGE = "a.TestClass";
  private static final String NAME_OF_B_PACKAGE = "b.TestClass";
  private static final String NAME_OF_LIST_PACKAGE = "java.util.List";
  private static final String TEST_METHOD =
      "package a;\n"
          + "public class TestClass{\n"
          + " public static void testMethod(){\n"
          + "     \n"
          + " };\n"
          + "}";
  private static final String CALL_TEST_TEXT =
      "TestClass.testMethod();\n" + "       List <String> testList=new ArrayList<>();";

  @Inject private TestWorkspace testWorkspace;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Ide ide;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private OrganizeImports organizeImports;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource =
        OrganizeImportsTest.this.getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void organizeImportsTest() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_CLASS_IN_SPRING_PACKAGE);
    loader.waitOnClosed();
    editor.waitActive();
    editor.setCursorToLine(15);
    editor.deleteCurrentLine();
    editor.waitMarkerInPosition(ERROR, 24);
    editor.waitMarkerInPosition(ERROR, 36);
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.ORGANIZE_IMPORTS);
    loader.waitOnClosed();
    editor.waitAllMarkersInvisibility(ERROR);
    Assert.assertTrue(
        editor.checkWhatTextLinePresentOnce(
            "import org.springframework.web.servlet.ModelAndView;"));

    editor.setCursorToLine(20);
    editor.typeTextIntoEditorWithoutDelayForSaving(
        "import org.springframework.web.servlet.ModelAndView;");
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(20, 8);
    editor.launchPropositionAssistPanel();
    editor.enterTextIntoFixErrorPropByEnter("Organize imports");
    loader.waitOnClosed();

    try {
      Assert.assertTrue(
          editor.checkWhatTextLinePresentOnce(
              "import org.springframework.web.servlet.ModelAndView;"));
    } catch (org.openqa.selenium.StaleElementReferenceException ex) {
      editor.selectTabByName("AppController");
      Assert.assertTrue(
          editor.checkWhatTextLinePresentOnce(
              "import org.springframework.web.servlet.ModelAndView;"));
    }
    editor.goToCursorPositionVisible(29, 23);
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.QUICK_FIX);
    editor.waitPropositionAssistContainer();
    loader.waitOnClosed();
    createNewStructure();
    editor.setCursorToLine(35);
    editor.typeTextIntoEditor(CALL_TEST_TEXT);
    editor.waitMarkerInPosition(ERROR, 36);
    editor.waitMarkerInPosition(ERROR, 37);

    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.ORGANIZE_IMPORTS);

    loader.waitOnClosed();
    organizeImports.waitOrganizeImportsDialog();
    organizeImports.selectImport(NAME_OF_A_PACKAGE);
    organizeImports.clickOnNextButton();
    organizeImports.selectImport(NAME_OF_LIST_PACKAGE);
    organizeImports.clickOnBackButton();
    organizeImports.selectImport(NAME_OF_B_PACKAGE);
    organizeImports.clickOnNextButton();
    organizeImports.clickOnFinishButton();
    editor.waitAllMarkersInvisibility(ERROR);
    loader.waitOnClosed();

    projectExplorer.waitItem(PATH_TO_CLASS_IN_SPRING_PACKAGE);
    projectExplorer.openItemByPath(PATH_TO_CLASS_IN_SPRING_PACKAGE);
    loader.waitOnClosed();

    Assert.assertTrue(editor.checkWhatTextLinePresentOnce("import b.TestClass;"));
    Assert.assertTrue(editor.checkWhatTextLinePresentOnce("import java.util.ArrayList;"));
    Assert.assertTrue(editor.checkWhatTextLinePresentOnce("import java.util.List;"));
  }

  private void createNewStructure() throws Exception {
    testProjectServiceClient.createFolder(testWorkspace.getId(), PATH_TO_A_PACKAGE);
    testProjectServiceClient.createFolder(testWorkspace.getId(), PATH_TO_B_PACKAGE);
    testProjectServiceClient.createFileInProject(
        testWorkspace.getId(), PATH_TO_A_PACKAGE, TEST_FILE_NAME, TEST_METHOD);
    testProjectServiceClient.createFileInProject(
        testWorkspace.getId(), PATH_TO_B_PACKAGE, TEST_FILE_NAME, TEST_METHOD);
  }
}
