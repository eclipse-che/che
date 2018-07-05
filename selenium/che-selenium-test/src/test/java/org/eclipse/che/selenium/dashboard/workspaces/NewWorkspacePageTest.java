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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ANDROID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.BLANK;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CENTOS_BLANK;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CENTOS_GO;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CENTOS_NODEJS;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CENTOS_WILDFLY_SWARM;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CEYLON_WITH_JAVA_JAVASCRIPT;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CPP;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.DOT_NET;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ECLIPSE_CHE;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ECLIPSE_VERTX;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.GO;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_CENTOS;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MYSQL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MYSQL_CENTOS;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_THEIA_DOCKER;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_THEIA_OPENSHIFT;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.KOTLIN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.NODE;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PHP;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PYTHON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.RAILS;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.SPRING_BOOT;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.ARROW_UP;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
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
  private static final String EXPECTED_WORKSPACE_NAME_PREFIX = "wksp-";
  private static final String MACHINE_NAME = "dev-machine";
  private static final double MAX_RAM_VALUE = 100.0;
  private static final double MIN_RAM_VALUE = 0.5;
  private static final double RAM_CHANGE_STEP = 0.5;
  private static final String JDK_SUGGESTION_TITLE = "JDK";
  private static final String JAVA_SUGGESTION_TITLE = "JAVA";
  private static final String JAVA_1_8_SUGGESTION_TITLE = "JAVA 1.8";
  private static final String NAME_WITH_ONE_HUNDRED_SYMBOLS = NameGenerator.generate("wksp-", 95);
  private static final List<String> NOT_VALID_NAMES =
      asList("wksp-", "-wksp", "wk sp", "wk_sp", "wksp@", "wksp$", "wksp&", "wksp*");
  private static final String LETTER_FOR_SEARCHING = "j";
  private static final List<NewWorkspace.Stack> EXPECTED_JDK_STACKS = asList(JAVA, ECLIPSE_CHE);
  private static List<NewWorkspace.Stack> EXPECTED_OPENSHIFT_QUICK_START_STACKS =
      asList(
          BLANK,
          JAVA,
          DOT_NET,
          ANDROID,
          CPP,
          ECLIPSE_CHE,
          GO,
          JAVA_THEIA_OPENSHIFT,
          NODE,
          PHP,
          PYTHON,
          RAILS);

  private static final List<NewWorkspace.Stack> EXPECTED_DOCKER_QUICK_START_STACKS =
      asList(
          BLANK,
          JAVA,
          JAVA_MYSQL,
          DOT_NET,
          ANDROID,
          CPP,
          ECLIPSE_CHE,
          GO,
          JAVA_THEIA_DOCKER,
          NODE,
          PHP,
          PYTHON,
          RAILS);

  private static List<String> EXPECTED_OPENSHIFT_QUICK_START_STACKS_ORDER =
      asList(
          BLANK.getId(),
          JAVA.getId(),
          DOT_NET.getId(),
          ANDROID.getId(),
          CPP.getId(),
          ECLIPSE_CHE.getId(),
          GO.getId(),
          JAVA_THEIA_OPENSHIFT.getId(),
          NODE.getId(),
          PHP.getId(),
          PYTHON.getId(),
          RAILS.getId());

  private static final List<String> EXPECTED_DOCKER_QUICK_START_STACKS_ORDER =
      asList(
          BLANK.getId(),
          JAVA.getId(),
          JAVA_MYSQL.getId(),
          DOT_NET.getId(),
          ANDROID.getId(),
          CPP.getId(),
          ECLIPSE_CHE.getId(),
          GO.getId(),
          JAVA_THEIA_DOCKER.getId(),
          NODE.getId(),
          PHP.getId(),
          PYTHON.getId(),
          RAILS.getId());

  private static List<NewWorkspace.Stack> EXPECTED_OPENSHIFT_SINGLE_MACHINE_STACKS =
      asList(
          BLANK,
          JAVA,
          DOT_NET,
          ANDROID,
          CPP,
          CENTOS_BLANK,
          CENTOS_GO,
          CENTOS_NODEJS,
          CENTOS_WILDFLY_SWARM,
          CEYLON_WITH_JAVA_JAVASCRIPT,
          ECLIPSE_CHE,
          ECLIPSE_VERTX,
          GO,
          JAVA_CENTOS,
          KOTLIN,
          NODE,
          PHP,
          PYTHON,
          RAILS,
          SPRING_BOOT);

  private static List<NewWorkspace.Stack> EXPECTED_DOCKER_SINGLE_MACHINE_STACKS =
      asList(
          BLANK,
          JAVA,
          DOT_NET,
          ANDROID,
          CPP,
          CENTOS_BLANK,
          CENTOS_GO,
          CENTOS_NODEJS,
          CENTOS_WILDFLY_SWARM,
          CEYLON_WITH_JAVA_JAVASCRIPT,
          ECLIPSE_CHE,
          ECLIPSE_VERTX,
          GO,
          JAVA_CENTOS,
          KOTLIN,
          NODE,
          PHP,
          PYTHON,
          RAILS,
          SPRING_BOOT);

  private static final List<NewWorkspace.Stack> EXPECTED_OPENSHIFT_MULTI_MACHINE_STACKS =
      asList(JAVA_MYSQL, JAVA_THEIA_DOCKER, JAVA_THEIA_OPENSHIFT, JAVA_MYSQL_CENTOS);

  private static final List<NewWorkspace.Stack> EXPECTED_DOCKER_MULTI_MACHINE_STACKS =
      asList(JAVA_MYSQL, JAVA_THEIA_DOCKER, JAVA_THEIA_OPENSHIFT, JAVA_MYSQL_CENTOS);

  private static final List<String> EXPECTED_OPENSHIFT_QUICK_START_STACKS_REVERSE_ORDER =
      asList(
          BLANK.getId(),
          JAVA.getId(),
          DOT_NET.getId(),
          RAILS.getId(),
          PYTHON.getId(),
          PHP.getId(),
          NODE.getId(),
          JAVA_THEIA_OPENSHIFT.getId(),
          GO.getId(),
          ECLIPSE_CHE.getId(),
          CPP.getId(),
          ANDROID.getId());

  private static final List<String> EXPECTED_DOCKER_QUICK_START_STACKS_REVERSE_ORDER =
      asList(
          BLANK.getId(),
          JAVA.getId(),
          JAVA_MYSQL.getId(),
          RAILS.getId(),
          PYTHON.getId(),
          PHP.getId(),
          NODE.getId(),
          JAVA_THEIA_DOCKER.getId(),
          GO.getId(),
          ECLIPSE_CHE.getId(),
          CPP.getId(),
          ANDROID.getId(),
          DOT_NET.getId());

  private static final List<NewWorkspace.Stack> EXPECTED_OPENSHIFT_JAVA_STACKS =
      asList(JAVA, ANDROID, ECLIPSE_CHE, JAVA_THEIA_OPENSHIFT);

  private static final List<NewWorkspace.Stack> EXPECTED_DOCKER_JAVA_STACKS =
      asList(JAVA_MYSQL, JAVA, JAVA_THEIA_DOCKER, ECLIPSE_CHE, ANDROID);

  private static final List<String> EXPECTED_FILTERS_SUGGESTIONS =
      asList(JAVA_SUGGESTION_TITLE, JDK_SUGGESTION_TITLE, JAVA_1_8_SUGGESTION_TITLE);

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

  @Test(groups = TestGroup.OPENSHIFT)
  public void checkOpenshiftStackButtons() {
    checkStackButtons(
        EXPECTED_OPENSHIFT_QUICK_START_STACKS,
        EXPECTED_OPENSHIFT_SINGLE_MACHINE_STACKS,
        EXPECTED_OPENSHIFT_MULTI_MACHINE_STACKS,
        EXPECTED_OPENSHIFT_QUICK_START_STACKS_ORDER,
        EXPECTED_OPENSHIFT_QUICK_START_STACKS_REVERSE_ORDER);
  }

  @Test(groups = TestGroup.DOCKER)
  public void checkDockerStackButtons() {
    checkStackButtons(
        EXPECTED_DOCKER_QUICK_START_STACKS,
        EXPECTED_DOCKER_SINGLE_MACHINE_STACKS,
        EXPECTED_DOCKER_MULTI_MACHINE_STACKS,
        EXPECTED_DOCKER_QUICK_START_STACKS_ORDER,
        EXPECTED_DOCKER_QUICK_START_STACKS_REVERSE_ORDER);
  }

  @Test(groups = TestGroup.OPENSHIFT)
  public void checkOpenshiftFiltersButton() {
    checkFiltersButton(EXPECTED_OPENSHIFT_QUICK_START_STACKS);
  }

  @Test(groups = TestGroup.DOCKER)
  public void checkDockerFiltersButton() {
    checkFiltersButton(EXPECTED_DOCKER_QUICK_START_STACKS);
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

  @Test(groups = TestGroup.OPENSHIFT)
  public void checkOpenshiftSearchField() {
    checkSearchField(EXPECTED_OPENSHIFT_JAVA_STACKS, EXPECTED_OPENSHIFT_QUICK_START_STACKS);
  }

  @Test(groups = TestGroup.DOCKER)
  public void checkDockerSearchField() {
    checkSearchField(EXPECTED_DOCKER_JAVA_STACKS, EXPECTED_DOCKER_QUICK_START_STACKS);
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

  private void checkStackButtons(
      List<NewWorkspace.Stack> expectedQuickStartStacks,
      List<NewWorkspace.Stack> expectedSingleMachineStacks,
      List<NewWorkspace.Stack> expectedMultiMachineStacks,
      List<String> expectedQuickStartStacksOrder,
      List<String> expectedQuickStartStacksReverseOrder) {

    newWorkspace.waitPageLoad();
    newWorkspace.waitQuickStartButton();
    newWorkspace.waitStacks(expectedQuickStartStacks);
    newWorkspace.waitStacksCount(expectedQuickStartStacks.size());

    // single machine stacks
    newWorkspace.clickOnSingleMachineButton();
    newWorkspace.waitStacks(expectedSingleMachineStacks);
    newWorkspace.waitStacksCount(expectedSingleMachineStacks.size());

    // multi-machine stacks
    newWorkspace.clickOnMultiMachineButton();
    newWorkspace.waitStacks(expectedMultiMachineStacks);
    newWorkspace.waitStacksCount(expectedMultiMachineStacks.size());

    // check that only expected stacks are displayed and no duplicates are presented and also checks
    // "All" stacks
    newWorkspace.clickOnAllButton();
    newWorkspace.waitStacks(expectedSingleMachineStacks);
    newWorkspace.waitStacks(expectedMultiMachineStacks);
    newWorkspace.waitStacksCount(
        expectedSingleMachineStacks.size() + expectedMultiMachineStacks.size());

    // quick start stacks
    newWorkspace.clickOnQuickStartButton();
    newWorkspace.waitStacksOrder(expectedQuickStartStacksOrder);
    newWorkspace.clickNameButton();

    try {
      newWorkspace.waitStacksOrder(expectedQuickStartStacksReverseOrder);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/5650", ex);
    }

    newWorkspace.clickNameButton();
    newWorkspace.waitStacksOrder(expectedQuickStartStacksOrder);
  }

  private void checkFiltersButton(List<NewWorkspace.Stack> expectedQuickStartStacks) {
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
    newWorkspace.waitSelectedFiltersSuggestion(JDK_SUGGESTION_TITLE);

    seleniumWebDriverHelper.sendKeys(ARROW_UP.toString());
    newWorkspace.waitSelectedFiltersSuggestion(JAVA_SUGGESTION_TITLE);

    seleniumWebDriverHelper.sendKeys(ARROW_UP.toString());
    newWorkspace.waitSelectedFiltersSuggestion(JAVA_1_8_SUGGESTION_TITLE);

    // interaction with suggested tads by mouse clicking
    newWorkspace.clickOnFiltersSuggestions(JAVA_SUGGESTION_TITLE);
    newWorkspace.waitSelectedFiltersSuggestion(JAVA_SUGGESTION_TITLE);

    newWorkspace.clickOnFiltersSuggestions(JDK_SUGGESTION_TITLE);
    newWorkspace.waitSelectedFiltersSuggestion(JDK_SUGGESTION_TITLE);

    newWorkspace.doubleClickOnFiltersSuggestion(JDK_SUGGESTION_TITLE);
    newWorkspace.waitFiltersInputTags(asList(JDK_SUGGESTION_TITLE));

    newWorkspace.deleteLastTagFromInputTagsField();
    newWorkspace.waitFiltersInputIsEmpty();

    // delete tags from input
    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitFiltersSuggestionsNames(EXPECTED_FILTERS_SUGGESTIONS);

    newWorkspace.waitSelectedFiltersSuggestion(JAVA_SUGGESTION_TITLE);
    newWorkspace.doubleClickOnFiltersSuggestion(JAVA_1_8_SUGGESTION_TITLE);

    newWorkspace.waitFiltersInputTags(asList(JAVA_1_8_SUGGESTION_TITLE));

    newWorkspace.deleteTagByRemoveButton(JAVA_1_8_SUGGESTION_TITLE);
    newWorkspace.waitFiltersInputIsEmpty();

    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitFiltersSuggestionsNames(EXPECTED_FILTERS_SUGGESTIONS);
    newWorkspace.waitSelectedFiltersSuggestion(JAVA_SUGGESTION_TITLE);
    newWorkspace.chooseFilterSuggestionByPlusButton(JDK_SUGGESTION_TITLE);
    newWorkspace.waitFiltersInputTags(asList(JDK_SUGGESTION_TITLE));
    newWorkspace.clickOnInputFieldTag(JDK_SUGGESTION_TITLE);
    seleniumWebDriverHelper.sendKeys(Keys.DELETE.toString());
    newWorkspace.waitFiltersInputIsEmpty();

    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitFiltersSuggestionsNames(EXPECTED_FILTERS_SUGGESTIONS);
    newWorkspace.waitSelectedFiltersSuggestion(JAVA_SUGGESTION_TITLE);
    newWorkspace.chooseFilterSuggestionByPlusButton(JAVA_SUGGESTION_TITLE);
    newWorkspace.waitFiltersInputTags(asList(JAVA_SUGGESTION_TITLE));
    newWorkspace.clickOnInputFieldTag(JAVA_SUGGESTION_TITLE);
    seleniumWebDriverHelper.sendKeys(Keys.DELETE.toString());
    newWorkspace.waitFiltersInputIsEmpty();
    newWorkspace.deleteLastTagFromInputTagsField();

    // navigation by "Tab" button
    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitSelectedFiltersSuggestion(JAVA_SUGGESTION_TITLE);
    seleniumWebDriverHelper.sendKeys(Keys.TAB.toString());
    newWorkspace.waitSelectedFiltersSuggestion(JDK_SUGGESTION_TITLE);
    seleniumWebDriverHelper.sendKeys(Keys.ENTER.toString());
    newWorkspace.waitFiltersInputTags(asList(JDK_SUGGESTION_TITLE));
    newWorkspace.clickOnTitlePlaceCoordinate();
    newWorkspace.waitFiltersFormClosed();

    newWorkspace.getAvailableStacks();
    newWorkspace.waitStacks(EXPECTED_JDK_STACKS);

    newWorkspace.clickOnFiltersButton();
    newWorkspace.waitFiltersFormOpened();
    newWorkspace.waitFiltersInputTags(asList(JDK_SUGGESTION_TITLE));
    newWorkspace.deleteLastTagFromInputTagsField();
    newWorkspace.waitFiltersInputIsEmpty();
    newWorkspace.clickOnTitlePlaceCoordinate();
    newWorkspace.waitFiltersFormClosed();
    newWorkspace.waitStacks(expectedQuickStartStacks);
  }

  private void checkSearchField(
      List<NewWorkspace.Stack> expectedJavaStacks,
      List<NewWorkspace.Stack> expectedQuickStartStacks) {
    newWorkspace.waitPageLoad();

    newWorkspace.typeToSearchInput("Java");
    newWorkspace.waitStacks(expectedJavaStacks);

    newWorkspace.typeToSearchInput("");
    newWorkspace.waitStacks(expectedQuickStartStacks);

    newWorkspace.typeToSearchInput("java");
    newWorkspace.waitStacks(expectedJavaStacks);

    newWorkspace.typeToSearchInput("");
    newWorkspace.waitStacks(expectedQuickStartStacks);

    newWorkspace.typeToSearchInput("JAVA");
    newWorkspace.waitStacks(expectedJavaStacks);

    newWorkspace.typeToSearchInput("");
    newWorkspace.waitStacks(expectedQuickStartStacks);
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
