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
package org.eclipse.che.selenium.editor;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckWorkingWithTabByUsingTabListTest {

  private static final String PROJECT_NAME =
      NameGenerator.generate(CheckWorkingWithTabByUsingTabListTest.class.getSimpleName(), 4);
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
  public void checkWorkingWithTabByUsingTabList() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    openTabsInEditor();

    editor.openTabList();
    editor.waitTabIsPresentInTabList(NAME_TO_JAVA_CLASS);
    editor.waitTabIsPresentInTabList(NAME_TO_CSS);
    editor.waitTabIsPresentInTabList(NAME_TO_HTML);
    editor.waitTabIsPresentInTabList(NAME_TO_XML);
    editor.waitTabIsPresent(NAME_TO_HTML);
    editor.clickOnTabInTabList(NAME_TO_HTML);
    editor.openContextMenuForTabByName(NAME_TO_HTML);
    editor.runActionForTabFromContextMenu(CodenvyEditor.TabAction.CLOSE);
    editor.openTabList();
    editor.waitTabIsNotPresentInTabList(NAME_TO_HTML);
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
