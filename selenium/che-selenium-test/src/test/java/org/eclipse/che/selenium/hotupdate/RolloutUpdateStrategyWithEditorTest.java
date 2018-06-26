/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.hotupdate;

import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.CheTestSystemClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.executor.OpenShiftCliCommandExecutor;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RolloutUpdateStrategyWithEditorTest {
  private static final int TIMEOUT_FOR_ROLLING_UPDATE_FINISH = 100;
  private static final int RESTORE_IDE_AFTER_REFRESH_TIMEOUT = 10;
  private static final int EXPECTED_PREFERENCES_RESPONCE_CODE = 200;
  private static final String PROJECT_NAME = "default-spring-project";
  private static final String ROLLOUT_COMMAND = "rollout latest che";
  private static final String COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE =
      "get dc | grep che | awk '{print $2}'";

  @Inject private Wizard projectWizard;
  @Inject private Menu menu;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles console;
  @Inject private CodenvyEditor editor;
  @Inject private Ide ide;
  @Inject private TestWorkspace workspace;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private WebDriverWaitFactory webDriverWaitFactory;
  @Inject private CheTestSystemClient cheTestSystemClient;
  @Inject private OpenShiftCliCommandExecutor openShiftCliCommandExecutor;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private DefaultTestUser defaultTestUser;

  @BeforeMethod
  public void prepare() throws Exception {
    Path pathToProject =
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI());

    testProjectServiceClient.importProject(
        workspace.getId(), pathToProject, PROJECT_NAME, MAVEN_SPRING);
  }

  @Test
  public void shouldUpdateMasterByRolloutStrategyWithAccessibleEditorInProcess() throws Exception {
    int currentRevision = getRevision();

    // check editor availability

    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    // check master status
    assertEquals(cheTestSystemClient.getStatus(), SystemStatus.RUNNING);

    // execute rollout
    executeRolloutUpdateCommand();

    // check IDE availability
    assertEquals(
        testUserPreferencesServiceClient.getPreferencesResponseCode(),
        EXPECTED_PREFERENCES_RESPONCE_CODE);

    seleniumWebDriver.navigate().refresh();

    projectExplorer.waitProjectExplorer(RESTORE_IDE_AFTER_REFRESH_TIMEOUT);
    projectExplorer.waitItem(PROJECT_NAME);

    // check that che is updated
    waitRevision(currentRevision + 1);
    assertTrue(testWorkspaceServiceClient.exists(workspace.getName(), defaultTestUser.getName()));
  }

  private int getRevision() {
    try {
      return Integer.parseInt(
          openShiftCliCommandExecutor.execute(COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE));
    } catch (IOException ex) {
      throw new RuntimeException(ex.getLocalizedMessage(), ex);
    }
  }

  private void waitRevision(int expectedRevision) {
    webDriverWaitFactory
        .get(TIMEOUT_FOR_ROLLING_UPDATE_FINISH)
        .until((ExpectedCondition<Boolean>) driver -> expectedRevision == getRevision());
  }

  private void executeRolloutUpdateCommand() throws Exception {
    openShiftCliCommandExecutor.execute(ROLLOUT_COMMAND);
  }
}
