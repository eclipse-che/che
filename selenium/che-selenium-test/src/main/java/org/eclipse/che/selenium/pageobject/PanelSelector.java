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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraiev */
@Singleton
public class PanelSelector {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public PanelSelector(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface PanelTypes {
    String LEFT_BOTTOM = "gwt-debug-selectorLeftBottom";
  }

  private interface Locators {
    String PANEL_SELECTOR_BTN = "gwt-debug-panelSelector";
    String PANEL_SELECTOR_POPUP = "gwt-debug-panelSelectorPopup";
  }

  @FindBy(id = Locators.PANEL_SELECTOR_BTN)
  WebElement panelSelectorBtn;

  @FindBy(id = Locators.PANEL_SELECTOR_POPUP)
  WebElement panelSelectorPopup;

  /** click on the 'panel selector' button */
  public void clickPanelSelectorBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(panelSelectorBtn))
        .click();
  }

  /** wait 'panel selector popup' is open */
  public void waitPanelSelectorPopupOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(panelSelectorPopup));
  }

  /** wait 'panel selector popup' is closed */
  public void waitPanelSelectorPopupClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.PANEL_SELECTOR_POPUP)));
  }

  /**
   * click on the panel type into selector popup
   *
   * @param typePanel is type panel into panel selector popup
   */
  public void clickPanelIntoSelectorPopup(String typePanel) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(typePanel)))
        .click();
    waitPanelSelectorPopupClosed();
  }

  /**
   * open panel selector and select panel type
   *
   * @param typePanel is type panel into panel selector popup
   */
  public void selectPanelTypeFromPanelSelector(String typePanel) {
    clickPanelSelectorBtn();
    waitPanelSelectorPopupOpen();
    clickPanelIntoSelectorPopup(typePanel);
  }
}
