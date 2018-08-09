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
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WorkspaceDetailsMachinesTest {
  private static final String MACHINE_NAME = "dev-machine";
  private static final String IMAGE_NAME = "eclipse/ubuntu_jdk8";
  private static final String EXPECTED_RAM_VALUE = "2";

  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private WorkspaceDetailsMachines workspaceDetailsMachines;
  @Inject private TestWorkspace testWorkspace;

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
    final String maxRamValidValue = "100";
    final String minRamValidValue = "0.5";

    workspaceDetailsMachines.waitMachinesListItemWithAllAttributes(
        MACHINE_NAME, IMAGE_NAME, EXPECTED_RAM_VALUE);

    // check behavior with invalid RAM value
    workspaceDetailsMachines.typeRamAmount(MACHINE_NAME, "");
    workspaceDetailsMachines.waitRamInvalidHighlighting(MACHINE_NAME);
    workspaceDetails.waitInvisibility(SAVE_BUTTON, CANCEL_BUTTON, APPLY_BUTTON);

    // check saving behavior
    workspaceDetailsMachines.typeRamAmount(MACHINE_NAME, "3");
    workspaceDetailsMachines.waitRamValidHighlighting(MACHINE_NAME);
    workspaceDetails.waitEnabled(SAVE_BUTTON);
    workspaceDetails.waitAndClickOn(SAVE_BUTTON);
    workspaceDetails.waitInvisibility(SAVE_BUTTON, CANCEL_BUTTON, APPLY_BUTTON);

    // check increment, decrement RAM buttons
    workspaceDetailsMachines.typeRamAmount(MACHINE_NAME, maxRamValidValue);
    workspaceDetailsMachines.waitRamValidHighlighting(MACHINE_NAME);
    workspaceDetails.waitEnabled(SAVE_BUTTON);

    workspaceDetailsMachines.clickOnIncrementRamButton(MACHINE_NAME);
    workspaceDetailsMachines.waitRamValidHighlighting(MACHINE_NAME);
    workspaceDetailsMachines.waitRamAmount(MACHINE_NAME, maxRamValidValue);

    workspaceDetailsMachines.clickOnDecrementRamButton(MACHINE_NAME);
    workspaceDetailsMachines.waitRamValidHighlighting(MACHINE_NAME);
    workspaceDetailsMachines.waitRamAmount(MACHINE_NAME, "95.5");

    workspaceDetailsMachines.typeRamAmount(MACHINE_NAME, minRamValidValue);
    workspaceDetailsMachines.waitRamValidHighlighting(MACHINE_NAME);
    workspaceDetails.waitEnabled(SAVE_BUTTON);

    workspaceDetailsMachines.clickOnDecrementRamButton(MACHINE_NAME);
    workspaceDetailsMachines.waitRamAmount(MACHINE_NAME, minRamValidValue);
    workspaceDetailsMachines.waitRamValidHighlighting(MACHINE_NAME);

    workspaceDetailsMachines.clickOnIncrementRamButton(MACHINE_NAME);
    workspaceDetailsMachines.waitRamAmount(MACHINE_NAME, "1");

    workspaceDetailsMachines.typeRamAmount(MACHINE_NAME, "3");
    workspaceDetailsMachines.waitRamValidHighlighting(MACHINE_NAME);
    workspaceDetails.waitEnabled(SAVE_BUTTON);
    workspaceDetails.waitAndClickOn(SAVE_BUTTON);
    workspaceDetailsMachines.clickOnIncrementRamButton(MACHINE_NAME);
    workspaceDetails.waitEnabled(SAVE_BUTTON);
    workspaceDetails.waitEnabled(CANCEL_BUTTON);
    workspaceDetails.waitAndClickOn(CANCEL_BUTTON);
    workspaceDetailsMachines.waitRamAmount(MACHINE_NAME, "3");
    workspaceDetails.waitInvisibility(SAVE_BUTTON);
    workspaceDetails.waitInvisibility(CANCEL_BUTTON);
  }
}
