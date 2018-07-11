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

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakFederatedIdentitiesPage.Locators.GITHUB_INPUT_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakFederatedIdentitiesPage.Locators.GITHUB_REMOVE_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakFederatedIdentitiesPage.Locators.TITLE_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakPasswordPage.PasswordLocators.SUCCESS_ALERT;
import static org.openqa.selenium.By.xpath;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.ws.rs.NotSupportedException;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.keycloak.TestKeycloakSettingsServiceClient;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Dmytro Nochevnov */
@Singleton
public class KeycloakFederatedIdentitiesPage {

  private static final String IDENTITY_PROVIDER_REMOVED_SUCCESSFULLY_MESSAGE =
      "Identity provider removed successfully.";

  private static final NotSupportedException NOT_SUPPORTED_EXCEPTION =
      new NotSupportedException(
          "Keycloak Federated Identities page can be used in Multi User Eclipse Che only.");

  private final WebDriverWait loadPageWait;
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final KeycloakHeaderButtons keycloakHeaderButtons;
  private final TestKeycloakSettingsServiceClient testKeycloakSettingsServiceClient;

  @Inject
  public KeycloakFederatedIdentitiesPage(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      KeycloakHeaderButtons keycloakHeaderButtons,
      TestKeycloakSettingsServiceClient testKeycloakSettingsServiceClient) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.keycloakHeaderButtons = keycloakHeaderButtons;
    this.testKeycloakSettingsServiceClient = testKeycloakSettingsServiceClient;
    this.loadPageWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
  }

  interface Locators {
    String GITHUB_REMOVE_BUTTON_ID = "remove-link-github";
    String TITLE_XPATH = "//div[@id='tab-content']//h2[text()='Federated Identities']";
    String GITHUB_INPUT_XPATH = "*//div[@id='federated-identities']//input";
  }

  public void open() {
    String identityPageUrl =
        format(
            "%s/identity", testKeycloakSettingsServiceClient.read().getKeycloakProfileEndpoint());

    seleniumWebDriver.navigate().to(identityPageUrl);
    waitPageIsLoaded();
  }

  public void ensureGithubIdentityIsAbsent() {
    if (!getGitHubIdentityFieldValue().isEmpty()) {
      removeGithubIdentity();
    }
  }

  public String getGitHubIdentityFieldValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.xpath(GITHUB_INPUT_XPATH));
  }

  public void removeGithubIdentity() {
    seleniumWebDriverHelper.waitAndClick(By.id(GITHUB_REMOVE_BUTTON_ID));
    waitTextInSuccessAlert(IDENTITY_PROVIDER_REMOVED_SUCCESSFULLY_MESSAGE);
  }

  private void waitPageIsLoaded() {
    keycloakHeaderButtons.waitAllHeaderButtonsAreVisible();
    waitAllBodyFieldsAndButtonsIsVisible();
  }

  private void waitTextInSuccessAlert(String expectedText) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>)
            driver ->
                seleniumWebDriverHelper
                    .waitVisibility(xpath(SUCCESS_ALERT))
                    .getText()
                    .equals(expectedText));
  }

  private void waitAllBodyFieldsAndButtonsIsVisible() {
    asList(xpath(TITLE_XPATH)).forEach(locator -> seleniumWebDriverHelper.waitVisibility(locator));
  }
}
