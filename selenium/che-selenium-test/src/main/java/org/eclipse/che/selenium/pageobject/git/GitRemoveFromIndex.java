/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.git;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Kuznetsov Mihail */
@Singleton
public class GitRemoveFromIndex {
  interface Locators {
    String REMOVE_BTN_ID = "git-removeFromIndex-remove";
    String REMOVE_TO_INDEX_FORM = "gwt-debug-git-removeFromIndex-window";
    String ONLY_INDEX_CHKBOX_ID = "gwt-debug-git-removeFromIndex-removeFromIndexOnly";
  }

  @FindBy(id = Locators.REMOVE_TO_INDEX_FORM)
  WebElement form;

  @FindBy(id = Locators.REMOVE_BTN_ID)
  WebElement removeBtn;

  @FindBy(id = Locators.ONLY_INDEX_CHKBOX_ID)
  WebElement onlyIndexChkbox;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitRemoveFromIndex(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /**
   * Get name of file to be added to index
   *
   * @param text
   */
  public void waitFileNameToRemove(final String text) {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver driver) {
                return form.getText().contains(text);
              }
            });
  }

  public void waitFormToOpen() {
    new WebDriverWait(seleniumWebDriver, 7).until(ExpectedConditions.visibilityOf(form));
  }

  public void waitFormToClose() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.REMOVE_TO_INDEX_FORM)));
  }

  /** Click add button */
  public void clickRemoveBtn() {
    removeBtn.click();
  }

  public void setRemoveOnlyFromIndexCheckBox() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.elementToBeClickable(onlyIndexChkbox))
        .click();
  }
}
