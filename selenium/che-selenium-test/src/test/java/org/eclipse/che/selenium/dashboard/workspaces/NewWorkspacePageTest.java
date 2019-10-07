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
package org.eclipse.che.selenium.dashboard.workspaces;

import static java.util.Arrays.asList;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Ihor Okhrimenko */
public class NewWorkspacePageTest {
  private static final String EXPECTED_WORKSPACE_NAME_PREFIX = "wksp-";
  private static final String NAME_WITH_ONE_HUNDRED_SYMBOLS = generate("wksp-", 95);
  private static final List<String> NOT_VALID_NAMES =
      asList("wksp-", "-wksp", "wk sp", "wk_sp", "wksp@", "wksp$", "wksp&", "wksp*");
  private static final List<String> VALID_NAMES =
      asList("Wk-sp", "Wk-sp1", "9wk-sp", "5wk-sp0", "Wk19sp", "Wksp-01");

  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  @BeforeClass
  public void setup() {
    dashboard.open();
  }

  @BeforeMethod
  public void prepareTestWorkspace() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitToolbarTitleName();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitPageLoad();
  }

  @Test
  public void checkNameField() {
    newWorkspace.waitPageLoad();
    assertEquals(
        newWorkspace.getWorkspaceNameValue().substring(0, 5), EXPECTED_WORKSPACE_NAME_PREFIX);

    // empty name field
    newWorkspace.typeWorkspaceName("");
    newWorkspace.waitErrorMessage("A name is required.");
    newWorkspace.waitBottomCreateWorkspaceButtonDisabled();

    // too short name
    newWorkspace.typeWorkspaceName("wk");
    newWorkspace.waitErrorMessage("The name has to be more than 3 characters long.");
    newWorkspace.waitBottomCreateWorkspaceButtonDisabled();

    // min valid name
    newWorkspace.typeWorkspaceName("wks");
    newWorkspace.waitErrorMessageDisappearance();
    newWorkspace.waitBottomCreateWorkspaceButtonEnabled();

    // max valid name
    newWorkspace.typeWorkspaceName(NAME_WITH_ONE_HUNDRED_SYMBOLS);
    newWorkspace.waitErrorMessageDisappearance();
    newWorkspace.waitBottomCreateWorkspaceButtonEnabled();

    // too long name
    newWorkspace.typeWorkspaceName(NAME_WITH_ONE_HUNDRED_SYMBOLS + "p");
    newWorkspace.waitErrorMessage("The name has to be less than 100 characters long.");
    newWorkspace.waitBottomCreateWorkspaceButtonDisabled();

    // max valid name after too long name
    newWorkspace.typeWorkspaceName(NAME_WITH_ONE_HUNDRED_SYMBOLS);
    newWorkspace.waitErrorMessageDisappearance();
    newWorkspace.waitBottomCreateWorkspaceButtonEnabled();

    checkNotValidNames();

    checkValidNames();
  }

  private void checkNotValidNames() {
    NOT_VALID_NAMES.forEach(
        name -> {
          newWorkspace.typeWorkspaceName("temporary");
          newWorkspace.waitErrorMessageDisappearance();
          newWorkspace.waitBottomCreateWorkspaceButtonEnabled();

          newWorkspace.typeWorkspaceName(name);
          newWorkspace.waitErrorMessage(
              "The name should not contain special characters like space, dollar, etc. and should start and end only with digits, latin letters or underscores.");
          newWorkspace.waitBottomCreateWorkspaceButtonDisabled();
        });
  }

  private void checkValidNames() {
    VALID_NAMES.forEach(
        name -> {
          newWorkspace.typeWorkspaceName("temporary");
          newWorkspace.waitErrorMessageDisappearance();
          newWorkspace.waitBottomCreateWorkspaceButtonEnabled();

          newWorkspace.typeWorkspaceName(name);
          newWorkspace.waitErrorMessageDisappearance();
          newWorkspace.waitBottomCreateWorkspaceButtonEnabled();
        });
  }
}
