/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

public class WorkspaceSsh {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public WorkspaceSsh(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String GENERATE_BUTTON_ID = "generate-ssh-key-button";
    String REMOVE_DEFAULT_SSH_KEY_ID = "remove-default-ssh-key-button";
    String PRIVATE_KEY_ID = "private-key-text";
    String PUBLIC_KEY_ID = "public-key-text";
    String SSH_KEY_NOT_GENERATED_ID = "ssh-key-not-generated-message";
  }

  public void clickOnGenerateButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(Locators.GENERATE_BUTTON_ID), ELEMENT_TIMEOUT_SEC);
  }

  public void clickOnRemoveDefaultSshKeyButton() {
    seleniumWebDriverHelper.waitAndClick(
        By.id(Locators.REMOVE_DEFAULT_SSH_KEY_ID), ELEMENT_TIMEOUT_SEC);
  }

  public Boolean isPrivateKeyExists() {
    return seleniumWebDriverHelper
        .waitVisibilityAndGetText(By.id(Locators.PRIVATE_KEY_ID))
        .contains("-----BEGIN RSA PRIVATE KEY-----");
  }

  public Boolean isPublicKeyExists() {
    return seleniumWebDriverHelper
        .waitVisibilityAndGetText(By.id(Locators.PUBLIC_KEY_ID))
        .contains("ssh-rsa");
  }

  public void waitSshKeyNotExists() {
    seleniumWebDriverHelper.waitVisibility(By.id(Locators.SSH_KEY_NOT_GENERATED_ID));
  }
}
