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

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.WARNING_MARKER;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.BrowserLogsUtil;
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
public class Eclipse0115Test {

  private static final String PATH_TO_PACKAGE_PREFIX = "/src/main/java/org/eclipse/qa/examples/";
  private static final Logger LOG = LoggerFactory.getLogger(Eclipse0115Test.class);
  private static final String PROJECT_NAME =
      NameGenerator.generate(Eclipse0115Test.class.getSimpleName(), 4);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private BrowserLogsUtil browserLogsUtil;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/resolveTests_1_5_t0115");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    ide.open(ws);
  }

  @Test
  public void test0115() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PROJECT_NAME + PATH_TO_PACKAGE_PREFIX + "X.java");
    editor.waitActive();
    waitMarkerInPosition();
    editor.goToCursorPositionVisible(32, 14);
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitSpecifiedValueForLineAndChar(35, 24);
  }

  private void waitMarkerInPosition() throws Exception {
    try {
      editor.waitMarkerInPosition(WARNING_MARKER, 14);
    } catch (TimeoutException ex) {
      logExternalLibraries();
      logProjectTypeChecking();
      logProjectLanguageChecking();
      browserLogsUtil.storeLogs();

      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7161", ex);
    }
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
