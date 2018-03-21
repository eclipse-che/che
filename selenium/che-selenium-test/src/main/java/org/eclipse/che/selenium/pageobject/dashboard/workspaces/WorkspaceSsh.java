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

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

public class WorkspaceSsh {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public WorkspaceSsh(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String GENERATE_BUTTON_XPATH = "//che-button-primary[@che-button-title='Generate']";
    String REMOVE_DEFAULT_SSH_KEY_XPATH =
        "//che-button-danger[@che-button-title='Remove default SSH key']";
    String PRIVATE_KEY_XPATH = "//div[@che-label-name='Private key']//div[2]//span";
    String PUBLIC_KEY_XPATH = "//div[@che-label-name='Public key']//div[2]//span";
    String SSH_KEY_NOT_GENERATED_XPATH = "//span[@class='workspace-ssh-content-notice']";
  }

  public void clickOnGenerateButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.GENERATE_BUTTON_XPATH), 20);
  }

  public void clickOnRemoveDefaultSshKeyButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.REMOVE_DEFAULT_SSH_KEY_XPATH), 20);
  }

  public Boolean isPrivateKeyExists() {
    return seleniumWebDriverHelper
        .waitVisibilityAndGetText(By.xpath(Locators.PRIVATE_KEY_XPATH))
        .contains("-----BEGIN RSA PRIVATE KEY-----");
  }

  public Boolean isPublicKeyExists() {
    return seleniumWebDriverHelper
        .waitVisibilityAndGetText(By.xpath(Locators.PUBLIC_KEY_XPATH))
        .contains("ssh-rsa");
  }

  public void waitSshKeyNotExists() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.SSH_KEY_NOT_GENERATED_XPATH));
  }
}
