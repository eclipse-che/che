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

  private static final String OVERWRITE_FOLDER_CHECKBOX_ID =
      "gwt-debug-file-uploadFolder-overwrite-input";
  private static final String OVERWRITE_FOLDER_CHECKBOX_LABEL_ID =
      "gwt-debug-file-uploadFolder-overwrite-label";

  private static final String SKIP_ROOT_FOLDER_CHECKBOX_ID =
      "gwt-debug-file-uploadFolder-skipFirstLevel-input";
  private static final String SKIP_ROOT_FOLDER_CHECKBOX_LABEL_ID =
      "gwt-debug-file-uploadFolder-skipFirstLevel-label";

  @FindBy(id = UPLOAD_BUTTON_ID)
  private WebElement uploadButton;

  @FindBy(xpath = TITLE_XPATH)
  private WebElement title;

  @FindBy(id = OVERWRITE_FOLDER_CHECKBOX_ID)
  private WebElement overwriteIfFolderExistsCheckbox;

  @FindBy(id = OVERWRITE_FOLDER_CHECKBOX_LABEL_ID)
  private WebElement overwriteIfFolderExistsCheckboxLabel;

  @FindBy(id = SKIP_ROOT_FOLDER_CHECKBOX_ID)
  private WebElement skipRootFolderCheckbox;

  @FindBy(id = SKIP_ROOT_FOLDER_CHECKBOX_LABEL_ID)
  private WebElement skipRootFolderCheckboxLabel;

  @Inject
  public UploadDirectoryDialogPage(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory) {
    super(seleniumWebDriver, seleniumWebDriverHelper, webDriverWaitFactory);
  }

  public void selectSkipRootFolderCheckbox() {
    seleniumWebDriverHelper.waitAndSetCheckbox(
        skipRootFolderCheckbox, skipRootFolderCheckboxLabel, true);
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
    return overwriteIfFolderExistsCheckbox;
  }

  @Override
  WebElement getOverwriteIfExistsCheckboxLabel() {
    return overwriteIfFolderExistsCheckboxLabel;
  }
}
