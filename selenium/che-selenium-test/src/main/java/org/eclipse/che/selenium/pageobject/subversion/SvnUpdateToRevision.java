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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
@Singleton
public class SvnUpdateToRevision {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public SvnUpdateToRevision(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  interface Locators {
    String UPDATE_TO_REVISION_FORM = "gwt-debug-svn-update-to-revision-window";
    String REVISION_RADIO_BUTTON = "//label[text()='Revision']";
    String REVISION_TEXT_BOX = "//label[text()='Revision']/following::input";
    String UPDATE_BUTTON = "svn-update-update";
    String CANCEL_BUTTON = "svn-update-cancel";
  }

  @FindBy(id = Locators.UPDATE_TO_REVISION_FORM)
  WebElement mainFormUpdateRevision;

  @FindBy(xpath = Locators.REVISION_RADIO_BUTTON)
  WebElement buttonRevision;

  @FindBy(xpath = Locators.REVISION_TEXT_BOX)
  WebElement textBoxRevision;

  @FindBy(id = Locators.UPDATE_BUTTON)
  WebElement buttonUpdate;

  public void waitMainFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainFormUpdateRevision));
  }

  public void waitMainFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.id(Locators.UPDATE_TO_REVISION_FORM)));
  }

  public void selectRevision() {
    buttonRevision.click();
  }

  public void typeTextRevision(String text) {
    textBoxRevision.clear();
    textBoxRevision.sendKeys(text);
  }

  public void clickOnUpdateBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(buttonUpdate));
    buttonUpdate.click();
  }
}
