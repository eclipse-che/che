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
package org.eclipse.che.selenium.workspaces;

import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey chizhikov */
public class ProjectStateAfterRefreshTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 5);

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource =
        ProjectStateAfterRefreshTest.this.getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkRestoreStateOfProjectAfterRefreshTest() {
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();

    openFilesInEditor();
    checkFilesAreOpened();

    seleniumWebDriver.navigate().refresh();
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(PROJECT_NAME);

    try {
      checkFilesAreOpened();
    } catch (TimeoutException ex) {
      // Remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7551");
    }

    editor.closeAllTabsByContextMenu();
  }

  @Test
  public void checkRestoreStateOfProjectIfPomXmlFileOpened() {
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();

    projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp");
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
    editor.waitActive();

    seleniumWebDriver.navigate().refresh();
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(PROJECT_NAME);

    try {
      editor.waitTabIsPresent("qa-spring-sample");
    } catch (TimeoutException ex) {
      // Remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7551");
    }
    projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp");

    editor.closeAllTabsByContextMenu();
  }

  private void checkFilesAreOpened() {
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/index.jsp");
    projectExplorer.waitItem(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitTabIsPresent("index.jsp");
    editor.waitTabIsPresent("AppController");
    editor.waitTabIsPresent("guess_num.jsp");
    editor.waitTabIsPresent("web.xml");
    editor.waitTabIsPresent("qa-spring-sample");
  }

  private void openFilesInEditor() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/index.jsp");
    editor.waitActive();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActive();
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
    editor.waitActive();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp" + "/guess_num.jsp");
    editor.waitActive();
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/WEB-INF" + "/web.xml");
    editor.waitActive();
  }
}
