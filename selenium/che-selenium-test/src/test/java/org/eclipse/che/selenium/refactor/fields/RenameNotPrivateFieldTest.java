/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.refactor.fields;

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
import org.openqa.selenium.Keys;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 12.11.15 */
public class RenameNotPrivateFieldTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate("RenameNotPrivateFieldProject", 4);
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
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/rename-non-private-field");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
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
  public void checkRenameNotPrivateField0() throws Exception {
    setFieldsForTest("test0");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    loader.waitOnClosed();
    editor.setCursorToLine(22);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameNotPrivateField1() throws Exception {
    setFieldsForTest("test1");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    loader.waitOnClosed();
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameNotPrivateField2() throws Exception {
    setFieldsForTest("test2");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(14, 9);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameNotPrivateField3() throws Exception {
    setFieldsForTest("test3");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(20);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameNotPrivateField4() throws Exception {
    setFieldsForTest("test4");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameNotPrivateField5() throws Exception {
    setFieldsForTest("test5");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameNotPrivateField6() throws Exception {
    setFieldsForTest("test6");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    loader.waitOnClosed();
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameNotPrivateField7() throws Exception {
    setFieldsForTest("test7");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("g");
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameNotPrivateField8() throws Exception {
    setFieldsForTest("test8");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("g");
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameNotPrivateField9() throws Exception {
    setFieldsForTest("test9");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameNotPrivateField10() throws Exception {
    setFieldsForTest("test10");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameAnnotation24() throws Exception {
    setFieldsForTest("test24");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 9);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("ZORRO");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    loader.waitOnClosed();
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameAnnotation25() throws Exception {
    setFieldsForTest("test25");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(17, 14);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("ZORRO");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkBugFiveEightTwoOne26() throws Exception {
    setFieldsForTest("test26");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(14, 17);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("test1");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameDelegate28() throws Exception {
    setFieldsForTest("test28");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(16);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameEnumField31() throws Exception {
    setFieldsForTest("test31");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(16);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("other");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameGenerics32() throws Exception {
    setFieldsForTest("test32");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(15);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameGenerics33() throws Exception {
    setFieldsForTest("test33");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(15);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameGenerics36() throws Exception {
    setFieldsForTest("test36");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(17);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest;

    URL resourcesIn =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/fields/none-private/"
                    + nameCurrentTest
                    + "/in/A.java");
    URL resourcesOut =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/fields/none-private/"
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
