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
package org.eclipse.che.selenium.miscellaneous;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.TestGroup.OPENSHIFT;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.ActionButton.SAVE_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.MACHINES;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.executor.OpenShiftCliCommandExecutor;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = OPENSHIFT)
public class MachinesAsynchronousStartTest {
  private static final String WORKSPACE_NAME = NameGenerator.generate("test-workspace", 4);
  private static final String MACHINE_NAME = "dev-machine";
  private static final String IMAGE_NAME = "eclipse/ubuntu_jdk8";
  private static final String IMAGE_NAME_SUFFIX = NameGenerator.generate("", 4);
  private static final String NOT_EXISTED_IMAGE_NAME = IMAGE_NAME + IMAGE_NAME_SUFFIX;
  private static final String SUCCESS_NOTIFICATION_TEST = "Workspace updated.";
  private static final String GET_WORKSPACE_EVENTS_COMMAND_TEMPLATE =
      "get event --no-headers=true | grep %s | awk '{print $7 \" \" $8}'";
  private static final String EXPECTED_ERROR_NOTIFICATION_TEXT =
      format(
          "Unrecoverable event occurred: 'Failed', 'Failed to pull image \"%s\": "
              + "rpc error: code = Unknown desc = Error response from daemon: pull "
              + "access denied for %s, repository does not exist or may require 'docker login''",
          NOT_EXISTED_IMAGE_NAME, NOT_EXISTED_IMAGE_NAME);

  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private OpenShiftCliCommandExecutor openShiftCliCommandExecutor;
  @Inject private WebDriverWaitFactory webDriverWaitFactory;
  @Inject private NewWorkspace newWorkspace;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private WorkspaceDetailsMachines workspaceDetailsMachines;
  @Inject private EditMachineForm editMachineForm;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  private Workspace brokenWorkspace;

  @AfterClass
  public void cleanUp() throws Exception {
    testWorkspaceServiceClient.delete(brokenWorkspace.getNamespace(), defaultTestUser.getName());
  }

  @BeforeClass
  public void setUp() {
    // open "New Workspace" page
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitPageLoad();

    // create new workspace
    newWorkspace.typeWorkspaceName(WORKSPACE_NAME);
    newWorkspace.clickOnCreateButtonAndEditWorkspace();

    // change base image of workspace to nonexistent one
    workspaceDetails.waitToolbarTitleName(WORKSPACE_NAME);
    workspaceDetails.selectTabInWorkspaceMenu(MACHINES);
    workspaceDetailsMachines.waitMachineListItem(MACHINE_NAME);
    workspaceDetailsMachines.clickOnEditButton(MACHINE_NAME);
    editMachineForm.waitForm();
    editMachineForm.clickOnRecipeForm();
    editMachineForm.waitRecipeCursorVisibility();
    seleniumWebDriverHelper.sendKeys(IMAGE_NAME_SUFFIX);
    editMachineForm.waitRecipeText(NOT_EXISTED_IMAGE_NAME);
    editMachineForm.waitSaveButtonEnabling();
    editMachineForm.clickOnSaveButton();
    editMachineForm.waitFormInvisibility();

    // save changes
    workspaceDetailsMachines.waitImageNameInMachineListItem(MACHINE_NAME, NOT_EXISTED_IMAGE_NAME);
    workspaceDetails.waitAllEnabled(SAVE_BUTTON);
    workspaceDetails.clickOnSaveChangesBtn();
    workspaceDetailsMachines.waitNotificationMessage(SUCCESS_NOTIFICATION_TEST);

    // open "Workspaces" page
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitPageLoading();
    workspaces.waitWorkspaceIsPresent(WORKSPACE_NAME);
  }

  @Test
  public void checkWorkspace() {
    // check behavior of the broken workspace
    workspaces.clickOnWorkspaceStopStartButton(WORKSPACE_NAME);
    workspaces.waitErrorNotificationContainsText(EXPECTED_ERROR_NOTIFICATION_TEXT);

    // check openshift events log
    waitEvent("Failed");
  }

  private List<String> getWorkspaceEvents() throws Exception {
    brokenWorkspace =
        testWorkspaceServiceClient.getByName(WORKSPACE_NAME, defaultTestUser.getName());
    final String command = format(GET_WORKSPACE_EVENTS_COMMAND_TEMPLATE, brokenWorkspace.getId());
    final String events = openShiftCliCommandExecutor.execute(command);

    return asList(events.split("[\\ \\n]"));
  }

  private boolean eventIsPresent(String event) {
    try {
      return getWorkspaceEvents().contains(event);
    } catch (Exception e) {
      throw new RuntimeException("Fail of events logs reading", e);
    }
  }

  private void waitEvent(String event) {
    final int timeoutInSeconds = 12;
    final int delayBetweenRequestsInSeconds = 2;

    webDriverWaitFactory
        .get(timeoutInSeconds, delayBetweenRequestsInSeconds)
        .until((ExpectedCondition<Boolean>) driver -> eventIsPresent(event));
  }
}
