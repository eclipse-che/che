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

import static java.util.Arrays.asList;
import static org.openqa.selenium.Keys.ESCAPE;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.Test;

public class WorkspaceDetailsOverviewTest {

  private static final String WORKSPACE_NAME = NameGenerator.generate("test-workspace", 4);
  private static final String CHANGED_WORKSPACE_NAME = NameGenerator.generate(WORKSPACE_NAME, 4);
  private static final String TOO_SHORT_NAME = "wk";
  private static final String MAX_LONG_NAME = NameGenerator.generate("wksp-", 95);
  private static final String TOO_LONG_NAME = NameGenerator.generate(MAX_LONG_NAME, 1);
  private static final String LONG_NAME_ERROR_MESSAGE =
      "The name has to be less than 101 characters long.";
  private static final String MIN_SHORT_NAME = "wks";
  private static final String SHORT_NAME_ERROR_MESSAGE =
      "The name has to be more than 3 characters long.";
  private static final String SPECIAL_CHARACTERS_ERROR_MESSAGE =
      "The name should not contain special characters like space, dollar, etc.";
  private static final List<String> VALID_NAMES =
      asList("Wk-sp", "Wk-sp1", "9wk-sp", "5wk-sp0", "Wk19sp", "Wksp-01");
  private static final List<String> NOT_VALID_NAMES =
      asList("wksp-", "-wksp", "wk sp", "wk_sp", "wksp@", "wksp$", "wksp&", "wksp*");

  @Inject private Dashboard dashboard;
  @Inject private NewWorkspace newWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject private WorkspaceDetails workspaceDetails;

  @Test
  public void shouldCreateWorkspaceAndOpenOverviewPage() {
    // prepare
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitToolbarTitleName();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitPageLoad();
    newWorkspace.typeWorkspaceName(WORKSPACE_NAME);

    selectDevfileAndCheckWorkspaceName(Devfile.JAVA_MAVEN);

    selectDevfileAndCheckWorkspaceName(Devfile.JAVA_GRADLE);

    // create workspace
    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    workspaceOverview.checkNameWorkspace(WORKSPACE_NAME);
  }

  @Test(priority = 1)
  public void shouldCheckExportAsFile() {
    workspaceOverview.checkNameWorkspace(WORKSPACE_NAME);

    // check of closing by "Esc"
    openExportWorkspaceForm();
    seleniumWebDriverHelper.sendKeys(ESCAPE.toString());
    workspaceOverview.waitExportWorkspaceFormClosed();

    // close by "x" icon
    openExportWorkspaceForm();
    workspaceOverview.clickOnCloseExportWorkspaceFormIcon();
    workspaceOverview.waitExportWorkspaceFormClosed();

    // close by "Close" button
    openExportWorkspaceForm();
    workspaceOverview.clickOnCloseExportWorkspaceFormButton();
    workspaceOverview.waitExportWorkspaceFormClosed();

    // check config
    openExportWorkspaceForm();
    workspaceOverview.clickOnToPrivateCloudButton();
    workspaceOverview.waitToPrivateCloudTabOpened();
    seleniumWebDriverHelper.sendKeys(ESCAPE.toString());
    workspaceOverview.waitExportWorkspaceFormClosed();
  }

  @Test(priority = 2)
  public void shouldCheckNameField() {
    workspaceOverview.waitNameFieldValue(WORKSPACE_NAME);

    // check of empty name
    workspaceOverview.enterNameWorkspace("");
    workspaceOverview.waitErrorBorderOfNameField();
    workspaceOverview.waitDisabledSaveButton();

    // check too short name
    nameShouldBeValid(CHANGED_WORKSPACE_NAME);
    nameShouldBeInvalid(TOO_SHORT_NAME, SHORT_NAME_ERROR_MESSAGE);

    // check too long name
    nameShouldBeValid(MIN_SHORT_NAME);
    nameShouldBeInvalid(TOO_LONG_NAME, LONG_NAME_ERROR_MESSAGE);

    nameShouldBeValid(MAX_LONG_NAME);
    namesShouldBeValid();
  }

  private void selectDevfileAndCheckWorkspaceName(Devfile devfile) {
    newWorkspace.selectDevfile(devfile);
    newWorkspace.waitDevfileSelected(devfile);
    newWorkspace.waitWorkspaceNameFieldValue(WORKSPACE_NAME);
  }

  private void nameShouldBeValid(String name) {
    workspaceOverview.enterNameWorkspace(name);
    workspaceOverview.waitUntilNoErrorsDisplayed();
    workspaceOverview.waitEnabledSaveButton();
  }

  private void nameShouldBeInvalid(String name, String expectedErrorMessage) {
    workspaceOverview.enterNameWorkspace(name);
    workspaceOverview.waitErrorBorderOfNameField();

    workspaceOverview.waitNameErrorMessage(expectedErrorMessage);
    workspaceOverview.waitDisabledSaveButton();
  }

  private void namesShouldBeValid() {
    VALID_NAMES.forEach(
        name -> {
          nameShouldBeInvalid(TOO_SHORT_NAME, SHORT_NAME_ERROR_MESSAGE);
          nameShouldBeValid(name);
        });
  }

  private void openExportWorkspaceForm() {
    workspaceOverview.clickExportWorkspaceBtn();
    workspaceOverview.waitExportWorkspaceFormOpened();
    workspaceOverview.waitAsFileTabOpened();
  }
}
