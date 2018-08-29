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
package org.eclipse.che.selenium.dashboard.workspaces.details;

import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.ActionButton.APPLY_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.ActionButton.CANCEL_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.ActionButton.SAVE_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.MACHINES;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WorkspaceDetailsMachinesRamTest {
  private static final String MACHINE_NAME = "dev-machine";
  private static final String IMAGE_NAME = "eclipse/ubuntu_jdk8";
  private static final String EXPECTED_RAM_VALUE = "2";
  private static final String MAX_RAM_VALID_VALUE = "100";
  private static final String MIN_RAM_VALID_VALUE = "0.5";
  private static final String SUCCESS_NOTIFICATION_MESSAGE = "Workspace updated.";

  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private WorkspaceDetailsMachines workspaceDetailsMachines;
  @Inject private TestWorkspace testWorkspace;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  @BeforeClass
  public void setup() throws Exception {
    // open workspace details "Machines" page
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitPageLoading();
    workspaces.selectWorkspaceItemName(testWorkspace.getName());
    workspaceDetails.waitToolbarTitleName(testWorkspace.getName());
    workspaceDetails.selectTabInWorkspaceMenu(MACHINES);
  }

  @Test
  public void checkRamSection() {
    workspaceDetailsMachines.waitMachineListItemWithAttributes(
        MACHINE_NAME, IMAGE_NAME, EXPECTED_RAM_VALUE);

    // check behavior with invalid RAM value
    workspaceDetailsMachines.typeRamAmount(MACHINE_NAME, "");
    workspaceDetailsMachines.waitInvalidRamHighlighting(MACHINE_NAME);
    workspaceDetails.waitAllInvisibility(SAVE_BUTTON, CANCEL_BUTTON, APPLY_BUTTON);

    // check saving behavior
    workspaceDetailsMachines.typeRamAmount(MACHINE_NAME, "3");
    workspaceDetailsMachines.waitValidRamHighlighting(MACHINE_NAME);
    workspaceDetails.waitAllEnabled(SAVE_BUTTON);
    workspaceDetails.waitAndClickOn(SAVE_BUTTON);
    workspaceDetailsMachines.waitNotificationMessage(SUCCESS_NOTIFICATION_MESSAGE);

    // check increment, decrement RAM buttons
    workspaceDetailsMachines.typeRamAmount(MACHINE_NAME, MAX_RAM_VALID_VALUE);
    workspaceDetailsMachines.waitValidRamHighlighting(MACHINE_NAME);
    workspaceDetails.waitAllEnabled(SAVE_BUTTON);

    workspaceDetailsMachines.clickOnIncrementRamButton(MACHINE_NAME);
    workspaceDetailsMachines.waitValidRamHighlighting(MACHINE_NAME);
    workspaceDetailsMachines.waitRamAmount(MACHINE_NAME, MAX_RAM_VALID_VALUE);

    workspaceDetailsMachines.clickOnDecrementRamButton(MACHINE_NAME);
    workspaceDetailsMachines.waitValidRamHighlighting(MACHINE_NAME);
    workspaceDetailsMachines.waitRamAmount(MACHINE_NAME, "99.5");

    workspaceDetailsMachines.typeRamAmount(MACHINE_NAME, MIN_RAM_VALID_VALUE);
    workspaceDetailsMachines.waitValidRamHighlighting(MACHINE_NAME);
    workspaceDetails.waitAllEnabled(SAVE_BUTTON);

    workspaceDetailsMachines.clickOnDecrementRamButton(MACHINE_NAME);
    workspaceDetailsMachines.waitRamAmount(MACHINE_NAME, MIN_RAM_VALID_VALUE);
    workspaceDetailsMachines.waitValidRamHighlighting(MACHINE_NAME);

    workspaceDetailsMachines.clickOnIncrementRamButton(MACHINE_NAME);
    workspaceDetailsMachines.waitRamAmount(MACHINE_NAME, "1");
    workspaceDetailsMachines.waitValidRamHighlighting(MACHINE_NAME);

    workspaceDetailsMachines.typeRamAmount(MACHINE_NAME, "3");
    workspaceDetailsMachines.waitValidRamHighlighting(MACHINE_NAME);
    workspaceDetails.waitAllEnabled(SAVE_BUTTON);
    workspaceDetails.waitAndClickOn(SAVE_BUTTON);

    workspaceDetailsMachines.clickOnIncrementRamButton(MACHINE_NAME);
    workspaceDetails.waitAllEnabled(SAVE_BUTTON);
    workspaceDetails.waitAllEnabled(CANCEL_BUTTON);

    workspaceDetails.waitAndClickOn(CANCEL_BUTTON);
    workspaceDetailsMachines.waitRamAmount(MACHINE_NAME, "3");
    workspaceDetails.waitAllDisabled(SAVE_BUTTON);
    workspaceDetails.waitAllDisabled(CANCEL_BUTTON);
  }
}
