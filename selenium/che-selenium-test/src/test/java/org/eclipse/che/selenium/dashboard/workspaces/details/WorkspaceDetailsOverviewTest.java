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
import org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.Test;

public class WorkspaceDetailsOverviewTest {
  private static final String WORKSPACE_NAME = NameGenerator.generate("test-workspace", 4);
  private static final String CHANGED_WORKSPACE_NAME = NameGenerator.generate(WORKSPACE_NAME, 4);
  private static final String MACHINE_NAME = "dev-machine";
  private static final String SAMPLE_NAME = "console-java-simple";
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

  private static final String EXPECTED_ATTRIBUTES_CONFIG =
      "  \"default\": {\n"
          + "      \"machines\": {\n"
          + "        \"dev-machine\": {\n"
          + "          \"attributes\": {\n"
          + "            \"memoryLimitBytes\": \"2147483648\"\n"
          + "          },\n"
          + "          \"servers\": {\n"
          + "            \"tomcat8-debug\": {\n"
          + "              \"attributes\": {},\n"
          + "              \"port\": \"8000\",\n"
          + "              \"protocol\": \"http\"\n"
          + "            },\n"
          + "            \"codeserver\": {\n"
          + "              \"attributes\": {},\n"
          + "              \"port\": \"9876\",\n"
          + "              \"protocol\": \"http\"\n"
          + "            },\n"
          + "            \"tomcat8\": {\n"
          + "              \"attributes\": {},\n"
          + "              \"port\": \"8080\",\n"
          + "              \"protocol\": \"http\"\n"
          + "            }\n"
          + "          },\n"
          + "          \"volumes\": {},\n"
          + "          \"installers\": [\n"
          + "            \"org.eclipse.che.exec\",\n"
          + "            \"org.eclipse.che.terminal\",\n"
          + "            \"org.eclipse.che.ws-agent\"\n"
          + "          ],\n";

  private static final String EXPECTED_IMAGE_CONFIG =
      "      \"recipe\": {\n"
          + "        \"type\": \"dockerimage\",\n"
          + "        \"content\": \"eclipse/ubuntu_jdk8\"\n"
          + "      }\n";

  private static final String EXPECTED_COMMAND_LINE_CONFIG =
      "  \"commands\": [\n"
          + "    {\n"
          + "      \"commandLine\": \"mvn clean install -f ${current.project.path}\",\n"
          + "      \"name\": \"build\",\n"
          + "      \"attributes\": {\n"
          + "        \"goal\": \"Build\",\n"
          + "        \"previewUrl\": \"\"\n"
          + "      },\n"
          + "      \"type\": \"mvn\"\n";

  @Inject private Dashboard dashboard;
  @Inject private NewWorkspace newWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private AddOrImportForm addOrImportForm;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

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

    selectStackAndCheckWorkspaceName(Stack.APACHE_CAMEL);

    selectStackAndCheckWorkspaceName(Stack.JAVA_GRADLE);

    // create workspace
    addOrImportForm.clickOnAddOrImportProjectButton();
    addOrImportForm.addSampleToWorkspace(SAMPLE_NAME);
    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    workspaceOverview.checkNameWorkspace(WORKSPACE_NAME);
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
    workspaceOverview.waitConfiguration(
        EXPECTED_ATTRIBUTES_CONFIG, EXPECTED_COMMAND_LINE_CONFIG, EXPECTED_IMAGE_CONFIG);
    workspaceOverview.clickOnToPrivateCloudButton();
    workspaceOverview.waitToPrivateCloudTabOpened();
    seleniumWebDriverHelper.sendKeys(ESCAPE.toString());
    workspaceOverview.waitExportWorkspaceFormClosed();
  }

  private void selectStackAndCheckWorkspaceName(NewWorkspace.Stack stack) {
    newWorkspace.selectStack(stack);
    newWorkspace.waitStackSelected(stack);
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
