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

import com.google.common.base.Strings;
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
 * @author Musienko Maxim
 * @author Aleksandr Shmaraev
 */
public class OpenInTerminalTest {
  private static final String PROJECT_NAME = generate("project", 4);
  private static final String PATH_TO_EXPAND = "/src/main/java";
  private static final String FILE = "/README.md";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private CheTerminal terminal;

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
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
  }

  @Test
  public void openInTerminalTest() {
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(OPEN_IN_TERMINAL);
    terminal.waitNumberTerminalTab(2);
    String terminalWorkDir = terminal.getVisibleTextFromTerminal();
    Assert.assertTrue(terminalWorkDir.trim().endsWith(PROJECT_NAME + "$"));
  }

  @Test
  public void openFolderInTerminalTest() {
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
