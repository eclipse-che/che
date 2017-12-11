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
package org.eclipse.che.selenium.projectexplorer.dependencies;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class UpdateListOfLibraryTest {

  private static final String PROJECT_NAME = UpdateListOfLibraryTest.class.getSimpleName();
  private static final String LIB_FOLDER = "External Libraries";
  private static final Logger LOG = LoggerFactory.getLogger(UpdateListOfLibraryTest.class);
  private static final List<String> LIST_OF_LIBRARY =
      Arrays.asList(
          "rt.jar",
          "sunjce_provider.jar",
          "dnsns.jar",
          "sunpkcs11.jar",
          "sunec.jar",
          "localedata.jar",
          "zipfs.jar",
          "spring-web-3.0.5.RELEASE.jar",
          "aopalliance-1.0.jar",
          "servlet-api-2.5.jar",
          "spring-webmvc-3.0.5.RELEASE.jar",
          "spring-expression-3.0.5.RELEASE.jar",
          "spring-aop-3.0.5.RELEASE.jar",
          "spring-context-3.0.5.RELEASE.jar",
          "commons-logging-1.1.1.jar",
          "spring-context-support-3.0.5.RELEASE.jar",
          "commons-logging-1.1.1.jar",
          "spring-context-support-3.0.5.RELEASE.jar",
          "spring-core-3.0.5.RELEASE.jar",
          "spring-beans-3.0.5.RELEASE.jar",
          "spring-asm-3.0.5.RELEASE.jar",
          "junit-4.12.jar");

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Loader loader;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private TestUser defaultTestUser;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void checkUpdateLibraryAfterChangingDependencyTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME);
    consoles.closeProcessesArea();
    projectExplorer.openItemByPath(PROJECT_NAME);
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");

    mavenPluginStatusBar.waitClosingInfoPanel();
    checkLibraries();

    loader.waitOnClosed();
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
    loader.waitOnClosed();
    editor.waitActive();

    addNewDependency();

    mavenPluginStatusBar.waitClosingInfoPanel();
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.MAVEN);
    projectExplorer.clickOnItemInContextMenu(
        ProjectExplorer.PROJECT_EXPLORER_CONTEXT_MENU_MAVEN.REIMPORT);
    projectExplorer.waitLibraryIsPresent("jackson-core-2.4.3.jar");
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");

    deleteDependency();

    mavenPluginStatusBar.waitClosingInfoPanel();
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.MAVEN);
    projectExplorer.clickOnItemInContextMenu(
        ProjectExplorer.PROJECT_EXPLORER_CONTEXT_MENU_MAVEN.REIMPORT);
    projectExplorer.waitLibraryIsNotPresent("servlet-api-2.5.jar");
  }

  private void addNewDependency() {
    editor.waitActive();
    loader.waitOnClosed();
    editor.setCursorToLine(43);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.typeTextIntoEditorWithoutDelayForSaving("<dependency>");
    editor.typeTextIntoEditorWithoutDelayForSaving("\n");
    editor.setCursorToLine(45);

    editor.typeTextIntoEditorWithoutDelayForSaving("<groupId>");
    editor.typeTextIntoEditorWithoutDelayForSaving("com.fasterxml.jackson.core");
    editor.typeTextIntoEditorWithoutDelayForSaving("</groupId>");
    editor.typeTextIntoEditorWithoutDelayForSaving("\n");
    editor.setCursorToLine(46);

    editor.typeTextIntoEditorWithoutDelayForSaving("<artifactId>");
    editor.typeTextIntoEditorWithoutDelayForSaving("jackson-core");
    editor.typeTextIntoEditorWithoutDelayForSaving("</artifactId>");
    editor.typeTextIntoEditorWithoutDelayForSaving("\n");
    editor.setCursorToLine(47);

    editor.typeTextIntoEditorWithoutDelayForSaving("<version>");
    editor.typeTextIntoEditorWithoutDelayForSaving("2.4.3");
    editor.typeTextIntoEditorWithoutDelayForSaving("</version>");
    editor.typeTextIntoEditorWithoutDelayForSaving("\n");

    editor.typeTextIntoEditorWithoutDelayForSaving("</dependency>");
    editor.typeTextIntoEditor(Keys.SPACE.toString());
  }

  private void deleteDependency() {
    editor.waitActive();
    loader.waitOnClosed();
    for (int i = 27; i <= 32; i++) {
      editor.setCursorToLine(i);
      editor.selectLineAndDelete();
    }
  }

  private void checkLibraries() {
    projectExplorer.openItemByVisibleNameInExplorer(LIB_FOLDER);
    LIST_OF_LIBRARY.forEach(projectExplorer::waitLibraryIsPresent);
  }
}
