/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.APPLICATION_START_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MULTIPLE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class WebDriverWaitFactory {
  private SeleniumWebDriver seleniumWebDriver;
  private WebDriverWait applicationStartWait;
  private WebDriverWait preparingWsWait;
  private WebDriverWait updatingProjectWait;
  private WebDriverWait expectedMessInConsoleWait;
  private WebDriverWait loaderWait;
  private WebDriverWait widgetWait;
  private WebDriverWait elementWait;
  private WebDriverWait loadPageWait;
  private WebDriverWait redrawUiElementsWait;
  private WebDriverWait attachingElementToDomWait;
  private WebDriverWait minimumSecWait;

  @Inject
  public WebDriverWaitFactory(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;

    this.applicationStartWait = createWait(APPLICATION_START_TIMEOUT_SEC);
    this.preparingWsWait = createWait(PREPARING_WS_TIMEOUT_SEC);
    this.updatingProjectWait = createWait(UPDATING_PROJECT_TIMEOUT_SEC);
    this.expectedMessInConsoleWait = createWait(EXPECTED_MESS_IN_CONSOLE_SEC);
    this.loaderWait = createWait(LOADER_TIMEOUT_SEC);
    this.widgetWait = createWait(WIDGET_TIMEOUT_SEC);
    this.elementWait = createWait(ELEMENT_TIMEOUT_SEC);
    this.loadPageWait = createWait(LOAD_PAGE_TIMEOUT_SEC);
    this.redrawUiElementsWait = createWait(REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.attachingElementToDomWait = createWait(ATTACHING_ELEM_TO_DOM_SEC);
    this.minimumSecWait = createWait(MULTIPLE);
  }

  public WebDriverWait get(int timeout) {
    switch (timeout) {
      case APPLICATION_START_TIMEOUT_SEC:
        return this.applicationStartWait;
      case PREPARING_WS_TIMEOUT_SEC:
        return this.preparingWsWait;
      case UPDATING_PROJECT_TIMEOUT_SEC:
        return this.updatingProjectWait;
      case EXPECTED_MESS_IN_CONSOLE_SEC:
        return this.expectedMessInConsoleWait;
      case LOADER_TIMEOUT_SEC:
        return this.loaderWait;
      case WIDGET_TIMEOUT_SEC:
        return this.widgetWait;
      case ELEMENT_TIMEOUT_SEC:
        return this.elementWait;
      case LOAD_PAGE_TIMEOUT_SEC:
        return this.loadPageWait;
      case REDRAW_UI_ELEMENTS_TIMEOUT_SEC:
        return this.redrawUiElementsWait;
      case ATTACHING_ELEM_TO_DOM_SEC:
        return this.attachingElementToDomWait;
      case MULTIPLE:
        return this.minimumSecWait;

      default:
        return createWait(timeout);
    }
  }

  private WebDriverWait createWait(int timeout) {
    return new WebDriverWait(seleniumWebDriver, timeout);
  }
}
