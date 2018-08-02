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

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.DEFAULT_TIMEOUT;

import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;

public abstract class AbstractUploadDialogPage {
  private static final String CHOOSE_FILE_BUTTON_ID = "gwt-debug-file-uploadFile-ChooseFile";

  final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory webDriverWaitFactory;

  @FindBy(id = CHOOSE_FILE_BUTTON_ID)
  private WebElement chooseFileButton;

  public AbstractUploadDialogPage(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.webDriverWaitFactory = webDriverWaitFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitOnOpen() {
    seleniumWebDriverHelper.waitAllVisibility(asList(getTitle(), chooseFileButton));
  }

  public void waitOnClose() {
    seleniumWebDriverHelper.waitAllInvisibility(asList(getTitle(), chooseFileButton));
  }

  public String selectResourceToUpload(Path resourceToUpload) throws IOException {
    String uploadingFileName =
        seleniumWebDriverHelper.selectResourceToUpload(chooseFileButton, resourceToUpload);
    webDriverWaitFactory
        .get(DEFAULT_TIMEOUT)
        .until((ExpectedCondition<Boolean>) driver -> isUploadButtonEnabled());

    return uploadingFileName;
  }

  private boolean isUploadButtonEnabled() {
    return getUploadButton().getCssValue("background-color").equals("rgba(74, 144, 226, 1)");
  }

  public void clickOnUploadButton() {
    WebElement uploadButton = getUploadButton();
    seleniumWebDriverHelper.waitVisibility(uploadButton);
    uploadButton.click();
  }

  public void selectOverwriteIfFileExistsCheckbox() {
    seleniumWebDriverHelper.waitAndSetCheckbox(
        getOverwriteIfExistsCheckbox(), getOverwriteIfExistsCheckboxLabel(), true);
  }

  abstract WebElement getTitle();

  abstract WebElement getUploadButton();

  abstract WebElement getOverwriteIfExistsCheckbox();

  abstract WebElement getOverwriteIfExistsCheckboxLabel();
}
