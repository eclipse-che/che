/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.selenium.pageobject.plugins;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MINIMUM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

/**
 * @author Dmytro Nochevnov
 */
@Singleton
public class JavaTestRunnerPluginConsole extends Consoles {

    interface Locators {
        String TOOLBAR_JUNIT_TITLE_XPATH_TEMPLATE = "//div[@role='toolbar-header']//div[text()='%s']";
        String TEST_RESULT_SUMMARY_XPATH_TEMPLATE = "//div[@id[contains(.,'gwt-uid')]]//span[text()='%s']";
        String TEST_RESULT_TREE_XPATH_TEMPLATE    = "//div[@style='undefined' and text()='%s']";
        String TEST_OUTPUT_XPATH                  = "//div[@id='gwt-debug-test-runner-output']";
    }

    @FindBy(xpath = Locators.TEST_OUTPUT_XPATH)
    private WebElement testOutput;

    @Inject
    public JavaTestRunnerPluginConsole(SeleniumWebDriver seleniumWebDriver, Loader loader) {
        super(seleniumWebDriver, loader);
        PageFactory.initElements(seleniumWebDriver, this);
    }

    public void waitTitlePresents(String title) {
        String locator = String.format(Locators.TOOLBAR_JUNIT_TITLE_XPATH_TEMPLATE, title);
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
    }

    /**
     * @return Stack trace displayed in the right test result panel.
     * @param item
     *         item of tree in left test result panel
     */
    public String getTestErrorMessage(String item) {
        clickOnTestResultItem(item);

        return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                .until(ExpectedConditions.visibilityOf(testOutput))
                .getText();
    }

    /**
     * Wait on certain summery, test class and method in tree in the left panel.
     * @param summary
     *         the test result summary at the top of the tree
     * @param className
     *         name of test class
     * @param methodName
     *         name of test method
     */
    public void waitTestResultsInTree(String summary, String className, String methodName) {
        waitSummaryInTree(summary);
        waitItemIsPresentInTree(className);
        waitItemIsPresentInTree(methodName);
    }

    /**
     * Wait until summary appears above the tree.
     *
     * @param summary
     */
    private void waitSummaryInTree(String summary) {
        String locator = String.format(Locators.TEST_RESULT_SUMMARY_XPATH_TEMPLATE, summary);
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
    }

    private void waitItemIsPresentInTree(String item) {
        String itemLocator = String.format(Locators.TEST_RESULT_TREE_XPATH_TEMPLATE, item);
        new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(itemLocator)));
    }

    private void clickOnTestResultItem(String item) {
        String locator = String.format(Locators.TEST_RESULT_TREE_XPATH_TEMPLATE, item);
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator))).click();
    }

}
