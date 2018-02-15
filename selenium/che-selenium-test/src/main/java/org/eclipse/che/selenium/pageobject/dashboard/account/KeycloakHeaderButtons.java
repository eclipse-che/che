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

import static org.openqa.selenium.By.xpath;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.openqa.selenium.By;

/** @author Igor Ohrimenko */
@Singleton
public class KeycloakHeaderButtons {
  private enum Button {
    ACCOUNT("Account"),
    PASSWORD("Password"),
    AUTHENTICATOR("Authenticator"),
    FEDERATED_IDENTITIES("Federated Identity"),
    SESSIONS("Sessions"),
    APPLICATIONS("Applications");

    private static final String BUTTON_XPATH_TEMPLATE = "//div[@id='tabs-menu']//a[text()='%s']";

    private final String text;

    Button(String text) {
      this.text = text;
    }

    private By getXpath() {
      return xpath(String.format(BUTTON_XPATH_TEMPLATE, text));
    }
  }

  private SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public KeycloakHeaderButtons(SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  /** click on the "Account" button in the header oh the page */
  public void clickOnAccountButton() {
    clickOnButton(Button.ACCOUNT);
  }

  /** click on the "Password" button in the header oh the page */
  public void clickOnPasswordButton() {
    clickOnButton(Button.PASSWORD);
  }

  /** click on the "Authenticator" button in the header oh the page */
  public void clickOnAuthenticatorButton() {
    clickOnButton(Button.AUTHENTICATOR);
  }

  /** click on the "Federated Identity" button in the header oh the page */
  public void clickOnFederatedIdentitiesButton() {
    clickOnButton(Button.FEDERATED_IDENTITIES);
  }

  /** click on the "Session" button in the header oh the page */
  public void clickOnSessionsButton() {
    clickOnButton(Button.SESSIONS);
  }

  /** click on the "Application" button in the header oh the page */
  public void clickOnApplicationsButton() {
    clickOnButton(Button.APPLICATIONS);
  }

  private void clickOnButton(Button button) {
    seleniumWebDriverHelper.waitAndClick(button.getXpath());
  }

  /** wait until all buttons which placed in the header of the page will be visible */
  public void waitAllHeaderButtonsAreVisible() {
    Arrays.asList(
            Button.ACCOUNT.getXpath(),
            Button.PASSWORD.getXpath(),
            Button.AUTHENTICATOR.getXpath(),
            Button.FEDERATED_IDENTITIES.getXpath(),
            Button.SESSIONS.getXpath(),
            Button.APPLICATIONS.getXpath())
        .forEach(locator -> seleniumWebDriverHelper.waitVisibility(locator));
  }
}
