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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ANDROID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.BLANK;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CAMEL_SPRINGBOOT;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CAMEL_SPRINGBOOT_CHE7;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CENTOS_BLANK;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CENTOS_GO;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CENTOS_NODEJS;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CENTOS_WILDFLY_SWARM;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CEYLON_WITH_JAVA_JAVASCRIPT;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CHE_7_PREVIEW;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CHE_7_PREVIEW_DEV;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CHE_7_THEIA_DEV;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CPP;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.DOT_NET;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.DOT_NET_DEFAULT;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ECLIPSE_CHE;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ECLIPSE_VERTX;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.GO;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.GO_DEFAULT;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_CENTOS;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_GRADLE;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MAVEN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MYSQL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MYSQL_CENTOS;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MYSQL_THEIA_ON_KUBERNETES;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_THEIA_DOCKER;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.KOTLIN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.NODE;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.NODEJS_AND_POSTGRES;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PHP;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PHP_CHE7;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PHP_MYSQL_CHE7;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PYTHON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PYTHON_DEFAULT;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.RAILS;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.SPRING_BOOT;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.ARROW_UP;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Ihor Okhrimenko */
public class NewWorkspacePageTest {
  private static final String EXPECTED_WORKSPACE_NAME_PREFIX = "wksp-";
  private static final String MACHINE_NAME = "dev-machine";
  private static final double MAX_RAM_VALUE = 100.0;
  private static final double MIN_RAM_VALUE = 0.1;
  private static final double RAM_CHANGE_STEP = 0.1;
  private static final String JDK_SUGGESTION_TITLE = "JDK";
  private static final String JAVA_SUGGESTION_TITLE = "JAVA";
  private static final String JAVA_1_8_SUGGESTION_TITLE = "JAVA 1.8";
  private static final String JAVA_TOMCAT_MYSQL_SUGGESTION_TITLE = "JAVA 1.8, TOMCAT 8, MYSQL 5.7";
  private static final String NAME_WITH_ONE_HUNDRED_SYMBOLS = generate("wksp-", 95);
  private static final List<String> NOT_VALID_NAMES =
      asList("wksp-", "-wksp", "wk sp", "wk_sp", "wksp@", "wksp$", "wksp&", "wksp*");
  private static final String LETTER_FOR_SEARCHING = "j";
  private static final List<NewWorkspace.Stack> EXPECTED_JDK_STACKS = asList(JAVA, ECLIPSE_CHE);

  private static List<NewWorkspace.Stack> EXPECTED_OPENSHIFT_QUICK_START_STACKS =
      asList(
          BLANK,
          JAVA,
          DOT_NET_DEFAULT,
          DOT_NET,
          ANDROID,
          CAMEL_SPRINGBOOT_CHE7,
          CPP,
          CHE_7_PREVIEW,
          CHE_7_PREVIEW_DEV,
          CHE_7_THEIA_DEV,
          ECLIPSE_CHE,
          GO_DEFAULT,
          GO,
          JAVA_MYSQL_THEIA_ON_KUBERNETES,
          JAVA_GRADLE,
          JAVA_MAVEN,
          NODE,
          PHP,
          PHP_MYSQL_CHE7,
          PHP_CHE7,
          PYTHON_DEFAULT,
          PYTHON,
          RAILS);

  private static List<NewWorkspace.Stack> EXPECTED_K8S_QUICK_START_STACKS =
      asList(
          BLANK,
          JAVA,
          DOT_NET_DEFAULT,
          DOT_NET,
          ANDROID,
          CAMEL_SPRINGBOOT_CHE7,
          CPP,
          CHE_7_PREVIEW,
          CHE_7_PREVIEW_DEV,
          CHE_7_THEIA_DEV,
          ECLIPSE_CHE,
          GO_DEFAULT,
          GO,
          JAVA_MYSQL_THEIA_ON_KUBERNETES,
          JAVA_GRADLE,
          JAVA_MAVEN,
          NODE,
          PHP,
          PHP_CHE7,
          PHP_MYSQL_CHE7,
          PYTHON_DEFAULT,
          PYTHON,
          RAILS);

  private static final List<NewWorkspace.Stack> EXPECTED_DOCKER_QUICK_START_STACKS =
      asList(
          BLANK,
          JAVA,
          JAVA_MYSQL,
          DOT_NET_DEFAULT,
          DOT_NET,
          ANDROID,
          CAMEL_SPRINGBOOT_CHE7,
          CPP,
          ECLIPSE_CHE,
          GO_DEFAULT,
          GO,
          JAVA_GRADLE,
          JAVA_MAVEN,
          NODE,
          PHP,
          PHP_CHE7,
          PHP_MYSQL_CHE7,
          PYTHON_DEFAULT,
          PYTHON,
          RAILS,
          JAVA_THEIA_DOCKER);

  private static List<NewWorkspace.Stack> EXPECTED_OPENSHIFT_SINGLE_MACHINE_STACKS =
      asList(
          BLANK,
          JAVA,
          DOT_NET_DEFAULT,
          DOT_NET,
          ANDROID,
          CAMEL_SPRINGBOOT_CHE7,
          CAMEL_SPRINGBOOT,
          CPP,
          CENTOS_BLANK,
          CENTOS_GO,
          CENTOS_NODEJS,
          CENTOS_WILDFLY_SWARM,
          CEYLON_WITH_JAVA_JAVASCRIPT,
          CHE_7_PREVIEW,
          CHE_7_PREVIEW_DEV,
          CHE_7_THEIA_DEV,
          ECLIPSE_CHE,
          ECLIPSE_VERTX,
          GO_DEFAULT,
          GO,
          JAVA_CENTOS,
          JAVA_GRADLE,
          JAVA_MAVEN,
          KOTLIN,
          NODE,
          PHP,
          PHP_CHE7,
          PYTHON_DEFAULT,
          PYTHON,
          RAILS,
          SPRING_BOOT);

  private static List<NewWorkspace.Stack> EXPECTED_K8S_SINGLE_MACHINE_STACKS =
      asList(
          BLANK,
          JAVA,
          DOT_NET_DEFAULT,
          DOT_NET,
          ANDROID,
          CAMEL_SPRINGBOOT_CHE7,
          CAMEL_SPRINGBOOT,
          CPP,
          CENTOS_BLANK,
          CENTOS_GO,
          CENTOS_NODEJS,
          CENTOS_WILDFLY_SWARM,
          CEYLON_WITH_JAVA_JAVASCRIPT,
          CHE_7_PREVIEW,
          CHE_7_PREVIEW_DEV,
          CHE_7_THEIA_DEV,
          ECLIPSE_CHE,
          ECLIPSE_VERTX,
          GO_DEFAULT,
          GO,
          JAVA_CENTOS,
          JAVA_GRADLE,
          JAVA_MAVEN,
          KOTLIN,
          NODE,
          PHP,
          PHP_CHE7,
          PHP_MYSQL_CHE7,
          PYTHON_DEFAULT,
          PYTHON,
          RAILS,
          SPRING_BOOT);

  private static List<NewWorkspace.Stack> EXPECTED_DOCKER_SINGLE_MACHINE_STACKS =
      asList(
          BLANK,
          JAVA,
          DOT_NET_DEFAULT,
          DOT_NET,
          ANDROID,
          CAMEL_SPRINGBOOT_CHE7,
          CAMEL_SPRINGBOOT,
          CPP,
          CENTOS_BLANK,
          CENTOS_GO,
          CENTOS_NODEJS,
          CENTOS_WILDFLY_SWARM,
          CEYLON_WITH_JAVA_JAVASCRIPT,
          ECLIPSE_CHE,
          ECLIPSE_VERTX,
          GO_DEFAULT,
          GO,
          JAVA_CENTOS,
          JAVA_GRADLE,
          JAVA_MAVEN,
          KOTLIN,
          NODE,
          PHP,
          PHP_CHE7,
          PYTHON_DEFAULT,
          PYTHON,
          RAILS,
          SPRING_BOOT);

  private static final List<NewWorkspace.Stack> EXPECTED_OPENSHIFT_MULTI_MACHINE_STACKS =
      asList(JAVA_MYSQL_THEIA_ON_KUBERNETES, NODEJS_AND_POSTGRES, PHP_MYSQL_CHE7);

  private static final List<NewWorkspace.Stack> EXPECTED_K8S_MULTI_MACHINE_STACKS =
      asList(JAVA_MYSQL_THEIA_ON_KUBERNETES, NODEJS_AND_POSTGRES);

  private static final List<NewWorkspace.Stack> EXPECTED_DOCKER_MULTI_MACHINE_STACKS =
      asList(JAVA_MYSQL, JAVA_MYSQL_CENTOS, JAVA_THEIA_DOCKER);

  private static final List<NewWorkspace.Stack>
      EXPECTED_OPENSHIFT_QUICK_START_STACKS_REVERSE_ORDER =
          asList(
              JAVA,
              BLANK,
              RAILS,
              PYTHON,
              PYTHON_DEFAULT,
              PHP_CHE7,
              PHP_MYSQL_CHE7,
              PHP,
              NODE,
              JAVA_MAVEN,
              JAVA_GRADLE,
              JAVA_MYSQL_THEIA_ON_KUBERNETES,
              GO,
              GO_DEFAULT,
              ECLIPSE_CHE,
              CHE_7_THEIA_DEV,
              CHE_7_PREVIEW_DEV,
              CHE_7_PREVIEW,
              CPP,
              CAMEL_SPRINGBOOT_CHE7,
              ANDROID,
              DOT_NET,
              DOT_NET_DEFAULT);

  private static final List<NewWorkspace.Stack> EXPECTED_K8S_QUICK_START_STACKS_REVERSE_ORDER =
      asList(
          JAVA,
          BLANK,
          RAILS,
          PYTHON,
          PYTHON_DEFAULT,
          PHP,
          PHP_CHE7,
          PHP_MYSQL_CHE7,
          NODE,
          JAVA_MAVEN,
          JAVA_GRADLE,
          JAVA_MYSQL_THEIA_ON_KUBERNETES,
          GO,
          GO_DEFAULT,
          ECLIPSE_CHE,
          CHE_7_THEIA_DEV,
          CHE_7_PREVIEW_DEV,
          CHE_7_PREVIEW,
          CPP,
          CAMEL_SPRINGBOOT_CHE7,
          ANDROID,
          DOT_NET,
          DOT_NET_DEFAULT);

  private static final List<NewWorkspace.Stack> EXPECTED_DOCKER_QUICK_START_STACKS_REVERSE_ORDER =
      asList(
          JAVA_MYSQL,
          JAVA,
          BLANK,
          JAVA_THEIA_DOCKER,
          RAILS,
          PYTHON,
          PYTHON_DEFAULT,
          PHP_CHE7,
          PHP_MYSQL_CHE7,
          PHP,
          NODE,
          JAVA_MAVEN,
          JAVA_GRADLE,
          GO,
          GO_DEFAULT,
          ECLIPSE_CHE,
          CPP,
          CAMEL_SPRINGBOOT_CHE7,
          ANDROID,
          DOT_NET,
          DOT_NET_DEFAULT);

  private static final List<NewWorkspace.Stack> EXPECTED_OPENSHIFT_JAVA_STACKS =
      asList(JAVA, ANDROID, ECLIPSE_CHE, JAVA_MYSQL_THEIA_ON_KUBERNETES);

  private static final List<NewWorkspace.Stack> EXPECTED_DOCKER_JAVA_STACKS =
      asList(JAVA, JAVA_MYSQL, ECLIPSE_CHE, ANDROID);

  private static final List<String> EXPECTED_OPENSHIFT_FILTERS_SUGGESTIONS =
      asList(JAVA_SUGGESTION_TITLE, JDK_SUGGESTION_TITLE, JAVA_1_8_SUGGESTION_TITLE);

  private static final List<String> EXPECTED_K8S_FILTERS_SUGGESTIONS =
      asList(JAVA_SUGGESTION_TITLE, JDK_SUGGESTION_TITLE);

  private static final List<String> EXPECTED_DOCKER_FILTERS_SUGGESTIONS =
      asList(JAVA_SUGGESTION_TITLE, JDK_SUGGESTION_TITLE, JAVA_TOMCAT_MYSQL_SUGGESTION_TITLE);

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

  @Test(groups = {TestGroup.OPENSHIFT})
  public void checkOpenshiftStackButtons() {
    checkStackButtons(
        EXPECTED_OPENSHIFT_QUICK_START_STACKS,
        EXPECTED_OPENSHIFT_SINGLE_MACHINE_STACKS,
        EXPECTED_OPENSHIFT_MULTI_MACHINE_STACKS,
        EXPECTED_OPENSHIFT_QUICK_START_STACKS_REVERSE_ORDER);
  }

  @Test(groups = {TestGroup.K8S})
  public void checkK8SStackButtons() {
    checkStackButtons(
        EXPECTED_K8S_QUICK_START_STACKS,
        EXPECTED_K8S_SINGLE_MACHINE_STACKS,
        EXPECTED_K8S_MULTI_MACHINE_STACKS,
        EXPECTED_K8S_QUICK_START_STACKS_REVERSE_ORDER);
  }

  @Test(groups = TestGroup.DOCKER)
  public void checkDockerStackButtons() {
    checkStackButtons(
        EXPECTED_DOCKER_QUICK_START_STACKS,
        EXPECTED_DOCKER_SINGLE_MACHINE_STACKS,
        EXPECTED_DOCKER_MULTI_MACHINE_STACKS,
        EXPECTED_DOCKER_QUICK_START_STACKS_REVERSE_ORDER);
  }

  @Test(groups = {TestGroup.OPENSHIFT})
  public void checkOpenshiftFiltersButton() {
    checkFiltersButton(
        EXPECTED_OPENSHIFT_FILTERS_SUGGESTIONS, EXPECTED_OPENSHIFT_QUICK_START_STACKS);
  }

  @Test(groups = {TestGroup.K8S})
  public void checkK8SFiltersButton() {
    checkFiltersButton(EXPECTED_K8S_FILTERS_SUGGESTIONS, EXPECTED_K8S_QUICK_START_STACKS);
  }

  @Test(groups = TestGroup.DOCKER)
  public void checkDockerFiltersButton() {
    checkFiltersButton(EXPECTED_DOCKER_FILTERS_SUGGESTIONS, EXPECTED_DOCKER_QUICK_START_STACKS);
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

    // close form by "Cancel" button
    newWorkspace.clickOnAddStackButton();
    newWorkspace.waitCreateStackDialog();
    newWorkspace.clickOnNoButtonInCreateStackDialog();
    newWorkspace.waitCreateStackDialogClosing();
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
      List<NewWorkspace.Stack> expectedQuickStartStacksReverseOrder) {

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
    newWorkspace.waitStacksOrder(expectedQuickStartStacks);
    newWorkspace.clickNameButton();
    newWorkspace.waitStacksOrder(expectedQuickStartStacksReverseOrder);

    newWorkspace.clickNameButton();
    newWorkspace.waitStacksOrder(expectedQuickStartStacks);
  }

  private void checkFiltersButton(
      List<String> expectedSuggestions, List<NewWorkspace.Stack> expectedQuickStartStacks) {
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
    newWorkspace.waitFiltersSuggestionsNames(expectedSuggestions);

    assertEquals(
        newWorkspace.getSelectedFiltersSuggestionName(),
        newWorkspace.getFiltersSuggestionsNames().get(0));

    // check navigation by keyboard arrows between suggested tags
    seleniumWebDriverHelper.sendKeys(ARROW_DOWN.toString());
    newWorkspace.waitSelectedFiltersSuggestion(JDK_SUGGESTION_TITLE);

    seleniumWebDriverHelper.sendKeys(ARROW_UP.toString());
    newWorkspace.waitSelectedFiltersSuggestion(JAVA_SUGGESTION_TITLE);

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
    newWorkspace.waitFiltersSuggestionsNames(expectedSuggestions);

    newWorkspace.waitSelectedFiltersSuggestion(JAVA_SUGGESTION_TITLE);
    newWorkspace.doubleClickOnFiltersSuggestion(JAVA_SUGGESTION_TITLE);
    newWorkspace.waitFiltersInputTags(asList(JAVA_SUGGESTION_TITLE));
    newWorkspace.deleteTagByRemoveButton(JAVA_SUGGESTION_TITLE);
    newWorkspace.waitFiltersInputIsEmpty();

    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitFiltersSuggestionsNames(expectedSuggestions);
    newWorkspace.waitSelectedFiltersSuggestion(JAVA_SUGGESTION_TITLE);
    newWorkspace.chooseFilterSuggestionByPlusButton(JDK_SUGGESTION_TITLE);
    newWorkspace.waitFiltersInputTags(asList(JDK_SUGGESTION_TITLE));
    newWorkspace.clickOnInputFieldTag(JDK_SUGGESTION_TITLE);
    seleniumWebDriverHelper.sendKeys(Keys.DELETE.toString());
    newWorkspace.waitFiltersInputIsEmpty();

    newWorkspace.typeToFiltersInput(LETTER_FOR_SEARCHING);
    newWorkspace.waitFiltersSuggestionsNames(expectedSuggestions);
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
