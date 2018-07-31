/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.mavenplugin;

import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckGeneratingMavenArchetypeTest {
  private static final String PROJECT_NAME = NameGenerator.generate("quickStart", 4);
  private static final String ARTIFACT_ID = "artifact";
  private static final String GROUP_ID = "group";
  @Inject private Wizard projectWizard;
  @Inject private Menu menu;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles console;
  @Inject private CodenvyEditor editor;
  @Inject private Ide ide;
  @Inject private TestWorkspace workspace;

  @Test
  public void createMavenArchetypeStartProjectByWizard() throws Exception {
    String expectedContnetInPomXml =
        String.format(
            "  <groupId>%s</groupId>\n"
                + "  <artifactId>%s</artifactId>\n"
                + "  <version>1.0-SNAPSHOT</version>",
            GROUP_ID, ARTIFACT_ID);

    Stream<String> expectedItems =
        Stream.of(
            PROJECT_NAME + "/src/main/java/" + GROUP_ID + "/App.java",
            PROJECT_NAME + "/src/test/java/" + GROUP_ID + "/AppTest.java",
            PROJECT_NAME + "/pom.xml");
    ide.open(workspace);
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    projectWizard.typeProjectNameOnWizard(PROJECT_NAME);
    projectWizard.clickNextButton();
    projectWizard.waitOpenProjectConfigForm();
    projectWizard.selectArcheTypeFromList(Wizard.Archetypes.QUICK_START);
    projectWizard.setArtifactIdOnWizard(ARTIFACT_ID);
    projectWizard.checkArtifactIdOnWizardContainsText(ARTIFACT_ID);
    projectWizard.setGroupIdOnWizard(GROUP_ID);
    projectWizard.checkGroupIdOnWizardContainsText(GROUP_ID);
    projectWizard.checkVersionOnWizardContainsText("1.0-SNAPSHOT");
    projectWizard.clickCreateButton();
    projectExplorer.waitItem(PROJECT_NAME);
    console.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS);
    projectExplorer.quickExpandWithJavaScript();
    expectedItems.forEach(projectExplorer::waitItem);
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
    editor.waitTextIntoEditor(expectedContnetInPomXml);
  }
}
