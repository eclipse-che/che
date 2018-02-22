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
package org.eclipse.che.selenium.projectexplorer;

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.NEW;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckDisplayingArtifactIdTest {

  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final String ARTIFACT_ID = "[qa-spring-sample]";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private Menu menu;
  @Inject private Preferences preferences;

  @BeforeClass
  public void setup() throws Exception {
    projectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void shouldEnableDisplayingProjIdAndCheckConfiguration() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.selectDroppedMenuByName("Maven");
    preferences.clickOnShowArtifactCheckBox();
    preferences.clickOnOkBtn();
    preferences.clickOnCloseBtn();
    preferences.waitPreferencesFormIsClosed();
    projectExplorer.waitVisibilityByName(PROJECT_NAME + " " + ARTIFACT_ID);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java");
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java");
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/src/main/java");
    projectExplorer.clickOnItemInContextMenu(NEW);
    projectExplorer.clickOnNewContextMenuItem(
        TestProjectExplorerContextMenuConstants.SubMenuNew.JAVA_CLASS);
    askForValueDialog.waitNewJavaClassOpen();
    askForValueDialog.clickCancelButtonJava();
    askForValueDialog.waitNewJavaClassClose();

    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.selectDroppedMenuByName("Maven");
    preferences.clickOnShowArtifactCheckBox();
    preferences.clickOnOkBtn();
    preferences.clickOnCloseBtn();

    projectExplorer.waitVisibilityByName(PROJECT_NAME);
  }
}
