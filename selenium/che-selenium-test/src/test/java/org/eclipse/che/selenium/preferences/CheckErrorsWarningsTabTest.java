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
package org.eclipse.che.selenium.preferences;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PREFERENCES;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.DEFAULT_TIMEOUT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR_OVERVIEW;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING_OVERVIEW;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckErrorsWarningsTabTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(CheckErrorsWarningsTabTest.class.getSimpleName(), 4);
  private static final String PATH_TO_CLASS_IN_SPRING_PACKAGE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Menu menu;
  @Inject private Preferences preferences;
  @Inject private Consoles consoles;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    consoles.waitExpectedTextIntoConsole(PROJECT_NAME);
  }

  @Test
  public void errorsWarningTest() throws Exception {
    List<String> expectedErrorsWarningsList = getTextInListFromFile("errors-warnings");
    String embedCode = getTextInStringFromFile("embed-code");
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitActive();
    loader.waitOnClosed();
    editor.waitActive();
    editor.deleteAllContent();
    editor.typeTextIntoEditorWithoutDelayForSaving(embedCode);
    loader.waitOnClosed();
    editor.removeLineAndAllAfterIt(105);

    menu.runCommand(TestMenuCommandsConstants.Profile.PROFILE_MENU, PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.waitMenuInCollapsedDropdown(Preferences.DropDownJavaCompilerMenu.ERRORS_WARNINGS);
    preferences.selectDroppedMenuByName(Preferences.DropDownJavaCompilerMenu.ERRORS_WARNINGS);
    preferences.getItemsFromErrorWarningsWidget();
    Assert.assertEquals(preferences.getItemsFromErrorWarningsWidget(), expectedErrorsWarningsList);

    preferences.close();
    consoles.closeProcessesArea();
    menu.runCommand(TestMenuCommandsConstants.Profile.PROFILE_MENU, PREFERENCES);
    changeAllSettingsInErrorsWarningsTab(Preferences.DropDownValueForErrorWaitingWidget.WARNING);
    editor.waitAnnotationsAreNotPresent(ERROR_OVERVIEW);
    Assert.assertEquals(editor.getMarkersQuantity(WARNING_OVERVIEW), 13);
    Assert.assertEquals(editor.getMarkersQuantity(WARNING), 22);

    menu.runCommand(TestMenuCommandsConstants.Profile.PROFILE_MENU, PREFERENCES);
    changeAllSettingsInErrorsWarningsTab(Preferences.DropDownValueForErrorWaitingWidget.ERROR);
    editor.waitAnnotationsAreNotPresent(WARNING_OVERVIEW);
    Assert.assertEquals(editor.getMarkersQuantity(ERROR_OVERVIEW), 13);
    Assert.assertEquals(editor.getMarkersQuantity(ERROR), 22);

    menu.runCommand(TestMenuCommandsConstants.Profile.PROFILE_MENU, PREFERENCES);
    changeAllSettingsInErrorsWarningsTab(Preferences.DropDownValueForErrorWaitingWidget.IGNORE);
    editor.waitAnnotationsAreNotPresent(ERROR_OVERVIEW);
    editor.waitAnnotationsAreNotPresent(WARNING_OVERVIEW);
  }

  private void changeAllSettingsInErrorsWarningsTab(
      Preferences.DropDownValueForErrorWaitingWidget valueOfRadioButton) {
    preferences.waitPreferencesForm();
    preferences.waitMenuInCollapsedDropdown(Preferences.DropDownJavaCompilerMenu.ERRORS_WARNINGS);
    preferences.selectDroppedMenuByName(Preferences.DropDownJavaCompilerMenu.ERRORS_WARNINGS);
    preferences.setAllSettingsInErrorWaitingWidget(valueOfRadioButton);
    preferences.clickOnOkBtn();
    preferences.close();
    loader.waitOnClosed();
    projectExplorer.waitItem(PATH_TO_CLASS_IN_SPRING_PACKAGE);
    projectExplorer.openItemByPath(PATH_TO_CLASS_IN_SPRING_PACKAGE);
    loader.waitOnClosed();
    editor.setCursorToLine(85);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ARROW_LEFT.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    WaitUtils.sleepQuietly(DEFAULT_TIMEOUT);
  }

  private String getTextInStringFromFile(String path) throws Exception {
    URL pathInUrl = CheckErrorsWarningsTabTest.class.getResource(path);
    List<String> textFromFile =
        Files.readAllLines(Paths.get(pathInUrl.toURI()), Charset.forName("UTF-8"));
    return Joiner.on('\n').join(textFromFile);
  }

  private List<String> getTextInListFromFile(String path) throws Exception {
    URL pathInUrl = CheckErrorsWarningsTabTest.class.getResource(path);
    return Files.readAllLines(Paths.get(pathInUrl.toURI()), Charset.forName("UTF-8"));
  }
}
