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
import static org.eclipse.che.selenium.core.utils.FileUtil.readFile;
import static org.eclipse.che.selenium.core.utils.FileUtil.readFileToString;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR_OVERVIEW;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING_OVERVIEW;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import java.net.URL;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    final URL resource = getClass().getResource("/projects/prefs-spring-project");
    final URL embedCodeFilePath = getClass().getResource("embed-code");
    final String embedCode = readFileToString(embedCodeFilePath);

    // import project
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    // open workspace
    ide.open(workspace);
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();

    // prepare file for testing
    testProjectServiceClient.updateFile(
        workspace.getId(), PATH_TO_CLASS_IN_SPRING_PACKAGE, embedCode);

    // expand project explorer tree and wait LS init
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void errorsWarningTest() throws Exception {
    final String expectedTabTitle = "AppController";
    final URL errorsWarningFilePath = getClass().getResource("errors-warnings");
    final List<String> expectedErrorsWarningsList = readFile(errorsWarningFilePath);

    // open file
    projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_CLASS_IN_SPRING_PACKAGE);
    projectExplorer.openItemByPath(PATH_TO_CLASS_IN_SPRING_PACKAGE);
    editor.waitTabIsPresent(expectedTabTitle);
    editor.waitTabSelection(0, expectedTabTitle);
    editor.waitActive();

    // check markers default settings
    menu.runCommand(TestMenuCommandsConstants.Profile.PROFILE_MENU, PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.waitMenuInCollapsedDropdown(Preferences.DropDownJavaCompilerMenu.ERRORS_WARNINGS);
    preferences.selectDroppedMenuByName(Preferences.DropDownJavaCompilerMenu.ERRORS_WARNINGS);
    assertEquals(preferences.getItemsFromErrorWarningsWidget(), expectedErrorsWarningsList);
    preferences.close();
    consoles.closeProcessesArea();

    // change and check markers settings for displaying warnings
    menu.runCommand(TestMenuCommandsConstants.Profile.PROFILE_MENU, PREFERENCES);
    changeAllSettingsInErrorsWarningsTab(Preferences.DropDownValueForErrorWaitingWidget.WARNING);
    editor.waitAnnotationsAreNotPresent(ERROR_OVERVIEW);
    waitWarningMarkersQuantity();

    // change and check markers settings for displaying errors
    editor.waitAnnotationsAreNotPresent(ERROR_OVERVIEW);
    menu.runCommand(TestMenuCommandsConstants.Profile.PROFILE_MENU, PREFERENCES);
    changeAllSettingsInErrorsWarningsTab(Preferences.DropDownValueForErrorWaitingWidget.ERROR);
    waitErrorMarkersQuantity();

    // change and check markers settings for ignoring all markers
    editor.waitAnnotationsAreNotPresent(WARNING_OVERVIEW);
    menu.runCommand(TestMenuCommandsConstants.Profile.PROFILE_MENU, PREFERENCES);
    changeAllSettingsInErrorsWarningsTab(Preferences.DropDownValueForErrorWaitingWidget.IGNORE);
    editor.waitAnnotationsAreNotPresent(ERROR_OVERVIEW);
    editor.waitAnnotationsAreNotPresent(WARNING_OVERVIEW);
  }

  private void waitWarningMarkersQuantity() {
    // browser window resolution a bit different on local mode and on CI
    // according to this different count of markers are displayed
    editor.waitMarkersQuantityBetween(WARNING_OVERVIEW, 12, 13);
    editor.waitMarkersQuantityBetween(WARNING, 22, 24);
  }

  private void waitErrorMarkersQuantity() {
    // browser window resolution a bit different on local mode and on CI
    // according to this different count of markers are displayed
    editor.waitMarkersQuantityBetween(ERROR_OVERVIEW, 12, 13);
    editor.waitMarkersQuantityBetween(ERROR, 22, 24);
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
}
