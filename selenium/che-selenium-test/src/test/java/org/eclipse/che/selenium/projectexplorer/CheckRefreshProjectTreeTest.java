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
package org.eclipse.che.selenium.projectexplorer;

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.REFRESH;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
public class CheckRefreshProjectTreeTest {

  private static String PROJECT_NAME = "RefreshProject";
  private static String FILE_NAME = "new_file";
  private static String FILE_CONTENT = "*** some content ***";
  private static String FILE_TEXT = "*** some text ***";
  private static String PATH_TO_FILE_1 = PROJECT_NAME + "/src/main/webapp";
  private static String PATH_TO_FILE_2 = PROJECT_NAME + "/src/main/webapp/WEB-INF";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

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
  public void checkRefreshProjectTree() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();

    // Create a file in the 'webapp' folder
    createFile(PATH_TO_FILE_1, FILE_NAME, FILE_CONTENT);
    loader.waitOnClosed();
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/src/main/webapp");
    projectExplorer.clickOnItemInContextMenu(REFRESH);
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/new_file");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/new_file");
    editor.waitTextIntoEditor(FILE_CONTENT);
    loader.waitOnClosed();

    // Create a file in the 'WEB-INF' folder
    createFile(PATH_TO_FILE_2, FILE_NAME, FILE_TEXT);
    loader.waitOnClosed();
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
    projectExplorer.clickOnItemInContextMenu(REFRESH);
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF/new_file");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/WEB-INF/new_file");
    editor.waitTextIntoEditor(FILE_TEXT);
  }

  private void createFile(String path, String fileName, String content) throws Exception {

    testProjectServiceClient.createFileInProject(testWorkspace.getId(), path, fileName, content);
  }
}
