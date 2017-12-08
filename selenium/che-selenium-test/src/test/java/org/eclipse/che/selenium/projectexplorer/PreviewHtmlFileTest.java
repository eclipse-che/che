/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.projectexplorer;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Random;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class PreviewHtmlFileTest {
  private static final String PROJECT_NAME = "PreviewHtmlFile" + new Random().nextInt(999);
  private static final String H2_CONTENT = "    <h2 style='color:red'>Test content</h2>";
  private static final String BODY_CONTENT = "    <b>Content of file<b/>";
  private String currentWindow;

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Loader loader;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/console-java-with-html-file");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void checkPreviewHtmlFile() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    currentWindow = seleniumWebDriver.getWindowHandle();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/file.html");
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.PREVIEW);
    seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);
    checkWebElementsHtmlFile("//h1[text()='Hello, this is check!']");
    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);

    // type a content into editor and check it by preview feature
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME + "/file.html");
    projectExplorer.openItemByPath(PROJECT_NAME + "/file.html");
    editor.waitActive();
    editor.setCursorToLine(19);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ARROW_UP.toString());
    editor.typeTextIntoEditor(H2_CONTENT);
    editor.waitTextIntoEditor(H2_CONTENT);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/file.html");
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.PREVIEW);
    seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);
    checkWebElementsHtmlFile("//h2[@style='color:red' and text()='Test content']");
    seleniumWebDriver.switchTo().window(currentWindow);
    editor.setCursorToLine(19);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ARROW_UP.toString());
    editor.typeTextIntoEditor(BODY_CONTENT);
    editor.waitTextIntoEditor(BODY_CONTENT);
    seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);
    loader.waitOnClosed();
    checkWebElementsHtmlFile("//h2[@style='color:red' and text()='Test content']");
    loader.waitOnClosed();
    seleniumWebDriver.navigate().refresh();
    checkWebElementsHtmlFile("//b[text()='Content of file']");
  }

  public void checkWebElementsHtmlFile(String locator) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
  }
}
