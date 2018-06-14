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
package org.eclipse.che.selenium.hotupdate;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RolloutUpdate {
  private static final String NAME_OF_ARTIFACT = NameGenerator.generate("quickStart", 4);
  private static final String PROJECT_NAME = "default-spring-project";

  @Inject private Wizard projectWizard;
  @Inject private Menu menu;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles console;
  @Inject private CodenvyEditor editor;
  @Inject private Ide ide;
  @Inject private TestWorkspace workspace;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private ProcessAgent processAgent;
  @Inject private WebDriverWaitFactory webDriverWaitFactory;

  @BeforeMethod
  public void prepare() throws Exception {
    Path pathToProject =
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI());

    workspace.await();
    testProjectServiceClient.importProject(
        workspace.getId(), pathToProject, PROJECT_NAME, MAVEN_SPRING);
  }

  @Test
  public void createMavenArchetypeStartProjectByWizard() throws Exception {
    int currentRevision = getRevision();

    executeRolloutUpdateCommand();

    waitRevision(currentRevision + 1);

    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    projectExplorer.quickExpandWithJavaScript();

    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitActive();
  }

  private int parseOutputAndGetRevision(String output) {
    String revision =
        asList(output.replace("che", "").replace("config", "").trim().split(" {8}")).get(0);
    return Integer.parseInt(revision);
  }

  private String executeCommand(String command) {
    try {
      return processAgent.process(command);
    } catch (Exception ex) {
      Assert.fail(ex.getLocalizedMessage());
    }
    return null;
  }

  private int getRevision() {
    return parseOutputAndGetRevision(executeCommand("/tmp/oc get dc/che --no-headers=true"));
  }

  private void waitRevision(int expectedRevision) {
    webDriverWaitFactory
        .get(100)
        .until((ExpectedCondition<Boolean>) driver -> expectedRevision == getRevision());
  }

  private void executeRolloutUpdateCommand() throws Exception {
    processAgent.process("/tmp/oc rollout latest che");
  }
}
