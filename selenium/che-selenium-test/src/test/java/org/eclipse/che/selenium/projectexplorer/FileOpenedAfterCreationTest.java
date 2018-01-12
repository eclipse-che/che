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
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class FileOpenedAfterCreationTest {

  private static final String PROJECT_NAME = "TestFileOpenedAfterCreation";
  private static final String EXPECTED_TEXT_1 =
      "package org.eclipse.qa.examples;\n" + "\n" + "public class TestClass {\n" + "}\n";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private Menu menu;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
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
  public void fileOpenedSuccessTest() throws Exception {
    // open .html file, get text from there and compare with expected text
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    notificationsPopupPanel.waitProgressPopupPanelClose();
    loader.waitOnClosed();
    projectExplorer.selectVisibleItem("webapp");
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.HTML_FILE);
    loader.waitOnClosed();
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("index");
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();

    // open .java file, get text from there and compare with expected text
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    loader.waitOnClosed();
    projectExplorer.selectItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples");
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVA_CLASS);
    loader.waitOnClosed();
    askForValueDialog.typeTextInFieldName("TestClass");
    askForValueDialog.clickOkBtnNewJavaClass();
    editor.waitActiveTabFileName("TestClass");
    editor.waitActive();
    loader.waitOnClosed();
    editor.waitTextIntoEditor(EXPECTED_TEXT_1);
  }
}
