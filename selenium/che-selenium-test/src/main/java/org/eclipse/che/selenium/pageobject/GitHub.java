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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** The Object containing GitHubAuthPage page`s webelenents */
@Singleton
public class GitHub {
  private final SeleniumWebDriver seleniumWebDriver;
  private final DefaultTestUser testUser;

  @Inject
  public GitHub(SeleniumWebDriver seleniumWebDriver, DefaultTestUser testUser) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.testUser = testUser;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {

    String LOGIN_FIELD = "login_field";

    String PASS_FIELD = "password";

    String SIGN_IN_BTN = "input[value='Sign in']";

    String ACCOUNT_SETTINGS = "account_settings";

    String APPLICATIONS_BTN = "//*[@id='site-container']//a[contains(.,'Applications')]";

    String REVOKE_BTN = "//a[contains(.,'envy')]/..//a[@data-method='delete']/span";

    String AUTHORIZE_BUTTON = "//button[@id='js-oauth-authorize-btn']";

    String LOGOUT_BTN = "logout";

    String SSH_KEYS_BTN = "//*[@id='site-container']//a[contains(.,'SSH Keys')]";

    String DELETE_SSH_KEY_BTN = "//li[contains(.,'%s')]/a[@data-method='delete']";

    String CONFIRM_PASS_INPUT_ID = "//input[@id='confirm-password']";

    String CONFIRM_PASS_BUTTON = "//button[text()='Confirm password']";

    String APPLICATIONS_PAGE = "//h3[text()='Authorized applications']";

    String SSH_PAGE = "//a[@id='add_key_action']";

    String SUDO_PASS_FORM_ID = "sudo_password";
  }

  @FindBy(id = Locators.LOGIN_FIELD)
  public WebElement loginField;

  @FindBy(id = Locators.PASS_FIELD)
  public WebElement passField;

  @FindBy(css = Locators.SIGN_IN_BTN)
  public WebElement signInBtn;

  @FindBy(id = Locators.ACCOUNT_SETTINGS)
  public WebElement accountSettings;

  @FindBy(xpath = Locators.APPLICATIONS_BTN)
  public WebElement applicationsBtn;

  @FindBy(xpath = Locators.REVOKE_BTN)
  public WebElement revokeBtn;

  @FindBy(xpath = Locators.AUTHORIZE_BUTTON)
  public WebElement authorizeBtn;

  @FindBy(id = Locators.LOGOUT_BTN)
  public WebElement logoutBtn;

  @FindBy(xpath = Locators.SSH_KEYS_BTN)
  public WebElement sshKeysBtn;

  @FindBy(xpath = Locators.CONFIRM_PASS_INPUT_ID)
  public WebElement confirmPassInput;

  @FindBy(xpath = Locators.CONFIRM_PASS_BUTTON)
  public WebElement confirmPassBtn;

  @FindBy(id = Locators.SUDO_PASS_FORM_ID)
  public WebElement sudoPassForm;

  /** Wait web elements for login on google. */
  public void waitAuthorizationPageOpened() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver elem) {
                return loginField.isDisplayed()
                    && passField.isDisplayed()
                    && signInBtn.isDisplayed();
              }
            });
  }

  /**
   * type login to login field
   *
   * @param login
   */
  public void typeLogin(String login) {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(loginField));
    loginField.sendKeys(login);
  }

  /**
   * type password to password field
   *
   * @param pass
   */
  public void typePass(String pass) {
    passField.sendKeys(pass);
  }

  /** wait closing github login form */
  public void waitClosingLoginPage() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.PASS_FIELD)));
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.LOGIN_FIELD)));
  }

  /** click on submit btn on github auth form */
  public void clickOnSignInButton() {
    new WebDriverWait(seleniumWebDriver, 15)
        .until(ExpectedConditions.elementToBeClickable(signInBtn));
    signInBtn.click();
  }

  public void openGithub() {
    seleniumWebDriver.get("https://github.com/login");
    waitAccountSettingsBtn();
  }

  public void openGithubAndLogin(String login, String pass) {
    seleniumWebDriver.get("https://github.com/login");
    waitAuthorizationPageOpened();
    typeLogin(login);
    typePass(pass);
    clickOnSignInButton();
    waitAccountSettingsBtn();
  }

  /** wait for appearing account settings button */
  public void waitAccountSettingsBtn() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.ACCOUNT_SETTINGS)));
  }

  /**
   * wait for appearing appereance supo password form after click on autorize button form if we have
   * not ssh key on github
   */
  public void waitSudoPassForm() {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(sudoPassForm));
  }

  /** type password to sudo field on github */
  public void typeToSudoPassForm(String pass) {
    sudoPassForm.sendKeys(pass);
  }

  /** click on account settings button */
  public void clickOnAccountSettingsButton() {
    accountSettings.click();
  }

  /** wait for appearing applications button in settings menu. */
  public void waitApplicationsBtn() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.APPLICATIONS_BTN)));
  }

  /** click on applications button in settings menu. */
  public void clickOnApplicationsButton() {
    applicationsBtn.click();
  }

  /** wait for appearing Revoke button in applications menu. */
  public boolean isRevokeBtnPresent() {
    try {
      WebElement element = seleniumWebDriver.findElement(By.xpath(Locators.REVOKE_BTN));
      return element.isDisplayed();
    } catch (Exception e) {
      System.err.println("Element not found!");
      return false;
    }
  }

  /** wait for disappearing Revoke button in applications menu. */
  public void waitRevokeBtnDisappear() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.REVOKE_BTN)));
  }

  /** click on Revoke button in applications menu. */
  public void clickOnRevokeButton() {
    revokeBtn.click();
  }

  /**
   * wait and check if authorization button is present.
   *
   * @return true if button is present, false otherwise
   */
  public boolean isAuthorizeButtonPresent() {
    try {
      waitAuthorizeBtn();
      return true;
    } catch (TimeoutException e) {
      return false;
    }
  }

  /** wait for authorize button. */
  public void waitAuthorizeBtn() {
    new WebDriverWait(seleniumWebDriver, 30).until(ExpectedConditions.visibilityOf(authorizeBtn));
  }

  /** click on authorize button */
  public void clickOnAuthorizeBtn() {
    waitAuthorizeBtn();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(authorizeBtn));
    authorizeBtn.click();
  }

  /** logout from github */
  public void clickOnLogoutBtn() {
    logoutBtn.click();
  }

  /** Open settings, check and delete SSH key */
  public void checkAndDeleteSshKey() {
    clickOnAccountSettingsButton();
    waitSshKeysBtn();
    clickOnSshKeysButton();
    waitSshPage();
    if (isDeleteSshKeyButtonPresent()) {
      clickOnDeleteSshKeyButton();
      waitDeleteButtonDisappear();
    }
  }

  /** wait for appearing ssh keys button in settings menu. */
  public void waitSshKeysBtn() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.SSH_KEYS_BTN)));
  }

  /** click on ssh keys button */
  public void clickOnSshKeysButton() {
    sshKeysBtn.click();
  }

  public boolean isDeleteSshKeyButtonPresent() {
    try {
      WebElement element =
          seleniumWebDriver.findElement(
              By.xpath(String.format(Locators.DELETE_SSH_KEY_BTN, testUser.getName())));
      return element.isDisplayed();
    } catch (Exception e) {
      System.err.println("Element not found!");
      return false;
    }
  }

  /** wait for appearing delete button in ssh keys menu. */
  public void waitDeleteButton() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.DELETE_SSH_KEY_BTN, testUser.getName()))));
  }

  /** wait for disappearing of delete button in ssh keys menu. */
  public void waitDeleteButtonDisappear() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(String.format(Locators.DELETE_SSH_KEY_BTN, testUser.getName()))));
  }

  /** click on delete ssh key */
  public void clickOnDeleteSshKeyButton() {
    WebElement elem =
        seleniumWebDriver.findElement(
            By.xpath(String.format(Locators.DELETE_SSH_KEY_BTN, testUser.getName())));
    elem.click();
  }

  /** wait for confirm button appear */
  public void waitConfirmPasswordInput() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.CONFIRM_PASS_INPUT_ID)));
  }

  public boolean isConfirmPasswordInputPresent() {
    try {
      WebElement element = seleniumWebDriver.findElement(By.xpath(Locators.CONFIRM_PASS_INPUT_ID));
      return element.isDisplayed();
    } catch (Exception ex) {
      return false;
    }
  }

  public void typeConfirmationPassword(String pass) {
    confirmPassInput.sendKeys(pass);
  }

  public void clickConfirmPasswordButton() {
    confirmPassBtn.click();
  }

  /** wait for applications page form */
  public void waitApplicationsPage() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.APPLICATIONS_PAGE)));
  }

  /** wait for ssh page form */
  public void waitSshPage() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.SSH_PAGE)));
  }
}
