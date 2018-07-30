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
package org.eclipse.che.selenium.git;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.OPEN_IN_TERMINAL;
import static org.openqa.selenium.Keys.ALT;
import static org.openqa.selenium.Keys.F12;
import static org.openqa.selenium.Keys.SHIFT;

/**
 * Test "Open in Terminal" action. Will check action calling from context menu on selected project,
 * folder and file
 *
 * @author Vitalii Parfonov
 */
public class OpenInGitHubTest {
  private static final String PROJECT_NAME = generate("project", 4);
  private static final String PATH_TO_EXPAND = "/src/main/java";
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

  @SuppressWarnings("unused")
  @Inject
  private TestGitHubRepository testRepo;

  @BeforeClass
  public void setUp() throws Exception {
    testProjectServiceClient.importProject(
        workspace.getId(),
        testRepo.getHtmlUrl(), PROJECT_NAME, "github", Collections.emptyMap());
    ide.open(workspace);
  }

  /**
   *
   *
   * <pre>
   *  - Select project in project explorer
   *  - Open context menu on selected project
   *  - Click on "Open in Terminal" action
   *  - Wait on opening new terminal, number of opened terminal should increase
   *  - Check working directory in open terminal, should point to the selected project
   * </pre>
   */
  @Test
  public void openProjectInTerminalTest() {
    seleniumWebDriver.navigate().refresh();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(OPEN_IN_TERMINAL);
    terminal.waitNumberTerminalTab(2);
    terminal.waitExpectedTextIntoTerminal(PROJECT_NAME + "$");
  }

  /**
   *
   *
   * <pre>
   * First check:
   *  - Expend project tree
   *  - Select some folder
   *  - Open context menu on selected folder
   *  - Click on "Open in Terminal" action
   *  - Wait on opening new terminal, number of opened terminal should increase
   *  - Check working directory in open terminal, should point to the selected folder
   *
   * Second check:
   *  - Select some file in project tree
   *  - Open context menu on selected folder
   *  - Click on "Open in Terminal" action
   *  - Wait on opening new terminal, number of opened terminal should increase
   *  - Check working directory in open terminal, should point to the parent of selected file
   * </pre>
   */
  @Test
  public void openFolderInTerminalTest() {
    seleniumWebDriver.navigate().refresh();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.expandPathInProjectExplorer(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(OPEN_IN_TERMINAL);
    terminal.waitNumberTerminalTab(2);
    terminal.waitExpectedTextIntoTerminal(PROJECT_NAME + PATH_TO_EXPAND + "$");

    projectExplorer.waitAndSelectItem(PROJECT_NAME + FILE);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + FILE);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(OPEN_IN_TERMINAL);
    terminal.waitNumberTerminalTab(3);
    terminal.waitExpectedTextIntoTerminal(PROJECT_NAME + "$");
  }

  /**
   *
   *
   * <pre>
   *  - Select project in project explorer
   *  - Open context menu on selected project
   *  - Call hot key Alt+Shift+F12
   *  - Wait on opening new terminal, number of opened terminal should increase
   *  - Check working directory in open terminal, should point to the selected project
   * </pre>
   */
  @Test
  public void openInTerminalTestByHotKey() {
    seleniumWebDriver.navigate().refresh();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    seleniumWebDriverHelper.sendKeys(Keys.chord(ALT, SHIFT, F12));
    terminal.waitNumberTerminalTab(2);
    terminal.waitExpectedTextIntoTerminal(PROJECT_NAME + "$");
  }
}
