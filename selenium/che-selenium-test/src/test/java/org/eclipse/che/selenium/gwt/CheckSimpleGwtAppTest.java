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
package org.eclipse.che.selenium.gwt;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.COMMON_GOAL;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.APPLICATION_START_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.UBUNTU_JDK8;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.api.workspace.shared.dto.ServerConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.utils.WorkspaceDtoDeserializer;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceImpl;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckSimpleGwtAppTest {
  private static final String BUILD_COMMAND = "build";
  private static final String RUN_GWT_COMMAND = "runGwt";
  private static final String GWT_CODESERVER_NAME = "gwt-codeserver";

  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ToastLoader toastLoader;
  @Inject private Consoles consoles;

  private TestWorkspace testWorkspace;

  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private WorkspaceDtoDeserializer workspaceDtoDeserializer;
  @Inject private DefaultTestUser testUser;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  private String projectName;

  @BeforeClass
  public void prepare() throws Exception {
    projectName = NameGenerator.generate("project", 4);

    WorkspaceConfigDto workspace =
        workspaceDtoDeserializer.deserializeWorkspaceTemplate(UBUNTU_JDK8);

    workspace
        .getEnvironments()
        .get("replaced_name")
        .getMachines()
        .get("dev-machine")
        .getServers()
        .put(
            GWT_CODESERVER_NAME,
            newDto(ServerConfigDto.class).withProtocol("http").withPort("9876"));

    testWorkspace =
        new TestWorkspaceImpl(
            NameGenerator.generate("check-gwt-test", 4),
            testUser,
            4,
            true,
            workspace,
            workspaceServiceClient);

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
    projectExplorer.waitAndSelectItem(projectName);
    projectExplorer.invokeCommandWithContextMenu(COMMON_GOAL, projectName, BUILD_COMMAND);
    consoles.waitExpectedTextIntoConsole(BUILD_SUCCESS, 600);
    projectExplorer.invokeCommandWithContextMenu(COMMON_GOAL, projectName, RUN_GWT_COMMAND);
    consoles.waitExpectedTextIntoConsole("The code server is ready", APPLICATION_START_TIMEOUT_SEC);

    String url =
        workspaceServiceClient
            .getServerFromDevMachineBySymbolicName(testWorkspace.getId(), GWT_CODESERVER_NAME)
            .getUrl()
            .replace("tcp", "http");

    // the timeout needs for ocp platform
    WaitUtils.sleepQuietly(10);
    seleniumWebDriver.get(url);

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), expectedTextOnCodeServerPage));
  }

  @AfterClass
  public void tearDown() {
    testWorkspace.delete();
  }
}
