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
package org.eclipse.che.selenium.git;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.ContextMenuLocator.OPEN_ON_GITHUB;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.BLANK;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test "Open in Terminal" action. Will check action calling from context menu on selected project,
 * folder and file
 *
 * @author Vitalii Parfonov
 */
public class OpenOnGitHubTest {
  private static final String PROJECT_NAME = generate("project", 4);
  private static final String PROJECT_WITHOUT_GIT_FOLDER = "projectWithoutGitCVS";
  private static final String PATH_TO_EXPAND = "/src/main/java/che/eclipse/sample";
  private String mainBrowserTabHandle;

  @SuppressWarnings("unused")
  @Inject
  private TestWorkspace workspace;

  @SuppressWarnings("unused")
  @Inject
  private Ide ide;

  @SuppressWarnings("unused")
  @Inject
  private ProjectExplorer projectExplorer;

  @SuppressWarnings("unused")
  @Inject
  private TestProjectServiceClient testProjectServiceClient;

  @SuppressWarnings("unused")
  @Inject
  private CheTerminal terminal;

  @SuppressWarnings("unused")
  @Inject
  private SeleniumWebDriverHelper seleniumWebDriverHelper;

  @SuppressWarnings("unused")
  @Inject
  private SeleniumWebDriver seleniumWebDriver;

  @Inject private CodenvyEditor editor;

  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  @Inject private DefaultTestUser productUser;

  @Inject private TestGitHubRepository testRepo;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private org.eclipse.che.selenium.pageobject.git.Git git;

  @BeforeClass
  public void setUp() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    Path entryPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath());
    testProjectServiceClient.importProject(
        workspace.getId(), entryPath, PROJECT_WITHOUT_GIT_FOLDER, ProjectTemplates.MAVEN_SPRING);

    testRepo.addContent(entryPath);
    ide.open(workspace);
    mainBrowserTabHandle = seleniumWebDriver.getWindowHandle();
    git.importJavaApp(testRepo.getHtmlUrl(), PROJECT_NAME, BLANK);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
  }

  @AfterMethod
  public void returnToMainWindow() {
    if (seleniumWebDriver.getWindowHandles().size() > 1) {
      seleniumWebDriverHelper.closeCurrentWinAndReturnToMainTab(mainBrowserTabHandle);
    }
  }

  @Test
  public void openProjectOnGitHubTest() {
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(
        TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.OPEN_ON_GITHUB);
    seleniumWebDriverHelper.switchToNextWindow(seleniumWebDriver.getWindowHandle());
    Assert.assertTrue(seleniumWebDriver.getCurrentUrl().startsWith(testRepo.getHtmlUrl()));
  }

  @Test
  public void openFolderOnGitHubTest() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(
        TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.OPEN_ON_GITHUB);
    seleniumWebDriverHelper.switchToNextWindow(seleniumWebDriver.getWindowHandle());
    Assert.assertEquals(
        seleniumWebDriver.getCurrentUrl(), testRepo.getHtmlUrl() + "/tree/master" + PATH_TO_EXPAND);
  }

  @Test
  public void openFileOnGitHubTest() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/che/eclipse/sample/Aclass.java");
    editor.selectLines(14, 8);
    editor.openContextMenuInEditor();
    editor.clickOnItemInContextMenu(OPEN_ON_GITHUB);
    seleniumWebDriverHelper.switchToNextWindow(seleniumWebDriver.getWindowHandle());
    Assert.assertEquals(
        seleniumWebDriver.getCurrentUrl(),
        testRepo.getHtmlUrl() + "/blob/master" + PATH_TO_EXPAND + "/Aclass.java#L14-L16");
  }

  @Test
  public void checkProjectWithoutGitFolder() {
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_WITHOUT_GIT_FOLDER);
    projectExplorer.waitItemOnContexMenuIsNotVisible(
        TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.OPEN_ON_GITHUB);
    // for closing context menu
    projectExplorer.clickOnItemInContextMenu(
        TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.COPY);
  }
}
