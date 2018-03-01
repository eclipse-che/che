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
package org.eclipse.che.selenium.languageserver;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MINIMUM_SEC;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckMainFeatureForCSharpLanguageTest {

  private final String PROJECT_NAME = NameGenerator.generate("AspProject", 4);
  private final String COMMAND_NAME_FOR_RESTORE_LS = PROJECT_NAME + ": update dependencies";

  @InjectTestWorkspace(template = WorkspaceTemplate.UBUNTU_LSP)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private CommandsPalette commandsPalette;

  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
  }

  @Test
  public void checkLaunchingCodeserver() {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    wizard.selectSample(Wizard.SamplesName.ASP_DOT_NET_WEB_SIMPLE);
    wizard.typeProjectNameOnWizard(PROJECT_NAME);
    wizard.clickCreateButton();
    wizard.waitCloseProjectConfigForm();
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME + "/Program.cs", 240);
    projectExplorer.openItemByPath(PROJECT_NAME + "/Program.cs");
    loader.waitOnClosed();
    checkLanguageServerInitStateAndLaunch();
    editor.goToCursorPositionVisible(24, 12);
    for (int i = 0; i < 9; i++) {
      editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    }
    editor.waitMarkerInPosition(ERROR, 23);
    editor.waitMarkerInPosition(ERROR, 21);
    editor.goToCursorPositionVisible(23, 49);
    editor.typeTextIntoEditor(".");
    editor.launchAutocomplete();
    editor.enterAutocompleteProposal("Build() ");
    editor.typeTextIntoEditor(";");
    editor.waitAllMarkersInvisibility(ERROR);
  }

  private void checkLanguageServerInitStateAndLaunch() {
    if (isLanguageServerInitFailed()) {
      reInitLanguageServer();
    }
  }

  private void reInitLanguageServer() {
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(COMMAND_NAME_FOR_RESTORE_LS);
    consoles.waitExpectedTextIntoConsole("Restore completed");
    editor.closeAllTabs();
    projectExplorer.openItemByPath(PROJECT_NAME + "/Program.cs");
    loader.waitOnClosed();
  }

  private boolean isLanguageServerInitFailed() {
    String xpathLocatorForEventMessages =
        "//div[contains(@id,'gwt-debug-notification-wrappergwt-uid')]";
    List<WebElement> textMessages =
        new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
            .until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath(xpathLocatorForEventMessages)));
    return textMessages
        .stream()
        .anyMatch(
            message -> message.getAttribute("textContent").contains("Timeout initializing error"));
  }
}
