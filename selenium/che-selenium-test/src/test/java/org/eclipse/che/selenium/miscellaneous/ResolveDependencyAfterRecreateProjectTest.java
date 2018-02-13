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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.DELETE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.ERROR_MARKER;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.WEB_JAVA_SPRING;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Musienko Maxim
 * @author Aleksandr Shmaraev
 */
public class ResolveDependencyAfterRecreateProjectTest {
  private static final String PROJECT_NAME1 = generate("project1", 4);
  private static final String PROJECT_NAME2 = generate("project2", 4);
  private static final String PATH_FOR_EXPAND =
      "/src/main/java/org/eclipse/che/examples/GreetingController.java";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private AskDialog askDialog;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
  }

  @Test
  public void updateDependencyWithInheritTest() throws InterruptedException {
    ide.waitOpenedWorkspaceIsReadyToUse();

    createProjectFromUI(PROJECT_NAME1);
    projectExplorer.waitItem(PROJECT_NAME1);
    mavenPluginStatusBar.waitClosingInfoPanel();
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PROJECT_NAME1 + PATH_FOR_EXPAND);
    editor.waitActive();
    editor.waitAllMarkersDisappear(ERROR_MARKER);

    removeProjectFromUI();
    createProjectFromUI(PROJECT_NAME2);

    projectExplorer.waitItem(PROJECT_NAME2);
    projectExplorer.selectVisibleItem(PROJECT_NAME2);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PROJECT_NAME2 + PATH_FOR_EXPAND);
    editor.waitActive();
    editor.waitAllMarkersDisappear(ERROR_MARKER);
  }

  private void removeProjectFromUI() {
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME1);
    projectExplorer.clickOnItemInContextMenu(DELETE);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    projectExplorer.waitItemIsNotPresentVisibleArea(PROJECT_NAME1);
  }

  /**
   * create project with UI
   *
   * @param nameOfTheProject name of created project
   */
  private void createProjectFromUI(String nameOfTheProject) {
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectTypeProject(MAVEN);
    wizard.selectSample(WEB_JAVA_SPRING);
    wizard.typeProjectNameOnWizard(nameOfTheProject);
    wizard.clickCreateButton();
    wizard.waitCloseProjectConfigForm();
  }
}
