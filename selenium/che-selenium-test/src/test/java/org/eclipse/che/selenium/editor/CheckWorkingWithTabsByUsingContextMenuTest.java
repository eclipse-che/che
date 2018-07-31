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
package org.eclipse.che.selenium.editor;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.CodenvyEditor.TabActionLocator;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckWorkingWithTabsByUsingContextMenuTest {

  private static final String PROJECT_NAME =
      NameGenerator.generate(CheckWorkingWithTabsByUsingContextMenuTest.class.getSimpleName(), 4);
  private static final String PATH_TO_CSS = PROJECT_NAME + "/src/main/webapp/WEB-INF/cssFile.css";
  private static final String PATH_TO_XML = PROJECT_NAME + "/src/main/webapp/WEB-INF/web.xml";
  private static final String PATH_TO_HTML =
      PROJECT_NAME + "/src/main/webapp/WEB-INF/htmlFile.html";
  private static final String PATH_TO_JAVA_CLASS =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private static final String NAME_TO_CSS = "cssFile.css";
  private static final String NAME_TO_XML = "web.xml";
  private static final String NAME_TO_HTML = "htmlFile.html";
  private static final String NAME_TO_JAVA_CLASS = "AppController";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkWorkingWithTabsByUsingContextMenu() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();

    // close all tab by using context menu
    openTabsInEditor();
    editor.openAndWaitContextMenuForTabByName(NAME_TO_JAVA_CLASS);
    editor.runActionForTabFromContextMenu(TabActionLocator.CLOSE_ALL);
    editor.waitTabIsNotPresent(NAME_TO_JAVA_CLASS);
    editor.waitTabIsNotPresent(NAME_TO_CSS);
    editor.waitTabIsNotPresent(NAME_TO_XML);
    editor.waitTabIsNotPresent(NAME_TO_HTML);

    // close one tab by using context menu
    openTabsInEditor();
    editor.openAndWaitContextMenuForTabByName(NAME_TO_JAVA_CLASS);
    editor.runActionForTabFromContextMenu(TabActionLocator.CLOSE);
    editor.waitTabIsNotPresent(NAME_TO_JAVA_CLASS);
    editor.waitTabIsPresent(NAME_TO_CSS);
    editor.waitTabIsPresent(NAME_TO_XML);
    editor.waitTabIsPresent(NAME_TO_HTML);
    editor.closeAllTabs();

    // close other tabs by using context menu
    openTabsInEditor();
    editor.openAndWaitContextMenuForTabByName(NAME_TO_JAVA_CLASS);
    editor.runActionForTabFromContextMenu(TabActionLocator.CLOSE_OTHER);
    editor.waitTabIsPresent(NAME_TO_JAVA_CLASS);
    editor.waitTabIsNotPresent(NAME_TO_CSS);
    editor.waitTabIsNotPresent(NAME_TO_XML);
    editor.waitTabIsNotPresent(NAME_TO_HTML);
    editor.closeAllTabs();

    // close other tabs by using context menu
    openTabsInEditor();
    editor.openAndWaitContextMenuForTabByName(NAME_TO_JAVA_CLASS);
    editor.runActionForTabFromContextMenu(TabActionLocator.CLOSE_OTHER);
    editor.waitTabIsPresent(NAME_TO_JAVA_CLASS);
    editor.waitTabIsNotPresent(NAME_TO_CSS);
    editor.waitTabIsNotPresent(NAME_TO_XML);
    editor.waitTabIsNotPresent(NAME_TO_HTML);
    editor.closeAllTabs();

    // pin and close all tabs without pinned by using context menu
    openTabsInEditor();
    editor.openAndWaitContextMenuForTabByName(NAME_TO_JAVA_CLASS);
    editor.runActionForTabFromContextMenu(TabActionLocator.PIN_UNPIN_TAB);
    editor.openAndWaitContextMenuForTabByName(NAME_TO_CSS);
    editor.runActionForTabFromContextMenu(TabActionLocator.PIN_UNPIN_TAB);
    editor.openAndWaitContextMenuForTabByName(NAME_TO_JAVA_CLASS);
    editor.runActionForTabFromContextMenu(TabActionLocator.CLOSE_ALL_BUT_PINNED);
    editor.waitTabIsPresent(NAME_TO_JAVA_CLASS);
    editor.waitTabIsPresent(NAME_TO_CSS);
    editor.waitTabIsNotPresent(NAME_TO_XML);
    editor.waitTabIsNotPresent(NAME_TO_HTML);
    editor.closeAllTabs();

    // reopen closed tab by using context menu
    openTabsInEditor();
    editor.openAndWaitContextMenuForTabByName(NAME_TO_JAVA_CLASS);
    editor.runActionForTabFromContextMenu(TabActionLocator.CLOSE);
    editor.waitTabIsNotPresent(NAME_TO_JAVA_CLASS);
    editor.waitTabIsPresent(NAME_TO_CSS);
    editor.waitTabIsPresent(NAME_TO_XML);
    editor.waitTabIsPresent(NAME_TO_HTML);
    editor.selectTabByName(NAME_TO_CSS);
    editor.openAndWaitContextMenuForTabByName(NAME_TO_CSS);
    editor.runActionForTabFromContextMenu(TabActionLocator.REOPEN_CLOSED_TAB);
    editor.waitTabIsPresent(NAME_TO_JAVA_CLASS);
    editor.waitTabIsPresent(NAME_TO_CSS);
    editor.waitTabIsPresent(NAME_TO_XML);
    editor.waitTabIsPresent(NAME_TO_HTML);
    editor.closeAllTabs();
  }

  private void openTabsInEditor() {
    projectExplorer.waitItem(PATH_TO_JAVA_CLASS);
    projectExplorer.openItemByPath(PATH_TO_JAVA_CLASS);
    projectExplorer.waitItem(PATH_TO_CSS);
    projectExplorer.openItemByPath(PATH_TO_CSS);
    projectExplorer.waitItem(PATH_TO_XML);
    projectExplorer.openItemByPath(PATH_TO_XML);
    projectExplorer.waitItem(PATH_TO_HTML);
    projectExplorer.openItemByPath(PATH_TO_HTML);
    loader.waitOnClosed();
  }
}
