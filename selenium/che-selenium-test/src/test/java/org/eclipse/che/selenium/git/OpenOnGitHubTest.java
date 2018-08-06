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

import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test "Open in Terminal" action. Will check action calling from context menu on selected project,
 * folder and file
 *
 * @author Vitalii Parfonov
 */
public class OpenOnGitHubTest {
  private static final String PROJECT_NAME = generate("project", 4);
  private static final String PATH_TO_EXPAND = "/src/main/java/commenttest";
  private static final String FILE = "/README.md";

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

  private static final String REPO_URL = "https://github.com/iedexmain1/testRepo-1";

  private String mainBrowserTabHandle;

  @BeforeClass
  public void setUp() throws Exception {
    testProjectServiceClient.importProject(
        workspace.getId(), PROJECT_NAME, REPO_URL, "github", Collections.emptyMap());
    ide.open(workspace);
    mainBrowserTabHandle = seleniumWebDriver.getWindowHandle();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();

  }

  @AfterMethod
  public void returnToMainWindow() {
    seleniumWebDriverHelper.closeCurrentWinAndReturnToMainTab(mainBrowserTabHandle);
  }

  /** */
  @Test
  public void openProjectOnGitHubTest() {
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(
        TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.OPEN_ON_GITHUB);
    seleniumWebDriverHelper.switchToNextWindow(seleniumWebDriver.getWindowHandle());
    Assert.assertTrue(seleniumWebDriver.getCurrentUrl().startsWith(REPO_URL));
  }

  /** */
  @Test
  public void openFolderOnGitHubTest() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(
        TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.OPEN_ON_GITHUB);
    seleniumWebDriverHelper.switchToNextWindow(seleniumWebDriver.getWindowHandle());
    Assert.assertEquals(
        seleniumWebDriver.getCurrentUrl(), REPO_URL + "/tree/master" + PATH_TO_EXPAND);
  }

  /** */
   @Test
  public void openFileOnGitHubTest() {
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/commenttest/JavaCommentsTest.java");
    editor.selectLines(10, 10);
    editor.openContextMenuInEditor();
    editor.clickOnItemInContextMenu(CodenvyEditor.ContextMenuLocator.OPEN_ON_GITHUB);
    seleniumWebDriverHelper.switchToNextWindow(seleniumWebDriver.getWindowHandle());
    Assert.assertEquals(
        seleniumWebDriver.getCurrentUrl(),
        REPO_URL + "/blob/master" + PATH_TO_EXPAND + "/JavaCommentsTest.java#L10-L20");
  }
}
