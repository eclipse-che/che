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
package org.eclipse.che.selenium.refactor.fields;

import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.eclipse.che.selenium.refactor.Services;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 08.11.15 */
public class RenamePrivateFieldTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate("CheckRenamePrivateFieldProject", 4);
  private static final String pathToPackageInChePrefix = PROJECT_NAME + "/src/main/java";

  private String pathToCurrentPackage;
  private String contentFromInA;
  private String contentFromOutA;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private Consoles consoles;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/rename-private-field");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    new Services(projectExplorer, notificationsPopupPanel, refactor)
        .expandSpringProjectNodes(PROJECT_NAME);
  }

  @AfterMethod
  public void closeForm() {
    if (refactor.isWidgetOpened()) {
      refactor.clickCancelButtonRefactorForm();
    }
    if (editor.isAnyTabsOpened()) {
      editor.closeAllTabs();
    }
  }

  @Test
  public void checkRenamePrivateField0() throws Exception {
    setFieldsForTest("test0");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("g");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField1() throws Exception {
    setFieldsForTest("test1");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("g");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField2() throws Exception {
    setFieldsForTest("test2");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("g");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(false);
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField3() throws Exception {
    setFieldsForTest("test3");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("gg");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateCommentsAndStringsCheckbox(true);
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenameFieldFormIsClosed();
    loader.waitOnClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField4() throws Exception {
    setFieldsForTest("test4");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(18);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("fYou");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.setAndWaitStateCommentsAndStringsCheckbox(true);
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenameFieldFormIsClosed();
    loader.waitOnClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField5() throws Exception {
    setFieldsForTest("test5");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("fYou");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenameFieldFormIsClosed();
    loader.waitOnClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField6() throws Exception {
    setFieldsForTest("test6");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("fYou");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenameFieldFormIsClosed();
    loader.waitOnClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField7() throws Exception {
    setFieldsForTest("test7");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("fSmall");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenameFieldFormIsClosed();
    loader.waitOnClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField8() throws Exception {
    setFieldsForTest("test8");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("g");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField9() throws Exception {
    setFieldsForTest("test9");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("fSmall");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField10() throws Exception {
    setFieldsForTest("test10");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(19);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    loader.waitOnClosed();
    typeAndWaitNewName("fElements");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenameFieldFormIsClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenamePrivateField11() throws Exception {
    setFieldsForTest("test11");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    consoles.closeProcessesArea();
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(21);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    loader.waitOnClosed();
    typeAndWaitNewName("fElements");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenameFieldFormIsClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameUnicode12() throws Exception {
    setFieldsForTest("test12");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    typeAndWaitNewName("feel");
    loader.waitOnClosed();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenameFieldFormIsClosed();
    loader.waitOnClosed();
    waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest;

    URL resourcesIn =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/fields/private/"
                    + nameCurrentTest
                    + "/in/A.java");
    URL resourcesOut =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/fields/private/"
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

  private void typeAndWaitNewName(String newName) {
    try {
      refactor.typeAndWaitNewName(newName);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7500");
    }
  }

  private void waitTextIntoEditor(String expectedText) {
    try {
      editor.waitTextIntoEditor(expectedText);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7500");
    }
  }
}
