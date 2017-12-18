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
package org.eclipse.che.selenium.assistant;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.FindAction;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckFindActionFeatureInCheTest {
  private static final String FIRST_ACTION_NAME = "config";
  private static final String SECOND_ACTION_NAME = "commands";
  private static final String THIRD_ACTION_NAME = "che";

  private static final String FIRST_EXPECTED_ITEMS_WITH_DISABLED_NONE_MENU_ACTIONS_CHECKBOX =
      "Update Project Configuration...  Project\n"
          + "Configure Classpath  Project\n"
          + "Edit Debug Configurations... [Alt+Shift+F9]  Run\n"
          + "Import From Che Config...  Project";

  private static final String SECOND_EXPECTED_ITEMS_WITH_DISABLED_NONE_MENU_ACTIONS_CHECKBOX =
      "Commands Palette [Shift+F10]  Run";

  private static final String THIRD_EXPECTED_ITEMS_WITH_DISABLED_NONE_MENU_ACTIONS_CHECKBOX =
      "Branches... [Ctrl+B]  GitCommandGroup\n" + "Checkout Reference...  GitCommandGroup";

  private static final String FIRST_EXPECTED_ITEMS_WITH_ENABLED_NONE_MENU_ACTIONS_CHECKBOX =
      "Update Project Configuration...  Project\n"
          + "Configure \n"
          + "Configure Classpath  Project\n"
          + "Edit Debug Configurations... [Alt+Shift+F9]  Run\n"
          + "Import From Che Config...  Project\n"
          + "breakpointConfiguration ";

  private static final String SECOND_EXPECTED_ITEMS_WITH_ENABLED_NONE_MENU_ACTIONS_CHECKBOX =
      "Commands \n"
          + "Commands [Ctrl+Alt+4]  Tool Windows\n"
          + "Commands \n"
          + "Commands Palette [Shift+F10]  Run";
  private static final String THIRD_EXPECTED_ITEMS_WITH_ENABLED_NONE_MENU_ACTIONS_CHECKBOX =
      "Branches... [Ctrl+B]  GitCommandGroup\n" + "Checkout Reference...  GitCommandGroup";

  private static final String PROJECT_NAME =
      NameGenerator.generate(CheckFindActionFeatureInCheTest.class.getSimpleName(), 4);

  @Inject private TestWorkspace testWorkspace;
  @Inject private FindAction findAction;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Ide ide;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = this.getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(testWorkspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitVisibleItem(PROJECT_NAME);
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.FIND_ACTION);
    findAction.setCheckBoxInSelectedPosition();
  }

  @Test(dataProvider = "checkingDataAllActionsData")
  public void checkSearchActionsForAllItemsTest(String actionName, String result) {
    checkAction(actionName, result);
  }

  @Test(dataProvider = "checkingDataWithMenuActionsOnly", priority = 1)
  public void checkSearchActionsForMenuItemsTest(String actionName, String result) {
    findAction.setCheckBoxInNotSelectedPosition();
    checkAction(actionName, result);
  }

  @DataProvider
  private Object[][] checkingDataWithMenuActionsOnly() {
    return new Object[][] {
      {FIRST_ACTION_NAME, FIRST_EXPECTED_ITEMS_WITH_DISABLED_NONE_MENU_ACTIONS_CHECKBOX},
      {SECOND_ACTION_NAME, SECOND_EXPECTED_ITEMS_WITH_DISABLED_NONE_MENU_ACTIONS_CHECKBOX},
      {THIRD_ACTION_NAME, THIRD_EXPECTED_ITEMS_WITH_DISABLED_NONE_MENU_ACTIONS_CHECKBOX}
    };
  }

  @DataProvider
  private Object[][] checkingDataAllActionsData() {
    return new Object[][] {
      {FIRST_ACTION_NAME, FIRST_EXPECTED_ITEMS_WITH_ENABLED_NONE_MENU_ACTIONS_CHECKBOX},
      {SECOND_ACTION_NAME, SECOND_EXPECTED_ITEMS_WITH_ENABLED_NONE_MENU_ACTIONS_CHECKBOX},
      {THIRD_ACTION_NAME, THIRD_EXPECTED_ITEMS_WITH_ENABLED_NONE_MENU_ACTIONS_CHECKBOX}
    };
  }

  private void checkAction(String actionName, String expectedResult) {
    findAction.typeTextIntoFindActionForm(actionName);
    findAction.waitTextInFormFindAction(expectedResult);
    findAction.clearTextBoxActionForm();
  }
}
