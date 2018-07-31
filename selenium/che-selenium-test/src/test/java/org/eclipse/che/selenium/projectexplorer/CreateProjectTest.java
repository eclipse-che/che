/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class CreateProjectTest {

  private static final List<String> PROJECT_NAMES =
      Arrays.asList("CreateProjectTest1", "CreateProjectTest2", "CreateProjectTest3");
  private static final String VERSION_OF_PROJECT = "1.0";
  private static final String ARTIFACT_ID_OF_PROJECT = "test.project";
  private static final String GROUP_ID_OF_PROJECT = "com.test.project";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Wizard projectWizard;
  @Inject private Menu menu;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void openProjectWizard() throws Exception {
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    // create project with help context menu in the list of projects
    createProject(PROJECT_NAMES.get(0), Wizard.PackagingMavenType.NOT_SPECIFIED);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAMES.get(0));
    loader.waitOnClosed();
    createProject(PROJECT_NAMES.get(1), Wizard.PackagingMavenType.JAR);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAMES.get(1));
    loader.waitOnClosed();
    createProject(PROJECT_NAMES.get(2), Wizard.PackagingMavenType.WAR);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAMES.get(2));
    loader.waitOnClosed();
  }

  private void createProject(String projectName, Wizard.PackagingMavenType packageType) {

    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.typeProjectNameOnWizard(projectName);
    projectWizard.clickNextButton();
    projectWizard.waitOpenProjectConfigForm();
    projectWizard.setArtifactIdOnWizard(ARTIFACT_ID_OF_PROJECT);
    projectWizard.checkArtifactIdOnWizardContainsText(ARTIFACT_ID_OF_PROJECT);
    projectWizard.setGroupIdOnWizard(GROUP_ID_OF_PROJECT);
    projectWizard.checkGroupIdOnWizardContainsText(GROUP_ID_OF_PROJECT);
    projectWizard.setVersionOnWizard(VERSION_OF_PROJECT);
    projectWizard.checkVersionOnWizardContainsText(VERSION_OF_PROJECT);
    projectWizard.selectPackagingType(packageType);
    projectWizard.clickCreateButton();
    loader.waitOnClosed();
    projectWizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
    loader.waitOnClosed();
  }
}
