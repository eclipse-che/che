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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.OPEN_IN_TERMINAL;
import static org.openqa.selenium.Keys.ALT;
import static org.openqa.selenium.Keys.F12;
import static org.openqa.selenium.Keys.SHIFT;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
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

/**
 * Test "Open in Terminal" action. Will check action calling from context menu on selected project,
 * folder and file
 *
 * @author Vitalii Parfonov
 */
public class OpenInTerminalTest {
  private static final String PROJECT_NAME = generate("project", 4);
  private static final String PATH_TO_EXPAND = "/src/main/java";
  private static final String FILE = "/README.md";

  @SuppressWarnings("unused")
  @Inject
  protected TestWorkspace workspace;

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

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = this.getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
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
    terminal.waitTerminalTab(2);
    terminal.waitTextInTerminal(2, PROJECT_NAME);
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
  public void openFolderInTerminalTest() throws Exception {
    seleniumWebDriver.navigate().refresh();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.expandPathInProjectExplorer(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(OPEN_IN_TERMINAL);
    terminal.waitTerminalTab(2);
    terminal.waitTextInTerminal(2, getExpectedTextInTerminal());

    projectExplorer.waitAndSelectItem(PROJECT_NAME + FILE);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + FILE);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(OPEN_IN_TERMINAL);
    terminal.waitTerminalTab(3);
    terminal.waitTextInTerminal(3, PROJECT_NAME);
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
    terminal.waitTerminalTab(2);
    terminal.waitTextInTerminal(2, PROJECT_NAME);
  }

  protected String getExpectedTextInTerminal() throws Exception {
    return PROJECT_NAME + PATH_TO_EXPAND;
  }
}
