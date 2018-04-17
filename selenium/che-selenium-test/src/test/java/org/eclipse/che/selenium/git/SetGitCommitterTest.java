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
package org.eclipse.che.selenium.git;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Kuznetsov Mihail */
public class SetGitCommitterTest {

  @Inject private DefaultTestUser defaultUser;
  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private Loader loader;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Menu menu;
  @Inject private Preferences preferences;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void testCommitterSettings() throws Exception {
    loader.waitOnClosed();
    notificationsPopupPanel.waitProgressPopupPanelClose();
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();

    preferences.waitMenuInCollapsedDropdown(
        Preferences.DropDownGitCommitterInformationMenu.COMMITTER);
    preferences.selectDroppedMenuByName(Preferences.DropDownGitCommitterInformationMenu.COMMITTER);
    preferences.typeAndWaitNameCommitter(defaultUser.getName());
    preferences.typeAndWaitEmailCommitter(defaultUser.getEmail());
    preferences.clickOnOkBtn();
  }
}
