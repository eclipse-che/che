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
package org.eclipse.che.selenium.dashboard;

import static java.lang.String.format;

import com.google.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace.StateWorkspace;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class RenameWorkspaceTest {
  private static final String CHANGE_WORKSPACE_NAME = NameGenerator.generate("workspace_new", 4);

  @Inject private Dashboard dashboard;
  @Inject private DashboardWorkspace dashboardWorkspace;
  @Inject private TestWorkspace ws;
  @Inject private TestUser user;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  private String workspaceName;

  @BeforeClass
  public void setUp() throws Exception {
    this.workspaceName = ws.getName();
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(CHANGE_WORKSPACE_NAME, user.getName());
  }

  @Test
  public void renameNameWorkspaceTest() throws IOException {
    dashboard.selectWorkspacesItemOnDashboard();
    dashboardWorkspace.waitToolbarTitleName("Workspaces");
    dashboardWorkspace.selectWorkspaceItemName(workspaceName);
    dashboardWorkspace.waitToolbarTitleName(workspaceName);
    dashboardWorkspace.selectTabInWorspaceMenu(DashboardWorkspace.TabNames.OVERVIEW);
    dashboardWorkspace.enterNameWorkspace(CHANGE_WORKSPACE_NAME);
    dashboardWorkspace.clickOnSaveBtn();
    dashboardWorkspace.checkStateOfWorkspace(StateWorkspace.STOPPING);

    // This temporary solution for detect problem with this test
    // we will make screenshot every 5 ms for understanding problem
    screenshot(StateWorkspace.STOPPING.getStatus());

    int i = 1;
    while (!checkStateOfWorkspaceIsStarting() && i < 120) {
      screenshot(StateWorkspace.STARTING.getStatus() + i);
      WaitUtils.sleepQuietly(500, TimeUnit.MILLISECONDS);
      i++;
    }

    dashboardWorkspace.checkStateOfWorkspace(StateWorkspace.RUNNING);
    dashboard.waitNotificationMessage("Workspace updated");
    dashboard.waitNotificationIsClosed();
    dashboardWorkspace.checkNameWorkspace(CHANGE_WORKSPACE_NAME);
  }

  private void screenshot(String m) throws IOException {
    byte[] data = ((SeleniumWebDriver) dashboard.driver()).getScreenshotAs(OutputType.BYTES);
    Path screenshot =
        Paths.get(Paths.get("target/screenshots").toString(), "RenameWorkspaceTest_" + m + ".png");
    Files.createDirectories(screenshot.getParent());
    Files.copy(new ByteArrayInputStream(data), screenshot);
  }

  public boolean checkStateOfWorkspaceIsStarting() {
    try {
      dashboard
          .driver()
          .findElement(
              By.xpath(
                  format("//div[contains(@class, 'workspace-status')]/span[text()='starting']")));
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }
}
