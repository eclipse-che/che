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
package org.eclipse.che.selenium.projectexplorer.dependencies;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OpenExternalLibraryFileAfterRefreshTest {
  private static final String PROJECT_NAME = NameGenerator.generate("ExternalFileTest", 4);
  private static final String CHECKING_FILE_NAME = "Filter";
  private static final String EXPECTED_EDITOR_TEXT =
      "\n"
          + " // Failed to get sources. Instead, stub sources have been generated.\n"
          + " // Implementation of methods is unavailable.\n"
          + "package javax.servlet;\n"
          + "public interface Filter {\n"
          + "\n"
          + "    public void init(javax.servlet.FilterConfig arg0) throws javax.servlet.ServletException;\n"
          + "\n"
          + "    public void doFilter(javax.servlet.ServletRequest arg0, javax.servlet.ServletResponse arg1, javax.servlet.FilterChain arg2) throws java.io.IOException, javax.servlet.ServletException;\n"
          + "\n"
          + "    public void destroy();\n"
          + "\n"
          + "}\n";

  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private SeleniumWebDriver seleniumWebDriver;

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
  public void shouldOpenTheSameFileAfterRefreshBrowser() {
    // prepare
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    // open file from external library and check displayed text in editor
    projectExplorer.openItemByPath(PROJECT_NAME);
    openFileFromExternalLibraryAndCheckDisplayedText();

    seleniumWebDriver.navigate().refresh();

    // check that after browser refresh it is still possible to open the same file
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    openFileFromExternalLibraryAndCheckDisplayedText();
  }

  private void openFileFromExternalLibraryAndCheckDisplayedText() {
    projectExplorer.waitVisibilitySeveralItemsByName("README.md", "pom.xml", "External Libraries");

    projectExplorer.openSeveralItemsByVisibleNameInExplorer(
        "External Libraries", "servlet-api-2.5.jar", "javax.servlet", CHECKING_FILE_NAME);

    editor.waitActive();
    editor.waitTabIsPresent(CHECKING_FILE_NAME);
    editor.waitTextIntoEditor(EXPECTED_EDITOR_TEXT);
  }
}
