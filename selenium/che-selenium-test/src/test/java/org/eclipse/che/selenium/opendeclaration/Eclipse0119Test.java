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
package org.eclipse.che.selenium.opendeclaration;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Maxim Musienko
 * @author Aleksandr Shmaraev
 */
public class Eclipse0119Test {

  private static final String PATH_TO_PACKAGE_PREFIX = "/src/main/java/org/eclipse/qa/examples/";
  private static final String PROJECT_NAME =
      NameGenerator.generate(Eclipse0119Test.class.getSimpleName(), 4);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/resolveTests_1_5_t0119");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    ide.open(ws);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void test0119() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PROJECT_NAME + PATH_TO_PACKAGE_PREFIX + "Test.java");
    editor.waitActive();
    editor.goToCursorPositionVisible(16, 28);
    editor.typeTextIntoEditor(Keys.F4.toString());
    editor.waitTabIsPresent("Collections");
    editor.waitSpecifiedValueForLineAndChar(15, 44);
  }
}
