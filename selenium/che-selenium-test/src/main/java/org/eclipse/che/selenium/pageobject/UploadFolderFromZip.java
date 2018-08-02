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
package org.eclipse.che.selenium.pageobject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by aleksandr shmaraev on 20.05.15. */
@Singleton
public class UploadFolderFromZip {

  private static final Logger LOG = LoggerFactory.getLogger(UploadFolderFromZip.class);

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public UploadFolderFromZip(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String UPLOAD_FOLDER_ZIP_FORM = "//table[@title='Upload Folder']";
    String UPLOAD_FOLDER_ZIP_SELECT = "gwt-debug-file-uploadFile-ChooseFile";
    String UPLOAD_FOLDER_ZIP_OVERWRITE = "gwt-debug-file-uploadFolder-overwrite";
    String UPLOAD_FOLDER_ZIP_SKIP_ROOT = "gwt-debug-file-uploadFolder-skipFirstLevel";
    String UPLOAD_FOLDER_ZIP_BUTTON = "file-uploadFolder-upload";
    String UPLOAD_FOLDER_ZIP_CANCEL = "file-uploadFolder-cancel";
  }

  @FindBy(xpath = Locators.UPLOAD_FOLDER_ZIP_FORM)
  WebElement folderZipUploadForm;

  @FindBy(id = Locators.UPLOAD_FOLDER_ZIP_SELECT)
  WebElement folderZipUploadSelect;

  @FindBy(id = Locators.UPLOAD_FOLDER_ZIP_OVERWRITE)
  WebElement folderZipUploadOverwrite;

  @FindBy(id = Locators.UPLOAD_FOLDER_ZIP_SKIP_ROOT)
  WebElement folderZipUploadSkipRoot;

  @FindBy(id = Locators.UPLOAD_FOLDER_ZIP_BUTTON)
  WebElement folderZipUploadButton;

  @FindBy(id = Locators.UPLOAD_FOLDER_ZIP_CANCEL)
  WebElement folderZipUploadCancel;

  public void waitUploadFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(ExpectedConditions.visibilityOf(folderZipUploadForm));
  }

  public void waitUploadFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(Locators.UPLOAD_FOLDER_ZIP_FORM)));
  }

  public void setPathToZip(String filePath) {
    File file = new File(filePath);
    LOG.info("Absolute path to zip: {}", file.getAbsolutePath());
    folderZipUploadSelect.sendKeys(file.getAbsolutePath());
    WaitUtils.sleepQuietly(3);
  }

  public void selectOverwriteFolder() {
    folderZipUploadOverwrite.click();
  }

  public void selectSkipRootFolderZip() {
    folderZipUploadSkipRoot.click();
  }

  public void waitUploadButtonIsDisabled() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(
            ExpectedConditions.presenceOfElementLocated(By.id(Locators.UPLOAD_FOLDER_ZIP_BUTTON)))
        .getAttribute("disabled");
  }

  public void clickUploadFolderZipButton() {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(ExpectedConditions.elementToBeClickable(folderZipUploadButton));
    folderZipUploadButton.click();
    waitUploadFormIsClosed();
  }

  public void clickUploadFolderCancelBtn() {
    folderZipUploadCancel.click();
    waitUploadFormIsClosed();
  }
}
