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
package org.eclipse.che.selenium.dashboard.workspaces;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.StackId.BLANK;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.StackId.DOT_NET;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.StackId.JAVA;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DocumentationPage;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.stacks.Stacks;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceConfig;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Ihor Okhrimenko */
public class AddOrImportProjectFormTest {

  private static final String NAME_WITH_MAX_AVAILABLE_LENGTH = NameGenerator.generate("name", 124);
  private static final String WORKSPACE_NAME = NameGenerator.generate("test-workspace", 4);
  private static final String TEST_BLANK_WORKSPACE_NAME = "test-blank-workspace";
  private static final String TEST_JAVA_WORKSPACE_NAME = "test-java-workspace";
  private static final String TEST_JAVA_WORKSPACE_NAME_EDIT =
      NameGenerator.generate("test-java-workspace", 4);
  private static final String NAME_WITH_SPECIAL_CHARACTERS = "@#$%^&*";
  private static final String SPRING_SAMPLE_NAME = "web-java-spring";
  private static final String EXPECTED_SPRING_REPOSITORY_URL =
      "https://github.com/che-samples/web-java-spring.git";
  private static final String CHE_SAMPLE_NAME = "che-in-che";
  private static final String EXPECTED_CHE_REPOSITORY_URL = "https://github.com/eclipse/che";
  private static final String CONSOLE_SAMPLE_NAME = "console-java-simple";
  private static final String RENAMED_CONSOLE_SAMPLE_NAME = "java-console-test";
  private static final String EXPECTED_CONSOLE_REPOSITORY_URL =
      "https://github.com/che-samples/console-java-simple.git";
  private static final String BLANK_FORM_DESCRIPTION = "example of description";
  private static final String CUSTOM_BLANK_PROJECT_NAME = "blank-project";
  private static final String BLANK_PROJECT_NAME = "blank";
  private static final String BLANK_DEFAULT_URL = "https://github.com/che-samples/blank";
  private static final Map<String, String> EXPECTED_SAMPLES_WITH_DESCRIPTIONS =
      ImmutableMap.of(
          SPRING_SAMPLE_NAME,
          "A basic example using Spring servlets. The app returns values entered into a submit form.",
          CHE_SAMPLE_NAME,
          "The Eclipse Che source code. Build Che-in-Che.",
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

  private Workspace customWorkspace;

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private WorkspaceConfig workspaceConfig;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private DocumentationPage documentationPage;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private Stacks stacks;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;

  @BeforeClass
  public void setup() {
    dashboard.open();
  }

  @AfterClass
  public void cleanup() throws Exception {
    checkWorkspaceStatusAndDelete(WORKSPACE_NAME);
    checkWorkspaceStatusAndDelete(TEST_BLANK_WORKSPACE_NAME);
    checkWorkspaceStatusAndDelete(TEST_JAVA_WORKSPACE_NAME);
    checkWorkspaceStatusAndDelete(TEST_JAVA_WORKSPACE_NAME_EDIT);
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
    newWorkspace.waitPageLoad();
    newWorkspace.selectStack(JAVA);
    newWorkspace.clickOnAddOrImportProjectButton();
    newWorkspace.waitAddOrImportFormOpened();
    newWorkspace.waitSamplesButtonSelected();
    newWorkspace.waitSamplesWithDescriptions(EXPECTED_SAMPLES_WITH_DESCRIPTIONS);
    waitAllCheckboxesDisabled();
    newWorkspace.waitCancelButtonInImportProjectFormDisabled();
    newWorkspace.waitAddButtonInImportProjectFormDisabled();

    newWorkspace.clickOnSampleCheckbox(CONSOLE_SAMPLE_NAME);
    newWorkspace.waitSampleCheckboxEnabled(CONSOLE_SAMPLE_NAME);
    newWorkspace.waitCancelButtonInImportProjectFormEnabled();
    newWorkspace.waitAddButtonInImportProjectFormEnabled();

    newWorkspace.clickOnCancelButtonInImportProjectForm();
    newWorkspace.waitSampleCheckboxDisabled(CONSOLE_SAMPLE_NAME);

    newWorkspace.clickOnSampleCheckbox(CHE_SAMPLE_NAME);
    newWorkspace.waitSampleCheckboxEnabled(CHE_SAMPLE_NAME);
    newWorkspace.waitCancelButtonInImportProjectFormEnabled();
    newWorkspace.waitAddButtonInImportProjectFormEnabled();

    newWorkspace.clickOnSampleCheckbox(CHE_SAMPLE_NAME);
    newWorkspace.waitSampleCheckboxDisabled(CHE_SAMPLE_NAME);
    newWorkspace.waitCancelButtonInImportProjectFormDisabled();
    newWorkspace.waitAddButtonInImportProjectFormDisabled();

    clickOnEachCheckbox();
    waitAllCheckboxesEnabled();
    newWorkspace.waitCancelButtonInImportProjectFormEnabled();
    newWorkspace.waitAddButtonInImportProjectFormEnabled();

    newWorkspace.clickOnCancelButtonInImportProjectForm();
    waitAllCheckboxesDisabled();
    newWorkspace.waitCancelButtonInImportProjectFormDisabled();
    newWorkspace.waitAddButtonInImportProjectFormDisabled();
  }

  @Test(priority = 1)
  public void checkProjectSamples() {
    newWorkspace.waitPageLoad();
    newWorkspace.selectStack(JAVA);
    newWorkspace.clickOnAddOrImportProjectButton();
    newWorkspace.waitAddOrImportFormOpened();
    newWorkspace.waitSamplesButtonSelected();
    newWorkspace.clickOnSampleCheckbox(CONSOLE_SAMPLE_NAME);
    newWorkspace.waitSampleCheckboxEnabled(CONSOLE_SAMPLE_NAME);
    newWorkspace.waitCancelButtonInImportProjectFormEnabled();
    newWorkspace.waitAddButtonInImportProjectFormEnabled();
    newWorkspace.clickOnAddButtonInImportProjectForm();

    checkProjectTabAppearanceAndFields(
        CONSOLE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CONSOLE_SAMPLE_NAME),
        EXPECTED_CONSOLE_REPOSITORY_URL);

    newWorkspace.clickOnRemoveButtonInProjectOptionsForm();
    newWorkspace.waitProjectTabDisappearance(CONSOLE_SAMPLE_NAME);

    newWorkspace.waitAddOrImportFormOpened();
    newWorkspace.waitSamplesButtonSelected();
    newWorkspace.waitSamplesWithDescriptions(EXPECTED_SAMPLES_WITH_DESCRIPTIONS);
    waitAllCheckboxesDisabled();
    newWorkspace.waitCancelButtonInImportProjectFormDisabled();
    newWorkspace.waitAddButtonInImportProjectFormDisabled();

    clickOnEachCheckbox();

    waitAllCheckboxesEnabled();
    newWorkspace.waitCancelButtonInImportProjectFormEnabled();
    newWorkspace.waitAddButtonInImportProjectFormEnabled();

    newWorkspace.clickOnAddButtonInImportProjectForm();

    newWorkspace.waitProjectTabAppearance(CONSOLE_SAMPLE_NAME);
    newWorkspace.waitProjectTabAppearance(CHE_SAMPLE_NAME);
    newWorkspace.waitProjectTabAppearance(SPRING_SAMPLE_NAME);

    newWorkspace.clickOnProjectTab(CONSOLE_SAMPLE_NAME);
    checkProjectTabAppearanceAndFields(
        CONSOLE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CONSOLE_SAMPLE_NAME),
        EXPECTED_CONSOLE_REPOSITORY_URL);

    newWorkspace.clickOnProjectTab(SPRING_SAMPLE_NAME);
    checkProjectTabAppearanceAndFields(
        SPRING_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(SPRING_SAMPLE_NAME),
        EXPECTED_SPRING_REPOSITORY_URL);

    newWorkspace.clickOnProjectTab(CHE_SAMPLE_NAME);
    checkProjectTabAppearanceAndFields(
        CHE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CHE_SAMPLE_NAME),
        EXPECTED_CHE_REPOSITORY_URL);

    newWorkspace.clickOnRemoveButtonInProjectOptionsForm();

    newWorkspace.waitProjectTabDisappearance(CHE_SAMPLE_NAME);
    newWorkspace.waitProjectTabAppearance(CONSOLE_SAMPLE_NAME);
    newWorkspace.waitProjectTabAppearance(SPRING_SAMPLE_NAME);
    newWorkspace.waitAddOrImportFormOpened();
    newWorkspace.waitSamplesButtonSelected();

    newWorkspace.clickOnProjectTab(CONSOLE_SAMPLE_NAME);
    newWorkspace.waitProjectOptionsForm();

    newWorkspace.setValueOfNameFieldInProjectOptionsForm("");
    newWorkspace.waitProjectNameErrorMessageInOptionsForm("A name is required.");
    newWorkspace.waitSaveButtonDisablingInProjectOptionsForm();
    newWorkspace.waitCancelButtonEnablingInProjectOptionsForm();

    newWorkspace.setValueOfNameFieldInProjectOptionsForm(RENAMED_CONSOLE_SAMPLE_NAME);
    newWorkspace.waitProjectNameErrorDisappearanceInOptionsForm();
    newWorkspace.waitSaveButtonEnablingInProjectOptionsForm();
    newWorkspace.waitCancelButtonEnablingInProjectOptionsForm();

    newWorkspace.setValueOfNameFieldInProjectOptionsForm("");
    newWorkspace.waitProjectNameErrorMessageInOptionsForm("A name is required.");
    newWorkspace.waitSaveButtonDisablingInProjectOptionsForm();
    newWorkspace.waitCancelButtonEnablingInProjectOptionsForm();

    newWorkspace.setValueOfNameFieldInProjectOptionsForm(NAME_WITH_MAX_AVAILABLE_LENGTH);
    newWorkspace.waitProjectNameErrorDisappearanceInOptionsForm();
    newWorkspace.waitSaveButtonEnablingInProjectOptionsForm();
    newWorkspace.waitCancelButtonEnablingInProjectOptionsForm();

    newWorkspace.setValueOfNameFieldInProjectOptionsForm(NAME_WITH_MAX_AVAILABLE_LENGTH + "p");
    newWorkspace.waitProjectNameErrorMessageInOptionsForm(
        "The name has to be less than 128 characters long.");
    newWorkspace.waitSaveButtonDisablingInProjectOptionsForm();
    newWorkspace.waitCancelButtonEnablingInProjectOptionsForm();

    newWorkspace.setValueOfNameFieldInProjectOptionsForm(NAME_WITH_SPECIAL_CHARACTERS);
    newWorkspace.waitProjectNameErrorMessageInOptionsForm(
        "The name should not contain special characters like space, dollar, etc.");
    newWorkspace.waitSaveButtonDisablingInProjectOptionsForm();
    newWorkspace.waitCancelButtonEnablingInProjectOptionsForm();

    newWorkspace.setValueOfDescriptionFieldInProjectOptionsForm("");
    newWorkspace.clickOnCancelButtonInProjectOptionsForm();

    checkProjectTabAppearanceAndFields(
        CONSOLE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CONSOLE_SAMPLE_NAME),
        EXPECTED_CONSOLE_REPOSITORY_URL);

    newWorkspace.setValueOfRepositoryUrlFieldInProjectOptionsForm("");

    newWorkspace.waitRepositoryUrlErrorMessageInOptionsForm("Invalid Git URL");
    newWorkspace.waitSaveButtonDisablingInProjectOptionsForm();
    newWorkspace.waitCancelButtonEnablingInProjectOptionsForm();

    newWorkspace.clickOnCancelButtonInProjectOptionsForm();

    checkProjectTabAppearanceAndFields(
        CONSOLE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CONSOLE_SAMPLE_NAME),
        EXPECTED_CONSOLE_REPOSITORY_URL);

    newWorkspace.setValueOfNameFieldInProjectOptionsForm("");
    newWorkspace.setValueOfDescriptionFieldInProjectOptionsForm("");
    newWorkspace.setValueOfRepositoryUrlFieldInProjectOptionsForm("");
    newWorkspace.clickOnProjectTab(SPRING_SAMPLE_NAME);
    newWorkspace.waitProjectNameFieldValueInProjectOptionsForm(SPRING_SAMPLE_NAME);
    newWorkspace.clickOnProjectTab(CONSOLE_SAMPLE_NAME);

    checkProjectTabAppearanceAndFields(
        CONSOLE_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(CONSOLE_SAMPLE_NAME),
        EXPECTED_CONSOLE_REPOSITORY_URL);

    newWorkspace.setValueOfNameFieldInProjectOptionsForm(RENAMED_CONSOLE_SAMPLE_NAME);
    newWorkspace.clickOnSaveButtonInProjectOptionsForm();
    newWorkspace.waitProjectTabAppearance(RENAMED_CONSOLE_SAMPLE_NAME);
    newWorkspace.waitSaveButtonDisablingInProjectOptionsForm();
    newWorkspace.waitCancelButtonDisablingInProjectOptionsForm();
  }

  @Test(priority = 2)
  public void checkProjectsBlank() throws Exception {
    newWorkspace.waitPageLoad();
    newWorkspace.selectStack(BLANK);
    newWorkspace.waitStackSelected(BLANK);
    newWorkspace.clickOnAddOrImportProjectButton();
    newWorkspace.waitAddOrImportFormOpened();
    newWorkspace.clickOnBlankButton();

    newWorkspace.typeToBlankNameField(NAME_WITH_MAX_AVAILABLE_LENGTH);
    newWorkspace.waitErrorMessageDissappearanceInBlankNameField();

    newWorkspace.waitAddButtonInImportProjectFormEnabled();
    newWorkspace.waitCancelButtonInImportProjectFormEnabled();

    newWorkspace.typeToBlankNameField("");
    newWorkspace.waitAddButtonInImportProjectFormDisabled();
    newWorkspace.waitCancelButtonInImportProjectFormDisabled();

    newWorkspace.typeToBlankNameField(NAME_WITH_MAX_AVAILABLE_LENGTH + "p");
    newWorkspace.waitNameFieldErrorMessageInBlankForm(
        "The name has to be less than 128 characters long.");
    newWorkspace.waitAddButtonInImportProjectFormDisabled();
    newWorkspace.waitCancelButtonInImportProjectFormEnabled();

    newWorkspace.typeToBlankNameField(NAME_WITH_SPECIAL_CHARACTERS);
    newWorkspace.waitNameFieldErrorMessageInBlankForm(
        "The name should not contain special characters like space, dollar, etc.");
    newWorkspace.waitAddButtonInImportProjectFormDisabled();
    newWorkspace.waitCancelButtonInImportProjectFormEnabled();

    newWorkspace.typeToBlankDescriptionField(BLANK_FORM_DESCRIPTION);
    newWorkspace.waitTextInBlankDescriptionField(BLANK_FORM_DESCRIPTION);

    newWorkspace.clickOnCancelButtonInImportProjectForm();

    newWorkspace.waitTextInBlankNameField("");
    newWorkspace.waitTextInBlankDescriptionField("");

    newWorkspace.typeToBlankNameField(CUSTOM_BLANK_PROJECT_NAME);
    newWorkspace.typeToBlankDescriptionField(BLANK_FORM_DESCRIPTION);
    newWorkspace.clickOnAddButtonInImportProjectForm();

    newWorkspace.waitProjectTabAppearance(CUSTOM_BLANK_PROJECT_NAME);
    checkProjectTabAppearanceAndFields(
        CUSTOM_BLANK_PROJECT_NAME, BLANK_FORM_DESCRIPTION, BLANK_DEFAULT_URL);

    newWorkspace.clickOnAddOrImportProjectButton();
    newWorkspace.waitAddOrImportFormOpened();
    newWorkspace.clickOnGitButton();
    newWorkspace.waitGitTabOpenedInImportProjectForm();
    newWorkspace.typeToGitUrlField(BLANK_DEFAULT_URL);
    newWorkspace.clickOnAddButtonInImportProjectForm();

    checkProjectTabAppearanceAndFields(BLANK_PROJECT_NAME, "", BLANK_DEFAULT_URL);

    newWorkspace.clickOnAddOrImportProjectButton();
    newWorkspace.waitAddOrImportFormOpened();
    newWorkspace.clickOnGitHubButton();

    newWorkspace.setMachineRAM("dev-machine", 5.0);
    newWorkspace.typeWorkspaceName(WORKSPACE_NAME);
    newWorkspace.clickOnCreateButtonAndOpenInIDE();
    testWorkspaceServiceClient.waitStatus(WORKSPACE_NAME, defaultTestUser.getName(), RUNNING);
    dashboard.selectWorkspacesItemOnDashboard();
  }

  @Test(priority = 3)
  public void checkCreatingProject() throws Exception {
    newWorkspace.waitPageLoad();
    newWorkspace.typeWorkspaceName(TEST_BLANK_WORKSPACE_NAME);
    newWorkspace.selectStack(DOT_NET);
    newWorkspace.waitStackSelected(DOT_NET);

    Assert.assertEquals(newWorkspace.getWorkspaceNameValue(), TEST_BLANK_WORKSPACE_NAME);

    newWorkspace.selectStack(JAVA);
    newWorkspace.waitStackSelected(JAVA);

    Assert.assertEquals(newWorkspace.getWorkspaceNameValue(), TEST_BLANK_WORKSPACE_NAME);

    newWorkspace.setMachineRAM("dev-machine", 3.0);
    newWorkspace.waitRamValue("dev-machine", 3.0);

    newWorkspace.clickOnAddOrImportProjectButton();
    newWorkspace.waitAddOrImportFormOpened();
    newWorkspace.clickOnSampleCheckbox(SPRING_SAMPLE_NAME);
    newWorkspace.waitSampleCheckboxEnabled(SPRING_SAMPLE_NAME);
    newWorkspace.clickOnAddButtonInImportProjectForm();
    newWorkspace.waitProjectTabAppearance(SPRING_SAMPLE_NAME);
    newWorkspace.clickOnBottomCreateButton();
    newWorkspace.waitWorkspaceCreatedDialogIsVisible();
    newWorkspace.closeWorkspaceCreatedDialog();
    newWorkspace.waitWorkspaceCreatedDialogDisappearance();
    workspaceOverview.checkNameWorkspace(TEST_BLANK_WORKSPACE_NAME);

    seleniumWebDriver.navigate().back();

    prepareJavaWorkspaceAndOpenCreateDialog(TEST_JAVA_WORKSPACE_NAME);

    newWorkspace.clickOnEditWorkspaceButton();
    workspaceOverview.checkNameWorkspace(TEST_JAVA_WORKSPACE_NAME);

    seleniumWebDriver.navigate().back();

    prepareJavaWorkspaceAndOpenCreateDialog(TEST_JAVA_WORKSPACE_NAME_EDIT);

    newWorkspace.waitWorkspaceCreatedDialogIsVisible();
    newWorkspace.clickOnOpenInIDEButton();

    testWorkspaceServiceClient.waitStatus(
        TEST_JAVA_WORKSPACE_NAME_EDIT, defaultTestUser.getName(), RUNNING);

    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(SPRING_SAMPLE_NAME);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        SPRING_SAMPLE_NAME + "/src/main/java/org.eclipse.che.examples", "GreetingController.java");
    editor.waitActive();
    editor.waitTextIntoEditor(EXPECTED_TEXT_IN_EDITOR);
  }

  private void waitAllCheckboxesDisabled() {
    newWorkspace
        .getSamplesNames()
        .forEach(sampleName -> newWorkspace.waitSampleCheckboxDisabled(sampleName));
  }

  private void waitAllCheckboxesEnabled() {
    newWorkspace
        .getSamplesNames()
        .forEach(sampleName -> newWorkspace.waitSampleCheckboxEnabled(sampleName));
  }

  private void clickOnEachCheckbox() {
    newWorkspace
        .getSamplesNames()
        .forEach(sampleName -> newWorkspace.clickOnSampleCheckbox(sampleName));
  }

  private void checkProjectTabAppearanceAndFields(
      String tabName, String expectedDescription, String expectedUrl) {
    newWorkspace.waitProjectOptionsForm();
    newWorkspace.waitProjectNameFieldValueInProjectOptionsForm(tabName);
    newWorkspace.waitDescriptionFieldValueInProjectOptionsForm(expectedDescription);
    newWorkspace.waitRepositoryUrlFieldValueInProjectOptionsForm(expectedUrl);
    newWorkspace.waitRemoveButtonInProjectOptionsForm();
    newWorkspace.waitCancelButtonDisablingInProjectOptionsForm();
    newWorkspace.waitSaveButtonDisablingInProjectOptionsForm();
  }

  private void prepareJavaWorkspaceAndOpenCreateDialog(String workspaceName) {
    newWorkspace.waitPageLoad();
    newWorkspace.typeWorkspaceName(workspaceName);
    newWorkspace.selectStack(JAVA);
    newWorkspace.waitStackSelected(JAVA);
    newWorkspace.clickOnAddOrImportProjectButton();
    newWorkspace.waitAddOrImportFormOpened();
    newWorkspace.clickOnSampleCheckbox(SPRING_SAMPLE_NAME);
    newWorkspace.waitSampleCheckboxEnabled(SPRING_SAMPLE_NAME);
    newWorkspace.clickOnAddButtonInImportProjectForm();
    checkProjectTabAppearanceAndFields(
        SPRING_SAMPLE_NAME,
        EXPECTED_SAMPLES_WITH_DESCRIPTIONS.get(SPRING_SAMPLE_NAME),
        EXPECTED_SPRING_REPOSITORY_URL);
    newWorkspace.clickOnBottomCreateButton();
    newWorkspace.waitWorkspaceCreatedDialogIsVisible();
  }

  private void checkWorkspaceStatusAndDelete(String workspaceName) throws Exception {
    if (!testWorkspaceServiceClient.exists(workspaceName, defaultTestUser.getName())) {
      return;
    }

    testWorkspaceServiceClient.delete(workspaceName, defaultTestUser.getName());
  }
}
