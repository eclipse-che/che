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
package org.eclipse.che.selenium.opendeclaration;

import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Maxim Musienko
 * @author Aleksandr Shmaraev
 */
public class Eclipse0121Test {

  private static final Logger LOG = LoggerFactory.getLogger(Eclipse0121Test.class);
  private static final String PROJECT_NAME =
      NameGenerator.generate(Eclipse0121Test.class.getSimpleName(), 4);
  private static final String PATH_FOR_EXPAND =
      PROJECT_NAME + "/src/main/java/org.eclipse.qa.examples";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/resolveTests_1_5_t0121");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    ide.open(ws);
  }

  @Test
  public void test0121() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(PATH_FOR_EXPAND, "Test.java");
    editor.waitActive();
    editor.goToCursorPositionVisible(15, 43);
    editor.typeTextIntoEditor(Keys.F4.toString());

    try {
      editor.waitTabIsPresent("Collections");
    } catch (TimeoutException ex) {
      logExternalLibraries();
      logProjectTypeChecking();
      logProjectLanguageChecking();

      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7161", ex);
    }

    editor.waitSpecifiedValueForLineAndChar(14, 35);
  }

  private void logExternalLibraries() throws Exception {
    testProjectServiceClient
        .getExternalLibraries(ws.getId(), PROJECT_NAME)
        .forEach(library -> LOG.info("project external library:  {}", library));
  }

  private void logProjectTypeChecking() throws Exception {
    LOG.info(
        "Project type of the {} project is \"maven\" - {}",
        PROJECT_NAME,
        testProjectServiceClient.checkProjectType(ws.getId(), PROJECT_NAME, "maven"));
  }

  private void logProjectLanguageChecking() throws Exception {
    LOG.info(
        "Project language of the {} project is \"java\" - {}",
        PROJECT_NAME,
        testProjectServiceClient.checkProjectLanguage(ws.getId(), PROJECT_NAME, "java"));
  }
}
