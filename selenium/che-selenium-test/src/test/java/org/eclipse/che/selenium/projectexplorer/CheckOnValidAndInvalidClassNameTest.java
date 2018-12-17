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

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.NEW;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.JAVA_CLASS;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test tries to create classes with valid and invalid names inside source root folder and inside
 * another package.
 *
 * @author Ihor Okhrimenko
 */
public class CheckOnValidAndInvalidClassNameTest {
  private static final String PROJECT_NAME = generate("classNameTest", 4);
  private static final String PATH_TO_JAVA_FOLDER = PROJECT_NAME + "/src/main/java";
  private static final String ROOT_PACKAGE = "/org/eclipse/qa/examples";
  private static final String TYPE = ".java";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);

    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
  }

  @Test(dataProvider = "validClassNames")
  public void shouldCreateClassWithValidNameInJavaFolder(String className) {
    createJavaClassByPath(PATH_TO_JAVA_FOLDER, className);

    projectExplorer.waitItem(PATH_TO_JAVA_FOLDER + "/" + className + TYPE, ELEMENT_TIMEOUT_SEC);
  }

  @Test(priority = 1, dataProvider = "validClassNames")
  public void shouldCreateClassWithValidNameInRootPackage(String className) {
    createJavaClassByPath(PATH_TO_JAVA_FOLDER + ROOT_PACKAGE, className);

    projectExplorer.waitItem(
        PATH_TO_JAVA_FOLDER + ROOT_PACKAGE + "/" + className + TYPE, ELEMENT_TIMEOUT_SEC);
  }

  @Test(priority = 2, dataProvider = "invalidClassNames")
  public void shouldNotCreateClassWithInvalidNameInJavaFolder(String className) {
    tryToCreateJavaClassWithNotValidNameByPath(PATH_TO_JAVA_FOLDER, className);
  }

  @Test(priority = 3, dataProvider = "invalidClassNames")
  public void shouldNotCreateClassWithInvalidNameInRootPackage(String className) {
    tryToCreateJavaClassWithNotValidNameByPath(PATH_TO_JAVA_FOLDER + ROOT_PACKAGE, className);
  }

  private void createJavaClassByPath(String classPath, String className) {
    projectExplorer.openContextMenuByPathSelectedItem(classPath);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(NEW);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnNewContextMenuItem(JAVA_CLASS);
    askForValueDialog.waitNewJavaClassOpen();
    askForValueDialog.typeTextInFieldName(className);
    askForValueDialog.clickOkBtnNewJavaClass();
    askForValueDialog.waitNewJavaClassClose();
  }

  private void tryToCreateJavaClassWithNotValidNameByPath(String elementPath, String className) {
    projectExplorer.openContextMenuByPathSelectedItem(elementPath);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(NEW);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnNewContextMenuItem(JAVA_CLASS);
    askForValueDialog.waitNewJavaClassOpen();
    askForValueDialog.typeTextInFieldName(className);
    askForValueDialog.waitErrorMessageInJavaClass();
    askForValueDialog.clickCancelButtonJava();
    askForValueDialog.waitNewJavaClassClose();
  }

  @DataProvider(name = "validClassNames")
  private Object[][] validNames() {
    return new Object[][] {{"Name"}, {"ClassName"}, {"ClassName1"}};
  }

  @DataProvider(name = "invalidClassNames")
  private Object[][] invalidNames() {
    return new Object[][] {
      {"1234"},
      {"###secondInvalid"},
      {"123NotValid"},
      {"@#$%"},
      {"invalidClass@#"},
      {"boolean"},
      {"private"},
      {"space between"},
      {"Class+Name"}
    };
  }
}
