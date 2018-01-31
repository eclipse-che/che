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

import static org.testng.Assert.fail;

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
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.TimeoutException;
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
  }

  @Test
  public void createNewPackageFromContextMenuTest() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.selectItem(PROJECT_NAME + "/src/main/java");
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

    projectExplorer.selectItem(PROJECT_NAME + "/src/main/java");
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVA_CLASS);

    askForValueDialog.createJavaFileByNameAndType(
        NEW_PACKAGE_NAME2 + "." + JAVA2, AskForValueDialog.JavaFiles.CLASS);

    editor.waitActive();
    editor.waitTabIsPresent("TestClass2");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test/ua");

    try {
      projectExplorer.waitItemInVisibleArea("TestClass2.java");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8122");
    }
  }
}
