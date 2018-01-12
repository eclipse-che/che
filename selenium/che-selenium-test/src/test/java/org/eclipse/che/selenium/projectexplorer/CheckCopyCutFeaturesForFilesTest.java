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

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckCopyCutFeaturesForFilesTest {
  private static final String PROJECT_NAME = CheckCopyCutFeaturesForFilesTest.class.getSimpleName();
  private static final String PATH_TO_JSP_FOLDER = PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp";
  private static final String PATH_TO_XML = PROJECT_NAME + "/src/main/webapp/WEB-INF/web.xml";
  private static final String PATH_TO_CSS = PROJECT_NAME + "/src/main/webapp/WEB-INF/cssFile.css";
  private static final String PATH_TO_HTML =
      PROJECT_NAME + "/src/main/webapp/WEB-INF/htmlFile.html";
  private static final String PATH_TO_XML_AFTER_MOVING =
      PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp/web.xml";
  private static final String PATH_TO_CSS_AFTER_MOVING =
      PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp/cssFile.css";
  private static final String PATH_TO_HTML_AFTER_MOVING =
      PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp/htmlFile.html";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void checkCopyPutFeaturesForFilesTest() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();

    moveFile(PATH_TO_XML, PATH_TO_JSP_FOLDER);
    moveFile(PATH_TO_CSS, PATH_TO_JSP_FOLDER);
    moveFile(PATH_TO_HTML, PATH_TO_JSP_FOLDER);

    projectExplorer.waitItemIsDisappeared(PATH_TO_XML);
    projectExplorer.waitItemIsDisappeared(PATH_TO_CSS);
    projectExplorer.waitItemIsDisappeared(PATH_TO_HTML);

    projectExplorer.waitItem(PATH_TO_XML_AFTER_MOVING);
    projectExplorer.waitItem(PATH_TO_CSS_AFTER_MOVING);
    projectExplorer.waitItem(PATH_TO_HTML_AFTER_MOVING);
  }

  private void moveFile(String filePath, String folderPath) {
    projectExplorer.selectItem(filePath);
    projectExplorer.openContextMenuByPathSelectedItem(filePath);
    projectExplorer.clickOnNewContextMenuItem(TestProjectExplorerContextMenuConstants.CUT);
    projectExplorer.selectItem(folderPath);
    projectExplorer.openContextMenuByPathSelectedItem(folderPath);
    projectExplorer.clickOnNewContextMenuItem(TestProjectExplorerContextMenuConstants.PASTE);
    loader.waitOnClosed();
  }
}
