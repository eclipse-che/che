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
package org.eclipse.che.selenium.pageobject.git;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Anatolii Bazko */
@Singleton
public class GitRevertCommit {
  private static final String REVERT_COMMIT_PANEL = "gwt-debug-revert-commit-panel";
  private static final String REVERT_BUTTON = "git-revert";
  private static final String CANCEL_BUTTON = "git-revert-cancel";

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public GitRevertCommit(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(id = REVERT_COMMIT_PANEL)
  private WebElement revertPanel;

  @FindBy(id = REVERT_BUTTON)
  private WebElement revertButton;

  @FindBy(id = CANCEL_BUTTON)
  private WebElement cancelButton;

  public void waitRevertPanelOpened() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(revertPanel));
  }

  public void waitRevertPanelClosed() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(REVERT_COMMIT_PANEL)));
  }

  public void clickRevertButton() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(revertButton))
        .click();
  }

  public String getTopCommitRevision() {
    loader.waitOnClosed();
    return new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(revertPanel))
        .getText()
        .split("\n")[1]
        .replaceAll("\\.", "");
  }

  public String getTopCommitComment() {
    loader.waitOnClosed();
    return new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(revertPanel))
        .getText()
        .split("\n")[4];
  }

  public String getTopCommitAuthor() {
    loader.waitOnClosed();
    return new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(revertPanel))
        .getText()
        .split("\n")[3];
  }

  public void selectRevision(String revision) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    format(
                        "//div[@id='%s']//*[contains(text(),'%s')]",
                        REVERT_COMMIT_PANEL, revision))))
        .click();
  }
}
