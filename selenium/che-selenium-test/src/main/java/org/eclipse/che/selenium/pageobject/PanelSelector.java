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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** @author Aleksandr Shmaraiev */
@Singleton
public class PanelSelector {

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public PanelSelector(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface PanelTypes {
    String LEFT_BOTTOM_ID = "gwt-debug-selectorLeftBottom";
    String LEFT_RIGHT_BOTTOM_ID = "gwt-debug-selectorLeftRightBottom";
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
    seleniumWebDriverHelper.waitAndClick(panelSelectorBtn, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** wait 'panel selector popup' is open */
  public void waitPanelSelectorPopupOpen() {
    seleniumWebDriverHelper.waitVisibility(panelSelectorPopup, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** wait 'panel selector popup' is closed */
  public void waitPanelSelectorPopupClosed() {
    seleniumWebDriverHelper.waitInvisibility(
        By.id(Locators.PANEL_SELECTOR_POPUP), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /**
   * click on the panel type into selector popup
   *
   * @param typePanel is type panel into panel selector popup
   */
  public void clickPanelIntoSelectorPopup(String typePanel) {
    seleniumWebDriverHelper.waitAndClick(By.id(typePanel), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
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
