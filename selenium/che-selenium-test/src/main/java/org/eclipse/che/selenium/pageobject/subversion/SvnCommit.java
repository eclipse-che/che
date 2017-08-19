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
package org.eclipse.che.selenium.pageobject.subversion;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
@Singleton
public class SvnCommit {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public SvnCommit(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  interface Locators {
    String COMMIT_MAIN_FORM = "//div[text()='Commit...']/ancestor::div[3]";
    String TEXT_AREA_COMMIT_MSG = "//textarea[@placeholder='Commit message...']";
    String DIFF_BUTTON = "//td[span[text()='%s'] and span[text()='(%s)']]/following::td[1]";
    String TEXT_AREA_DIFF_VIEW = "//button[@id='svn-diff-view-close']/preceding::iframe[1]";
    String DIFF_VIEW_CLOSE_BUTTON = "svn-diff-view-close";
    String COMMIT_BUTTON = "svn-commit-commit";
    String CANCEL_BUTTON = "svn-commit-cancel";
  }

  @FindBy(xpath = Locators.COMMIT_MAIN_FORM)
  WebElement mainFormCommit;

  @FindBy(xpath = Locators.TEXT_AREA_COMMIT_MSG)
  WebElement textAreaCommitMess;

  @FindBy(xpath = Locators.TEXT_AREA_DIFF_VIEW)
  WebElement textAreaDiffView;

  @FindBy(id = Locators.DIFF_VIEW_CLOSE_BUTTON)
  WebElement buttonDiffViewClose;

  @FindBy(id = Locators.COMMIT_BUTTON)
  WebElement buttonCommit;

  @FindBy(id = Locators.CANCEL_BUTTON)
  WebElement buttonCancel;

  public void waitMainFormOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainFormCommit));
  }

  public void waitMainFormClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.COMMIT_MAIN_FORM)));
  }

  public void typeCommitMess(String mess) {
    textAreaCommitMess.clear();
    textAreaCommitMess.sendKeys(mess);
  }

  public void clickDiffButton(String fileName, String folderName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(Locators.DIFF_BUTTON, fileName, folderName))))
        .click();
  }

  /**
   * wait the text into 'Commit...' diff view
   *
   * @param expContent is the expected text
   */
  public void waitTextDiffView(final String expContent) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(Locators.TEXT_AREA_DIFF_VIEW)));
    seleniumWebDriver.switchTo().frame(textAreaDiffView);
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    seleniumWebDriver
                        .findElement(By.tagName("pre"))
                        .getText()
                        .contains(expContent));
    seleniumWebDriver.switchTo().defaultContent();
  }

  public void clickCloseDiffViewBtn() throws InterruptedException {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(buttonDiffViewClose));
    buttonDiffViewClose.click();
    loader.waitOnClosed();
  }

  public void clickCommitBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(buttonCommit));
    buttonCommit.click();
  }

  public void clickCancelBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(buttonCancel));
    buttonCancel.click();
  }
}
