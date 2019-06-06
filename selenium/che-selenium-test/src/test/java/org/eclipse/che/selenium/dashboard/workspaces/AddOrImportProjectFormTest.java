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
package org.eclipse.che.selenium.dashboard.workspaces;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectOptions;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Ihor Okhrimenko */
public class AddOrImportProjectFormTest {

  private static final String NAME_WITH_MAX_AVAILABLE_LENGTH = generate("name", 124);
  private static final String WORKSPACE_NAME = generate("test-workspace", 4);
  private static final String TEST_BLANK_WORKSPACE_NAME = "test-blank-workspace";
  private static final String TEST_JAVA_WORKSPACE_NAME = "test-java-workspace";
  private static final String TEST_JAVA_WORKSPACE_NAME_EDIT = generate("test-java-workspace", 4);
  private static final String NAME_WITH_SPECIAL_CHARACTERS = "@#$%^&*";
  private static final String SPRING_SAMPLE_NAME = "web-java-spring";
  private static final String EXPECTED_SPRING_REPOSITORY_URL =
      "https://github.com/che-samples/web-java-spring.git";
  private static final String CONSOLE_SAMPLE_NAME = "console-java-simple";
  private static final String RENAMED_CONSOLE_SAMPLE_NAME = "java-console-test";
  private static final String EXPECTED_CONSOLE_REPOSITORY_URL =
      "https://github.com/che-samples/console-java-simple.git";
  private static final String BLANK_FORM_DESCRIPTION = "example of description";
  private static final String SPRING_FUSE_SAMPLE = "fuse-rest-http-booster";
  private static final String CUSTOM_BLANK_PROJECT_NAME = "blank-project";
  private static final String BLANK_PROJECT_NAME = "blank";
  private static final String BLANK_DEFAULT_URL = "https://github.com/che-samples/blank";
  private static final ImmutableMap<String, String> EXPECTED_SAMPLES_WITH_DESCRIPTIONS =
      ImmutableMap.of(
          SPRING_SAMPLE_NAME,
          "A basic example using Spring servlets. The app returns values entered into a submit form.",
          SPRING_FUSE_SAMPLE,
          "Fuse booster.",
          CONSOLE_SAMPLE_NAME,
          "A hello world Java application.");
  private static final String EXPECTED_TEXT_IN_EDITOR =
      "package org.eclipse.che.examples;\n"
          + "\n"
          + "import org.springframework.web.servlet.ModelAndView;\n"
          + "import org.springframework.web.servlet.mvc.Controller;\n"
          + "\n"
          + "import javax.servlet.http.HttpServletRequest;\n"
          + "import javax.servlet.http.HttpServletResponse;\n"
          + "\n"
          + "public class GreetingController implements Controller\n"
          + "{\n"
          + "\n"
          + "   @Override\n"
          + "   public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception\n"
          + "   {\n"
          + "      String userName = request.getParameter(\"user\");\n"
          + "      String result = \"\";\n"
          + "      if (userName != null)\n"
          + "      {\n"
          + "        result = \"Hello, \" + userName + \"!\";\n"
          + "      }\n"
          + "\n"
          + "      ModelAndView view = new ModelAndView(\"hello_view\");\n"
          + "      view.addObject(\"greeting\", result);\n"
          + "      return view;\n"
          + "   }\n"
          + "}\n";

  private static final String PATH_TO_JAVA_FILE =
      "/src/main/java/org/eclipse/che/examples/GreetingController.java";

  @Inject private Ide ide;
  @Inject private Dashboard dashboard;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private ProjectOptions projectOptions;
  @Inject private AddOrImportForm addOrImportForm;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private Consoles consoles;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void setup() {
    dashboard.open();
  }

  @AfterClass
  public void cleanup() throws Exception {
    testWorkspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
    testWorkspaceServiceClient.delete(TEST_BLANK_WORKSPACE_NAME, defaultTestUser.getName());
    testWorkspaceServiceClient.delete(TEST_JAVA_WORKSPACE_NAME, defaultTestUser.getName());
    testWorkspaceServiceClient.delete(TEST_JAVA_WORKSPACE_NAME_EDIT, defaultTestUser.getName());
  }

  @BeforeMethod
  public void prepareToTestMethod() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitToolbarTitleName();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitPageLoad();
  }

  @Test
  public void checkOfCheckboxes() {
    // preparing
    newWorkspace.waitPageLoad();
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    addOrImportForm.clickOnAddOrImportProjectButton();
    addOrImportForm.waitAddOrImportFormOpened();
    addOrImportForm.waitSamplesButtonSelected();
    addOrImportForm.waitSamplesWithDescriptions(EXPECTED_SAMPLES_WITH_DESCRIPTIONS);
    waitAllCheckboxesDisabled();
    addOrImportForm.waitCancelButtonDisabled();
    addOrImportForm.waitAddButtonDisabled();

    // click on single disabled checkbox
    addOrImportForm.clickOnSampleCheckbox(CONSOLE_SAMPLE_NAME);
    addOrImportForm.waitSampleCheckboxEnabled(CONSOLE_SAMPLE_NAME);
    addOrImportForm.waitCancelButtonEnabled();
    addOrImportForm.waitAddButtonEnabled();

    // unselect checkbox by "Cancel" button
    addOrImportForm.clickOnCancelButton();
    addOrImportForm.waitSampleCheckboxDisabled(CONSOLE_SAMPLE_NAME);

    // select and unselect single checkbox by clicking on it
    addOrImportForm.clickOnSampleCheckbox(SPRING_SAMPLE_NAME);
    addOrImportForm.waitSampleCheckboxEnabled(SPRING_SAMPLE_NAME);
    addOrImportForm.waitCancelButtonEnabled();
    addOrImportForm.waitAddButtonEnabled();

    addOrImportForm.clickOnSampleCheckbox(SPRING_SAMPLE_NAME);
    addOrImportForm.waitSampleCheckboxDisabled(SPRING_SAMPLE_NAME);
    addOrImportForm.waitCancelButtonDisabled();
    addOrImportForm.waitAddButtonDisabled();

    // unselect multiple checkboxes by "Cancel" button
    clickOnEachCheckbox();
    waitAllCheckboxesEnabled();
    addOrImportForm.waitCancelButtonEnabled();
    addOrImportForm.waitAddButtonEnabled();

    addOrImportForm.clickOnCancelButton();
    waitAllCheckboxesDisabled();
    addOrImportForm.waitCancelButtonDisabled();
    addOrImportForm.waitAddButtonDisabled();
  }

  @Test(priority = 1)
  public void checkProjectSamples() {
    // preparing
    newWorkspace.waitPageLoad();
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    addOrImportForm.clickOnAddOrImportProjectButton();
    addOrImportForm.waitAddOrImportFormOpened();
    addOrImportForm.waitSamplesButtonSelected();

    // add single sample to workspace
    addOrImportForm.clickOnSampleCheckbox(CONSOLE_SAMPLE_NAME);
    addOrImportForm.waitSampleCheckboxEnabled(CONSOLE_SAMPLE_NAME);
    addOrImportForm.waitCancelButtonEnabled();
    addOrImportForm.waitAddButtonEnabled();

    addOrImportForm.clickOnAddButton();
    checkProjectTabAppearanceAndFields(
        CONSOLE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CONSOLE_SAMPLE_NAME),
        EXPECTED_CONSOLE_REPOSITORY_URL);

    // remove added sample by "Remove" button
    projectOptions.clickOnRemoveButton();
    addOrImportForm.waitProjectTabDisappearance(CONSOLE_SAMPLE_NAME);
    addOrImportForm.waitAddOrImportFormOpened();
    addOrImportForm.waitSamplesButtonSelected();
    addOrImportForm.waitSamplesWithDescriptions(EXPECTED_SAMPLES_WITH_DESCRIPTIONS);
    waitAllCheckboxesDisabled();
    addOrImportForm.waitCancelButtonDisabled();
    addOrImportForm.waitAddButtonDisabled();

    // add multiple samples
    clickOnEachCheckbox();
    waitAllCheckboxesEnabled();
    addOrImportForm.waitCancelButtonEnabled();
    addOrImportForm.waitAddButtonEnabled();

    addOrImportForm.clickOnAddButton();
    addOrImportForm.waitProjectTabAppearance(CONSOLE_SAMPLE_NAME);
    addOrImportForm.waitProjectTabAppearance(SPRING_SAMPLE_NAME);

    addOrImportForm.clickOnProjectTab(CONSOLE_SAMPLE_NAME);
    checkProjectTabAppearanceAndFields(
        CONSOLE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CONSOLE_SAMPLE_NAME),
        EXPECTED_CONSOLE_REPOSITORY_URL);

    addOrImportForm.clickOnProjectTab(SPRING_SAMPLE_NAME);
    checkProjectTabAppearanceAndFields(
        SPRING_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(SPRING_SAMPLE_NAME),
        EXPECTED_SPRING_REPOSITORY_URL);

    // remove sample
    projectOptions.clickOnRemoveButton();
    addOrImportForm.waitProjectTabAppearance(CONSOLE_SAMPLE_NAME);
    addOrImportForm.waitProjectTabDisappearance(SPRING_SAMPLE_NAME);
    addOrImportForm.waitAddOrImportFormOpened();
    addOrImportForm.waitSamplesButtonSelected();

    // check name field of the project tab
    addOrImportForm.clickOnProjectTab(CONSOLE_SAMPLE_NAME);
    projectOptions.waitProjectOptionsForm();

    projectOptions.setValueOfNameField("");
    projectOptions.waitProjectNameErrorMessage("A name is required.");
    projectOptions.waitSaveButtonDisabling();
    projectOptions.waitCancelButtonEnabling();

    projectOptions.setValueOfNameField(RENAMED_CONSOLE_SAMPLE_NAME);
    projectOptions.waitProjectNameErrorDisappearance();
    projectOptions.waitSaveButtonEnabling();
    projectOptions.waitCancelButtonEnabling();

    projectOptions.setValueOfNameField("");
    projectOptions.waitProjectNameErrorMessage("A name is required.");
    projectOptions.waitSaveButtonDisabling();
    projectOptions.waitCancelButtonEnabling();

    projectOptions.setValueOfNameField(NAME_WITH_MAX_AVAILABLE_LENGTH);
    projectOptions.waitProjectNameErrorDisappearance();
    projectOptions.waitSaveButtonEnabling();
    projectOptions.waitCancelButtonEnabling();

    projectOptions.setValueOfNameField(NAME_WITH_MAX_AVAILABLE_LENGTH + "p");
    projectOptions.waitProjectNameErrorMessage("The name has to be less than 128 characters long.");
    projectOptions.waitSaveButtonDisabling();
    projectOptions.waitCancelButtonEnabling();

    projectOptions.setValueOfNameField(NAME_WITH_SPECIAL_CHARACTERS);
    projectOptions.waitProjectNameErrorMessage(
        "The name should not contain special characters like space, dollar, etc.");
    projectOptions.waitSaveButtonDisabling();
    projectOptions.waitCancelButtonEnabling();

    // check of restoring the previous values of the tab after click "Cancel" button
    projectOptions.typeTextInDescriptionField("");
    projectOptions.clickOnCancelButton();
    checkProjectTabAppearanceAndFields(
        CONSOLE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CONSOLE_SAMPLE_NAME),
        EXPECTED_CONSOLE_REPOSITORY_URL);

    // Check "Url" field
    projectOptions.typeTextInRepositoryUrlField("");
    projectOptions.waitRepositoryUrlErrorMessage("Invalid Git URL");
    projectOptions.waitSaveButtonDisabling();
    projectOptions.waitCancelButtonEnabling();

    // check of restoring the previous values of the tab after click "Cancel" button
    projectOptions.clickOnCancelButton();
    checkProjectTabAppearanceAndFields(
        CONSOLE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CONSOLE_SAMPLE_NAME),
        EXPECTED_CONSOLE_REPOSITORY_URL);

    // check of restoring the previous values of the tab after click on the another project tab
    // without saving
    addOrImportForm.clickOnAddOrImportProjectButton();
    addOrImportForm.waitAddOrImportFormOpened();
    addOrImportForm.clickOnSampleCheckbox(SPRING_SAMPLE_NAME);
    addOrImportForm.waitSampleCheckboxEnabled(SPRING_SAMPLE_NAME);
    addOrImportForm.clickOnAddButton();
    addOrImportForm.waitProjectTabAppearance(SPRING_SAMPLE_NAME);
    addOrImportForm.clickOnProjectTab(CONSOLE_SAMPLE_NAME);
    projectOptions.waitProjectNameFieldValue(CONSOLE_SAMPLE_NAME);

    projectOptions.setValueOfNameField("");
    projectOptions.typeTextInDescriptionField("");
    projectOptions.typeTextInRepositoryUrlField("");

    addOrImportForm.clickOnProjectTab(SPRING_SAMPLE_NAME);
    projectOptions.waitProjectNameFieldValue(SPRING_SAMPLE_NAME);

    addOrImportForm.clickOnProjectTab(CONSOLE_SAMPLE_NAME);
    checkProjectTabAppearanceAndFields(
        CONSOLE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CONSOLE_SAMPLE_NAME),
        EXPECTED_CONSOLE_REPOSITORY_URL);

    // check ability of creation the sample with specified valid name
    projectOptions.setValueOfNameField(RENAMED_CONSOLE_SAMPLE_NAME);
    projectOptions.clickOnSaveButton();
    addOrImportForm.waitProjectTabAppearance(RENAMED_CONSOLE_SAMPLE_NAME);
    projectOptions.waitSaveButtonDisabling();
    projectOptions.waitCancelButtonDisabling();
  }

  @Test(priority = 2)
  public void checkProjectsBlank() throws Exception {
    // preparing
    newWorkspace.waitPageLoad();
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.waitStackSelected(Stack.JAVA_MAVEN);
    addOrImportForm.clickOnAddOrImportProjectButton();
    addOrImportForm.waitAddOrImportFormOpened();
    addOrImportForm.clickOnBlankButton();

    // check name field
    addOrImportForm.typeToBlankNameField(NAME_WITH_MAX_AVAILABLE_LENGTH);
    addOrImportForm.waitErrorMessageDissappearanceInBlankNameField();
    addOrImportForm.waitAddButtonEnabled();
    addOrImportForm.waitCancelButtonEnabled();

    addOrImportForm.typeToBlankNameField("");
    addOrImportForm.waitAddButtonDisabled();
    addOrImportForm.waitCancelButtonDisabled();

    addOrImportForm.typeToBlankNameField(NAME_WITH_MAX_AVAILABLE_LENGTH + "p");
    addOrImportForm.waitNameFieldErrorMessageInBlankForm(
        "The name has to be less than 128 characters long.");
    addOrImportForm.waitAddButtonDisabled();
    addOrImportForm.waitCancelButtonEnabled();

    addOrImportForm.typeToBlankNameField(NAME_WITH_SPECIAL_CHARACTERS);
    addOrImportForm.waitNameFieldErrorMessageInBlankForm(
        "The name should not contain special characters like space, dollar, etc.");
    addOrImportForm.waitAddButtonDisabled();
    addOrImportForm.waitCancelButtonEnabled();

    // check description field
    addOrImportForm.typeToBlankDescriptionField(BLANK_FORM_DESCRIPTION);
    addOrImportForm.waitTextInBlankDescriptionField(BLANK_FORM_DESCRIPTION);

    addOrImportForm.clickOnCancelButton();
    addOrImportForm.waitTextInBlankNameField("");
    addOrImportForm.waitTextInBlankDescriptionField("");

    // add sample with specified valid name and description
    addOrImportForm.typeToBlankNameField(CUSTOM_BLANK_PROJECT_NAME);
    addOrImportForm.typeToBlankDescriptionField(BLANK_FORM_DESCRIPTION);
    addOrImportForm.clickOnAddButton();

    addOrImportForm.waitProjectTabAppearance(CUSTOM_BLANK_PROJECT_NAME);
    checkProjectTabAppearanceAndFields(
        CUSTOM_BLANK_PROJECT_NAME, BLANK_FORM_DESCRIPTION, BLANK_DEFAULT_URL);

    // check that added by "Git" button project has an expected name and description
    addOrImportForm.clickOnAddOrImportProjectButton();
    addOrImportForm.waitAddOrImportFormOpened();

    addOrImportForm.clickOnGitButton();
    addOrImportForm.waitGitTabOpened();

    addOrImportForm.typeToGitUrlField(BLANK_DEFAULT_URL);
    addOrImportForm.clickOnAddButton();
    checkProjectTabAppearanceAndFields(BLANK_PROJECT_NAME, "", BLANK_DEFAULT_URL);

    addOrImportForm.clickOnAddOrImportProjectButton();
    addOrImportForm.waitAddOrImportFormOpened();

    addOrImportForm.clickOnGitHubButton();
    newWorkspace.typeWorkspaceName(WORKSPACE_NAME);

    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    workspaceDetails.waitToolbarTitleName(WORKSPACE_NAME);
    workspaceDetails.checkStateOfWorkspace(StateWorkspace.STOPPED);
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace = testWorkspaceProvider.getWorkspace(WORKSPACE_NAME, defaultTestUser);
  }

  @Test(priority = 3)
  public void checkCreatingProject() throws Exception {
    // check that name field saves it state after choosing another stack
    newWorkspace.waitPageLoad();
    newWorkspace.typeWorkspaceName(TEST_BLANK_WORKSPACE_NAME);
    newWorkspace.selectStack(Stack.DOT_NET);
    newWorkspace.waitStackSelected(Stack.DOT_NET);
    assertEquals(newWorkspace.getWorkspaceNameValue(), TEST_BLANK_WORKSPACE_NAME);

    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.waitStackSelected(Stack.JAVA_MAVEN);
    assertEquals(newWorkspace.getWorkspaceNameValue(), TEST_BLANK_WORKSPACE_NAME);

    addOrImportForm.clickOnAddOrImportProjectButton();
    addOrImportForm.waitAddOrImportFormOpened();

    addOrImportForm.clickOnSampleCheckbox(SPRING_SAMPLE_NAME);
    addOrImportForm.waitSampleCheckboxEnabled(SPRING_SAMPLE_NAME);

    addOrImportForm.clickOnAddButton();
    addOrImportForm.waitProjectTabAppearance(SPRING_SAMPLE_NAME);

    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    workspaceOverview.checkNameWorkspace(TEST_BLANK_WORKSPACE_NAME);

    seleniumWebDriver.navigate().back();

    prepareJavaWorkspace(TEST_JAVA_WORKSPACE_NAME);
    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    workspaceOverview.checkNameWorkspace(TEST_JAVA_WORKSPACE_NAME);

    seleniumWebDriver.navigate().back();

    prepareJavaWorkspace(TEST_JAVA_WORKSPACE_NAME_EDIT);
    newWorkspace.clickOnTopCreateButton();

    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    testWorkspaceServiceClient.waitStatus(
        TEST_JAVA_WORKSPACE_NAME_EDIT, defaultTestUser.getName(), RUNNING);

    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(SPRING_SAMPLE_NAME);
    projectExplorer.waitItem(SPRING_SAMPLE_NAME);
    projectExplorer.quickRevealToItemWithJavaScript(SPRING_SAMPLE_NAME + PATH_TO_JAVA_FILE);
    projectExplorer.openItemByPath(SPRING_SAMPLE_NAME + PATH_TO_JAVA_FILE);
    editor.waitActive();
    editor.waitTextIntoEditor(EXPECTED_TEXT_IN_EDITOR);
  }

  private void waitAllCheckboxesDisabled() {
    addOrImportForm
        .getSamplesNames()
        .forEach(sampleName -> addOrImportForm.waitSampleCheckboxDisabled(sampleName));
  }

  private void waitAllCheckboxesEnabled() {
    addOrImportForm
        .getSamplesNames()
        .forEach(sampleName -> addOrImportForm.waitSampleCheckboxEnabled(sampleName));
  }

  private void clickOnEachCheckbox() {
    addOrImportForm
        .getSamplesNames()
        .forEach(sampleName -> addOrImportForm.clickOnSampleCheckbox(sampleName));
  }

  private void checkProjectTabAppearanceAndFields(
      String tabName, String expectedDescription, String expectedUrl) {
    projectOptions.waitProjectOptionsForm();
    projectOptions.waitProjectNameFieldValue(tabName);
    projectOptions.waitDescriptionFieldValue(expectedDescription);
    projectOptions.waitRepositoryUrlFieldValue(expectedUrl);
    projectOptions.waitRemoveButton();
    projectOptions.waitCancelButtonDisabling();
    projectOptions.waitSaveButtonDisabling();
  }

  private void prepareJavaWorkspace(String workspaceName) {
    // prepare workspace
    newWorkspace.waitPageLoad();
    newWorkspace.typeWorkspaceName(workspaceName);

    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.waitStackSelected(Stack.JAVA_MAVEN);

    addOrImportForm.clickOnAddOrImportProjectButton();
    addOrImportForm.waitAddOrImportFormOpened();

    addOrImportForm.clickOnSampleCheckbox(SPRING_SAMPLE_NAME);
    addOrImportForm.waitSampleCheckboxEnabled(SPRING_SAMPLE_NAME);

    addOrImportForm.clickOnAddButton();
    checkProjectTabAppearanceAndFields(
        SPRING_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(SPRING_SAMPLE_NAME),
        EXPECTED_SPRING_REPOSITORY_URL);
  }
}
