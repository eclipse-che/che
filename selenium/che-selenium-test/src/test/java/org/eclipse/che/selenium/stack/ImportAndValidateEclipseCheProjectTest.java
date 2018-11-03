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
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ECLIPSE_CHE;
import static org.openqa.selenium.Keys.DELETE;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.InformationDialog;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = UNDER_REPAIR)
public class ImportAndValidateEclipseCheProjectTest {

  private static final Logger LOG =
      LoggerFactory.getLogger(ImportAndValidateEclipseCheProjectTest.class);
  private static final String WORKSPACE_NAME = generate("EclipseCheWs", 4);
  private static final String PROJECT_NAME = "eclipse-che";
  private static final String PACKAGE_NAME = "org.eclipse.che.selenium.pageobject";
  private static final String ECLIPSE_CHE_PROJECT_URL = "https://github.com/eclipse/che.git";
  private static final String PATH_TO_JAVA_FILE =
      PROJECT_NAME
          + "/selenium/che-selenium-test/src/main/java/org/eclipse/che/selenium/pageobject/CodenvyEditor.java";
  private static final String PATH_TO_POM_FILE = PROJECT_NAME + "/dashboard/pom.xml";
  private static final String PATH_TO_TS_FILE = PROJECT_NAME + "/dashboard/src/app/index.module.ts";

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private Loader loader;
  @Inject private Consoles consoles;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Wizard projectWizard;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private InformationDialog informationDialog;
  @Inject private CodenvyEditor editor;
  @Inject private Dashboard dashboard;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void prepare() {
    dashboard.open();

    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithoutProject(ECLIPSE_CHE, WORKSPACE_NAME);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSStartedMessage();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkImportAndResolveDependenciesEclipseCheProject() {
    final int timeoutToOpenInfoPanelInSec = 500;
    final int timeoutToClosingInfoPanelInSec = 1200;

    // import the eclipse-che project
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    importProject.waitAndTypeImporterAsGitInfo(ECLIPSE_CHE_PROJECT_URL, PROJECT_NAME);

    // config the project
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(MAVEN);
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    loader.waitOnClosed();

    // waits on project resolving message
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);

    // waits while the 'Import Maven project(s)' info bar is closed
    mavenPluginStatusBar.waitClosingInfoPanel(timeoutToClosingInfoPanelInSec);

    // expand the project
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    loader.waitOnClosed();

    // waits the text in the progress info bar
    loader.waitOnClosed();
    mavenPluginStatusBar.waitExpectedTextInInfoPanel(
        "Building workspace", timeoutToOpenInfoPanelInSec);

    // should close the progress monitor form by Esc
    mavenPluginStatusBar.clickOnInfoPanel();
    mavenPluginStatusBar.waitProgressMonitorFormToOpen();
    mavenPluginStatusBar.closeProgressMonitorFormByKeys(ESCAPE.toString());

    // then open it again
    mavenPluginStatusBar.clickOnInfoPanel();
    mavenPluginStatusBar.waitProgressMonitorFormToOpen();

    // wait while progress info is closed
    mavenPluginStatusBar.waitClosingInfoPanel(timeoutToClosingInfoPanelInSec);
    mavenPluginStatusBar.waitProgressMonitorFormToClose();

    // then open files
    // open a java file
    quickRevealToItemWithJavaScriptAndOpenFile(PATH_TO_JAVA_FILE);
    editor.waitActive();

    // checks packages in project Explorer
    try {
      projectExplorer.waitVisibilityByName(PACKAGE_NAME);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/11537");
    }

    // open a xml file
    quickRevealToItemWithJavaScriptAndOpenFile(PATH_TO_POM_FILE);
    editor.waitActive();

    // open a ts file
    quickRevealToItemWithJavaScriptAndOpenFile(PATH_TO_TS_FILE);
    editor.waitActive();

    // wait the project and the files
    projectExplorer.waitItem(PROJECT_NAME);
    editor.waitTabIsPresent("CodenvyEditor");
    editor.waitTabIsPresent("che-dashboard-war");
    editor.waitTabIsPresent("index.module.ts");
  }

  @Test(priority = 1)
  public void checkErrorMarkersInEditor() {
    // check an error marker in the pom.xml file
    projectExplorer.openItemByPath(PATH_TO_POM_FILE);
    editor.waitActive();
    editor.waitAllMarkersInvisibility(ERROR);
    editor.typeTextIntoEditor("q");
    editor.goToPosition(1, 1);
    editor.waitMarkerInPosition(ERROR, 1);
    editor.typeTextIntoEditor(DELETE.toString());
    editor.waitMarkerInvisibility(ERROR, 1);

    // check error marker in the java file
    projectExplorer.openItemByPath(PATH_TO_JAVA_FILE);
    editor.waitActive();
    editor.waitAllMarkersInvisibility(ERROR);
    editor.setCursorToLine(12);
    editor.typeTextIntoEditor("q");
    editor.goToPosition(12, 1);
    editor.waitMarkerInPosition(ERROR, 12);
    editor.typeTextIntoEditor(DELETE.toString());
    editor.waitMarkerInvisibility(ERROR, 12);

    // check error marker in the ts file
    projectExplorer.openItemByPath(PATH_TO_TS_FILE);
    editor.waitActive();
    editor.typeTextIntoEditor("q");
    editor.waitMarkerInPosition(ERROR, 1);
  }

  private void quickRevealToItemWithJavaScriptAndOpenFile(String pathToItem) {
    projectExplorer.quickRevealToItemWithJavaScript(pathToItem);
    projectExplorer.openItemByPath(pathToItem);
  }
}
