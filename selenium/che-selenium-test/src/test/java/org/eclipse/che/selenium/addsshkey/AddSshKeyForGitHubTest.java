/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.addsshkey;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
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
public class AddSshKeyForGitHubTest {

  @Inject private DefaultTestUser defaultUser;
  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private Loader loader;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Menu menu;
  @Inject private Preferences preferences;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private TestGitHubServiceClient gitHubClientService;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void generateKey() throws Exception {
    loader.waitOnClosed();
    notificationsPopupPanel.waitProgressPopupPanelClose();
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();

    // set data of a committer
    preferences.waitMenuInCollapsedDropdown(
        Preferences.DropDownGitCommitterInformationMenu.COMMITTER);
    preferences.selectDroppedMenuByName(Preferences.DropDownGitCommitterInformationMenu.COMMITTER);
    preferences.typeAndWaitNameCommitter(gitHubUsername);
    preferences.typeAndWaitEmailCommitter(defaultUser.getEmail());
    preferences.clickOnOkBtn();

    gitHubClientService.deleteAllGrants(gitHubUsername, gitHubPassword);
    preferences.regenerateAndUploadSshKeyOnGithub(gitHubUsername, gitHubPassword);
  }
}
