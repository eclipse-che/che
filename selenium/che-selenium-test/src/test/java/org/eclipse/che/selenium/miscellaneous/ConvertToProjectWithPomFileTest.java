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

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.CONVERT_TO_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.NEW;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.XML_FILE;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.InformationDialog;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class ConvertToProjectWithPomFileTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final String NEW_FOLDER_NAME = "new-folder";
  private static final String NEW_MODULE_NAME = "new-module";
  private static final String PATH_TO_POM_FILE = PROJECT_NAME + "/" + NEW_MODULE_NAME;
  private static final String EXPECTED_TEXT =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
          + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n"
          + "<modelVersion>4.0.0</modelVersion>\n"
          + "<groupId>groupId</groupId>\n"
          + "<artifactId>new-module</artifactId>\n"
          + "<packaging>jar</packaging>\n"
          + "<version>1.0-SNAPSHOT</version>\n"
          + "<name>new-module</name>\n"
          + "</project>";

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

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);
    ide.open(workspace);

    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.selectItem(PROJECT_NAME);
  }

  @BeforeMethod
  public void expandProject() {
    projectExplorer.quickCollapseJavaScript();
    projectExplorer.quickExpandWithJavaScript();
  }

  @Test
  public void checkConvertToProjectWithPomFile() throws Exception {

    // create a folder and check message if the path is wrong
    createNewFolder(PROJECT_NAME, NEW_FOLDER_NAME);
    projectExplorer.selectVisibleItem(NEW_FOLDER_NAME);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/" + NEW_FOLDER_NAME);
    projectExplorer.clickOnItemInContextMenu(CONVERT_TO_PROJECT);
    wizard.waitOpenProjectConfigForm();
    wizard.waitTextParentDirectoryName("/" + PROJECT_NAME);
    wizard.waitTextProjectNameInput(NEW_FOLDER_NAME);
    wizard.selectSample(Wizard.TypeProject.MAVEN);
    informationDialog.acceptInformDialogWithText("pom.xml does not exist.");
    wizard.selectSample(Wizard.TypeProject.BLANK);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    wizard.waitCloseProjectConfigForm();

    // create a folder with pom file
    createNewFolder(PROJECT_NAME, NEW_MODULE_NAME);
    createNewFile("pom", PATH_TO_POM_FILE, XML_FILE);
    editor.waitActive();
    editor.waitTabIsPresent("pom.xml");
    editor.selectTabByName("pom.xml");
    editor.deleteAllContent();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(EXPECTED_TEXT).perform();
    editor.waitTextIntoEditor(EXPECTED_TEXT);

    // this timeout is needed for waiting that the Editor tab name of 'pom.xml' file is changed
    WaitUtils.sleepQuietly(5);
    editor.waitTabIsPresent("pom.xml");
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(PATH_TO_POM_FILE, "simpleFolder");

    editor.closeAllTabs();
    seleniumWebDriver.navigate().refresh();
    projectExplorer.waitProjectExplorer();

    try {
      projectExplorer.waitFolderDefinedTypeOfFolderByPath(PATH_TO_POM_FILE, "simpleFolder");
    } catch (TimeoutException ex) {
      // Remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7551");
    }
  }

  @Test
  public void checkEditorTabNameAfterChangingArtifactID() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
    editor.waitActive();
    editor.waitTabIsPresent("qa-spring-sample");
    editor.goToCursorPositionVisible(18, 17);
    editor.typeTextIntoEditor("new-");

    // this timeout is needed for waiting that the Editor tab name of 'pom.xml' file is changed
    WaitUtils.sleepQuietly(5);
    editor.waitTabIsPresent("new-qa-spring-sample");

    seleniumWebDriver.navigate().refresh();
    ide.waitOpenedWorkspaceIsReadyToUse();
    try {
      projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
    } catch (TimeoutException ex) {
      // Remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7551");
    }
    editor.waitTabIsPresent("new-qa-spring-sample");

    editor.closeAllTabsByContextMenu();
  }

  private void createNewFolder(String path, String folderName) {
    projectExplorer.selectItem(path);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.FOLDER);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(folderName);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    projectExplorer.waitItemInVisibleArea(folderName);
    loader.waitOnClosed();
  }

  private void createNewFile(String name, String pathToFile, String type) {
    projectExplorer.selectItem(pathToFile);
    projectExplorer.openContextMenuByPathSelectedItem(pathToFile);
    projectExplorer.clickOnItemInContextMenu(NEW);
    projectExplorer.clickOnItemInContextMenu(type);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(name);
    askForValueDialog.clickOkBtn();
  }
}
