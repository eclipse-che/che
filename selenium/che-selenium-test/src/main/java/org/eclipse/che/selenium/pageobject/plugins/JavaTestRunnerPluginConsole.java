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
package org.eclipse.che.selenium.pageobject.plugins;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MINIMUM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Dmytro Nochevnov */
@Singleton
public class JavaTestRunnerPluginConsole extends Consoles {

  private static final String TEST_RESULT_TREE_XPATH_TEMPLATE =
      "//div[contains(@id,'gwt-uid')]//div[text()='%s']";
  private static final String TEST_OUTPUT_XPATH =
      "//div[@focused]//div[@id='gwt-debug-commandConsoleLines']//pre";
  private static final String METHODS_MARKED_AS_PASSED =
      "//div[contains(@id,'gwt-uid')]//div[@style='color: green;']";
  private static final String METHODS_MARKED_AS_FAILED =
      "//div[contains(@id,'gwt-uid')]//div[@style='color: red;']";
  private static final String METHODS_MARKED_AS_IGNORED =
      "//div[contains(@id,'gwt-uid')]//div[@style='text-decoration: line-through; color: yellow;']";

  private static final String TEST_RESULT_NAVIGATION_TREE = "gwt-debug-test-tree-navigation-panel";

  @FindAll({@FindBy(xpath = TEST_OUTPUT_XPATH)})
  private List<WebElement> testOutput;

  @FindBy(xpath = METHODS_MARKED_AS_PASSED)
  private WebElement passedMethod;

  @FindBy(xpath = METHODS_MARKED_AS_IGNORED)
  private WebElement ignoredMethod;

  @FindBy(xpath = METHODS_MARKED_AS_FAILED)
  private WebElement failedMethod;

  @FindAll({@FindBy(xpath = METHODS_MARKED_AS_FAILED)})
  private List<WebElement> failedMethods;

  @FindAll({@FindBy(xpath = METHODS_MARKED_AS_PASSED)})
  private List<WebElement> passedMethods;

  @FindAll({@FindBy(xpath = METHODS_MARKED_AS_IGNORED)})
  private List<WebElement> ignoredMethods;

  @FindBy(id = TEST_RESULT_NAVIGATION_TREE)
  private WebElement resultTreeMainForm;

  @Inject
  public JavaTestRunnerPluginConsole(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    super(seleniumWebDriver, loader);
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
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(ExpectedConditions.textToBePresentInElement(failedMethod, nameOfFailedMethods));
  }

  /**
   * Wait single method in the result tree marked as failed (red color).
   *
   * @param nameOfFailedMethods name of that should fail
   */
  public void waitMethodMarkedAsIgnored(String nameOfFailedMethods) {
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(ExpectedConditions.textToBePresentInElement(ignoredMethod, nameOfFailedMethods));
  }

  /**
   * Wait single method in the result tree marked as passed (green color).
   *
   * @param nameOfFailedMethods name of expected method
   */
  public void waitMethodMarkedAsPassed(String nameOfFailedMethods) {
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(ExpectedConditions.textToBePresentInElement(passedMethod, nameOfFailedMethods));
  }

  /**
   * Wait the FQN of the test class in result tree class that has been launched.
   *
   * @param fqn
   */
  public void waitFqnOfTesClassInResultTree(String fqn) {
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
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
        definedMethods = getAllMetodsWithDefinedStatus(passedMethods);
        break;
      case FAILED:
        definedMethods = getAllMetodsWithDefinedStatus(failedMethods);
        break;
      case IGNORED:
        definedMethods = getAllMetodsWithDefinedStatus(ignoredMethods);
        break;
    }
    return definedMethods;
  }

  /**
   * Get all defined methods from result tree and return as list WebElements.
   *
   * @param methodState the enumeration with defined status
   * @return List WebElements with defined status
   */
  public List<WebElement> getAllMethodsMarkedDefinedStatus(JunitMethodsState methodState) {
    List<WebElement> definedMethods = null;
    switch (methodState) {
      case PASSED:
        definedMethods = passedMethods;
        break;
      case FAILED:
        definedMethods = failedMethods;
        break;
      case IGNORED:
        definedMethods = ignoredMethods;
        break;
    }
    return definedMethods;
  }

  private List<String> getAllMetodsWithDefinedStatus(List<WebElement> definedMethod) {
    return new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(ExpectedConditions.visibilityOfAllElements(definedMethod))
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
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(ExpectedConditions.visibilityOf(resultTreeMainForm))
        .findElement(By.xpath(String.format("//div[text()='%s']", item)))
        .click();
  }

  /**
   * Click on faled, passed or ignored method on the result tree.
   *
   * @param nameOfMethod
   * @param state
   */
  public void selectMethodWithDefinedStatus(JunitMethodsState state, String nameOfMethod) {
    getAllMethodsMarkedDefinedStatus(state)
        .stream()
        .filter(webElement -> Objects.equals(webElement.getText(), nameOfMethod))
        .findFirst()
        .get()
        .click();
  }
}
