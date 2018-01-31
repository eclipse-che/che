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
package org.eclipse.che.selenium.mavenplugin;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class GenerateEffectivePomTest {
  private static final String PROJECT_NAME = GenerateEffectivePomTest.class.getSimpleName();
  private static final String JAR_MODULE_PATH = PROJECT_NAME + "/my-lib";
  private static final String WAR_MODULE_PATH = PROJECT_NAME + "/my-webapp";

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/java-multimodule");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
  }

  @Test(
      description =
          "add additional try/catch block for workaround problem in https://github.com/eclipse/che/issues/2877")
  public void generateEffectivePomTest() throws Exception {
    ide.open(workspace);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
    URL mainEffectivePomPath = GenerateEffectivePomTest.class.getResource("main");
    URL jarModuleEffectivePomPath = GenerateEffectivePomTest.class.getResource("jar-module");
    URL warModuleEffectivePomPath = GenerateEffectivePomTest.class.getResource("war-module");

    List<String> expectedMainEffectivePom =
        Files.readAllLines(Paths.get(mainEffectivePomPath.toURI()), Charset.forName("UTF-8"));
    List<String> expectedJarEffectivePom =
        Files.readAllLines(Paths.get(jarModuleEffectivePomPath.toURI()), Charset.forName("UTF-8"));
    List<String> expectedWarEffectivePom =
        Files.readAllLines(Paths.get(warModuleEffectivePomPath.toURI()), Charset.forName("UTF-8"));

    projectExplorer.waitItem(PROJECT_NAME);
    loader.waitOnClosed();
    Assert.assertTrue(
        checkCurrentEffectivePom(
            PROJECT_NAME, expectedMainEffectivePom, "qa-multimodule [effective pom]"),
        "Check Effective Pom for multi-module project");
    editor.closeAllTabsByContextMenu();
    Assert.assertTrue(
        checkCurrentEffectivePom(
            JAR_MODULE_PATH, expectedJarEffectivePom, "my-lib [effective pom]"),
        "Check Effective Pom for jar maven module");
    editor.closeAllTabsByContextMenu();
    Assert.assertTrue(
        checkCurrentEffectivePom(
            WAR_MODULE_PATH, expectedWarEffectivePom, "qa-webapp [effective pom]"),
        "Check Effective Pom for war maven module");
  }

  private boolean checkCurrentEffectivePom(
      String modulePath, List<String> expectedText, String nameOfTab) {
    projectExplorer.openItemByPath(modulePath);
    loader.waitOnClosed();
    projectExplorer.waitAndSelectItem(modulePath);
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.GENERATE_EFFECTIVE_POM);
    loader.waitOnClosed();
    editor.selectTabByName(nameOfTab);
    List<String> actualText = Arrays.asList(editor.getVisibleTextFromEditor().split("\n"));
    return actualText.containsAll(expectedText);
  }
}
