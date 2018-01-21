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
package org.eclipse.che.selenium.pageobject.dashboard.account;

import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakHeaderButtons.ButtonsLocators.ACCOUNT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakHeaderButtons.ButtonsLocators.APPLICATIONS_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakHeaderButtons.ButtonsLocators.AUTHENTICATOR_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakHeaderButtons.ButtonsLocators.PASSWORD_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakHeaderButtons.ButtonsLocators.SESSIONS_BUTTON;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import org.openqa.selenium.By;

@Singleton
public class KeycloakHeaderButtons {
  private SeleniumWebDriverUtils seleniumWebDriverUtils;

  @Inject
  public KeycloakHeaderButtons(SeleniumWebDriverUtils seleniumWebDriverUtils) {
    this.seleniumWebDriverUtils = seleniumWebDriverUtils;
  }

  protected interface ButtonsLocators {
    String ACCOUNT_BUTTON = "//div[@id='tabs-menu']//a[text()='Account']";
    String PASSWORD_BUTTON = "//div[@id='tabs-menu']//a[text()='Password']";
    String AUTHENTICATOR_BUTTON = "//div[@id='tabs-menu']//a[text()='Authenticator']";
    String SESSIONS_BUTTON = "//div[@id='tabs-menu']//a[text()='Sessions']";
    String APPLICATIONS_BUTTON = "//div[@id='tabs-menu']//a[text()='Applications']";
  }

  /** click on the "Account" button in the header oh the page */
  public void clickOnAccountButton() {
    seleniumWebDriverUtils.waitAndClickOnElement(By.xpath(ACCOUNT_BUTTON));
  }

  /** click on the "Password" button in the header oh the page */
  public void clickOnPasswordButton() {
    seleniumWebDriverUtils.waitAndClickOnElement(By.xpath(PASSWORD_BUTTON));
  }

  /** click on the "Authenticator" button in the header oh the page */
  public void clickOnAuthenticatorButton() {
    seleniumWebDriverUtils.waitAndClickOnElement(By.xpath(AUTHENTICATOR_BUTTON));
  }

  /** click on the "Session" button in the header oh the page */
  public void clickOnSessionsButton() {
    seleniumWebDriverUtils.waitAndClickOnElement(By.xpath(SESSIONS_BUTTON));
  }

  /** click on the "Application" button in the header oh the page */
  public void clickOnApplicationsButton() {
    seleniumWebDriverUtils.waitAndClickOnElement(By.xpath(APPLICATIONS_BUTTON));
  }

  /** wait until all buttons which placed in the header of the page will be visible */
  public void waitAllHeaderButtonsIsVisible() {
    Arrays.asList(
            By.xpath(ACCOUNT_BUTTON),
            By.xpath(PASSWORD_BUTTON),
            By.xpath(AUTHENTICATOR_BUTTON),
            By.xpath(SESSIONS_BUTTON),
            By.xpath(APPLICATIONS_BUTTON))
        .forEach(locator -> seleniumWebDriverUtils.waitElementIsVisible(locator));
  }
}
