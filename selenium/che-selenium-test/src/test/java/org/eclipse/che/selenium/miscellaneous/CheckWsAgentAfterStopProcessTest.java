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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestWorkspaceConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * //
 *
 * @author Musienko Maxim
 */
public class CheckWsAgentAfterStopProcessTest {
  private static final String PROJECT_NAME = CheckWsAgentAfterStopProcessTest.class.getSimpleName();
  private static final String nameCommandForKillWsAgent = "killWsAgent";
  private static final String killPIDWSAgentCommand =
      "kill -9 $(ps ax | grep java | grep ws-agent | grep conf | grep -v grep | awk '{print $1}')";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Events events;
  @Inject private ToastLoader toastLoader;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING.toString());
    testCommandServiceClient.createCommand(
        killPIDWSAgentCommand,
        nameCommandForKillWsAgent,
        TestCommandsConstants.CUSTOM,
        workspace.getId());
    ide.open(workspace);
  }

  @Test
  public void checkDialogAfterKillingProcess() throws Exception {
    String expectedMessageOInDialog =
        "Workspace agent is no longer responding. To fix the problem, restart the workspace.";
    projectExplorer.waitItem(PROJECT_NAME);
    toastLoader.waitAppeareanceAndClosing();
    projectExplorer.invokeCommandWithContextMenu(
        ProjectExplorer.CommandsGoal.COMMON, PROJECT_NAME, nameCommandForKillWsAgent);
    new WebDriverWait(ide.driver(), LOAD_PAGE_TIMEOUT_SEC * 3)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[text()='" + expectedMessageOInDialog + "']")));
    new WebDriverWait(ide.driver(), REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id("ask-dialog-first")))
        .click();
    projectExplorer.waitItem(PROJECT_NAME);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestWorkspaceConstants.RUNNING_WORKSPACE_MESS);
  }
}
