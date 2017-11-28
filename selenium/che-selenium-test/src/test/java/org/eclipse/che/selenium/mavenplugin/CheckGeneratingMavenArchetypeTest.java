/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.mavenplugin;

import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.commons.lang.IoUtil;
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
  private static final String NAME_OF_ARTIFACT = "quickStart";
  @Inject private Wizard projectWizard;
  @Inject private Menu menu;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles console;
  @Inject private CodenvyEditor editor;
  @Inject private Ide ide;
  @Inject private TestWorkspace workspace;

  @Test
  public void createMavenArchetypeStartProjectByWizard() throws Exception {
    Stream<String> expectedItems =
        Stream.of(
            NAME_OF_ARTIFACT + "/src/main/java/" + NAME_OF_ARTIFACT + "/App.java",
            NAME_OF_ARTIFACT + "/src/test/java/" + NAME_OF_ARTIFACT + "/AppTest.java",
            NAME_OF_ARTIFACT + "/pom.xml");
    ide.open(workspace);
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    projectWizard.typeProjectNameOnWizard(NAME_OF_ARTIFACT);
    projectWizard.clickNextButton();
    projectWizard.waitOpenProjectConfigForm();
    projectWizard.selectArcheTypeFromList(Wizard.Archetypes.QUICK_START);
    projectWizard.checkArtifactIdOnWizardContainsText(NAME_OF_ARTIFACT);
    projectWizard.checkGroupIdOnWizardContainsText(NAME_OF_ARTIFACT);
    projectWizard.checkVersionOnWizardContainsText("1.0-SNAPSHOT");
    projectWizard.clickCreateButton();
    projectExplorer.waitItem(NAME_OF_ARTIFACT);
    console.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS);
    projectExplorer.quickExpandWithJavaScript();
    expectedItems.forEach(projectExplorer::waitItem);
    projectExplorer.openItemByPath(NAME_OF_ARTIFACT + "/pom.xml");

    String pathToExpectedContentOfPom =
        CheckGeneratingMavenArchetypeTest.class
            .getResource("pom-quick-start-archetype-context")
            .getFile();
    editor.waitTextIntoEditor(
        IoUtil.readAndCloseQuietly(IoUtil.getResource(pathToExpectedContentOfPom)));
  }
}
