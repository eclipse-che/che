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
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.WarningDialog;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skorik Sergey */
public class CheckErrorMessageWhenCreationDuplicateFolderOrFileTest {
  private static final String PROJECT_NAME =
      CheckErrorMessageWhenCreationDuplicateFolderOrFileTest.class.getSimpleName();
  private static final String DUPLICATED_FOLDER_NAME = "src";
  private static final String DUPLICATED_FILE_NAME = "pom.xml";
  private static final String ERROR_DIALOG_MESSAGE = "Resource already exists";
  private static final String NOTIFICATION_MESSAGE = "Failed to create resource";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Menu menu;
  @Inject private Events events;
  @Inject private WarningDialog warningDialog;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void checkDuplicatedFile() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitVisibilityByName(DUPLICATED_FILE_NAME);
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(DUPLICATED_FILE_NAME);
    askForValueDialog.clickOkBtn();
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClose(NOTIFICATION_MESSAGE);
    askForValueDialog.waitFormToClose();
    events.clickEventLogBtn();
    events.waitExpectedMessage(NOTIFICATION_MESSAGE);
  }

  @Test(priority = 1)
  public void checkDuplicatedFolder() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.FOLDER);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(DUPLICATED_FOLDER_NAME);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    warningDialog.waitWaitWarnDialogWindowWithSpecifiedTextMess(ERROR_DIALOG_MESSAGE);
    warningDialog.clickOkBtn();
    warningDialog.waitWaitClosingWarnDialogWindow();
  }
}
