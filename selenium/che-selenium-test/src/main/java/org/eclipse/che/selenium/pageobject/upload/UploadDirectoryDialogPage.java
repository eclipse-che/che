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
package org.eclipse.che.selenium.pageobject.upload;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@Singleton
public class UploadDirectoryDialogPage extends AbstractUploadDialogPage {
  private static final String UPLOAD_BUTTON_ID = "file-uploadFolder-upload";
  private static final String TITLE_XPATH = "//div[text()='Upload Folder']";

  @FindBy(id = UPLOAD_BUTTON_ID)
  private WebElement uploadButton;

  @FindBy(xpath = TITLE_XPATH)
  private WebElement title;

  @Inject
  public UploadDirectoryDialogPage(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory) {
    super(seleniumWebDriver, seleniumWebDriverHelper, webDriverWaitFactory);
  }

  @Override
  WebElement getTitle() {
    return title;
  }

  @Override
  WebElement getUploadButton() {
    return uploadButton;
  }
}
