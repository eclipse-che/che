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
package org.eclipse.che.selenium.pageobject.upload;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@Singleton
public class UploadFileDialogPage extends AbstractUploadDialogPage {
  private static final String UPLOAD_BUTTON_ID = "file-uploadFile-upload";
  private static final String TITLE_XPATH = "//div[text()='Upload File']";
  private static final String OVERWRITE_FILE_CHECKBOX_ID =
      "gwt-debug-file-uploadFile-overwrite-input";
  private static final String OVERWRITE_FILE_CHECKBOX_LABEL_ID =
      "gwt-debug-file-uploadFile-overwrite-label";

  @FindBy(id = UPLOAD_BUTTON_ID)
  private WebElement uploadButton;

  @FindBy(xpath = TITLE_XPATH)
  private WebElement title;

  @FindBy(id = OVERWRITE_FILE_CHECKBOX_ID)
  private WebElement overwriteIfFileExistsCheckbox;

  @FindBy(id = OVERWRITE_FILE_CHECKBOX_LABEL_ID)
  private WebElement overwriteIfFileExistsCheckboxLabel;

  @Inject
  public UploadFileDialogPage(
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

  @Override
  WebElement getOverwriteIfExistsCheckbox() {
    return overwriteIfFileExistsCheckbox;
  }

  @Override
  WebElement getOverwriteIfExistsCheckboxLabel() {
    return overwriteIfFileExistsCheckboxLabel;
  }
}
