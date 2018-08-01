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
package org.eclipse.che.selenium.refactor.methods;

import com.google.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class RenameMethodInInterfaceTest {
  private static final Logger LOG = LoggerFactory.getLogger(RenameMethodInInterfaceTest.class);
  private static final String nameOfProject =
      NameGenerator.generate(RenameMethodInInterfaceTest.class.getSimpleName(), 3);
  private static final String pathToPackageInChePrefix =
      nameOfProject + "/src" + "/main" + "/java" + "/renameMethodsInInterface";
  private static final String expectedWarnMessForFail5 =
      "A related type declares a method with the new name (and same number of parameters)";
  private static final String expectedWarnMessForFail12 =
      "Hierarchy declares a method 'k' with the same number of parameters and the same parameter type names.";
  private static final String expectedErrMessForFail33 =
      "Cannot rename this method because it is a special case (see the language specification section 9.2 for details)";

  private String pathToCurrentPackage;
  private String contentFromInA;
  private String contentFromOutA;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private AskDialog askDialog;
  @Inject private Consoles consoles;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setup() throws Exception {
    URL resource = getClass().getResource("/projects/RenameMethodsInInterface");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        nameOfProject,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
    projectExplorer.waitVisibleItem(nameOfProject);
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
  }

  @BeforeMethod
  public void expandTreeOfProject(Method testName) throws IOException {
    try {
      setFieldsForTest(testName.getName());
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @AfterMethod
  public void closeForm() {
    try {
      if (refactor.isWidgetOpened()) refactor.clickCancelButtonRefactorForm();
      editor.closeAllTabs();
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @Test
  public void test0() {
    doRefactoringWithKeys(18, 10, "k");
  }

  @Test(priority = 1)
  public void test12() {
    doRefactoringWithKeys(14, 6, "k");
  }

  @Test(priority = 2)
  public void test20() {
    doRefactoringWithKeys(14, 10, "k");
  }

  @Test(priority = 3)
  public void test31() {
    doRefactoringWithKeys(14, 6, "k");
  }

  @Test(priority = 4)
  public void test44() {
    doRefactoringWithKeys(14, 10, "k");
  }

  @Test(priority = 5)
  public void testAnnotation1() {
    doRefactorByWizard(24, 8, "number");
    editor.waitTextIntoEditor(contentFromOutA);
  }

  @Test(priority = 6)
  public void testFail5() {
    doRefactorByWizardWithExpectedWarningMessage(14, 10, "k", expectedWarnMessForFail5);
  }

  @Test(priority = 7)
  public void testFail12() {
    doRefactorByWizardWithExpectedWarningMessage(15, 17, "k", expectedWarnMessForFail12);
  }

  @Test(priority = 8)
  public void testFail33() {
    doRefactorByWizardWithExpectedWarningMessage(14, 12, "toString", expectedErrMessForFail33);
  }

  @Test(priority = 9)
  public void testGenerics01() {
    doRefactoringWithKeys(19, 24, "zYXteg");

    editor.waitTextIntoEditor(contentFromOutA);
  }

  private void doRefactoringWithKeys(
      int cursorPositionLine, int cursorPositionChar, String newName) {
    prepareProjectForRefactor(cursorPositionLine, cursorPositionChar);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor(newName);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    loader.waitOnClosed();
    editor.waitTextIntoEditor(contentFromOutA);
  }

  private void doRefactorByWizard(int cursorPositionLine, int cursorPositionChar, String newName) {
    prepareProjectForRefactor(cursorPositionLine, cursorPositionChar);
    editor.launchRefactorForm();
    refactor.waitRenameMethodFormIsOpen();
    refactor.typeAndWaitNewName(newName);
    refactor.sendKeysIntoField(Keys.ARROW_LEFT.toString());
    refactor.sendKeysIntoField(Keys.ARROW_LEFT.toString());
    refactor.clickOkButtonRefactorForm();
  }

  private void doRefactorByWizardWithExpectedWarningMessage(
      int cursorPositionLine,
      int cursorPositionChar,
      String newName,
      String expectedWarningMessage) {

    prepareProjectForRefactor(cursorPositionLine, cursorPositionChar);
    editor.launchRefactorForm();
    refactor.waitRenameMethodFormIsOpen();
    refactor.typeAndWaitNewName(newName);
    WaitUtils.sleepQuietly(1);
    refactor.sendKeysIntoField(Keys.ARROW_LEFT.toString());
    refactor.sendKeysIntoField(Keys.ARROW_LEFT.toString());
    WaitUtils.sleepQuietly(1);
    refactor.clickOkButtonRefactorForm();
    askDialog.waitFormToOpen();
    askDialog.containsText(expectedWarningMessage);
    askDialog.clickCancelBtn();
    askDialog.waitFormToClose();
    refactor.clickCancelButtonRefactorForm();
  }

  private void prepareProjectForRefactor(int cursorPositionLine, int cursorPositionChar) {
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(cursorPositionLine, cursorPositionChar);
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest;

    URL resourcesIn =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/methods/interface/"
                    + nameCurrentTest
                    + "/in/A.java");
    URL resourcesOut =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/methods/interface/"
                    + nameCurrentTest
                    + "/out/A.java");

    contentFromInA = getTextFromFile(resourcesIn);
    contentFromOutA = getTextFromFile(resourcesOut);
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
