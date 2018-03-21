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
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** @author Anatolii Bazko */
@Singleton
public class GitRevertCommit {
  private static final String REVERT_COMMIT_PANEL = "gwt-debug-revert-commit-panel";
  private static final String REVERT_BUTTON = "git-revert";
  private static final String CANCEL_BUTTON = "git-revert-cancel";

  private final Loader loader;

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public GitRevertCommit(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.loader = loader;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(id = REVERT_COMMIT_PANEL)
  private WebElement revertPanel;

  @FindBy(id = REVERT_BUTTON)
  private WebElement revertButton;

  @FindBy(id = CANCEL_BUTTON)
  private WebElement cancelButton;

  public void waitRevertPanelOpened() {
    seleniumWebDriverHelper.waitVisibility(revertPanel, ELEMENT_TIMEOUT_SEC);
  }

  public void waitRevertPanelClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.id(REVERT_COMMIT_PANEL), ELEMENT_TIMEOUT_SEC);
  }

  public void clickRevertButton() {
    seleniumWebDriverHelper.waitAndClick(revertButton, ELEMENT_TIMEOUT_SEC);
  }

  public void clickCancelButton() {
    seleniumWebDriverHelper.waitAndClick(cancelButton, ELEMENT_TIMEOUT_SEC);
    waitRevertPanelClosed();
  }

  public String getTopCommitRevision() {
    loader.waitOnClosed();
    int numberOfTopRevisionCell = 1;
    return getRevertCommitTableContent().split("\n")[numberOfTopRevisionCell];
  }

  public String getTopCommitAuthor() {
    loader.waitOnClosed();
    int numberOfTopAuthorCell = 3;
    return getRevertCommitTableContent().split("\n")[numberOfTopAuthorCell];
  }

  public String getTopCommitComment() {
    loader.waitOnClosed();
    int numberOfTopCommitCell = 4;
    return getRevertCommitTableContent().split("\n")[numberOfTopCommitCell];
  }

  public void selectRevision(String revision) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(
            format("//div[@id='%s']//*[contains(text(),'%s')]", REVERT_COMMIT_PANEL, revision)),
        REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** Returns all cells from top left to bottom right divided by "\n" */
  private String getRevertCommitTableContent() {
    return seleniumWebDriverHelper.waitVisibility(revertPanel, ELEMENT_TIMEOUT_SEC).getText();
  }
}
