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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Anton Korneta
 * @author Andrey Chizhikov
 */
@Singleton
public class SvnResolve {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public SvnResolve(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String RESOLVE_FORM = "//div[text()='Resolve...']/ancestor::div[3]";
    String RESOLVE_TYPE_SELECT = "//*[text()='tags/file.txt']/following::select[1]";
    String RESOLVE_BUTTON = "svn-resolve-resolve";
    String CANCEL_BUTTON = "svn-resolve-cancel";
  }

  @FindBy(xpath = Locators.RESOLVE_FORM)
  WebElement resolveForm;

  @FindBy(xpath = Locators.RESOLVE_TYPE_SELECT)
  WebElement resolveType;

  @FindBy(id = Locators.RESOLVE_BUTTON)
  WebElement resolve;

  @FindBy(id = Locators.CANCEL_BUTTON)
  WebElement cancel;

  public void waitResolveFormOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(resolveForm));
  }

  public void waitResolveFormClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.RESOLVE_FORM)));
  }

  public void selectResolveType(String type) {
    new Select(resolveType).selectByVisibleText(type);
  }

  public void clickResolve() {
    resolve.click();
  }
}
