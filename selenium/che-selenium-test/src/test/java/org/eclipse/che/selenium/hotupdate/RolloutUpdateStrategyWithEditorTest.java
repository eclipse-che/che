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

import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.selenium.core.client.CheTestSystemClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.executor.OpenShiftCliCommandExecutor;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RolloutUpdateStrategyWithEditorTest {
  private static final int TIMEOUT_FOR_ROLLING_UPDATE_FINISH = 100;
  private static final String PROJECT_NAME = "default-spring-project";
  private static final String ROLLOUT_COMMAND = "rollout latest che";
  private static final String COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE =
      "get dc | grep che | awk '{print $2}'";

  private static final String NAME_OF_CHECKED_CLASS = "AppController";
  private static final String NAME_OF_CHECKED_CLASS_FILE = NAME_OF_CHECKED_CLASS + ".java";
  private static final String TEXT_FOR_TYPING = "test\n";
  private static final String EXPECTED_DEFAULT_TEXT =
      "/*\n"
          + " * Copyright (c) 2012-2018 Red Hat, Inc.\n"
          + " * All rights reserved. This program and the accompanying materials\n"
          + " * are made available under the terms of the Eclipse Public License v1.0\n"
          + " * which accompanies this distribution, and is available at\n"
          + " * http://www.eclipse.org/legal/epl-v10.html\n"
          + " *\n"
          + " * Contributors:\n"
          + " *   Red Hat, Inc. - initial API and implementation\n"
          + " */\n"
          + "package org.eclipse.qa.examples;\n"
          + "\n"
          + "import java.util.Random;\n"
          + "\n";

  private static final String EXPECTED_CHANGED_TEXT = TEXT_FOR_TYPING + EXPECTED_DEFAULT_TEXT;

  @Inject private Wizard projectWizard;
  @Inject private Menu menu;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles console;
  @Inject private CodenvyEditor editor;
  @Inject private Ide ide;
  @Inject private TestWorkspace workspace;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private WebDriverWaitFactory webDriverWaitFactory;
  @Inject private CheTestSystemClient cheTestSystemClient;
  @Inject private OpenShiftCliCommandExecutor openShiftCliCommandExecutor;

  @BeforeMethod
  public void prepare() throws Exception {
    Path pathToProject =
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI());

    testProjectServiceClient.importProject(
        workspace.getId(), pathToProject, PROJECT_NAME, MAVEN_SPRING);
  }

  @Test
  public void shouldUpdateMasterByRolloutStrategyWithAccessibleEditorInProcess() throws Exception {
    int currentRevision = getRevision();

    // check editor availability

    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    projectExplorer.quickExpandWithJavaScript();

    projectExplorer.openItemByVisibleNameInExplorer(NAME_OF_CHECKED_CLASS_FILE);
    editor.waitActive();
    editor.waitTextIntoEditor(EXPECTED_DEFAULT_TEXT);

    // check master status
    assertEquals(cheTestSystemClient.getStatus(), SystemStatus.RUNNING);

    // execute rollout
    executeRolloutUpdateCommand();

    // check editor availability during rollout updating
    assertEquals(cheTestSystemClient.getStatus(), SystemStatus.RUNNING);
    editor.selectTabByName(NAME_OF_CHECKED_CLASS);
    editor.waitActive();
    editor.typeTextIntoEditor(TEXT_FOR_TYPING);
    editor.waitTextIntoEditor(EXPECTED_DEFAULT_TEXT);

    editor.waitTabFileWithSavedStatus(NAME_OF_CHECKED_CLASS);
    editor.closeAllTabs();
    editor.waitTabIsNotPresent(NAME_OF_CHECKED_CLASS);

    projectExplorer.openItemByVisibleNameInExplorer(NAME_OF_CHECKED_CLASS_FILE);
    editor.waitActive();
    editor.waitTextIntoEditor(EXPECTED_CHANGED_TEXT);

    // check that che is updated
    waitRevision(currentRevision + 1);
  }

  private int getRevision() {
    try {
      return Integer.parseInt(
          openShiftCliCommandExecutor.execute(COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE));
    } catch (IOException ex) {
      throw new RuntimeException(ex.getLocalizedMessage(), ex);
    }
  }

  private void waitRevision(int expectedRevision) {
    webDriverWaitFactory
        .get(TIMEOUT_FOR_ROLLING_UPDATE_FINISH)
        .until((ExpectedCondition<Boolean>) driver -> expectedRevision == getRevision());
  }

  private void executeRolloutUpdateCommand() throws Exception {
    openShiftCliCommandExecutor.execute(ROLLOUT_COMMAND);
  }
}
