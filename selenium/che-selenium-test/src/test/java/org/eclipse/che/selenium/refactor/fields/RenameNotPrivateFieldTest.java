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
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.eclipse.che.selenium.refactor.Services;
import org.openqa.selenium.Keys;
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

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/rename-non-private-field");
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

  @Test
  public void checkRenameNotPrivateField0() throws Exception {
    setFieldsForTest("test0");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    loader.waitOnClosed();
    editor.setCursorToLine(21);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 1)
  public void checkRenameNotPrivateField1() throws Exception {
    setFieldsForTest("test1");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    loader.waitOnClosed();
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 2)
  public void checkRenameNotPrivateField2() throws Exception {
    setFieldsForTest("test2");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(13, 9);
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 3)
  public void checkRenameNotPrivateField3() throws Exception {
    setFieldsForTest("test3");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(19);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 4)
  public void checkRenameNotPrivateField4() throws Exception {
    setFieldsForTest("test4");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 5)
  public void checkRenameNotPrivateField5() throws Exception {
    setFieldsForTest("test5");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 6)
  public void checkRenameNotPrivateField6() throws Exception {
    setFieldsForTest("test6");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    loader.waitOnClosed();
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 7)
  public void checkRenameNotPrivateField7() throws Exception {
    setFieldsForTest("test7");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameFieldFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("g");
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 8)
  public void checkRenameNotPrivateField8() throws Exception {
    setFieldsForTest("test8");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameFieldFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("g");
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 9)
  public void checkRenameNotPrivateField9() throws Exception {
    setFieldsForTest("test9");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 10)
  public void checkRenameNotPrivateField10() throws Exception {
    setFieldsForTest("test10");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(13);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 11)
  public void checkRenameAnnotation24() throws Exception {
    setFieldsForTest("test24");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(14, 9);
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("ZORRO");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    loader.waitOnClosed();
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 12)
  public void checkRenameAnnotation25() throws Exception {
    setFieldsForTest("test25");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(16, 14);
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("ZORRO");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 13)
  public void checkBugFiveEightTwoOne26() throws Exception {
    setFieldsForTest("test26");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(13, 17);
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("test1");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 14)
  public void checkRenameDelegate28() throws Exception {
    setFieldsForTest("test28");
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(15);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 15)
  public void checkRenameEnumField31() throws Exception {
    setFieldsForTest("test31");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(15);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("other");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 16)
  public void checkRenameGenerics32() throws Exception {
    setFieldsForTest("test32");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 17)
  public void checkRenameGenerics33() throws Exception {
    setFieldsForTest("test33");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
    editor.typeTextIntoEditor("g");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 18)
  public void checkRenameGenerics36() throws Exception {
    setFieldsForTest("test36");
    projectExplorer.scrollAndSelectItem(pathToCurrentPackage);
    projectExplorer.openItemByPath(pathToCurrentPackage);
    projectExplorer.waitVisibleItem(pathToCurrentPackage + "/A.java");
    projectExplorer.sendToItemDownArrowKey();
    projectExplorer.sendToItemEnterKey();
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(16);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorFormFromEditor();
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
