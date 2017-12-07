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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WorkspaceAgents {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public WorkspaceAgents(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String AGENT_NAME = "//span[@agent-name='%s']";
    String AGENT_DESCRIPTION = "//span[@agent-description='%s']";
    String AGENT_STATE = "//md-switch[@agent-switch='%s']";
  }

  public void checkAgentExists(String agentName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.AGENT_NAME, agentName))));
  }

  public void switchAgentState(String agentName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.AGENT_STATE, agentName))))
        .click();
  }

  public Boolean getAgentState(String agentName) {
    String state =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(visibilityOfElementLocated(By.xpath(format(Locators.AGENT_STATE, agentName))))
            .getAttribute("aria-checked");

    return Boolean.parseBoolean(state);
  }

  public String checkAgentDescription(String agentName) {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.AGENT_DESCRIPTION, agentName))))
        .getText();
  }
}
