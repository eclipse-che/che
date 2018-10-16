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
package org.eclipse.che.selenium.refactor.packages;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev 22.01.16 */
public class RenamePackageSpringTest {
  private static final String PROJECT_NAME_1 =
      NameGenerator.generate("CheckRenamePackageSpringApp-", 4);
  private static final String PROJECT_NAME_2 =
      NameGenerator.generate("RenamePackageWithMainMethod-", 4);

  private static final String NEW_NAME_PACKAGE = "org.eclipse.dev.examples";
  private static final String OLD_NAME_PACKAGE = "org.eclipse.qa.examples";

  private static final String WARNING_TEXT =
      "Type org.eclipse.qa.examples.HelloWorld contains a main method - some "
          + "applications (such as scripts) may not work after refactoring.";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private Menu menu;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private AskDialog askDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME_1,
        ProjectTemplates.MAVEN_SPRING);

    resource = getClass().getResource("/projects/spring-project-with-main-method");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME_2,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
  }

  @Test(priority = 1)
  public void checkRenamePackageSpringApp() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME_1);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();

    // check the project tree is not wrap after renaming the package
    projectExplorer.waitAndSelectItem(PROJECT_NAME_1 + "/src/main/java/org/eclipse/qa/examples");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);

    refactor.waitRenamePackageFormIsOpen();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    loader.waitOnClosed();
    refactor.sendKeysIntoField(NEW_NAME_PACKAGE);
    refactor.waitTextIntoNewNameField(NEW_NAME_PACKAGE);
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.openItemByPath(
        PROJECT_NAME_1 + "/src/main/java/org/eclipse/dev/examples/AppController.java");
    editor.waitTextIntoEditor(NEW_NAME_PACKAGE);
    projectExplorer.waitAndSelectItem(PROJECT_NAME_1 + "/src/main/java/org/eclipse/dev/examples");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);

    refactor.waitRenamePackageFormIsOpen();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    loader.waitOnClosed();
    refactor.sendKeysIntoField(OLD_NAME_PACKAGE);
    refactor.waitTextIntoNewNameField(OLD_NAME_PACKAGE);
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    loader.waitOnClosed();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItem(PROJECT_NAME_1 + "/src/main/java/org/eclipse/qa/examples");
  }

  @Test(priority = 2)
  public void checkRenamePackageWithMainMethod() {
    // check the warning dialog for static main method
    projectExplorer.waitItem(PROJECT_NAME_2);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.waitAndSelectItem(PROJECT_NAME_2 + "/src/main/java/org/eclipse/qa/examples");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);

    refactor.waitRenamePackageFormIsOpen();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    loader.waitOnClosed();
    refactor.sendKeysIntoField("org.eclipse.dev.examples");
    refactor.waitTextIntoNewNameField(NEW_NAME_PACKAGE);
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    askDialog.acceptDialogWithText(WARNING_TEXT);
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItem(PROJECT_NAME_2 + "/src/main/java/org/eclipse/dev/examples");
  }
}
