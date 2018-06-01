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
package org.eclipse.che.selenium.dashboard.workspaces;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DocumentationPage;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceConfig;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class NewWorkspacePageTest {
  private static final int EXPECTED_QUICK_START_STACKS_COUNT = 15;
  private static final int EXPECTED_SINGLE_MACHINE_STACKS_COUNT = 34;
  private static final int EXPECTED_ALL_STACKS_COUNT = 39;
  private static final int EXPECTED_MULTI_MACHINE_STACKS_COUNT = 5;
  private static final String EXPECTED_WORKSPACE_NAME_PREFIX = "wksp-";
  private static final String NAME_WITH_ONE_HUNDRED_SYMBOLS =
      "wksp-ppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp";
  private static final List<String> NOT_VALID_NAMES =
      asList("wksp-", "-wksp", "wk sp", "wk_sp", "wksp@", "wksp$", "wksp&", "wksp*");
  private static List<String> EXPECTED_QUICK_START_STACKS =
      asList(
          "blank-default",
          "java-default",
          "dotnet-default",
          "android-default",
          "cpp-default",
          "che-in-che",
          "go-default",
          "java-theia-openshift",
          "node-default",
          "openshift-default",
          "openshift-sql",
          "php-default",
          "platformio",
          "python-default",
          "rails-default");
  private static List<String> EXPECTED_SINGLE_MACHINE_STACKS =
      asList(
          "blank-default",
          "java-default",
          "dotnet-default",
          "android-default",
          "cpp-default",
          "centos",
          "centos-go",
          "nodejs4",
          "wildfly-swarm",
          "ceylon-java-javascript-dart-centos",
          "debian",
          "debianlsp",
          "che-in-che",
          "vert.x",
          "go-default",
          "hadoop-default",
          "java-centos",
          "java-debian",
          "kotlin-default",
          "node-default",
          "openshift-default",
          "php-default",
          "php-gae",
          "php5.6-default",
          "platformio",
          "python-default",
          "python-2.7",
          "python-gae",
          "rails-default",
          "selenium",
          "spring-boot",
          "tomee-default",
          "ubuntu",
          "zend");

  private static final List<String> EXPECTED_ALL_STACKS =
      Arrays.asList(
          "blank-default",
          "java-default",
          "java-mysql",
          "dotnet-default",
          "android-default",
          "cpp-default",
          "centos",
          "centos-go",
          "nodejs4",
          "wildfly-swarm",
          "ceylon-java-javascript-dart-centos",
          "debian",
          "debianlsp",
          "che-in-che",
          "vert.x",
          "go-default",
          "hadoop-default",
          "java-centos",
          "java-debian",
          "java-theia-docker",
          "java-theia-openshift",
          "java-centos-mysql",
          "kotlin-default",
          "node-default",
          "openshift-default",
          "openshift-sql",
          "php-default",
          "php-gae",
          "php5.6-default",
          "platformio",
          "python-default",
          "python-2.7",
          "python-gae",
          "rails-default",
          "selenium",
          "spring-boot",
          "tomee-default",
          "ubuntu",
          "zend");

  private static final List<String> EXPECTED_MULTI_MACHINE_STACKS =
      asList(
          "java-mysql",
          "java-theia-docker",
          "java-theia-openshift",
          "java-centos-mysql",
          "openshift-sql");

  private static final List<String> EXPECTED_QUICK_START_STACKS_REVERSE_ORDER =
      asList(
          "blank-default",
          "java-default",
          "dotnet-default",
          "rails-default",
          "python-default",
          "platformio",
          "php-default",
          "openshift-sql",
          "openshift-default",
          "node-default",
          "java-theia-openshift",
          "go-default",
          "che-in-che",
          "cpp-default",
          "android-default");

  private static final List<String> EXPECTED_FILTERS_SUGGESTIONS =
      asList("JAVA", "JDK", "JAVA 1.8, THEIA");

  private static final List<String> VALID_NAMES =
      asList("Wk-sp", "Wk-sp1", "9wk-sp", "5wk-sp0", "Wk19sp", "Wksp-01");

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private WorkspaceConfig workspaceConfig;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private DocumentationPage documentationPage;
  @Inject private WorkspaceOverview workspaceOverview;

  @BeforeClass
  public void setup() {
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitToolbarTitleName();
    workspaces.clickOnAddWorkspaceBtn();
  }

  // @Test
  public void checkNameField() {
    newWorkspace.waitPageLoad();
    assertEquals(
        newWorkspace.getWorkspaceNameValue().substring(0, 5), EXPECTED_WORKSPACE_NAME_PREFIX);

    newWorkspace.typeWorkspaceName("");

    newWorkspace.waitErrorMessage("A name is required.");
    newWorkspace.waitCreateWorkspaceButtonDisabled();

    newWorkspace.typeWorkspaceName("wk");

    newWorkspace.waitErrorMessage("The name has to be more than 3 characters long.");
    newWorkspace.waitCreateWorkspaceButtonDisabled();

    newWorkspace.typeWorkspaceName("wks");

    newWorkspace.waitErrorMessageDisappearance();
    newWorkspace.waitCreateWorkspaceButtonEnabled();

    newWorkspace.typeWorkspaceName(NAME_WITH_ONE_HUNDRED_SYMBOLS);

    newWorkspace.waitErrorMessageDisappearance();
    newWorkspace.waitCreateWorkspaceButtonEnabled();

    newWorkspace.typeWorkspaceName(NAME_WITH_ONE_HUNDRED_SYMBOLS + "p");

    newWorkspace.waitErrorMessage("The name has to be less than 100 characters long.");
    newWorkspace.waitCreateWorkspaceButtonDisabled();

    newWorkspace.typeWorkspaceName(NAME_WITH_ONE_HUNDRED_SYMBOLS);

    newWorkspace.waitErrorMessageDisappearance();
    newWorkspace.waitCreateWorkspaceButtonEnabled();

    checkNotValidNames();

    checkValidNames();
  }

  // @Test(priority = 1)
  public void checkStackButtons() {
    newWorkspace.waitQuickStartButton();
    newWorkspace.waitStacks(EXPECTED_QUICK_START_STACKS);
    assertEquals(newWorkspace.getAvailableStacksCount(), EXPECTED_QUICK_START_STACKS_COUNT);

    newWorkspace.clickOnSingleMachineButton();
    newWorkspace.waitStacks(EXPECTED_SINGLE_MACHINE_STACKS);
    assertEquals(newWorkspace.getAvailableStacksCount(), EXPECTED_SINGLE_MACHINE_STACKS_COUNT);

    newWorkspace.clickOnAllButton();
    newWorkspace.waitStacks(EXPECTED_ALL_STACKS);
    assertEquals(newWorkspace.getAvailableStacksCount(), EXPECTED_ALL_STACKS_COUNT);

    newWorkspace.clickOnMultiMachineButton();
    newWorkspace.waitStacks(EXPECTED_MULTI_MACHINE_STACKS);
    assertEquals(newWorkspace.getAvailableStacksCount(), EXPECTED_MULTI_MACHINE_STACKS_COUNT);

    newWorkspace.clickOnAllButton();
    newWorkspace.waitStacks(EXPECTED_SINGLE_MACHINE_STACKS);
    newWorkspace.waitStacks(EXPECTED_MULTI_MACHINE_STACKS);
    assertEquals(
        newWorkspace.getAvailableStacksCount(),
        EXPECTED_SINGLE_MACHINE_STACKS_COUNT + EXPECTED_MULTI_MACHINE_STACKS_COUNT);

    newWorkspace.clickOnQuickStartButton();
    newWorkspace.waitStacksOrder(EXPECTED_QUICK_START_STACKS);
    newWorkspace.clickNameButton();

    try {
      newWorkspace.waitStacksOrder(EXPECTED_QUICK_START_STACKS_REVERSE_ORDER);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/5650", ex);
    }

    newWorkspace.clickNameButton();
    newWorkspace.waitStacksOrder(EXPECTED_QUICK_START_STACKS);
  }

  @Test(priority = 2)
  public void checkFiltersButton() {
    /*newWorkspace.clickOnFiltersButton();
    newWorkspace.waitFiltersFormOpened();
    seleniumWebDriverHelper.sendKeys(ESCAPE.toString());
    newWorkspace.waitFiltersFormClosed();*/

    newWorkspace.clickOnFiltersButton();
    newWorkspace.waitFiltersFormOpened();
    newWorkspace.clickOnTitlePlaceCoordinate();
    newWorkspace.waitFiltersFormClosed();

    newWorkspace.clickOnFiltersButton();
    newWorkspace.waitFiltersFormOpened();
    newWorkspace.typeToFiltersInput("j");
    newWorkspace.waitFiltersSuggestions(EXPECTED_FILTERS_SUGGESTIONS);

    newWorkspace.getSelectedSuggestion();
    newWorkspace.getSelectedSuggestionName();

    newWorkspace.getFiltersSuggestions();
  }

  private void checkNotValidNames() {
    NOT_VALID_NAMES.forEach(
        name -> {
          newWorkspace.typeWorkspaceName("temporary");

          newWorkspace.waitErrorMessageDisappearance();
          newWorkspace.waitCreateWorkspaceButtonEnabled();

          newWorkspace.typeWorkspaceName(name);

          newWorkspace.waitErrorMessage(
              "The name should not contain special characters like space, dollar, etc. and should start and end only with digits, latin letters or underscores.");
          newWorkspace.waitCreateWorkspaceButtonDisabled();
        });
  }

  private void checkValidNames() {
    VALID_NAMES.forEach(
        name -> {
          newWorkspace.typeWorkspaceName("temporary");

          newWorkspace.waitErrorMessageDisappearance();
          newWorkspace.waitCreateWorkspaceButtonEnabled();

          newWorkspace.typeWorkspaceName(name);

          newWorkspace.waitErrorMessageDisappearance();
          newWorkspace.waitCreateWorkspaceButtonEnabled();
        });
  }
}
