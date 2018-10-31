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
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestGitConstants.CONFIGURING_PROJECT_AND_CLONING_SOURCE_CODE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_FACTORY;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.CONVERT_TO_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;
import static org.openqa.selenium.Keys.F4;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.CreateFactoryWidget;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.LoadingBehaviorPage;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CreateFactoryFromUiWithKeepDirTest {
  private static final String PROJECT_NAME = generate("project", 5);
  private static final String PROJECT_URL = "https://github.com/spring-guides/gs-rest-service";
  private static final String KEEPED_DIR = "complete";
  private static final String FACTORY_NAME = generate("keepFactory", 2);
  private static final String[] autocompleteContentAfterFirst = {
    "GreetingController", "GreetingControllerTest", "Greeting"
  };

  @Inject private DefaultTestUser user;
  @Inject private CodenvyEditor editor;
  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private Events events;
  @Inject private CreateFactoryWidget factoryWidget;
  @Inject private LoadingBehaviorPage loadingBehaviorPage;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private ImportProjectFromLocation importProjectFromLocation;
  @Inject private Loader loader;
  @Inject private TestIdeUrlProvider ideUrlProvider;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestFactoryServiceClient factoryServiceClient;
  @Inject private PullRequestPanel pullRequestPanel;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @AfterClass
  public void deleteFactoryRelatedStaff() throws Exception {
    workspaceServiceClient.deleteFactoryWorkspaces(testWorkspace.getName(), user.getName());
    factoryServiceClient.deleteFactory(FACTORY_NAME);
  }

  @AfterClass
  public void restoreContributionTabPreference() throws Exception {
    testUserPreferencesServiceClient.restoreDefaultContributionTabPreference();
  }

  @Test
  public void createFactoryFromUiWithKeepDirTest() {
    projectExplorer.waitProjectExplorer();

    consoles.waitJDTLSStartedMessage();
    makeKeepDirectoryFromGitUrl(PROJECT_URL, PROJECT_NAME, KEEPED_DIR);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);

    setUpModuleForFactory();

    consoles.clickOnProcessesButton();
    checkAutocompletion();
    checkOpenDeclaration();
  }

  private void setUpModuleForFactory() {
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME + "/" + KEEPED_DIR);

    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/" + KEEPED_DIR);
    projectExplorer.clickOnItemInContextMenu(CONVERT_TO_PROJECT);
    wizard.selectSample(MAVEN);
    wizard.clickSaveButton();

    projectExplorer.openItemByPath(PROJECT_NAME + "/" + KEEPED_DIR);

    events.clickEventLogBtn();
    createFactoryAndSwitchToWs();
  }

  private void createFactoryAndSwitchToWs() {
    String currentWin = seleniumWebDriver.getWindowHandle();

    menu.runCommand(WORKSPACE, CREATE_FACTORY);
    factoryWidget.waitOpen();
    factoryWidget.typeNameFactory(FACTORY_NAME);
    factoryWidget.clickOnCreateBtn();
    factoryWidget.waitTextIntoFactoryField(ideUrlProvider.get().toString());
    factoryWidget.clickOnInvokeBtn();
    seleniumWebDriverHelper.switchToNextWindow(currentWin);
    loadingBehaviorPage.waitWhileLoadPageIsClosed();
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();

    try {
      projectExplorer.waitProjectExplorer(LOADER_TIMEOUT_SEC);
    } catch (org.openqa.selenium.TimeoutException ex) {
      seleniumWebDriver.switchTo().defaultContent();
      projectExplorer.waitProjectExplorer(LOADER_TIMEOUT_SEC);
    }

    events.clickEventLogBtn();

    try {
      events.waitExpectedMessage(CONFIGURING_PROJECT_AND_CLONING_SOURCE_CODE);
      events.waitExpectedMessage("Project " + PROJECT_NAME + " imported");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known random failure https://github.com/eclipse/che/issues/7253");
    }

    consoles.clickOnProcessesButton();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);

    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PROJECT_NAME + "/" + KEEPED_DIR + "/src/main/java/hello", "GreetingController.java");

    mavenPluginStatusBar.waitClosingInfoPanel(UPDATING_PROJECT_TIMEOUT_SEC);
    editor.waitAllMarkersInvisibility(ERROR);
  }

  private void makeKeepDirectoryFromGitUrl(String url, String projectName, String folderName) {
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    importProjectFromLocation.waitMainForm();
    loader.waitOnClosed();
    importProjectFromLocation.selectGitHubSourceItem();
    loader.waitOnClosed();
    importProjectFromLocation.typeURi(url);
    importProjectFromLocation.typeProjectName(projectName);
    importProjectFromLocation.waitKeepDirectoryIsNotSelected();
    importProjectFromLocation.clickOnKeepDirectoryCheckbox();
    importProjectFromLocation.waitKeepDirectoryIsSelected();
    importProjectFromLocation.typeDirectoryName(folderName);
    importProjectFromLocation.clickImportBtn();
    importProjectFromLocation.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
  }

  private void checkAutocompletion() {
    editor.goToCursorPositionVisible(18, 6);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("Greeting");
    editor.launchAutocompleteAndWaitContainer();

    String textFromEditorAfterFirstCall = editor.getAllVisibleTextFromAutocomplete();

    for (String content : autocompleteContentAfterFirst) {
      assertTrue(textFromEditorAfterFirstCall.contains(content));
    }

    editor.closeAutocomplete();
    editor.typeTextIntoEditor(" greeting =null;");
    editor.waitAllMarkersInvisibility(ERROR);
  }

  private void checkOpenDeclaration() {
    String expectedTextBeforeDownloadSources =
        "package hello;\n"
            + "\n"
            + "public class Greeting {\n"
            + "\n"
            + "    private final long id;\n"
            + "    private final String content;\n"
            + "\n"
            + "    public Greeting(long id, String content) {\n"
            + "        this.id = id;\n"
            + "        this.content = content;\n"
            + "    }\n"
            + "\n"
            + "    public long getId() {\n"
            + "        return id;\n"
            + "    }\n"
            + "\n"
            + "    public String getContent() {\n"
            + "        return content;\n"
            + "    }\n"
            + "}";

    editor.goToCursorPositionVisible(15, 12);
    editor.typeTextIntoEditor(F4.toString());
    editor.waitTabIsPresent("Greeting");
    editor.waitTextIntoEditor(expectedTextBeforeDownloadSources);
  }
}
