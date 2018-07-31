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
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.BUILD_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.DEBUG_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.RUN_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.DEBUG_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ECLIPSE_VERTX;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromEclipseVertxStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String HEALTH_CHECKS_BOOSTER_PROJECT = "vertx-health-checks-booster";
  private static final String HEALTH_HTTP_BOOSTER_PROJECT = "vertx-http-booster";

  private List<String> projects =
      ImmutableList.of(HEALTH_CHECKS_BOOSTER_PROJECT, HEALTH_HTTP_BOOSTER_PROJECT);
  private String currentWindow;

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromEclipseVertxStack() {
    createWorkspaceHelper.createWorkspaceFromStackWithProjects(
        ECLIPSE_VERTX, WORKSPACE_NAME, projects);

    currentWindow = ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(HEALTH_CHECKS_BOOSTER_PROJECT);
    projectExplorer.waitProjectInitialization(HEALTH_HTTP_BOOSTER_PROJECT);
  }

  @Test(priority = 1)
  public void checkVertxHealthChecksBoosterProjectCommands() {
    By textOnPreviewPage = By.id("_vert_x_health_check_booster");

    // build and run web application
    consoles.executeCommandFromProjectExplorer(
        HEALTH_CHECKS_BOOSTER_PROJECT, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);
    consoles.executeCommandFromProjectExplorer(
        HEALTH_CHECKS_BOOSTER_PROJECT,
        RUN_GOAL,
        RUN_COMMAND,
        "[INFO] INFO: Succeeded in deploying verticle");
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);
    consoles.closeProcessTabWithAskDialog(RUN_COMMAND);

    consoles.executeCommandFromProcessesArea(
        "dev-machine",
        DEBUG_GOAL,
        DEBUG_COMMAND,
        "[INFO] Listening for transport dt_socket at address: 5005");
    consoles.closeProcessTabWithAskDialog(DEBUG_COMMAND);
  }

  @Test(priority = 2)
  public void checkVertxHttpBoosterProjectCommands() {
    By textOnPreviewPage = By.id("_http_booster");

    // build and run web application
    consoles.executeCommandFromProjectExplorer(
        HEALTH_HTTP_BOOSTER_PROJECT, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);
    consoles.executeCommandFromProjectExplorer(
        HEALTH_HTTP_BOOSTER_PROJECT,
        RUN_GOAL,
        RUN_COMMAND,
        "[INFO] INFO: Succeeded in deploying verticle");

    // refresh application web page and check visibility of web element on opened page
    checkApplicationPage(textOnPreviewPage);

    consoles.closeProcessTabWithAskDialog(RUN_COMMAND);

    consoles.executeCommandFromProcessesArea(
        "dev-machine",
        DEBUG_GOAL,
        DEBUG_COMMAND,
        "[INFO] Listening for transport dt_socket at address: 5005");
    consoles.closeProcessTabWithAskDialog(DEBUG_COMMAND);
  }

  private void checkApplicationPage(By webElement) {
    consoles.waitPreviewUrlIsPresent();
    consoles.waitPreviewUrlIsResponsive(10);
    consoles.clickOnPreviewUrl();

    seleniumWebDriverHelper.switchToNextWindow(currentWindow);

    seleniumWebDriver.navigate().refresh();
    seleniumWebDriverHelper.waitVisibility(webElement, LOADER_TIMEOUT_SEC);

    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
  }
}
