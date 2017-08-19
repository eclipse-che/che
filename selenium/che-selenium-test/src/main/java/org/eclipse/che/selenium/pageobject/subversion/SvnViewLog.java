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
 * @author Anton Korneta
 * @author Andrey Chizhikov
 */
@Singleton
public class SvnViewLog {

  private interface Locators {
    String VIEW_LOG_FORM = "//div[text()='Show Log...']/ancestor::div[3]";
    String MAX_REVISION_SPAN = "//div[text()=' max )']/b/span";
    String CANCEL_BUTTON = "svn-showlogs-cancel";
  }

  @FindBy(xpath = Locators.VIEW_LOG_FORM)
  WebElement viewLogForm;

  @FindBy(xpath = Locators.MAX_REVISION_SPAN)
  WebElement maxRevisionSpan;

  @FindBy(id = Locators.CANCEL_BUTTON)
  WebElement cancelButton;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public SvnViewLog(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitSvnLogFormOpened() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(viewLogForm));
  }

  public void waitSvnLogFormClosed() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.VIEW_LOG_FORM)));
  }

  public void clickSvnLogFormCancel() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(cancelButton));
    cancelButton.click();
  }

  public String getLatestRevision() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(maxRevisionSpan));
    return maxRevisionSpan.getText();
  }
}
