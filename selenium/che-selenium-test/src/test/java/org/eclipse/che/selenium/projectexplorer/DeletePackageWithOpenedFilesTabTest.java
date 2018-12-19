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

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andery Chizhikov
 */
public class DeletePackageWithOpenedFilesTabTest {
  private static final String PROJECT_NAME =
      DeletePackageWithOpenedFilesTabTest.class.getSimpleName();
  private static final String PATH_TO_WEB_APP = PROJECT_NAME + "/src/main/webapp";
  private static final String PATH_TO_PACKAGE1 =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";
  private static final String PATH_TO_PACKAGE2 = PROJECT_NAME + "/src/main/webapp";
  private static final String PATH_TO_PACKAGE3 = PROJECT_NAME + "/src/main/java/com/example";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private AskDialog askDialog;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void deletePackageTest() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    // active tab is first tab

    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActive();
    openFile("LessFile.less");
    openFile("sqlFile.sql");
    openFile("another");
    editor.waitActive();
    editor.selectTabByName("AppController");

    projectExplorer.waitAndSelectItem(PATH_TO_PACKAGE1);
    deletePackage();
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE1);
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE1 + "/AppController.java");
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE1 + "/LessFile.less");
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE1 + "/sqlFile.sql");
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE1 + "/another");

    // active tab is middle tab
    projectExplorer.openItemByPath(PATH_TO_WEB_APP + "/index.jsp");
    editor.waitActive();
    openFile("spring-servlet.xml");
    openFile("web.xml");
    openFile("guess_num.jsp");
    openFile("htmlFile.html");
    editor.waitActive();
    editor.selectTabByName("web.xml");

    projectExplorer.waitAndSelectItem(PATH_TO_PACKAGE2);
    deletePackage();
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE2);
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE2 + "/index.jsp");
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE2 + "/spring-servlet.xml");
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE2 + "/web.xml");
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE2 + "/guess_num.jsp");
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE2 + "/htmlFile.html");

    // active tab is last tab
    openJavaFile(PATH_TO_PACKAGE3 + "/Test1.java", "Test1");
    openJavaFile(PATH_TO_PACKAGE3 + "/Test2.java", "Test2");
    openJavaFile(PATH_TO_PACKAGE3 + "/Test3.java", "Test3");
    openJavaFile(PATH_TO_PACKAGE3 + "/Test4.java", "Test4");

    projectExplorer.waitAndSelectItem(PATH_TO_PACKAGE3);
    deletePackage();
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE3);
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE3 + "/Test1.java");
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE3 + "/Test2.java");
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE3 + "/Test3.java");
    projectExplorer.waitRemoveItemsByPath(PATH_TO_PACKAGE3 + "/Test4.java");
  }

  /** delete package for menu File */
  private void deletePackage() {
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
  }

  private void openFile(String fileName) {
    projectExplorer.openItemByVisibleNameInExplorer(fileName);
    editor.waitTabIsPresent(fileName);
    editor.waitActive();
  }

  private void openJavaFile(String path, String fileName) {
    projectExplorer.openItemByPath(path);
    try {
      editor.waitTabIsPresent(fileName);
    } catch (TimeoutException ex) {
      projectExplorer.openItemByPath(path);
      editor.waitTabIsPresent(fileName);
    }

    editor.waitActive();
  }
}
