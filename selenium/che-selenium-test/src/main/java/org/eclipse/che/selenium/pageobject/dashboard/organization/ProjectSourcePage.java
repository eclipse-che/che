/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.dashboard.organization;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Ann Shumilova */
@Singleton
public class ProjectSourcePage {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public ProjectSourcePage(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public enum Sources {
    SAMPLES("Samples"),
    BLANK("Blank"),
    GIT("Git"),
    GITHUB("Github"),
    ZIP("Zip");

    private final String title;

    Sources(String title) {
      this.title = title;
    }
  }

  private interface Locators {
    String ADD_OR_IMPORT_PROJECT_BUTTON_XPATH = "//*[@id=\"ADD_PROJECT\"]";

    String POPOVER_CSS = "project-source-selector";
    String POPOVER_XPATH = "project-source-selector";
    String ADD_BUTTON_XPATH = "//che-button-primary[@che-button-title='Add']";

    String SOURCE_XPATH = "//che-toggle-joined-button//span[text()='%s']";

    String SAMPLE_XPATH = "//template-selector-item//span[text()='%s']";
    String SAMPLE_CHECKBOX_XPATH = "//md-checkbox[@aria-label='Sample %s']";

    String GIT_REPO_XPATH = "//input[@name='remoteGitURL']";
    String ZIP_XPATH = "//input[@name='remoteZipURL']";
    String ZIP_SKIP_ROOT_XPATH = "//div[contains(@class, 'skip-root-container')]/md-checkbox";
  }

  @FindBy(xpath = Locators.ADD_OR_IMPORT_PROJECT_BUTTON_XPATH)
  WebElement addOrImportProjectButton;

  @FindBy(css = Locators.POPOVER_CSS)
  WebElement popover;

  @FindBy(xpath = Locators.GIT_REPO_XPATH)
  WebElement gitRepositoryInput;

  @FindBy(xpath = Locators.ZIP_XPATH)
  WebElement zipLocationInput;

  @FindBy(xpath = Locators.ZIP_SKIP_ROOT_XPATH)
  WebElement zipSkipRoot;

  @FindBy(xpath = Locators.ADD_BUTTON_XPATH)
  WebElement addButton;

  public void clickAddOrImportProjectButton() {
    addOrImportProjectButton.click();
  }

  public void waitOpened() {
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOADER_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(Locators.POPOVER_CSS)));
  }

  public void selectSourceTab(Sources source) {
    WebElement sourceTab =
        seleniumWebDriver.findElement(By.xpath(String.format(Locators.SOURCE_XPATH, source.title)));
    sourceTab.click();
  }

  public void selectSample(String name) {
    WebElement sample =
        seleniumWebDriver.findElement(
            By.xpath(String.format(Locators.SAMPLE_CHECKBOX_XPATH, name)));
    sample.click();
  }

  public void typeGitRepositoryLocation(String url) {
    gitRepositoryInput.clear();
    gitRepositoryInput.sendKeys(url);
  }

  public void typeZipLocation(String url) {
    zipLocationInput.clear();
    zipLocationInput.sendKeys(url);
  }

  public void skipRootFolder() {
    zipSkipRoot.click();
  }

  public void clickAdd() {
    addButton.click();
  }
}
