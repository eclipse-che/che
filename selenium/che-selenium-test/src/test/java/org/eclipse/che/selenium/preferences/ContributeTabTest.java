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
package org.eclipse.che.selenium.preferences;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ToolWindows.CONTRIBUTE_TOOL_WIDOWS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ToolWindows.TOOL_WINDOWS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PREFERENCES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PROFILE_MENU;
import static org.eclipse.che.selenium.pageobject.PanelSelector.PanelTypes.LEFT_RIGHT_BOTTOM_ID;
import static org.eclipse.che.selenium.pageobject.Preferences.DropDownGitInformationMenu.CONTRIBUTE_PREFERENCES;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.PanelSelector;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.refactor.move.MoveItemsTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class ContributeTabTest {
  private static final Logger LOG = LoggerFactory.getLogger(MoveItemsTest.class);
  private static final String FIRST_PROJECT_NAME = "first-vcs-project";
  private static final String SECOND_PROJECT_NAME = "second-vcs-project";
  private static final String THIRD_PROJECT_NAME = "not-vcs-project";
  private static final String EXP_TEXT_NOT_VCS =
      "Project does not provide VCS, supported by contribution plugin.";
  private static final String NOTIFICATION_MESSAGE =
      "To activate Contribute Panel by clicking on a project go to Profile -> Preferences -> Git -> Contribute";

  private String firstProjectUrl;
  private String secondProjectUrl;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private DefaultTestUser user;
  @Inject private AskDialog askDialog;
  @Inject private Preferences preferences;
  @Inject private TestWorkspace testWorkspace;
  @Inject private TestGitHubRepository testRepo;
  @Inject private TestGitHubRepository testRepo2;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private PullRequestPanel pullRequestPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private PanelSelector panelSelector;

  @BeforeClass
  public void setUp() throws Exception {
    Path entryPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath());
    testRepo.addContent(entryPath);
    testRepo2.addContent(entryPath);

    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        THIRD_PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(testWorkspace);
  }

  @AfterMethod
  public void closeForm() {
    if (askDialog.isOpened()) {
      askDialog.confirmAndWaitClosed();
    }

    if (preferences.isPreferencesFormOpened()) {
      preferences.clickOnCloseButton();
    }

    if (askDialog.isOpened()) {
      askDialog.confirmAndWaitClosed();
    }

    preferences.waitPreferencesFormIsClosed();
  }

  @Test
  public void checkSettingContributeTab() {
    firstProjectUrl = testRepo.getHtmlUrl() + ".git";
    secondProjectUrl = testRepo2.getHtmlUrl() + ".git";

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(THIRD_PROJECT_NAME);

    // import the first and second projects
    git.importJavaApp(firstProjectUrl, FIRST_PROJECT_NAME, MAVEN);
    git.importJavaApp(secondProjectUrl, SECOND_PROJECT_NAME, MAVEN);

    // check opening the PR panel by select a project
    pullRequestPanel.waitClosePanel();
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();

    // check the text when a project is not under VCS
    projectExplorer.waitAndSelectItem(THIRD_PROJECT_NAME);
    pullRequestPanel.waitTextNotVcsProject(EXP_TEXT_NOT_VCS);

    // check the 'Contribute' checkbox is true by default
    openContributeTab();
    preferences.waitContributeCheckboxIsSelected();
    preferences.closeForm();
  }

  @Test(priority = 1)
  public void checkRefreshAndSaveButton() {
    openContributeTab();

    preferences.setContributeCheckbox(true);
    preferences.clickOnOkBtn();

    assertFalse(preferences.isSaveButtonIsEnabled());

    // check the 'Refresh button
    preferences.clickOnContributeCheckbox();
    preferences.waitContributeCheckboxIsNotSelected();

    assertTrue(preferences.isSaveButtonIsEnabled());

    preferences.clickRefreshButton();
    preferences.waitContributeCheckboxIsSelected();

    assertFalse(preferences.isSaveButtonIsEnabled());

    preferences.closeForm();
  }

  @Test(priority = 1)
  public void checkSwitchProjectsWhenContributeIsFalse() {
    openContributeTab();
    setStateContributeChecboxAndCloseForm(true);

    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();

    openContributeTab();
    setStateContributeChecboxAndCloseForm(false);

    // switch between projects
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    pullRequestPanel.waitRepoUrl(firstProjectUrl);
    pullRequestPanel.waitProjectName(FIRST_PROJECT_NAME);

    projectExplorer.waitAndSelectItem(SECOND_PROJECT_NAME);
    pullRequestPanel.waitRepoUrl(secondProjectUrl);
    pullRequestPanel.waitProjectName(SECOND_PROJECT_NAME);

    projectExplorer.waitAndSelectItem(THIRD_PROJECT_NAME);
    pullRequestPanel.waitTextNotVcsProject(EXP_TEXT_NOT_VCS);
  }

  @Test(priority = 1)
  public void checkAutomaticChangeContributeToFalse() {
    openContributeTab();
    setStateContributeChecboxAndCloseForm(true);

    // change 'Contribute' to false by 'Hide' button
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();
    pullRequestPanel.closePanelByHideButton();
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed(NOTIFICATION_MESSAGE);

    openContributeTab();
    preferences.waitContributeCheckboxIsNotSelected();

    // change 'Contribute' to false by 'Hide' from 'Options' on the PR panel
    setStateContributeChecboxAndCloseForm(true);

    projectExplorer.waitAndSelectItem(SECOND_PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();
    pullRequestPanel.openOptionsMenu();
    pullRequestPanel.closePanelFromContextMenu();
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed(NOTIFICATION_MESSAGE);

    openContributeTab();
    preferences.waitContributeCheckboxIsNotSelected();

    // change 'Contribute' to false by 'Contribute' action from 'Assistant'
    setStateContributeChecboxAndCloseForm(true);

    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();
    pullRequestPanel.clickPullRequestBtn();

    menu.runCommand(ASSISTANT, TOOL_WINDOWS, CONTRIBUTE_TOOL_WIDOWS);
    pullRequestPanel.waitClosePanel();

    openContributeTab();
    preferences.waitContributeCheckboxIsNotSelected();
    preferences.closeForm();
  }

  @Test(priority = 1)
  public void checkDirectAccessToContributeTab() {
    openContributeTab();
    setStateContributeChecboxAndCloseForm(false);

    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    pullRequestPanel.waitClosePanel();

    // open PR panel by the 'Panel Selector'
    panelSelector.selectPanelTypeFromPanelSelector(LEFT_RIGHT_BOTTOM_ID);
    pullRequestPanel.waitOpenPanel();
    pullRequestPanel.closePanelByHideButton();

    // open PR panel by the 'Contribute' action from the 'Assistant
    menu.runCommand(ASSISTANT, TOOL_WINDOWS, CONTRIBUTE_TOOL_WIDOWS);
    pullRequestPanel.waitOpenPanel();

    menu.runCommand(ASSISTANT, TOOL_WINDOWS, CONTRIBUTE_TOOL_WIDOWS);
    pullRequestPanel.waitClosePanel();

    // open PR panel from call the 'Contribute' by hot key (Ctrl + ALT + '6')
    preferences.callContributeActionByHotKey();
    pullRequestPanel.waitOpenPanel();

    preferences.callContributeActionByHotKey();
    pullRequestPanel.waitClosePanel();
  }

  @Test(priority = 1)
  public void checkHidingPrPanelWhenProjectExplorerIsMaximized() {
    openContributeTab();
    setStateContributeChecboxAndCloseForm(true);

    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();
    projectExplorer.clickOnMaximizeButton();
    pullRequestPanel.waitClosePanel();

    // check the PR panel is closed after select project
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    pullRequestPanel.waitClosePanel();

    projectExplorer.clickOnMaximizeButton();
    pullRequestPanel.waitOpenPanel();
  }

  private void setStateContributeChecboxAndCloseForm(boolean state) {
    preferences.setContributeCheckbox(state);
    preferences.clickOnOkBtn();
    preferences.closeForm();
  }

  private void openContributeTab() {
    menu.runCommand(PROFILE_MENU, PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.waitMenuInCollapsedDropdown(CONTRIBUTE_PREFERENCES);
    preferences.selectDroppedMenuByName(CONTRIBUTE_PREFERENCES);
  }
}
