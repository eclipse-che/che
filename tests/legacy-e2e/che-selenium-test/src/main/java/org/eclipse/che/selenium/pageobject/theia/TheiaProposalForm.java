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
package org.eclipse.che.selenium.pageobject.theia;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProposalForm.Locators.PROPOSALS_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProposalForm.Locators.PROPOSAL_DESCRIPTION_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProposalForm.Locators.PROPOSAL_KEY_BINDING_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProposalForm.Locators.PROPOSAL_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProposalForm.Locators.SEARCH_FIELD_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProposalForm.Locators.WIDGET_BODY_XPATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;

@Singleton
public class TheiaProposalForm {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final SeleniumWebDriver seleniumWebDriver;
  private final TestWebElementRenderChecker testWebElementRenderChecker;

  @Inject
  private TheiaProposalForm(
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      SeleniumWebDriver seleniumWebDriver,
      TestWebElementRenderChecker testWebElementRenderChecker) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.seleniumWebDriver = seleniumWebDriver;
    this.testWebElementRenderChecker = testWebElementRenderChecker;
  }

  public interface Locators {
    // for selection checking get "aria-selected" attribute. "true" if selected, "false" if not.
    String WIDGET_BODY_XPATH = "//div[@class='monaco-quick-open-widget']";
    String SEARCH_FIELD_XPATH = WIDGET_BODY_XPATH + "//div[@class='quick-open-input']//input";
    String PROPOSALS_XPATH =
        WIDGET_BODY_XPATH
            + "//div[contains(@class, 'monaco-tree-row') and not(contains(@class, 'monaco-tree-rows'))]";
    String PROPOSAL_XPATH_TEMPLATE = "(" + PROPOSALS_XPATH + ")[%s]";
    String PROPOSAL_DESCRIPTION_XPATH_TEMPLATE =
        PROPOSAL_XPATH_TEMPLATE + "//div[@class='monaco-icon-label']";
    // get attribute "title" which contains text. For example - "Shift+Alt+W";
    String PROPOSAL_KEY_BINDING_XPATH_TEMPLATE =
        PROPOSAL_XPATH_TEMPLATE + "//div[@class='monaco-keybinding']";
  }

  public void waitForm() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(WIDGET_BODY_XPATH));
    testWebElementRenderChecker.waitElementIsRendered(By.xpath(WIDGET_BODY_XPATH));
    waitSearchField();
  }

  public void waitFormDisappearance() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(WIDGET_BODY_XPATH));
  }

  public void waitSearchField() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(SEARCH_FIELD_XPATH));
  }

  public void enterTextToSearchField(String text) {
    clearSearchFieldByPressingBackspace();

    seleniumWebDriverHelper.setValue(By.xpath(SEARCH_FIELD_XPATH), text);
  }

  public void waitSearchFieldText(String expectedText) {
    seleniumWebDriverHelper.waitValueEqualsTo(By.xpath(SEARCH_FIELD_XPATH), expectedText);
  }

  public void waitProposal(String proposalText) {
    getAllDescriptions().contains(proposalText);
  }

  private int getProposalsCount() {
    return seleniumWebDriverHelper.waitVisibilityOfAllElements(By.xpath(PROPOSALS_XPATH)).size();
  }

  public boolean isKeyBindingFieldExisted(int proposalIndex) {
    final String keyBindingFieldXpath = format(PROPOSAL_KEY_BINDING_XPATH_TEMPLATE, proposalIndex);
    return seleniumWebDriverHelper.isVisible(By.xpath(keyBindingFieldXpath));
  }

  public String getProposalDescription(int proposalIndex) {
    final int adoptedIndex = proposalIndex + 1;
    final String proposalDescriptionXpath =
        format(PROPOSAL_DESCRIPTION_XPATH_TEMPLATE, adoptedIndex);

    return seleniumWebDriverHelper.waitVisibilityAndGetText(By.xpath(proposalDescriptionXpath));
  }

  public String getProposalKeyBinding(int proposalIndex) {
    final int adoptedIndex = proposalIndex + 1;
    final String keyBindingContainerXpath =
        format(PROPOSAL_KEY_BINDING_XPATH_TEMPLATE, adoptedIndex);

    if (isKeyBindingFieldExisted(adoptedIndex)) {
      return seleniumWebDriverHelper.waitVisibilityAndGetText(By.xpath(keyBindingContainerXpath));
    }

    return "";
  }

  public Pair<String, String> getProposal(int proposalIndex) {
    return new Pair<>(getProposalDescription(proposalIndex), getProposalKeyBinding(proposalIndex));
  }

  public List<Pair<String, String>> getAllProposals() {
    List<Pair<String, String>> result = new ArrayList<>();

    for (int i = 0; i < getProposalsCount(); i++) {
      result.add(getProposal(i));
    }

    return result;
  }

  public List<String> getAllDescriptions() {
    return getAllProposals().stream().map(proposal -> proposal.first).collect(Collectors.toList());
  }

  public List<String> getProposalsKeyBinding() {
    return getAllProposals().stream().map(proposal -> proposal.second).collect(Collectors.toList());
  }

  public String getProposalDescription(String keyBinding) {
    return getAllProposals()
        .stream()
        .filter(proposal -> keyBinding.equals(proposal.second))
        .findFirst()
        .get()
        .first;
  }

  public String getProposalKeyBinding(String proposalDescription) {
    return getAllProposals()
        .stream()
        .filter(proposal -> proposalDescription.equals(proposal.first))
        .findFirst()
        .get()
        .second;
  }

  public boolean isProposalSelected(int proposalIndex) {
    final String proposalXpath = format(PROPOSAL_XPATH_TEMPLATE, proposalIndex);
    final String selectionStateAttribute = "aria-selected";

    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(By.xpath(proposalXpath), selectionStateAttribute)
        .equals("true");
  }

  public boolean isProposalSelected(String proposalDescription) {
    return isProposalSelected(getProposalIndex(proposalDescription));
  }

  public int getProposalIndex(String proposalDescription) {

    for (int i = 0; i < getProposalsCount(); i++) {
      if (proposalDescription.equals(getProposalDescription(i))) {
        return i;
      }
    }

    final String errorMessage =
        format(
            "Proposal with description: \"%s\" has not been found. Available descriptions: %s",
            proposalDescription, getAllDescriptions());

    throw new RuntimeException(errorMessage);
  }

  public void clickOnProposal(int proposalIndex) {
    final int adoptedProposalIndex = proposalIndex + 1;
    String proposalItemXpath = format(PROPOSAL_XPATH_TEMPLATE, adoptedProposalIndex);

    seleniumWebDriverHelper.waitNoExceptions(
        () -> clickOnItem(proposalItemXpath), StaleElementReferenceException.class);
  }

  private void clickOnItem(String itemXpath) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(itemXpath));
  }

  public void clickOnProposal(String proposalDescription) {
    final int proposalIndex = getProposalIndex(proposalDescription);
    clickOnProposal(proposalIndex);
  }

  public void pressEnter() {
    seleniumWebDriverHelper.pressEnter();
  }

  public void pressArrowUp() {
    seleniumWebDriverHelper.pressArrowUp();
  }

  public void pressArrowDown() {
    seleniumWebDriverHelper.pressArrowDown();
  }

  public String getSearchFieldText() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.xpath(SEARCH_FIELD_XPATH));
  }

  public void clearSearchFieldByPressingBackspace() {
    final int symbolsCount = getSearchFieldText().length();

    seleniumWebDriverHelper.waitNoExceptions(
        () -> {
          for (int i = 0; i < symbolsCount; i++) {
            seleniumWebDriverHelper.pressBackspace();
          }
        },
        ELEMENT_TIMEOUT_SEC,
        StaleElementReferenceException.class);
  }
}
