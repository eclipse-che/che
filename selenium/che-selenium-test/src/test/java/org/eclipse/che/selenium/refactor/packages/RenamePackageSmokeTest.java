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
package org.eclipse.che.selenium.refactor.packages;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 06.12.15 */
public class RenamePackageSmokeTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate("CheckRenamePackageProject-", 4);
  private static final String TEST0_P1_OUT =
      "package test0.p1;\n"
          + "/**\n"
          + " * This is in test0.r.\n"
          + " * @see test0.p1\n"
          + " * @see test0.p1.A\n"
          + " * @see test0.p1.A#A()\n"
          + " */\n"
          + "public class A {\n"
          + "}\n";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/rename-package");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void checkTest0() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/test0/r/A.java");
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(TEST0_P1_OUT);
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/test0/r");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);

    refactor.waitRenamePackageFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    loader.waitOnClosed();
    refactor.typeAndWaitNewName("test0.p1");
    loader.waitOnClosed();
    refactor.clickOkButtonRefactorForm();
    refactor.waitRenamePackageFormIsClosed();
    projectExplorer.waitItemInvisibility(PROJECT_NAME + "/src/main/java/test0/r/A.java");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/test0/p1/A.java");
    editor.waitTextIntoEditor(TEST0_P1_OUT);
    editor.closeFileByNameWithSaving("A");
  }
}
