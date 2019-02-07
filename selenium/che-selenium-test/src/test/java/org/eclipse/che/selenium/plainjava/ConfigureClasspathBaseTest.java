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
package org.eclipse.che.selenium.plainjava;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.CONFIGURE_CLASSPATH;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.ConfigureClasspath;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class ConfigureClasspathBaseTest {
  private static final String PROJECT_NAME = NameGenerator.generate("ConfigureClasspathBase_", 4);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ConfigureClasspath configureClasspath;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/java-multimodule");
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_JAVA_MULTIMODULE);
    ide.open(ws);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void checkConfigureClasspath() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME + "/my-lib");
    projectExplorer.waitItem(PROJECT_NAME + "/my-webapp");

    // check opening main form and closing it by icon
    menu.runCommand(PROJECT, CONFIGURE_CLASSPATH);
    configureClasspath.waitConfigureClasspathFormIsOpen();
    configureClasspath.closeConfigureClasspathFormByIcon();

    // check the 'Java Build Path' header
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(PROJECT, CONFIGURE_CLASSPATH);
    configureClasspath.waitConfigureClasspathFormIsOpen();
    configureClasspath.waitExpectedTextJavaBuildPathArea("Libraries");
    configureClasspath.waitExpectedTextJavaBuildPathArea("Source");
    configureClasspath.clickOnJavaBuildPathHeader();
    configureClasspath.waitExpectedTextIsNotPresentInJavaBuildPathArea("Libraries");
    configureClasspath.waitExpectedTextIsNotPresentInJavaBuildPathArea("Source");
    configureClasspath.clickOnJavaBuildPathHeader();
    configureClasspath.waitExpectedTextJavaBuildPathArea("Libraries");
    configureClasspath.waitExpectedTextJavaBuildPathArea("Source");
    configureClasspath.clickOnDoneBtnConfigureClasspath();

    // check the 'JARs and folders' area for 'my-lib' module
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/my-lib");
    projectExplorer.waitItemIsSelected(PROJECT_NAME + "/my-lib");
    menu.runCommand(PROJECT, CONFIGURE_CLASSPATH);
    configureClasspath.waitConfigureClasspathFormIsOpen();
    configureClasspath.selectSourceCategory();
    configureClasspath.waitExpectedTextJarsAndFolderArea(
        "/" + PROJECT_NAME + "/my-lib/src/main/java");
    configureClasspath.waitExpectedTextJarsAndFolderArea(
        "/" + PROJECT_NAME + "/my-lib/src/test/java");
    configureClasspath.selectLibrariesCategory();
    configureClasspath.waitExpectedTextJarsAndFolderArea(
        "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER");
    configureClasspath.waitExpectedTextJarsAndFolderArea("org.eclipse.jdt.launching.JRE_CONTAINER");
    configureClasspath.closeConfigureClasspathFormByIcon();

    // check the 'JARs and folders' area for 'my-webapp' module
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/my-webapp");
    projectExplorer.waitItemIsSelected(PROJECT_NAME + "/my-webapp");
    menu.runCommand(PROJECT, CONFIGURE_CLASSPATH);
    configureClasspath.waitConfigureClasspathFormIsOpen();
    configureClasspath.selectSourceCategory();
    configureClasspath.waitExpectedTextJarsAndFolderArea(
        "/" + PROJECT_NAME + "/my-webapp/src/main/java");
    configureClasspath.selectLibrariesCategory();
    configureClasspath.waitExpectedTextJarsAndFolderArea(
        "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER");
    configureClasspath.waitExpectedTextJarsAndFolderArea("org.eclipse.jdt.launching.JRE_CONTAINER");
    configureClasspath.closeConfigureClasspathFormByIcon();
  }
}
