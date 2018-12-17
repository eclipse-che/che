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
package org.eclipse.che.selenium.refactor.parameters;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.DEFAULT_TIMEOUT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.openqa.selenium.Keys;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 19.11.15 */
public class RenameParametersTest {
  private static final String PROJECT_NAME = NameGenerator.generate("ParametersProject-", 4);
  private static final String pathToPackageInChePrefix = PROJECT_NAME + "/src/main/java";
  private static final String APPLY_WORKSPACE_CHANGES = "Apply Workspace Changes\nDone";

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
  @Inject private Events events;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = RenameParametersTest.this.getClass().getResource("/projects/rename-parameters");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    events.clickEventLogBtn();
  }

  @BeforeMethod
  public void setUp() throws Exception {
    events.clearAllMessages();
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
  public void checkRenameParameters0() throws Exception {
    setFieldsForTest("test0");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 17);
    editor.launchRefactorForm();
    refactor.waitRenameParametersFormIsOpen();
    refactor.typeAndWaitNewName("j");
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameParametersFormIsClosed();
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters3() throws Exception {
    setFieldsForTest("test3");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 15);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("j");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    loader.waitOnClosed();
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.goToCursorPositionVisible(15, 23);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("j1");
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters6() throws Exception {
    setFieldsForTest("test6");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 17);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("k");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters9() throws Exception {
    setFieldsForTest("test9");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 17);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("j");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters12() throws Exception {
    setFieldsForTest("test12");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 23);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("j");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters15() throws Exception {
    // preparations
    setFieldsForTest("test15");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 15);

    // rename the 'i' parameter to the 'j'
    renameLocalVariableByRefactorForm("j");

    // accept the ask dialog about duplicate parameters
    askDialog.acceptDialogWithText("Duplicate parameter j");

    loader.waitOnClosed();
    refactor.waitRenameParametersFormIsClosed();
    events.clearAllMessages();
    editor.waitActive();
    editor.goToCursorPositionVisible(15, 23);

    // rename the 'j' parameter to the 'i'
    renameLocalVariableByRefactorForm("i");
    refactor.waitRenameParametersFormIsClosed();
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitMarkerInvisibility(ERROR, 15);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters18() throws Exception {
    setFieldsForTest("test18");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 20);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("j");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters21() throws Exception {
    setFieldsForTest("test21");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 17);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("j");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters25() throws Exception {
    setFieldsForTest("test25");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(16, 16);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("j");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters28() throws Exception {
    setFieldsForTest("test28");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 18);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("j");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters31() throws Exception {
    setFieldsForTest("test31");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 30);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("kk");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void checkRenameParameters33() throws Exception {
    setFieldsForTest("test33");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(16, 12);
    editor.launchRefactorForm();
    refactor.waitRenameParametersFormIsOpen();
    refactor.setAndWaitStateUpdateReferencesCheckbox(false);
    refactor.typeAndWaitNewName("b");
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenameParametersFormIsClosed();
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    editor.waitTextIntoEditor(contentFromOutA);
    editor.closeFileByNameWithSaving("A");
  }

  private void renameLocalVariableByRefactorForm(String newValue) {
    editor.launchRefactorForm();
    refactor.waitRenameParametersFormIsOpen();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    refactor.typeAndWaitNewName(newValue);
    refactor.clickOkButtonRefactorForm();
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest;

    URL resourcesInA =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/parameters/" + nameCurrentTest + "/in/A.java");
    URL resourcesOutA =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/parameters/" + nameCurrentTest + "/out/A.java");

    contentFromInA = IoUtil.readAndCloseQuietly(resourcesInA.openStream());
    contentFromOutA = IoUtil.readAndCloseQuietly(resourcesOutA.openStream());
  }
}
