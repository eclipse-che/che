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
package org.eclipse.che.selenium.refactor.methods;

import com.google.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.eclipse.che.selenium.refactor.Services;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class RenameStaticMethodsTest {
  private static final Logger LOG = LoggerFactory.getLogger(RenameStaticMethodsTest.class);
  private static final String nameOfProject =
      RenameStaticMethodsTest.class.getSimpleName() + new Random().nextInt(9999);
  private static final String pathToPackageInChePrefix =
      nameOfProject + "/src" + "/main" + "/java" + "/renameStaticMethods";
  private static final String testsFail5ErrorMess =
      "Related method 'm' (declared in 'renameStaticMethods.testFail5.A') is native. Renaming will cause an UnsatisfiedLinkError on runtime.";

  private String pathToCurrentPackage;
  private String contentFromInA;
  private String contentFromOutB;
  private URL resourcesInA;
  private URL resourcesOutB;
  private URL resourceOutA;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private AskDialog askDialog;
  @Inject private Consoles consoles;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setup() throws Exception {
    URL resource = getClass().getResource("/projects/RenameStaticMethods");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        nameOfProject,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
    new Services(projectExplorer, notificationsPopupPanel, refactor)
        .expandRenamePrivateMethodProject(nameOfProject, "renameStaticMethods");
    consoles.closeProcessesArea();
  }

  @BeforeMethod
  public void expandTreeOfProject(Method testName) throws IOException {
    try {
      loader.waitOnClosed();
      if (refactor.isWidgetOpened()) {
        refactor.clickCancelButtonRefactorForm();
      }
      setFieldsForTest(testName.getName());
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @AfterMethod
  public void closeForm() {
    try {
      if (refactor.isWidgetOpened()) {
        loader.waitOnClosed();
        refactor.clickCancelButtonRefactorForm();
      }
      editor.closeAllTabs();
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @Test
  public void test0() {
    doRefactoringWithKeys(13, 14, "k");
  }

  @Test(priority = 1)
  public void test2() {
    doRefactoringWithKeys(13, 17, "k");
  }

  @Test(priority = 2)
  public void test8() {
    doRefactorByWizard(13, 17, "k");
    editor.waitTextIntoEditor(contentFromOutB);
  }

  @Test(priority = 3)
  public void testFail5() {
    doRefactorByWizardWithExpectedWarningMessage(14, 24, "k", testsFail5ErrorMess);
  }

  @Test(priority = 4)
  public void test11() throws Exception {
    contentFromOutB = getTextFromFile(resourcesOutB);

    String contentFromOutA = getTextFromFile(resourceOutA);
    doRefactorByWizard(15, 23, "fred");
    editor.waitTextIntoEditor(contentFromOutA);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/B.java");
    editor.waitTextIntoEditor(contentFromOutB);
  }

  private void doRefactoringWithKeys(
      int cursorPositionLine, int cursorPositionChar, String newName) {
    prepareProjectForRefactor(cursorPositionLine, cursorPositionChar);
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("k");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutB);
  }

  private void doRefactorByWizard(int cursorPositionLine, int cursorPositionChar, String newName) {
    prepareProjectForRefactor(cursorPositionLine, cursorPositionChar);
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameMethodFormIsOpen();
    refactor.typeNewName(newName);
    refactor.sendKeysIntoField(Keys.ARROW_LEFT.toString());
    refactor.sendKeysIntoField(Keys.ARROW_LEFT.toString());
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameMethodFormIsClosed();
  }

  private void doRefactorByWizardWithExpectedWarningMessage(
      int cursorPositionLine,
      int cursorPositionChar,
      String newName,
      String expectedWarningMessage) {

    prepareProjectForRefactor(cursorPositionLine, cursorPositionChar);
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameMethodFormIsOpen();
    refactor.typeNewName(newName);
    refactor.clickOkButtonRefactorForm();
    askDialog.waitFormToOpen();
    askDialog.containsText(expectedWarningMessage);
    askDialog.clickCancelBtn();
    askDialog.waitFormToClose();
    refactor.clickCancelButtonRefactorForm();
  }

  private void prepareProjectForRefactor(int cursorPositionLine, int cursorPositionChar) {
    projectExplorer.waitItem(pathToPackageInChePrefix);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(cursorPositionLine, cursorPositionChar);
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest;

    resourcesInA =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/methods/static/"
                    + nameCurrentTest
                    + "/in/A.java");
    resourcesOutB =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/methods/static/"
                    + nameCurrentTest
                    + "/out/B.java");
    resourceOutA =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/methods/static/"
                    + nameCurrentTest
                    + "/out/A.java");

    contentFromInA = getTextFromFile(resourcesInA);
    contentFromOutB = getTextFromFile(resourcesOutB);
  }

  private String getTextFromFile(URL url) throws Exception {
    String result = "";
    List<String> listWithAllLines =
        Files.readAllLines(Paths.get(url.toURI()), Charset.forName("UTF-8"));
    for (String buffer : listWithAllLines) {
      result += buffer + '\n';
    }

    return result;
  }
}
