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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.OPEN_IN_TERMINAL;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.Assert;
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

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = this.getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitVisibleItem(PROJECT_NAME);
  }

  /**
   * - Select project in project explorer - Open context menu on selected project - Click on "Open
   * in Terminal" action - Wait on opening new terminal, number of opened terminal should increase -
   * Check working directory in open terminal, should point to the selected project
   */
  @Test
  public void openInTerminalTest() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(OPEN_IN_TERMINAL);
    terminal.waitNumberTerminalTab(2);
    String terminalWorkDir = terminal.getVisibleTextFromTerminal();
    Assert.assertTrue(terminalWorkDir.trim().endsWith(PROJECT_NAME + "$"));
  }

  /**
   * First check: - Expend project tree - Select some folder - Open context menu on selected folder
   * - Click on "Open in Terminal" action - Wait on opening new terminal, number of opened terminal
   * should increase - Check working directory in open terminal, should point to the selected folder
   *
   * <p>Second check: - Select some file in project tree - Open context menu on selected folder -
   * Click on "Open in Terminal" action - Wait on opening new terminal, number of opened terminal
   * should increase - Check working directory in open terminal, should point to the parent of
   * selected file
   */
  @Test
  public void openFolderInTerminalTest() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.expandPathInProjectExplorer(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + PATH_TO_EXPAND);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(OPEN_IN_TERMINAL);
    terminal.waitNumberTerminalTab(2);
    String terminalWorkDir = terminal.getVisibleTextFromTerminal();
    Assert.assertTrue(terminalWorkDir.trim().endsWith(PROJECT_NAME + PATH_TO_EXPAND + "$"));

    projectExplorer.waitAndSelectItem(PROJECT_NAME + FILE);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + FILE);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(OPEN_IN_TERMINAL);
    terminal.waitNumberTerminalTab(3);
    terminalWorkDir = terminal.getVisibleTextFromTerminal();
    Assert.assertTrue(terminalWorkDir.trim().endsWith(PROJECT_NAME + "$"));
  }
}
