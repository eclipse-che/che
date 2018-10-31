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

import static org.eclipse.che.selenium.core.TestGroup.DOCKER;
import static org.eclipse.che.selenium.core.TestGroup.K8S;
import static org.eclipse.che.selenium.core.TestGroup.OPENSHIFT;
import static org.eclipse.che.selenium.core.TestGroup.OSIO;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.ActionButton.APPLY_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.ActionButton.CANCEL_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.ActionButton.SAVE_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.MACHINES;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.ARROW_UP;
import static org.openqa.selenium.Keys.ESCAPE;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceInstallers;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceServers;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WorkspaceDetailsMachineActionsTest {
  private static final String MACHINE_NAME = "dev-machine";
  private static final String CHANGED_MACHINE_NAME = "test-machine";
  private static final String IMAGE_NAME = "eclipse/ubuntu_jdk8";
  private static final String EXPECTED_RAM_VALUE = "2";
  private static final String EMPTY_NAME_ERROR_MESSAGE = "Machine's name is required.";
  private static final String SPECIAL_CHARACTERS_ERRORS_MESSAGE =
      "The name should not contain special characters like space, dollar, etc.";
  private static final String NAME_WITH_SPECIAL_CHARACTERS = "@#$^&*(!";
  private static final String MAX_VALID_NAME = NameGenerator.generate("max_name", 120);
  private static final String TOO_BIG_NAME = NameGenerator.generate(MAX_VALID_NAME, 1);
  private static final String TOO_BIG_RAM_SIZE = "1000";
  private static final String MAX_VALID_RAM_VALUE = "100";
  private static final String NOT_EXISTED_IMAGE = NameGenerator.generate("wrong/image", 5);
  private static final String CHANGED_RAM_SIZE = "7";
  private static final String MIN_VALID_RAM_VALUE = "0.5";

  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private WorkspaceDetailsMachines workspaceDetailsMachines;
  @Inject private TestWorkspace testWorkspace;
  @Inject private EditMachineForm editMachineForm;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private WorkspaceInstallers workspaceInstallers;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private WorkspaceServers workspaceServers;

  @BeforeMethod
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
  public void checkEditFormClosing() {
    workspaceDetailsMachines.waitMachineListItemWithAttributes(
        MACHINE_NAME, IMAGE_NAME, EXPECTED_RAM_VALUE);

    // close form by "ESC" button
    workspaceDetailsMachines.clickOnEditButton(MACHINE_NAME);
    editMachineForm.waitForm();
    seleniumWebDriverHelper.sendKeys(ESCAPE.toString());
    editMachineForm.waitFormInvisibility();

    // close form by "x" icon
    workspaceDetailsMachines.clickOnEditButton(MACHINE_NAME);
    editMachineForm.waitForm();
    editMachineForm.clickOnCloseIcon();
    editMachineForm.waitFormInvisibility();

    // close form by "Close" button
    workspaceDetailsMachines.clickOnEditButton(MACHINE_NAME);
    editMachineForm.waitForm();
    editMachineForm.clickOnCloseIcon();
    editMachineForm.waitFormInvisibility();
  }

  @Test(groups = {OPENSHIFT, K8S, OSIO})
  public void checkEditMachineNameOpenshift() {
    checkEditOfMachineName(IMAGE_NAME);
  }

  @Test(groups = DOCKER)
  public void checkEditMachineNameDocker() {
    checkEditOfMachineName("FROM " + IMAGE_NAME + "\n");
  }

  @Test(groups = {OPENSHIFT, K8S, OSIO})
  public void checkRamSectionOpenshift() {
    checkRamSection(IMAGE_NAME);
  }

  @Test(groups = DOCKER)
  public void checkRamSectionDocker() {
    checkRamSection("FROM " + IMAGE_NAME + "\n");
  }

  private void checkEditOfMachineName(String expectedRecipeText) {
    // check default values
    workspaceDetailsMachines.clickOnEditButton(MACHINE_NAME);
    editMachineForm.waitForm();
    editMachineForm.waitName(MACHINE_NAME);
    editMachineForm.waitSliderRamValue(EXPECTED_RAM_VALUE + " GB");
    editMachineForm.waitRamFieldText(EXPECTED_RAM_VALUE);
    editMachineForm.waitRecipeText(expectedRecipeText);

    // name empty field
    editMachineForm.typeName("");
    editMachineForm.waitInvalidNameHighlighting();
    editMachineForm.waitNameErrorMessage(EMPTY_NAME_ERROR_MESSAGE);
    editMachineForm.waitSaveButtonDisabling();
    setValidName();

    // special characters
    editMachineForm.typeName(NAME_WITH_SPECIAL_CHARACTERS);
    editMachineForm.waitInvalidNameHighlighting();
    editMachineForm.waitNameErrorMessage(SPECIAL_CHARACTERS_ERRORS_MESSAGE);
    editMachineForm.waitSaveButtonDisabling();
    setValidName();

    // max valid name
    editMachineForm.typeName(MAX_VALID_NAME);
    editMachineForm.waitValidNameHighlighting();
    editMachineForm.waitSaveButtonEnabling();

    // too long name
    editMachineForm.typeName(TOO_BIG_NAME);
    editMachineForm.waitInvalidNameHighlighting();
    editMachineForm.waitSaveButtonDisabling();
  }

  public void checkRamSection(String expectedRecipeText) {
    // check machine name editing
    workspaceDetailsMachines.clickOnEditButton(MACHINE_NAME);
    editMachineForm.waitForm();
    editMachineForm.typeName(CHANGED_MACHINE_NAME);
    editMachineForm.waitValidNameHighlighting();
    editMachineForm.waitSaveButtonEnabling();

    // check RAM field behavior with min valid value
    editMachineForm.typeRam(MIN_VALID_RAM_VALUE);
    editMachineForm.waitSliderRamValue(MIN_VALID_RAM_VALUE + " GB");
    seleniumWebDriverHelper.sendKeys(ARROW_DOWN.toString());
    editMachineForm.waitSliderRamValue(MIN_VALID_RAM_VALUE + " GB");
    editMachineForm.waitRamFieldText(MIN_VALID_RAM_VALUE);

    seleniumWebDriverHelper.sendKeys(ARROW_UP.toString());
    editMachineForm.waitRamFieldText("1");
    editMachineForm.waitSliderRamValue("1 GB");

    // check RAM behavior with more than max valid value
    editMachineForm.typeRam(TOO_BIG_RAM_SIZE);
    editMachineForm.waitSaveButtonDisabling();
    editMachineForm.waitSliderRamValue("GB");

    // check RAM behavior with max valid value
    editMachineForm.typeRam("100");
    editMachineForm.waitSaveButtonEnabling();
    editMachineForm.waitSliderRamValue(MAX_VALID_RAM_VALUE + " GB");

    seleniumWebDriverHelper.sendKeys(ARROW_UP.toString());
    editMachineForm.waitValidNameHighlighting();
    editMachineForm.waitSaveButtonEnabling();
    editMachineForm.waitRamFieldText(MAX_VALID_RAM_VALUE);
    editMachineForm.waitSliderRamValue(MAX_VALID_RAM_VALUE + " GB");

    seleniumWebDriverHelper.sendKeys(ARROW_DOWN.toString());
    editMachineForm.waitValidNameHighlighting();
    editMachineForm.waitSaveButtonEnabling();
    editMachineForm.waitRamFieldText("99.5");
    editMachineForm.waitSliderRamValue("99.5 GB");

    // check restoring of default values after clicking on "Cancel" button
    editMachineForm.typeRam("4");
    editMachineForm.waitSaveButtonEnabling();
    editMachineForm.waitSliderRamValue("4 GB");
    editMachineForm.clickOnCancelButton();
    editMachineForm.waitFormInvisibility();
    workspaceDetailsMachines.clickOnEditButton(MACHINE_NAME);
    editMachineForm.waitForm();
    editMachineForm.waitRecipeText(expectedRecipeText);

    // check saving of the changes
    editMachineForm.typeRam(CHANGED_RAM_SIZE);
    editMachineForm.waitRamFieldText(CHANGED_RAM_SIZE);
    editMachineForm.waitSaveButtonEnabling();
    editMachineForm.clickOnSaveButton();
    editMachineForm.waitFormInvisibility();
    workspaceDetails.waitAllEnabled(SAVE_BUTTON, APPLY_BUTTON, CANCEL_BUTTON);
    workspaceDetailsMachines.waitMachineListItemWithAttributes(
        MACHINE_NAME, IMAGE_NAME, CHANGED_RAM_SIZE);
    workspaceDetails.waitAllEnabled(SAVE_BUTTON, APPLY_BUTTON, CANCEL_BUTTON);
    workspaceDetails.waitAndClickOn(SAVE_BUTTON);
    workspaceDetailsMachines.waitMachineListItemWithAttributes(
        MACHINE_NAME, IMAGE_NAME, CHANGED_RAM_SIZE);
  }

  @Test
  public void checkMachineSettings() {
    final String installerName = "Exec";
    final String serverName = "tomcat8";

    // check the "Installers" link
    waitMachineListItemAndClickOnSettingsButton();
    workspaceDetailsMachines.clickOnInstallersLink();
    workspaceInstallers.checkInstallerExists(installerName);

    seleniumWebDriver.navigate().back();

    // check the "Servers" link
    waitMachineListItemAndClickOnSettingsButton();
    workspaceDetailsMachines.clickOnServersLink();
    workspaceServers.checkServerName(serverName);

    seleniumWebDriver.navigate().back();

    workspaceDetailsMachines.waitMachineListItem(MACHINE_NAME);
    closeSettingsPopoverIfOpen();
  }

  private void waitMachineListItemAndClickOnSettingsButton() {
    workspaceDetailsMachines.waitMachineListItem(MACHINE_NAME);
    closeSettingsPopoverIfOpen();
    workspaceDetailsMachines.clickOnSettingsButton(MACHINE_NAME);
    workspaceDetailsMachines.waitSettingsPopover();
  }

  private void closeSettingsPopoverIfOpen() {
    if (!workspaceDetailsMachines.isSettingsPopoverOpened()) {
      return;
    }

    workspaceDetailsMachines.clickOnSettingsButton(MACHINE_NAME);
    workspaceDetailsMachines.waitSettingsPopoverInvisibility();
  }

  private void setValidName() {
    editMachineForm.typeName(MACHINE_NAME);
    editMachineForm.waitValidNameHighlighting();
  }
}
