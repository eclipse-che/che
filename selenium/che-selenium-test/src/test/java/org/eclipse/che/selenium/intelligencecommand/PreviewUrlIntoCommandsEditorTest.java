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
package org.eclipse.che.selenium.intelligencecommand;

import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsDefaultNames.MAVEN_NAME;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.COMMON_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsTypes.MAVEN_TYPE;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsEditorLocator.PREVIEW_URL_EDITOR;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class PreviewUrlIntoCommandsEditorTest {
  private static final String PROJ_NAME = NameGenerator.generate("PeviewUrlCommandsEditor", 4);
  private static final String NAME_COMM = "testCommand";
  private static final String PREVIEW_URL = "https://www.eclipse.org/che/getting-started/cloud/";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles consoles;
  @Inject private Loader loader;
  @Inject private CommandsExplorer commandsExplorer;
  @Inject private CommandsEditor commandsEditor;
  @Inject private CodenvyEditor editor;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(), Paths.get(resource.toURI()), PROJ_NAME, ProjectTemplates.PLAIN_JAVA);
    ide.open(testWorkspace);
  }

  @Test
  public void checkSavePreviewUrlIntoCommand() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJ_NAME);
    String currentWindow = seleniumWebDriver.getWindowHandle();

    // create new preview url
    createMavenCommand();
    commandsExplorer.waitCommandExplorerIsOpened();
    commandsExplorer.selectCommandByName(MAVEN_NAME);
    commandsEditor.waitActive();
    commandsEditor.typeTextIntoNameCommandField(NAME_COMM);
    commandsEditor.waitTextIntoNameCommandField(NAME_COMM);
    commandsEditor.setFocusIntoTypeCommandsEditor(PREVIEW_URL_EDITOR);
    commandsEditor.setCursorToLine(1);
    commandsEditor.deleteAllContent();
    commandsEditor.typeTextIntoEditor(PREVIEW_URL);
    commandsEditor.waitTextIntoEditor(PREVIEW_URL);
    commandsEditor.waitTabCommandWithUnsavedStatus(MAVEN_NAME);
    commandsEditor.clickOnSaveButtonInTheEditCommand();
    editor.waitTabFileWithSavedStatus(NAME_COMM);

    // check preview url after run
    commandsEditor.clickOnRunButton();
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS);
    consoles.waitPreviewUrlIsPresent();
    consoles.clickOnPreviewUrl();
    seleniumWebDriverHelper.switchToNextWindow(currentWindow);
    checkPageOpenedByPreviewUrl();
  }

  private void createMavenCommand() {
    commandsExplorer.openCommandsExplorer();
    commandsExplorer.waitCommandExplorerIsOpened();
    commandsExplorer.clickAddCommandButton(COMMON_GOAL);
    loader.waitOnClosed();
    commandsExplorer.chooseCommandTypeInContextMenu(MAVEN_TYPE);
    loader.waitOnClosed();
    commandsExplorer.waitCommandInExplorerByName(MAVEN_NAME);
  }

  private void checkPageOpenedByPreviewUrl() {
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//nav[@class='menu-desktop']//a[text()='Technology']")));
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//nav[@class='menu-desktop']//a[text()='Docs']")));
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//nav[@class='menu-desktop']//a[text()='Get Started']")));
  }
}
