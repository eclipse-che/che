/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.gwt;

import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.APPLICATION_START_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.CommandsGoal.COMMON;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckSimpleGwtAppTest {
  private static final String BUILD_COMMAND = "build";
  private static final String RUN_GWT_COMMAND = "runGwt";

  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ToastLoader toastLoader;
  @Inject private Consoles consoles;

  @InjectTestWorkspace(memoryGb = 4)
  private TestWorkspace testWorkspace;

  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  private String projectName;

  @BeforeClass
  public void prepare() throws Exception {
    projectName = NameGenerator.generate("project", 4);

    URL resource = getClass().getResource("/projects/web-gwt-java-simple");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        projectName,
        ProjectTemplates.MAVEN_SPRING);
    testCommandServiceClient.createCommand(
        "cd /projects/" + projectName + " && mvn clean install -DskipTests",
        BUILD_COMMAND,
        CUSTOM,
        testWorkspace.getId());

    testCommandServiceClient.createCommand(
        "mvn clean gwt:run-codeserver -f ${current.project.path} -Dgwt.bindAddress=0.0.0.0",
        RUN_GWT_COMMAND,
        CUSTOM,
        testWorkspace.getId());
  }

  @Test
  public void checkLaunchingCodeServer() throws Exception {
    ide.open(testWorkspace);

    String expectedTextOnCodeServerPage =
        "GWT Code Server\n"
            + "Drag these two bookmarklets to your browser's bookmark bar:\n"
            + "Dev Mode On Dev Mode Off\n"
            + "Visit a web page that uses one of these modules:\n"
            + "GWT\n"
            + "Click \"Dev Mode On\" to start development mode.";

    projectExplorer.waitItem(projectName);
    toastLoader.waitAppeareanceAndClosing();
    projectExplorer.selectItem(projectName);
    projectExplorer.invokeCommandWithContextMenu(COMMON, projectName, BUILD_COMMAND);
    consoles.waitExpectedTextIntoConsole(BUILD_SUCCESS, 600);
    projectExplorer.invokeCommandWithContextMenu(COMMON, projectName, RUN_GWT_COMMAND);
    consoles.waitExpectedTextIntoConsole("The code server is ready", APPLICATION_START_TIMEOUT_SEC);

    String url =
        workspaceServiceClient.getServerByExposedPort(testWorkspace.getId(), "9876/tcp").getUrl();
    seleniumWebDriver.get(url);

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), expectedTextOnCodeServerPage));
  }
}
