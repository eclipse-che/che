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
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class CreateNewPackagesWithHelpCreationJavaClassTest {
  private static final String PROJECT_NAME =
      CreateNewPackagesWithHelpCreationJavaClassTest.class.getSimpleName();
  private static final String NEW_PACKAGE_NAME1 = "tu";
  private static final String NEW_PACKAGE_NAME2 = "test.ua";
  private static final String JAVA1 = "TestClass1";
  private static final String JAVA2 = "TestClass2";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private HttpJsonRequestFactory httpJsonRequestFactory;
  @Inject private TestApiEndpointUrlProvider testApiEndpointUrlProvider;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
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
  public void createNewPackageFromContextMenuTest() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.expandPathInProjectExplorer(PROJECT_NAME + "/src/main/java");
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java");
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVA_CLASS);
    askForValueDialog.createJavaFileByNameAndType(
        NEW_PACKAGE_NAME1 + "." + JAVA1, AskForValueDialog.JavaFiles.CLASS);

    editor.waitActive();
    editor.waitTabIsPresent("TestClass1");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/tu");

    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/tu/TestClass1.java");

    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java");
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVA_CLASS);

    askForValueDialog.createJavaFileByNameAndType(
        NEW_PACKAGE_NAME2 + "." + JAVA2, AskForValueDialog.JavaFiles.CLASS);

    editor.waitActive();
    editor.waitTabIsPresent("TestClass2");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test/ua");
    projectExplorer.waitVisibilityByName("TestClass2.java");
  }
}
