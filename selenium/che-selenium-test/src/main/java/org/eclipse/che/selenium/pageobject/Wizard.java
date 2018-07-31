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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Zaryana Dombrovskaya */
@Singleton
public class Wizard {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public Wizard(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String CREATE_PROJECT_WIZARD = "gwt-debug-newProject-categoriesPanel";
    String PROJECT_NAME_INPUT = "gwt-debug-file-newProject-projectName";
    String SAMPLES_SELECTOR_BY_FORMAT_STRING =
        "//div[@id='gwt-debug-newProject-categoriesPanel']//div[text()='%s']";
    String SAVE_CREATE_BUTTON = "projectWizard-saveButton";
    String NEXT_BTN_ID = "projectWizard-nextStepButton";
    String MAIN_FORM_ID = "gwt-debug-projectWizard-window";
    String GROUP_ID_INPUT = "//div[text() = 'Group ID:']//following::input";
    String ARTIFACT_ID_INPUT = "//div[text() = 'Artifact ID:']//following::input";
    String VERSION_INPUT = "//div[text() = 'Version:']//following::input";
    String PARENT_DIRECTORY_INPUT =
        "//table[@id='gwt-debug-projectWizard-window']//div[text()='Parent:']/following-sibling::div";
    String SELECT_PACKAGING_DROPDOWN = "//div[@id='gwt-debug-mavenPageView-packagingField']";
    String SELECT_PACKAGING_DROPDOWN_BLOCK =
        "//div[@id='gwt-debug-mavenPageView-packagingField']//span";
    String FOLDER_BROWSE_BUTTON_XPATH =
        "//div[text()='%s']/following-sibling::button[text()='Browse']";
    String FOLDER_PATH_FIELD_XPATH = "//div[text()='%s']/following-sibling::input";
    String SELECT_PATH_FOR_PARENT_BTN = "//div[text()='Parent:']/parent::div/button";
    String CLOSE_ICON_CSS = "table#gwt-debug-projectWizard-window svg[width='8px'][height='8px']";
    String ARCHETYPE_CK_BOX_ID = "gwt-debug-mavenPageView-generateFromArchetype-label";
    String ARCHETYPE_DROP_DAWN_ID = "gwt-debug-mavenPageView-archetypeField";
  }

  public interface TypeProject {
    String TYPE_PREFIX_ID = "gwt-debug-projectWizard-";
    String MAVEN = "Maven";
    String PLAIN_JAVA = "Java";
    String BLANK = "Blank";
  }

  public interface SamplesName {
    String WEB_JAVA_SPRING = "web-java-spring";
    String CONSOLE_JAVA_SIMPLE = "console-java-simple";
    String ASP_DOT_NET_WEB_SIMPLE = "dotnet-web-simple";
    String WEB_JAVA_PETCLINIC = "web-java-petclinic";
    String CONSOLE_CPP_SIMPLE = "console-cpp-simple";
    String CONSOLE_PYTHON3_SIMPLE = "console-python3.5-simple";
    String NODEJS_HELLO_WORLD = "nodejs-hello-world";
  }

  public interface TypeFolder {
    String SOURCE_FOLDER = "Source Folder:";
    String LIBRARY_FOLDER = "Library Folder:";
  }

  public enum PackagingMavenType {
    JAR,
    WAR,
    POM,
    NOT_SPECIFIED
  }

  public enum Archetypes {
    QUICK_START("org.apache.maven.archetypes:maven-archetype-webapp:RELEASE"),
    WEB_APP("org.apache.maven.archetypes:maven-archetype-webapp:RELEASE"),
    TOMEE("org.apache.openejb.maven:tomee-webapp-archetype:1.7.1");
    private final String type;

    Archetypes(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return type;
    }
  }

  @FindBy(id = Locators.MAIN_FORM_ID)
  WebElement mainForm;

  @FindBy(id = Locators.CREATE_PROJECT_WIZARD)
  WebElement createProjectWizardForm;

  @FindBy(id = Locators.PROJECT_NAME_INPUT)
  WebElement projectNameInput;

  @FindBy(xpath = Locators.PARENT_DIRECTORY_INPUT)
  WebElement parentDirectoryInput;

  @FindBy(id = Locators.SAVE_CREATE_BUTTON)
  WebElement saveCreateBtn;

  @FindBy(id = Locators.NEXT_BTN_ID)
  WebElement nextButton;

  @FindBy(css = Locators.CLOSE_ICON_CSS)
  WebElement closeIcon;

  @FindBy(id = Locators.ARCHETYPE_CK_BOX_ID)
  WebElement fromArchetypeChkBox;

  @FindBy(id = Locators.ARCHETYPE_DROP_DAWN_ID)
  WebElement archetypeDropDown;

  /**
   * select type project on the Wizard/Configuration form
   *
   * @param typeProject is the type project
   */
  public void selectTypeProject(String typeProject) {
    waitTypeProject(typeProject);
    loader.waitOnClosed();
    seleniumWebDriver.findElement(By.id(TypeProject.TYPE_PREFIX_ID + typeProject)).click();
  }

  /** wait type project in the Configuration wizard form */
  public void waitTypeProject(String typeProject) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(TypeProject.TYPE_PREFIX_ID + typeProject)));
  }

  /**
   * check present a text in group id field On Wizard
   *
   * @param text
   * @return is text present
   */
  public void checkGroupIdOnWizardContainsText(final String text) {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver input) {
                WebElement element =
                    seleniumWebDriver.findElement(By.xpath(Locators.GROUP_ID_INPUT));
                return element.getAttribute("value").contains(text);
              }
            });
  }

  /**
   * check present a text in artifact id field On Wizard
   *
   * @param text
   * @return is text present
   */
  public void checkArtifactIdOnWizardContainsText(final String text) {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver input) {
                WebElement element =
                    seleniumWebDriver.findElement(By.xpath(Locators.ARTIFACT_ID_INPUT));
                return element.getAttribute("value").contains(text);
              }
            });
  }

  /**
   * check present a text in version field On Wizard
   *
   * @param text
   * @return is text present
   */
  public void checkVersionOnWizardContainsText(final String text) {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver input) {
                WebElement element =
                    seleniumWebDriver.findElement(By.xpath(Locators.VERSION_INPUT));
                return element.getAttribute("value").contains(text);
              }
            });
  }

  /**
   * Set value in version field on Wizard
   *
   * @param version value of version
   */
  public void setVersionOnWizard(String version) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(Locators.VERSION_INPUT)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(Locators.VERSION_INPUT)))
        .sendKeys(version);
  }

  /**
   * Set value in artifact ID field on Wizard
   *
   * @param artifactId value of artifact ID
   */
  public void setArtifactIdOnWizard(String artifactId) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(Locators.ARTIFACT_ID_INPUT)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(Locators.ARTIFACT_ID_INPUT)))
        .sendKeys(artifactId);
  }

  /**
   * Set value in group ID field on Wizard
   *
   * @param groupId value of group ID
   */
  public void setGroupIdOnWizard(String groupId) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(Locators.GROUP_ID_INPUT)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(Locators.GROUP_ID_INPUT)))
        .sendKeys(groupId);
  }

  /** wait wizard form */
  public void waitCreateProjectWizardForm() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(ExpectedConditions.visibilityOf(createProjectWizardForm));
  }

  /** wait wizard form is closed */
  public void waitCreateProjectWizardFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, 20)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.CREATE_PROJECT_WIZARD)));
  }

  /**
   * wait sample into Samples list
   *
   * @param sample
   */
  public void waitSample(String sample) {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.SAMPLES_SELECTOR_BY_FORMAT_STRING, sample))));
  }

  /**
   * select sample into Samples list with click
   *
   * @param sample
   */
  public void selectSample(String sample) {
    waitSample(sample);
    loader.waitOnClosed();
    seleniumWebDriver
        .findElement(By.xpath(String.format(Locators.SAMPLES_SELECTOR_BY_FORMAT_STRING, sample)))
        .click();
  }

  /** wait project name on wizard form */
  public void waitProjectNameOnWizard() {
    new WebDriverWait(seleniumWebDriver, 120)
        .until(ExpectedConditions.visibilityOf(projectNameInput));
  }

  /** get name project name from the 'Project Configuration' form */
  public String getProjectNameInputNameOnWizard() {
    waitProjectNameOnWizard();
    return projectNameInput.getAttribute("value");
  }

  /**
   * wait text from the project 'Name' input
   *
   * @param projectName is expected text from the project 'Name' input
   */
  public void waitTextProjectNameInput(String projectName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getProjectNameInputNameOnWizard().equals(projectName));
  }

  /** type project name on wizard */
  public void typeProjectNameOnWizard(String projectName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(projectNameInput))
        .clear();
    waitProjectNameOnWizard();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(projectNameInput))
        .sendKeys(projectName);
    loader.waitOnClosed();
  }

  /** wait appear create button on wizard form and click */
  public void clickCreateButton() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(saveCreateBtn));
    saveCreateBtn.click();
    loader.waitOnClosed();
  }

  /** click on save button on the Project Configuration form */
  public void clickSaveButton() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(saveCreateBtn));
    saveCreateBtn.click();
    loader.waitOnClosed();
  }

  /** wait main form Project Configuration when importing project from remote repo */
  public void waitOpenProjectConfigForm() {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(mainForm));
    loader.waitOnClosed();
  }

  public void waitCloseProjectConfigForm() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.MAIN_FORM_ID)));
  }

  /** wait parent directory name on the 'Project Configuration' form */
  public void waitParentDirectoryInputOnWizard() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(parentDirectoryInput));
  }

  /** get text from the 'Parent Directory' input name */
  public String getNameFromParentDirectoryInput() {
    waitParentDirectoryInputOnWizard();
    return parentDirectoryInput.getText();
  }

  /**
   * wait text from the 'Parent Directory' input
   *
   * @param nameDirectory is expected text from the 'Parent Directory' input
   */
  public void waitTextParentDirectoryName(String nameDirectory) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getNameFromParentDirectoryInput().equals(nameDirectory));
  }

  /** click on next button on Project Configuration form */
  public void clickNextButton() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(nextButton))
        .click();
  }

  /** click the 'Browse' button on the 'Project Configuration' form */
  public void clickBrowseButton(String typeFolder) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(Locators.FOLDER_BROWSE_BUTTON_XPATH, typeFolder))))
        .click();
  }

  /**
   * wait text into 'Source Folder'
   *
   * @param expText is a value of text
   */
  public void waitExpTextInSourceFolder(String expText, String typeFolder) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.FOLDER_PATH_FIELD_XPATH, typeFolder))));
    WebElement fieldFolder =
        seleniumWebDriver.findElement(
            By.xpath(String.format(Locators.FOLDER_PATH_FIELD_XPATH, typeFolder)));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (WebDriver webDriver) -> {
              return fieldFolder.getAttribute("value").contains(expText);
            });
  }

  /**
   * Select the type of packaging on Wizard
   *
   * @param mavenType type project of Maven
   */
  public void selectPackagingType(PackagingMavenType mavenType) {
    List<WebElement> DropDownList =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath(Locators.SELECT_PACKAGING_DROPDOWN_BLOCK)));

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.SELECT_PACKAGING_DROPDOWN)))
        .click();

    switch (mavenType) {
      case JAR:
        DropDownList.get(1).click();
        break;
      case WAR:
        DropDownList.get(2).click();
        break;
      case POM:
        DropDownList.get(3).click();
        break;
      default:
        DropDownList.get(0).click();
        break;
    }
  }

  /**
   * create a project from existed samples in the wizard widget, type name of project field and
   * click on the create button
   *
   * @param projectFromList
   * @param nameProject
   */
  public void selectProjectAndCreate(String projectFromList, String nameProject) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainForm));
    selectSample(projectFromList);
    typeProjectNameOnWizard(nameProject);
    clickCreateButton();
    waitCloseProjectConfigForm();
  }

  /** Click on '...' button in the 'Project Configuration' wizard */
  public void clickOnSelectPathForParentBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeClickable(By.xpath(Locators.SELECT_PATH_FOR_PARENT_BTN)))
        .click();
  }

  /** wait the widget, click on close ('x') icon, wait closing of the widget */
  public void closeWithIcon() {
    waitOpenProjectConfigForm();
    closeIcon.click();
    waitCloseProjectConfigForm();
  }

  /** wait expected type of packaging on the 'Create New Project' widget */
  public void waitExpectedPackaging(PackagingMavenType mavenType) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.textToBe(
                By.xpath(Locators.SELECT_PACKAGING_DROPDOWN), mavenType.toString()));
  }

  /** wait 'From Archetype:' check box and click it */
  public void clickOnFromArchetypeChkBox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(fromArchetypeChkBox))
        .click();
  }

  /** wait archetype drop dawn field on the widget */
  public void waitArcheTypeDropdawn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(archetypeDropDown));
  }

  public void selectArcheTypeFromList(Archetypes type) {
    clickOnFromArchetypeChkBox();
    waitArcheTypeDropdawn();
    archetypeDropDown.click();
    WebElement item =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.visibilityOf(
                    archetypeDropDown.findElement(
                        By.xpath("//label[text()='" + type.toString() + "']"))));
    item.click();
  }
}
