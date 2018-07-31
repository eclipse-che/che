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
package org.eclipse.che.selenium.pageobject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Created by aleksandr shmaraev on 29.01.15. */
@Singleton
public class UploadFile {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public UploadFile(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String FILE_UPLOAD_FORM = "//table[@title='Upload File']";
    String UPLOAD_FILE_BUTTON = "file-uploadFile-upload";
    String UPLOAD_FILE_CANCEL = "file-uploadFile-cancel";
    String UPLOAD_SELECT_FILE = "gwt-debug-file-uploadFile-ChooseFile";
    String UPLOAD_FILE_OVERWRITE = "gwt-debug-file-uploadFile-overwrite";
  }

  @FindBy(xpath = Locators.FILE_UPLOAD_FORM)
  WebElement fileUploadForm;

  @FindBy(id = Locators.UPLOAD_FILE_BUTTON)
  WebElement uploadFileButton;

  @FindBy(id = Locators.UPLOAD_FILE_CANCEL)
  WebElement uploadFileCancel;

  @FindBy(id = Locators.UPLOAD_SELECT_FILE)
  WebElement uploadSelectFile;

  @FindBy(id = Locators.UPLOAD_FILE_OVERWRITE)
  WebElement uploadFileOverwrite;

  public void waitUploadFormOpen() {
    new WebDriverWait(seleniumWebDriver, 5).until(ExpectedConditions.visibilityOf(fileUploadForm));
  }

  public void waitUploadFormClosed() {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.FILE_UPLOAD_FORM)));
  }

  public void clickUploadButton() {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(ExpectedConditions.elementToBeClickable(uploadFileButton));
    uploadFileButton.click();
  }

  public void clickUploadCancelBtn() {
    uploadFileCancel.click();
  }

  public void clickUploadFileOverwrite() {
    uploadFileOverwrite.click();
  }

  public void setPathToFile(String filePath) {
    uploadSelectFile.sendKeys(filePath);
    WaitUtils.sleepQuietly(3);
  }

  public void waitUploadButtonIsDisabled() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.presenceOfElementLocated(By.id(Locators.UPLOAD_FILE_BUTTON)))
        .getAttribute("disabled");
  }
}
