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

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.ERROR_MARKER;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
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
  private static final String NAME_OF_THE_PROJECT_1 =
      NameGenerator.generate("ResolveDependencyAfterRecreateProject", 4);
  private static final String NAME_OF_THE_PROJECT_2 =
      NameGenerator.generate("ResolveDependencyAfterRecreateProject", 4);
  private static final String PATH_FOR_EXPAND =
      "/src/main/java/org/eclipse/che/examples/GreetingController.java";

  @Inject private Loader loader;
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
    projectExplorer.waitProjectExplorer();
    createProjectFromUI(NAME_OF_THE_PROJECT_1);
    projectExplorer.waitItem(NAME_OF_THE_PROJECT_1);
    mavenPluginStatusBar.waitClosingInfoPanel();
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(NAME_OF_THE_PROJECT_1 + PATH_FOR_EXPAND);
    editor.waitActive();
    editor.waitAllMarkersDisappear(ERROR_MARKER);
    removeProjectFromUI();
    createProjectFromUI(NAME_OF_THE_PROJECT_2);
    projectExplorer.waitItem(NAME_OF_THE_PROJECT_2);
    projectExplorer.selectVisibleItem(NAME_OF_THE_PROJECT_2);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(NAME_OF_THE_PROJECT_2 + PATH_FOR_EXPAND);
    editor.waitActive();
    editor.waitAllMarkersDisappear(ERROR_MARKER);
  }

  public void removeProjectFromUI() {
    projectExplorer.openContextMenuByPathSelectedItem(NAME_OF_THE_PROJECT_1);
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.DELETE);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    projectExplorer.waitItemIsNotPresentVisibleArea(NAME_OF_THE_PROJECT_1);
  }

  /**
   * create project with UI
   *
   * @param nameOfTheProject name of created project
   */
  public void createProjectFromUI(String nameOfTheProject) {
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    wizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    wizard.selectSample(Wizard.SamplesName.WEB_JAVA_SPRING);
    wizard.typeProjectNameOnWizard(nameOfTheProject);
    wizard.clickCreateButton();
    wizard.waitCloseProjectConfigForm();
  }
}
