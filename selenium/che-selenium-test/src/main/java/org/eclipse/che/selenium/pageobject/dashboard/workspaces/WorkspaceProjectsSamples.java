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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.ActionButton.ADD_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.ActionButton.CANCEL_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.Locators.CHECKBOX_BY_SAMPLE_NAME_ID_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.Locators.SAMPLE_ITEM_TEMPLATE_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.Locators.TAB_STATE_TEMPLATE_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.TabButton.BLANK_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.TabButton.GITHUB_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.TabButton.GIT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.TabButton.SAMPLES_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjectsSamples.TabButton.ZIP_BUTTON;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@Singleton
public class WorkspaceProjectsSamples {

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  private WorkspaceProjectsSamples(SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  public interface Locators {
    String SAMPLE_ITEM_TEMPLATE_XPATH =
        "//ng-transclude[@class='che-list-item-content']//span[text()='%s']";
    String TAB_STATE_TEMPLATE_XPATH = "//che-toggle-joined-button[@id='%s']/button";
    String CHECKBOX_BY_SAMPLE_NAME_ID_TEMPLATE = "sample-%s";
  }

  private interface Button {
    String get();
  }

  public enum TabButton implements Button {
    SAMPLES_BUTTON("samples-button"),
    BLANK_BUTTON("blank-button"),
    GIT_BUTTON("git-button"),
    GITHUB_BUTTON("github-button"),
    ZIP_BUTTON("zip-button");

    private final String buttonId;

    TabButton(String buttonId) {
      this.buttonId = buttonId;
    }

    public String get() {
      return this.buttonId;
    }
  }

  public enum ActionButton implements Button {
    CANCEL_BUTTON("cancel-button"),
    ADD_BUTTON("add-project-button");

    private final String buttonId;

    ActionButton(String buttonId) {
      this.buttonId = buttonId;
    }

    @Override
    public String get() {
      return this.buttonId;
    }
  }

  public void waitSamplesForm() {
    waitButtons(
        asList(
            SAMPLES_BUTTON,
            BLANK_BUTTON,
            GIT_BUTTON,
            GITHUB_BUTTON,
            ZIP_BUTTON,
            CANCEL_BUTTON,
            ADD_BUTTON));
  }

  public WebElement waitButton(Button button) {
    return seleniumWebDriverHelper.waitVisibility(By.id(button.get()));
  }

  public void clickOnButton(Button button) {
    waitButton(button).click();
  }

  public void waitTabSelected(TabButton tabButton) {
    String tabXpath = format(TAB_STATE_TEMPLATE_XPATH, tabButton.get());
    seleniumWebDriverHelper.waitAttributeContainsValue(
        By.xpath(tabXpath), "class", "che-toggle-button-enabled");
  }

  public void waitTabNotSelected(TabButton tabButton) {
    String tabXpath = format(TAB_STATE_TEMPLATE_XPATH, tabButton.get());
    seleniumWebDriverHelper.waitAttributeContainsValue(
        By.xpath(tabXpath), "class", "che-toggle-button-disabled");
  }

  public void waitButtons(List<Button> buttons) {
    buttons.forEach(button -> waitButton(button));
  }

  public void waitSampleByName(String... sampleNames) {
    asList(sampleNames)
        .forEach(
            sampleName -> {
              String sampleXpath = format(SAMPLE_ITEM_TEMPLATE_XPATH, sampleName);
              seleniumWebDriverHelper.waitVisibility(By.xpath(sampleXpath));
            });
  }

  public boolean isCheckboxEnabled(String sampleName) {
    final String checkboxId = format(CHECKBOX_BY_SAMPLE_NAME_ID_TEMPLATE, sampleName);
    final String checkboxLocator = format("//div[@id='%s']/md-checkbox", checkboxId);
    final String checkboxStateAttribute = "aria-checked";
    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(By.xpath(checkboxLocator), checkboxStateAttribute)
        .equals("true");
  }

  public void waitAllCheckboxesEnabled(String... sampleNames) {
    asList(sampleNames)
        .forEach(
            name ->
                seleniumWebDriverHelper.waitSuccessCondition(driver -> isCheckboxEnabled(name)));
  }

  public void waitAllCheckboxesDisabled(String... sampleNames) {
    asList(sampleNames)
        .forEach(
            name ->
                seleniumWebDriverHelper.waitSuccessCondition(driver -> !isCheckboxEnabled(name)));
  }

  public void clickOnAllCheckboxes(String... sampleNames) {
    asList(sampleNames)
        .forEach(
            name -> {
              String checkboxId = format(CHECKBOX_BY_SAMPLE_NAME_ID_TEMPLATE, name);
              String checkboxLocator = format("//div[@id='%s']/md-checkbox", checkboxId);
              seleniumWebDriverHelper.waitAndClick(By.xpath(checkboxLocator));
            });
  }

  private void waitButtonState(ActionButton actionButton, boolean state) {
    final String buttonXpath = format("//*[@id='%s']/button", actionButton.get());
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(buttonXpath), "aria-disabled", Boolean.toString(!state));
  }

  public void waitButtonDisabled(ActionButton... actionButtons) {
    asList(actionButtons).forEach(button -> waitButtonState(button, false));
  }

  public void waitButtonEnabled(ActionButton... actionButtons) {
    asList(actionButtons).forEach(button -> waitButtonState(button, true));
  }
}
