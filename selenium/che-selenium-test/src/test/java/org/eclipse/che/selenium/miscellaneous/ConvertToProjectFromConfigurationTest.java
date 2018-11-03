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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.CONFIGURATION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.FOLDER;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.CONVERT_TO_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.NEW;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.FILE;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.JAVASCRIPT_FILE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuItems;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class ConvertToProjectFromConfigurationTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final String PHP_FOLDER_NAME = "phpFolder";
  private static final String JS_FOLDER_NAME = "jsFolder";
  private static final String pathToPhpFile = PROJECT_NAME + "/src/main/webapp/" + PHP_FOLDER_NAME;
  private static final String pathToJsFile =
      PROJECT_NAME + "/src/main/webapp/WEB-INF/" + JS_FOLDER_NAME;

  private static final String TEXT_FILE_JS =
      "var express = require('express');\n"
          + "var app = express();\n"
          + "\n"
          + "app.get('/', function (req, res) {\n"
          + "    res.send('Hello World!');\n"
          + "});\n"
          + "\n"
          + "app.listen(3000, function () {\n"
          + "    console.log('Example app listening on port 3000!');\n"
          + "});";
  private static final String TEXT_FILE_CSS = "echo \"Hello World!\"\n" + "\n" + "?>";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private Wizard wizard;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private ActionsFactory actionsFactory;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkConvertToProjectFromConfiguration() throws Exception {
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();

    // create folder with php file and convert to project
    createNewFolder(PROJECT_NAME + "/src/main/webapp", PHP_FOLDER_NAME);
    createNewFile("file.php", pathToPhpFile, FILE);
    projectExplorer.waitVisibilityByName("file.php");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/phpFolder/file.php");
    editor.waitActive();
    actionsFactory
        .createAction(seleniumWebDriver)
        .sendKeys(getContentFromFile("file.php"))
        .perform();
    editor.waitTextIntoEditor(TEXT_FILE_CSS);

    convertToProject(PHP_FOLDER_NAME, PROJECT_NAME + "/src/main/webapp");
    wizard.selectTypeProject(Wizard.TypeProject.BLANK);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    wizard.waitCloseProjectConfigForm();

    projectExplorer.waitItem(pathToPhpFile);
    projectExplorer.waitAndSelectItem(pathToPhpFile);
    menu.runCommand(PROJECT, CONFIGURATION);
    wizard.waitOpenProjectConfigForm();
    wizard.waitTextParentDirectoryName("/" + PROJECT_NAME + "/src/main/webapp");
    wizard.waitTextProjectNameInput(PHP_FOLDER_NAME);
    wizard.selectTypeProject(Wizard.TypeProject.BLANK);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    wizard.waitCloseProjectConfigForm();

    // create folder with js file and convert to project
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/WEB-INF");
    createNewFolder(PROJECT_NAME + "/src/main/webapp/WEB-INF", JS_FOLDER_NAME);
    createNewFile("fileJS", pathToJsFile, JAVASCRIPT_FILE);
    projectExplorer.waitVisibilityByName("fileJS.js");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsFolder/fileJS.js");
    editor.waitActive();
    actionsFactory
        .createAction(seleniumWebDriver)
        .sendKeys(getContentFromFile("file.js"))
        .perform();
    editor.waitTextIntoEditor(TEXT_FILE_JS);

    convertToProject(JS_FOLDER_NAME, PROJECT_NAME + "/src/main/webapp/WEB-INF");
    wizard.selectTypeProject(Wizard.TypeProject.BLANK);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    wizard.waitCloseProjectConfigForm();

    projectExplorer.waitItem(pathToJsFile);
    projectExplorer.waitAndSelectItem(pathToJsFile);
    menu.runCommand(PROJECT, CONFIGURATION);
    wizard.waitOpenProjectConfigForm();
    wizard.waitTextParentDirectoryName("/" + PROJECT_NAME + "/src/main/webapp/WEB-INF");
    wizard.waitTextProjectNameInput(JS_FOLDER_NAME);
    wizard.selectTypeProject(Wizard.TypeProject.BLANK);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    wizard.waitCloseProjectConfigForm();
  }

  public void createNewFolder(String path, String folderName) {
    projectExplorer.waitAndSelectItem(path);
    menu.runCommand(PROJECT, TestMenuCommandsConstants.Project.New.NEW, FOLDER);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(folderName);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    projectExplorer.waitVisibilityByName(folderName, ELEMENT_TIMEOUT_SEC);
    loader.waitOnClosed();
  }

  private void convertToProject(String folderName, String parentDirectory) {
    projectExplorer.waitAndSelectItemByName(folderName);
    projectExplorer.openContextMenuByPathSelectedItem(parentDirectory + "/" + folderName);
    projectExplorer.clickOnItemInContextMenu(CONVERT_TO_PROJECT);
    wizard.waitOpenProjectConfigForm();
    wizard.waitTextParentDirectoryName("/" + parentDirectory);
    wizard.waitTextProjectNameInput(folderName);
  }

  private void createNewFile(String name, String pathToFile, ContextMenuItems type)
      throws Exception {
    projectExplorer.waitAndSelectItem(pathToFile);
    projectExplorer.openContextMenuByPathSelectedItem(pathToFile);
    projectExplorer.clickOnItemInContextMenu(NEW);
    projectExplorer.clickOnItemInContextMenu(type);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(name);
    askForValueDialog.clickOkBtn();
  }

  private String getContentFromFile(String path) throws Exception {
    List<String> textFromFile =
        Files.readAllLines(
            Paths.get(getClass().getResource(path).toURI()), Charset.forName("UTF-8"));
    return Joiner.on("\n").join(textFromFile);
  }
}
