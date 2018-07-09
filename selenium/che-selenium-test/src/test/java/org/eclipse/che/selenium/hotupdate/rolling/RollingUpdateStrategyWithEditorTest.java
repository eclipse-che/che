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
package org.eclipse.che.selenium.hotupdate.rolling;

import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.CheTestSystemClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.executor.hotupdate.HotUpdateUtil;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Ihor Okhrimenko */
public class RollingUpdateStrategyWithEditorTest {
  private static final int RESTORE_IDE_AFTER_REFRESH_TIMEOUT = 10;
  private static final String PROJECT_NAME = "default-spring-project";

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Ide ide;
  @Inject private TestWorkspace workspace;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private CheTestSystemClient cheTestSystemClient;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private HotUpdateUtil hotUpdateUtil;

  @BeforeClass
  public void prepare() throws Exception {
    Path pathToProject =
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI());

    testProjectServiceClient.importProject(
        workspace.getId(), pathToProject, PROJECT_NAME, MAVEN_SPRING);
  }

  @Test
  public void shouldUpdateMasterByRollingStrategyWithAccessibleEditorInProcess() throws Exception {
    // prepare
    int currentRevision = hotUpdateUtil.getMasterPodRevision();
    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    // check that master is running
    assertEquals(cheTestSystemClient.getStatus(), SystemStatus.RUNNING);

    hotUpdateUtil.executeMasterPodUpdateCommand();

    checkIdeAvailability();

    // check that che is updated
    hotUpdateUtil.waitMasterPodRevision(currentRevision + 1);
    hotUpdateUtil.waitFullMasterPodUpdate(currentRevision);

    // check that workspace is successfully migrated to the new master
    assertTrue(testWorkspaceServiceClient.exists(workspace.getName(), defaultTestUser.getName()));

    checkIdeAvailability();
  }

  private void checkIdeAvailability() {
    hotUpdateUtil.checkMasterPodAvailabilityByPreferencesRequest();

    seleniumWebDriver.navigate().refresh();

    projectExplorer.waitProjectExplorer(RESTORE_IDE_AFTER_REFRESH_TIMEOUT);
    projectExplorer.waitItem(PROJECT_NAME);
  }
}
