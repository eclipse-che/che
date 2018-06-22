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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.ARROW_UP;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
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
import org.eclipse.che.selenium.pageobject.dashboard.stacks.Stacks;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceConfig;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Ihor Okhrimenko */
public class NewWorkspacePageTest {
  private static final int EXPECTED_QUICK_START_STACKS_COUNT = 15;
  private static final int EXPECTED_SINGLE_MACHINE_STACKS_COUNT = 34;
  private static final int EXPECTED_ALL_STACKS_COUNT = 39;
  private static final int EXPECTED_MULTI_MACHINE_STACKS_COUNT = 5;
  private static final String EXPECTED_WORKSPACE_NAME_PREFIX = "wksp-";
  private static final String MACHINE_NAME = "dev-machine";
  private static final double MAX_RAM_VALUE = 100.0;
  private static final double MIN_RAM_VALUE = 0.5;
  private static final double RAM_CHANGE_STEP = 0.5;
  private static final String NAME_WITH_ONE_HUNDRED_SYMBOLS =
      "wksp-ppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp";
  private static final List<String> NOT_VALID_NAMES =
      asList("wksp-", "-wksp", "wk sp", "wk_sp", "wksp@", "wksp$", "wksp&", "wksp*");
  private static final String LETTER_FOR_SEARCHING = "j";
  private static final List<String> EXPECTED_JDK_STACKS = asList("java-default", "che-in-che");
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
      asList(
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

  private static final List<String> EXPECTED_JAVA_STACKS =
      asList("java-default", "android-default", "che-in-che", "java-theia-openshift");

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
  @Inject private Stacks stacks;

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

  @Test
  public void checkStackButtons() {
    newWorkspace.waitPageLoad();
    newWorkspace.waitQuickStartButton();
    newWorkspace.waitStacks(EXPECTED_QUICK_START_STACKS);
    newWorkspace.waitStacksCount(EXPECTED_QUICK_START_STACKS_COUNT);

    // single machine stacks
    newWorkspace.clickOnSingleMachineButton();
    newWorkspace.waitStacks(EXPECTED_SINGLE_MACHINE_STACKS);
    newWorkspace.waitStacksCount(EXPECTED_SINGLE_MACHINE_STACKS_COUNT);

    // all stacks
    newWorkspace.clickOnAllButton();
    newWorkspace.waitStacks(EXPECTED_ALL_STACKS);
    newWorkspace.waitStacksCount(EXPECTED_ALL_STACKS_COUNT);

    // multi-machine stacks
    newWorkspace.clickOnMultiMachineButton();
    newWorkspace.waitStacks(EXPECTED_MULTI_MACHINE_STACKS);
    newWorkspace.waitStacksCount(EXPECTED_MULTI_MACHINE_STACKS_COUNT);

    // check that only expected stacks are displayed and no duplicates are presented
    newWorkspace.clickOnAllButton();
    newWorkspace.waitStacks(EXPECTED_SINGLE_MACHINE_STACKS);
    newWorkspace.waitStacks(EXPECTED_MULTI_MACHINE_STACKS);
    newWorkspace.waitStacksCount(
        EXPECTED_SINGLE_MACHINE_STACKS_COUNT + EXPECTED_MULTI_MACHINE_STACKS_COUNT);

    // quick start stacks
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

  @Test
  public void checkFiltersButton() {
    newWorkspace.waitPageLoad();

    // close by "Escape" button
    newWorkspace.clickOnFiltersButton();
    newWorkspace.waitFiltersFormOpened();
    seleniumWebDriverHelper.sendKeys(ESCAPE.toString());
    newWorkspace.waitFiltersFormClosed();

    // close by clicking on the outside of the "Filters" form
    newWorkspace.clickOnFiltersButton();
    newWorkspace.waitFiltersFormOpened();
    newWorkspace.clickOnTitlePlaceCoordinate();
    newWorkspace.waitFiltersFormClosed();

    // check suggestion list
    newWorkspace.clickOnFiltersButton();
    newWorkspace.waitFiltersFormOpened();
    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitFiltersSuggestionsNames(EXPECTED_FILTERS_SUGGESTIONS);

    assertEquals(
        newWorkspace.getSelectedFiltersSuggestionName(),
        newWorkspace.getFiltersSuggestionsNames().get(0));

    // check navigation by keyboard arrows between suggested tags
    seleniumWebDriverHelper.sendKeys(ARROW_DOWN.toString());
    newWorkspace.waitSelectedFiltersSuggestion("JDK");

    seleniumWebDriverHelper.sendKeys(ARROW_UP.toString());
    newWorkspace.waitSelectedFiltersSuggestion("JAVA");

    seleniumWebDriverHelper.sendKeys(ARROW_UP.toString());
    newWorkspace.waitSelectedFiltersSuggestion("JAVA 1.8, THEIA");

    // interaction with suggested tads by mouse clicking
    newWorkspace.clickOnFiltersSuggestions("JAVA");
    newWorkspace.waitSelectedFiltersSuggestion("JAVA");

    newWorkspace.clickOnFiltersSuggestions("JDK");
    newWorkspace.waitSelectedFiltersSuggestion("JDK");

    newWorkspace.doubleClickOnFiltersSuggestion("JDK");
    newWorkspace.waitFiltersInputTags(asList("JDK"));

    newWorkspace.deleteLastTagFromInputTagsField();
    newWorkspace.waitFiltersInputIsEmpty();

    // delete tags from input
    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitFiltersSuggestionsNames(EXPECTED_FILTERS_SUGGESTIONS);

    newWorkspace.waitSelectedFiltersSuggestion("JAVA");
    newWorkspace.doubleClickOnFiltersSuggestion("JAVA 1.8, THEIA");

    newWorkspace.waitFiltersInputTags(asList("JAVA 1.8, THEIA"));

    newWorkspace.deleteTagByRemoveButton("JAVA 1.8, THEIA");
    newWorkspace.waitFiltersInputIsEmpty();

    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitFiltersSuggestionsNames(EXPECTED_FILTERS_SUGGESTIONS);
    newWorkspace.waitSelectedFiltersSuggestion("JAVA");
    newWorkspace.chooseFilterSuggestionByPlusButton("JDK");
    newWorkspace.waitFiltersInputTags(asList("JDK"));
    newWorkspace.clickOnInputFieldTag("JDK");
    seleniumWebDriverHelper.sendKeys(Keys.DELETE.toString());
    newWorkspace.waitFiltersInputIsEmpty();

    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitFiltersSuggestionsNames(EXPECTED_FILTERS_SUGGESTIONS);
    newWorkspace.waitSelectedFiltersSuggestion("JAVA");
    newWorkspace.chooseFilterSuggestionByPlusButton("JAVA");
    newWorkspace.waitFiltersInputTags(asList("JAVA"));
    newWorkspace.clickOnInputFieldTag("JAVA");
    seleniumWebDriverHelper.sendKeys(Keys.DELETE.toString());
    newWorkspace.waitFiltersInputIsEmpty();
    newWorkspace.deleteLastTagFromInputTagsField();

    // navigation by "Tab" button
    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitSelectedFiltersSuggestion("JAVA");
    seleniumWebDriverHelper.sendKeys(Keys.TAB.toString());
    newWorkspace.waitSelectedFiltersSuggestion("JDK");
    seleniumWebDriverHelper.sendKeys(Keys.ENTER.toString());
    newWorkspace.waitFiltersInputTags(asList("JDK"));
    newWorkspace.clickOnTitlePlaceCoordinate();
    newWorkspace.waitFiltersFormClosed();

    newWorkspace.getAvailableStacks();
    newWorkspace.waitStacks(EXPECTED_JDK_STACKS);

    newWorkspace.clickOnFiltersButton();
    newWorkspace.waitFiltersFormOpened();
    newWorkspace.waitFiltersInputTags(asList("JDK"));
    newWorkspace.deleteLastTagFromInputTagsField();
    newWorkspace.waitFiltersInputIsEmpty();
    newWorkspace.clickOnTitlePlaceCoordinate();
    newWorkspace.waitFiltersFormClosed();
    newWorkspace.waitStacks(EXPECTED_QUICK_START_STACKS);
  }

  @Test
  public void checkAddStackButton() {
    newWorkspace.waitPageLoad();

    // close form by "ESCAPE" button
    newWorkspace.clickOnAddStackButton();
    newWorkspace.waitCreateStackDialog();
    seleniumWebDriverHelper.sendKeys(ESCAPE.toString());
    newWorkspace.waitCreateStackDialogClosing();

    // close form by clicking on outside of form bounds
    newWorkspace.clickOnAddStackButton();
    newWorkspace.waitCreateStackDialog();
    newWorkspace.clickOnTitlePlaceCoordinate();
    newWorkspace.waitCreateStackDialogClosing();

    // close form by "Close" button
    newWorkspace.clickOnAddStackButton();
    newWorkspace.waitCreateStackDialog();
    newWorkspace.closeCreateStackDialogByCloseButton();
    newWorkspace.waitCreateStackDialogClosing();

    // close form by "No" button
    newWorkspace.clickOnAddStackButton();
    newWorkspace.waitCreateStackDialog();
    newWorkspace.clickOnNoButtonInCreateStackDialog();
    newWorkspace.waitCreateStackDialogClosing();

    // click on "Yes" button
    newWorkspace.clickOnAddStackButton();
    newWorkspace.waitCreateStackDialog();
    newWorkspace.clickOnYesButtonInCreateStackDialog();
    stacks.waitToolbarTitleName();
    seleniumWebDriver.navigate().back();
    newWorkspace.waitPageLoad();
  }

  @Test
  public void checkSearchField() {
    newWorkspace.waitPageLoad();

    newWorkspace.typeToSearchInput("Java");
    newWorkspace.waitStacks(EXPECTED_JAVA_STACKS);

    newWorkspace.typeToSearchInput("");
    newWorkspace.waitStacks(EXPECTED_QUICK_START_STACKS);

    newWorkspace.typeToSearchInput("java");
    newWorkspace.waitStacks(EXPECTED_JAVA_STACKS);

    newWorkspace.typeToSearchInput("");
    newWorkspace.waitStacks(EXPECTED_QUICK_START_STACKS);

    newWorkspace.typeToSearchInput("JAVA");
    newWorkspace.waitStacks(EXPECTED_JAVA_STACKS);

    newWorkspace.typeToSearchInput("");
    newWorkspace.waitStacks(EXPECTED_QUICK_START_STACKS);
  }

  @Test
  public void checkRamSelection() {
    newWorkspace.waitPageLoad();

    // empty RAM
    newWorkspace.selectStack(JAVA);
    newWorkspace.waitStackSelected(JAVA);
    newWorkspace.waitRamValue(MACHINE_NAME, 2.0);
    newWorkspace.typeToRamField("");
    newWorkspace.waitRedRamFieldBorders();
    newWorkspace.waitTopCreateWorkspaceButtonDisabled();
    newWorkspace.waitBottomCreateWorkspaceButtonDisabled();

    // max valid value
    newWorkspace.typeToRamField(Double.toString(MAX_RAM_VALUE));
    newWorkspace.waitRedRamFieldBordersDisappearance();
    newWorkspace.waitTopCreateWorkspaceButtonEnabled();
    newWorkspace.waitBottomCreateWorkspaceButtonEnabled();

    // increment and decrement buttons with max valid value
    newWorkspace.clickOnIncrementMemoryButton(MACHINE_NAME);
    newWorkspace.waitRamValue(MACHINE_NAME, MAX_RAM_VALUE);

    newWorkspace.clickOnDecrementMemoryButton(MACHINE_NAME);
    newWorkspace.waitRamValue(MACHINE_NAME, MAX_RAM_VALUE - RAM_CHANGE_STEP);

    // min valid value
    newWorkspace.typeToRamField("");
    newWorkspace.waitRedRamFieldBorders();
    newWorkspace.waitTopCreateWorkspaceButtonDisabled();
    newWorkspace.waitBottomCreateWorkspaceButtonDisabled();

    newWorkspace.typeToRamField(Double.toString(MIN_RAM_VALUE));
    newWorkspace.waitRedRamFieldBordersDisappearance();
    newWorkspace.waitTopCreateWorkspaceButtonEnabled();
    newWorkspace.waitBottomCreateWorkspaceButtonEnabled();

    // increment and decrement buttons with min valid value
    newWorkspace.clickOnDecrementMemoryButton(MACHINE_NAME);
    newWorkspace.waitRamValue(MACHINE_NAME, MIN_RAM_VALUE);
    newWorkspace.clickOnIncrementMemoryButton(MACHINE_NAME);
    newWorkspace.waitRamValue(MACHINE_NAME, MIN_RAM_VALUE + RAM_CHANGE_STEP);

    // increment and decrement by click and hold
    newWorkspace.clickAndHoldIncrementMemoryButton(MACHINE_NAME, 3);
    newWorkspace.waitRamValueInSpecifiedRange(MACHINE_NAME, 3, MAX_RAM_VALUE);

    double currentRamAmount = newWorkspace.getRAM(MACHINE_NAME);
    newWorkspace.clickAndHoldDecrementMemoryButton(MACHINE_NAME, 3);
    newWorkspace.waitRamValueInSpecifiedRange(MACHINE_NAME, MIN_RAM_VALUE, currentRamAmount - 2);
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
