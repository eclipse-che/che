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
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckShowHideHiddenFilesTest {
  private static final String PROJECT_NAME = "RefreshProject";
  private static final String PATH_TO_CLASSPATH_FILE = PROJECT_NAME + "/.classpath";
  private static final String PATH_TO_PROJECT_FILE = PROJECT_NAME + "/.project";
  public static final String CLASSPATH_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private Wizard projectWizard;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void checkShowHideHiddenFilesTest() {
    createProject(PROJECT_NAME);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.SHOW_HIDE_HIDDEN_FILES);
    loader.waitOnClosed();
    projectExplorer.waitItem(PATH_TO_CLASSPATH_FILE);
    projectExplorer.waitItem(PATH_TO_PROJECT_FILE);
    projectExplorer.openItemByPath(PATH_TO_CLASSPATH_FILE);
    loader.waitOnClosed();
    editor.waitActive();
    editor.waitTextIntoEditor(CLASSPATH_CONTENT);
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.SHOW_HIDE_HIDDEN_FILES);
    loader.waitOnClosed();
    editor.waitTabIsPresent(".classpath");
    projectExplorer.waitItemInvisibility(PATH_TO_CLASSPATH_FILE);
    projectExplorer.waitItemInvisibility(PATH_TO_PROJECT_FILE);
  }

  private void createProject(String projectName) {
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.typeProjectNameOnWizard(projectName);
    projectWizard.selectSample(Wizard.SamplesName.WEB_JAVA_SPRING);
    projectWizard.clickCreateButton();
    loader.waitOnClosed();
    projectWizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
    loader.waitOnClosed();
  }
}
