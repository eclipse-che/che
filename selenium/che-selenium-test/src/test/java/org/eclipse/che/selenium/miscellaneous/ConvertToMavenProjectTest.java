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

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.UPDATE_PROJECT_CONFIGURATION;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.CONVERT_TO_PROJECT;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.PROJECT_OF_UNDEFINED_TYPE;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.InformationDialog;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev, Musienko Maxim */
public class ConvertToMavenProjectTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final String WEB_APP_MODULE = "my-webapp";
  private static final String NONE_MAVEN_PROJECT = NameGenerator.generate("noneMavenProject", 4);
  private static final String PARENT_ARTIFACT_SECTION =
      "<parent>\n"
          + "<groupId>org.eclipse.che.examples</groupId>\n"
          + "<artifactId>qa-multimodule</artifactId>\n"
          + "    <version>1.0-SNAPSHOT</version>\n"
          + "</parent>\n";
  private static final String CONVERT_PATH = format("%s/%s", PROJECT_NAME, WEB_APP_MODULE);
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private Wizard wizard;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private InformationDialog informationDialog;
  @Inject private ActionsFactory actionsFactory;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  private String workspaceId;

  @BeforeClass
  public void setUp() throws Exception {
    workspaceId = workspace.getId();
    URL mavenMultimodule = getClass().getResource("/projects/java-multimodule");
    URL noneMavenProject = getClass().getResource("/projects/console-cpp-simple");
    testProjectServiceClient.importProject(
        workspaceId, Paths.get(mavenMultimodule.toURI()), PROJECT_NAME, PROJECT_OF_UNDEFINED_TYPE);
    testProjectServiceClient.importProject(
        workspaceId,
        Paths.get(noneMavenProject.toURI()),
        NONE_MAVEN_PROJECT,
        PROJECT_OF_UNDEFINED_TYPE);

    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.quickRevealToItemWithJavaScript(format("%s/%s", PROJECT_NAME, WEB_APP_MODULE));
  }

  @Test
  public void shouldConvertToMavenMultimoduleProject() throws Exception {
    convertPredefinedFolderToMavenProjectWithContextMenu(CONVERT_PATH);
    testProjectServiceClient.checkProjectType(workspaceId, CONVERT_PATH, "maven");
    addParentArticatSectionIntoPomFile();
    menu.runCommand(PROJECT, UPDATE_PROJECT_CONFIGURATION);
    convertToMavenByWizard("/", PROJECT_NAME);
    testProjectServiceClient.checkProjectType(workspaceId, CONVERT_PATH, "maven");
  }

  @Test
  public void shouldNotConvertToMavenProject() {
    projectExplorer.waitAndSelectItem(NONE_MAVEN_PROJECT);
    menu.runCommand(PROJECT, UPDATE_PROJECT_CONFIGURATION);
    wizard.waitOpenProjectConfigForm();
    wizard.waitTextParentDirectoryName("/");
    wizard.waitTextProjectNameInput(NONE_MAVEN_PROJECT);
    wizard.selectSample(Wizard.TypeProject.MAVEN);
    informationDialog.acceptInformDialogWithText("pom.xml does not exist.");
    wizard.closeWithIcon();
  }

  private void convertPredefinedFolderToMavenProjectWithContextMenu(String converPath) {
    projectExplorer.waitAndSelectItem(converPath);
    projectExplorer.openContextMenuByPathSelectedItem(converPath);
    projectExplorer.clickOnItemInContextMenu(CONVERT_TO_PROJECT);
    convertToMavenByWizard("/" + PROJECT_NAME, WEB_APP_MODULE);
  }

  private void convertToMavenByWizard(String pathToDirectory, String convertedFolder) {
    wizard.waitOpenProjectConfigForm();
    wizard.waitTextParentDirectoryName(pathToDirectory);
    wizard.waitTextProjectNameInput(convertedFolder);
    wizard.selectSample(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    wizard.waitCloseProjectConfigForm();
  }

  private void addParentArticatSectionIntoPomFile() {
    projectExplorer.openItemByPath(CONVERT_PATH + "/pom.xml");
    editor.goToPosition(23, 3);
    editor.typeTextIntoEditor(PARENT_ARTIFACT_SECTION);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
  }
}
