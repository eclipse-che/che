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
package org.eclipse.che.selenium.pageobject.site;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Andrey Chizhikov */
@Singleton
public class ProfilePage {

  private interface Locators {
    String GET_STARTED = "//input[@value='Get Started']";
    String ROLE = "jobtitle";
    String COUNTRY = "country";
  }

  public enum Field {
    FirstName("firstName"),
    LastName("lastName"),
    Company("employer");
    private final String fieldId;

    Field(String fieldId) {
      this.fieldId = fieldId;
    }

    public String getFieldId() {
      return fieldId;
    }
  }

  public enum Role {
    Developer("Developer"),
    Frilanse("Freelance");
    private final String role;

    Role(String role) {
      this.role = role;
    }

    public String getRole() {
      return role;
    }
  }

  public enum Country {
    Ukraine("Ukraine"),
    UnitedStates("United States");
    private final String country;

    Country(String country) {
      this.country = country;
    }

    public String getCountry() {
      return country;
    }
  }

  @FindBy(xpath = Locators.GET_STARTED)
  WebElement getStartedButton;

  @FindBy(id = Locators.ROLE)
  WebElement roleSelect;

  @FindBy(id = Locators.COUNTRY)
  WebElement countySelect;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public ProfilePage(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /**
   * enter value into field
   *
   * @param value text for field
   * @param field name Of field
   */
  public void enterValueInField(String value, Field field) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.id(field.getFieldId())))
        .clear();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.id(field.getFieldId())))
        .sendKeys(value);
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBePresentInElementValue(By.id(field.getFieldId()), value));
  }

  public boolean profileFormExists() {
    try {
      new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
          .until(
              ExpectedConditions.visibilityOf(seleniumWebDriver.findElement(By.id("firstName"))));
      return true;
    } catch (TimeoutException e) {
      return false;
    }
  }

  public boolean isGetStartedButtonPresent() {
    try {
      new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
          .until(ExpectedConditions.visibilityOf(getStartedButton));
      return true;
    } catch (TimeoutException e) {
      return false;
    }
  }

  /** click on 'Get Started' button */
  public void clickOnGetStarted() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(getStartedButton))
        .click();
  }

  /**
   * select role for the profile
   *
   * @param role
   */
  public void selectRole(Role role) {
    Select select =
        new Select(
            new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
                .until(ExpectedConditions.visibilityOf(roleSelect)));
    select.selectByVisibleText(role.getRole());
  }

  /**
   * select country for the profile
   *
   * @param country
   */
  public void selectCountry(Country country) {
    Select select =
        new Select(
            new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
                .until(ExpectedConditions.visibilityOf(countySelect)));
    select.selectByVisibleText(country.getCountry());
  }

  public void handleProfileOnboardingWithTestData() {
    if (isGetStartedButtonPresent()) {
      if (profileFormExists()) {
        enterValueInField("Test", ProfilePage.Field.FirstName);
        enterValueInField("Account", ProfilePage.Field.LastName);
        enterValueInField("AnyCompany", ProfilePage.Field.Company);
        selectRole(ProfilePage.Role.Developer);
        selectCountry(ProfilePage.Country.Ukraine);
        clickOnGetStarted();
      }
    }
  }
}
