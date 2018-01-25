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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WorkspaceConfig {

  private final WebDriverWait redrawUiElementsTimeout;

  @Inject
  public WorkspaceConfig(SeleniumWebDriver seleniumWebDriver) {
    PageFactory.initElements(seleniumWebDriver, this);
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  private interface Locators {
    String CONFIG_FORM = "//ng-form[@name='workspaceConfigForm']";
  }

  public void waitConfigForm() {
    redrawUiElementsTimeout.until(visibilityOfElementLocated(By.xpath(Locators.CONFIG_FORM)));
  }
}
