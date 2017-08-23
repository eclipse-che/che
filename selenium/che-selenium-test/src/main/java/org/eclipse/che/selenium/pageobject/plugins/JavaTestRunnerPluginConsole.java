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
   * wait single method in the result tree marked as failed (red color)
   *
   * @param nameOfFailedMethods name of that should fail
   */
  public void waitMethodMarkedAsFailed(String nameOfFailedMethods) {
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(ExpectedConditions.textToBePresentInElement(failedMethod, nameOfFailedMethods));
  }

  /**
   * wait single method in the result tree marked as failed (red color)
   *
   * @param nameOfFailedMethods name of that should fail
   */
  public void waitMethodMarkedAsIgnored(String nameOfFailedMethods) {
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(ExpectedConditions.textToBePresentInElement(ignoredMethod, nameOfFailedMethods));
  }

  /**
   * wait single method in the result tree marked as passed (green color)
   *
   * @param nameOfFailedMethods name of expected method
   */
  public void waitMethodMarkedAsPassed(String nameOfFailedMethods) {
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(ExpectedConditions.textToBePresentInElement(passedMethod, nameOfFailedMethods));
  }

  /**
   * wait the FQN of the test class in result tree class that has been launched
   *
   * @param fqn
   */
  public void waitFqnOfTesClassInResultTree(String fqn) {
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath(String.format(TEST_RESULT_TREE_XPATH_TEMPLATE, fqn))));
  }

  /**
   * get all name of the test methods form the test result tree marked with defined status (may be
   * passed, failed or ignored)
   *
   * @param methodState the enumeration with defined status
   * @return the list with names of methods with defined status
   */
  public List<String> getAllMethodsMarkedDefinedStatus(JunitMethodsState methodState) {
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

  private List<String> getAllMetodsWithDefinedStatus(List<WebElement> definedMethod) {
    return new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(ExpectedConditions.visibilityOfAllElements(definedMethod))
        .stream()
        .map(WebElement::getText)
        .collect(Collectors.toList());
  }
}
