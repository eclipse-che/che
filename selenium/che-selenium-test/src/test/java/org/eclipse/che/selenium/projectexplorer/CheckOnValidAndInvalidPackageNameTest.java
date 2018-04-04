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

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.NEW;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.JAVA_PACKAGE;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test tries to create packages with valid and invalid names inside source root folder and inside
 * another package.
 *
 * @author Igor Ohrimenko
 */
public class CheckOnValidAndInvalidPackageNameTest {
  private static final String PROJECT_NAME = "packageNameTest";
  private static final String PATH_TO_JAVA_FOLDER = "/src/main/java";
  private static final String ROOT_PACKAGE = "/org/eclipse/qa/examples";
  private static final String PREFIX = "inner";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
  }

  @Test(dataProvider = "validPackageNames")
  public void createValidPackageNameInJavaFolderTest(String packageName) {
    createPackageByPath(PROJECT_NAME + PATH_TO_JAVA_FOLDER, packageName);

    projectExplorer.waitVisibilityByName(packageName);
    projectExplorer.openItemByVisibleNameInExplorer(packageName);
  }

  @Test(dataProvider = "validPackageNames", priority = 1)
  public void createValidPackageNameInRootPackageTest(String packageName) {
    createPackageByPath(PROJECT_NAME + PATH_TO_JAVA_FOLDER + ROOT_PACKAGE, packageName + PREFIX);

    projectExplorer.waitVisibilityByName(packageName + PREFIX);
    projectExplorer.openItemByVisibleNameInExplorer(packageName + PREFIX);
  }

  @Test(dataProvider = "invalidPackageNames", priority = 2)
  public void createInvalidPackageNameInJavaFolderTest(String packageName) {
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + PATH_TO_JAVA_FOLDER);
    projectExplorer.clickOnItemInContextMenu(NEW);
    projectExplorer.clickOnNewContextMenuItem(JAVA_PACKAGE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(packageName);
    if (!askForValueDialog.waitErrorMessage()) {
      askForValueDialog.clickCancelBtn();
      askForValueDialog.waitFormToClose();
      fail("Expected error message not shown , user able to create package with invalid name");
    }
    askForValueDialog.clickCancelBtn();
    askForValueDialog.waitFormToClose();
  }

  @Test(dataProvider = "invalidPackageNames", priority = 3)
  public void createInvalidPackageNameInRootPackageTest(String packageName) {
    projectExplorer.openContextMenuByPathSelectedItem(
        PROJECT_NAME + PATH_TO_JAVA_FOLDER + ROOT_PACKAGE);
    projectExplorer.clickOnItemInContextMenu(NEW);
    projectExplorer.clickOnNewContextMenuItem(JAVA_PACKAGE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(packageName);
    if (!askForValueDialog.waitErrorMessage()) {
      askForValueDialog.clickCancelBtn();
      askForValueDialog.waitFormToClose();
      fail("Expected error message not shown , user able to create package with invalid name");
    }
    askForValueDialog.clickCancelBtn();
    askForValueDialog.waitFormToClose();
  }

  private void createPackageByPath(String packagePath, String packageName) {
    projectExplorer.openContextMenuByPathSelectedItem(packagePath);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(NEW);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnNewContextMenuItem(JAVA_PACKAGE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(packageName);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
  }

  @DataProvider
  private Object[][] validPackageNames() {
    return new Object[][] {
      {"com.valid"}, {"validPackage"}, {"_validPackage"}, {"PackageName"}, {"gov._1valid"}
    };
  }

  @DataProvider
  private Object[][] invalidPackageNames() {
    return new Object[][] {
      {"...packageName"},
      {"1packageName"},
      {"+packageName"},
      {" spaceOnBegin"},
      {"spaceOnEnd "},
      {"space between"},
      {"package-name"},
      {"package"},
      {"int"},
      {"boolean"}
    };
  }
}
