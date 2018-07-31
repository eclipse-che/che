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
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class JustCreatedFileNotExistIntoEditorAfterDeleteTest {

  private static final String PROJECT_NAME = "DeletionPrj4";
  private static final String PATH_TO_FILE = PROJECT_NAME + "/src/main/webapp/fileForeTest.jsp";
  private static final String DELETE_TEXT = "Delete file \"fileForeTest.jsp\"?";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private AskDialog askDialog;
  @Inject private Menu menu;
  @Inject private Loader loader;
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
  public void deleteFileTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitAndSelectItemByName("webapp");
    loader.waitOnClosed();

    // create new file
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.FILE);

    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("fileForeTest.jsp");
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    projectExplorer.openItemByVisibleNameInExplorer("fileForeTest.jsp");
    loader.waitOnClosed();
    editor.waitTabIsPresent("fileForeTest.jsp");

    // delete new file
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/webapp/fileForeTest.jsp");
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);

    askDialog.waitFormToOpen();
    askDialog.containsText(DELETE_TEXT);
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(PATH_TO_FILE);

    editor.waitTabIsNotPresent("fileForeTest.jsp");
  }
}
