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
package org.eclipse.che.selenium.pageobject.plugins;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Dmytro Nochevnov */
@Singleton
public class JavaTestRunnerPluginConsole extends Consoles {

  private static final String TEST_RESULT_TREE_XPATH_TEMPLATE =
      "//div[contains(@id,'gwt-uid')]//div[text()='%s']";
  private static final String TEST_OUTPUT_XPATH =
      "//div[@focused]//div[@id='gwt-debug-commandConsoleLines']//pre";
  private static final String METHODS_MARKED_AS_PASSED = "gwt-debug-test-state-passed";
  private static final String METHODS_MARKED_AS_FAILED = "gwt-debug-test-state-failed";
  private static final String METHODS_MARKED_AS_IGNORED = "gwt-debug-test-state-ignore";
  private static final String TEST_RESULT_NAVIGATION_TREE = "gwt-debug-test-tree-navigation-panel";

  @FindAll({@FindBy(xpath = TEST_OUTPUT_XPATH)})
  private List<WebElement> testOutput;

  @FindBy(id = METHODS_MARKED_AS_PASSED)
  private WebElement passedMethod;

  @FindBy(id = METHODS_MARKED_AS_IGNORED)
  private WebElement ignoredMethod;

  @FindBy(id = METHODS_MARKED_AS_FAILED)
  private WebElement failedMethod;

  @FindAll({@FindBy(id = METHODS_MARKED_AS_FAILED)})
  private List<WebElement> failedMethods;

  @FindAll({@FindBy(id = METHODS_MARKED_AS_PASSED)})
  private List<WebElement> passedMethods;

  @FindAll({@FindBy(id = METHODS_MARKED_AS_IGNORED)})
  private List<WebElement> ignoredMethods;

  @FindBy(id = TEST_RESULT_NAVIGATION_TREE)
  private WebElement resultTreeMainForm;

  @Inject
  public JavaTestRunnerPluginConsole(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    super(seleniumWebDriver, loader, actionsFactory);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public enum JunitMethodsState {
    PASSED,
    FAILED,
    IGNORED
  }

  /** @return Stack trace displayed in the right test result panel. */
  public String getTestErrorMessage() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfAllElements(testOutput))
        .stream()
        .map(WebElement::getText)
        .collect(Collectors.joining());
  }

  /**
   * Wait single method in the result tree marked as failed (red color).
   *
   * @param nameOfFailedMethods name of that should fail
   */
  public void waitMethodMarkedAsFailed(String nameOfFailedMethods) {
    FluentWait<WebDriver> wait =
        new FluentWait<WebDriver>(seleniumWebDriver)
            .withTimeout(ATTACHING_ELEM_TO_DOM_SEC, TimeUnit.SECONDS)
            .pollingEvery(200, TimeUnit.MILLISECONDS)
            .ignoring(NotFoundException.class, StaleElementReferenceException.class);
    String xpathToExpectedMethod = "//span[@id='%s']/following-sibling::*/div[text()='%s']";
    wait.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath(
                String.format(
                    xpathToExpectedMethod, METHODS_MARKED_AS_FAILED, nameOfFailedMethods))));
  }

  /**
   * Wait single method in the result tree marked as failed (red color).
   *
   * @param nameOfFailedMethods name of that should fail
   */
  public void waitMethodMarkedAsIgnored(String nameOfFailedMethods) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.id(METHODS_MARKED_AS_IGNORED)))
        .findElement(By.xpath(String.format("//div[text()='%s']", nameOfFailedMethods)));
  }

  /**
   * Wait single method in the result tree marked as passed (green color).
   *
   * @param nameOfFailedMethods name of expected method
   */
  public void waitMethodMarkedAsPassed(String nameOfFailedMethods) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.id(METHODS_MARKED_AS_PASSED)))
        .findElement(By.xpath(String.format("//div[text()='%s']", nameOfFailedMethods)));
  }

  /**
   * Wait the FQN of the test class in result tree class that has been launched.
   *
   * @param fqn
   */
  public void waitFqnOfTesClassInResultTree(String fqn) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(TEST_RESULT_TREE_XPATH_TEMPLATE, fqn))));
  }

  /**
   * Get all name of the test methods form the test result tree marked with defined status (may be
   * passed, failed or ignored).
   *
   * @param methodState the enumeration with defined status
   * @return the list with names of methods with defined status
   */
  public List<String> getAllNamesOfMethodsMarkedDefinedStatus(JunitMethodsState methodState) {
    List<String> definedMethods = null;
    switch (methodState) {
      case PASSED:
        definedMethods = getNamesOfMethodsWithDefinedStatus(METHODS_MARKED_AS_PASSED);
        break;
      case FAILED:
        definedMethods = getNamesOfMethodsWithDefinedStatus(METHODS_MARKED_AS_FAILED);
        break;
      case IGNORED:
        definedMethods = getNamesOfMethodsWithDefinedStatus(METHODS_MARKED_AS_IGNORED);
        break;
    }
    return definedMethods;
  }

  private List<String> getNamesOfMethodsWithDefinedStatus(String definedMethod) {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id(definedMethod)))
        .stream()
        .map(WebElement::getText)
        .collect(Collectors.toList());
  }

  /**
   * Get text from the test result tree. Mote! This method represent only text from test result tree
   * without styles and formatting
   *
   * @return text representation of results of the test result tree widget
   */
  public String getTextFromResultTree() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(resultTreeMainForm))
        .getText();
  }

  /**
   * Click on the item in the result tree. If will be some items with the same name - will select
   * first.
   *
   * @param item name of the item (method or fqn of test class) in the test result tree
   */
  public void selectItemInResultTree(String item) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(resultTreeMainForm))
        .findElement(By.xpath(String.format("//div[text()='%s']", item)))
        .click();
  }

  /**
   * Click on failed, passed or ignored method of the result tree. If in the result tree will be
   * methods with the same statuses and names - will be selected first method in the DOM methods
   * with the same statuses and names - will be selected first method in the DOM
   *
   * @param nameOfMethod
   * @param methodState
   */
  public void selectMethodWithDefinedStatus(JunitMethodsState methodState, String nameOfMethod) {
    WebDriverWait wait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    switch (methodState) {
      case PASSED:
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(METHODS_MARKED_AS_PASSED)))
            .findElement(
                By.xpath(
                    String.format("//div[@style='color: green;' and text()='%s']", nameOfMethod)))
            .click();
        break;
      case FAILED:
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(METHODS_MARKED_AS_FAILED)))
            .findElement(
                By.xpath(
                    String.format("//div[@style='color: red;' and text()='%s']", nameOfMethod)))
            .click();
        break;
      case IGNORED:
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(METHODS_MARKED_AS_IGNORED)))
            .findElement(
                By.xpath(
                    String.format(
                        "//div[@style[contains(.,'color: yellow')] and text()='%s']",
                        nameOfMethod)))
            .click();
        break;
    }
  }
}
