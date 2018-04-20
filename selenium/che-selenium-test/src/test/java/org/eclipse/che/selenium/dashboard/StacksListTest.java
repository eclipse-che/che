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
package org.eclipse.che.selenium.dashboard;

import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.stacks.StackDetails;
import org.eclipse.che.selenium.pageobject.dashboard.stacks.Stacks;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StacksListTest {
  @Inject private Dashboard dashboard;
  @Inject private Stacks stacks;
  @Inject private StackDetails stackDetails;

  private final String NEW_STACK_NAME = NameGenerator.generate("", 8);

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();
  }

  @BeforeMethod
  public void openStacksListPage() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectStacksItemOnDashboard();

    stacks.waitToolbarTitleName();
  }

  @Test
  public void checkStacksList() {
    Assert.assertTrue(stacks.isStackItemExists("Java"));
    assertEquals(
        stacks.getStackDescription("Java"), "Default Java Stack with JDK 8, Maven and Tomcat.");
    assertEquals(stacks.getStackComponents("Java"), "JDK, Maven, Tomcat");
    // Assert.assertFalse(stacks.isDeleteStackButtonEnabled("Java"));
    stacks.openStackDetails("Java");
    stackDetails.waitToolbar("Java");
  }

  @Test
  public void createNewStack() {
    stacks.clickOnAddStackButton();

    stackDetails.setStackName(NEW_STACK_NAME);
    stackDetails.clickOnSaveChangesButton();
    stackDetails.backToStacksList();
    Assert.assertTrue(stacks.isStackItemExists(NEW_STACK_NAME));

    stacks.selectAllStacksByBulk();
    stacks.clickOnDeleteStackButton();
    stacks.clickOnDeleteDialogButton();
    dashboard.waitNotificationMessage("Selected stacks have been successfully removed.");
    dashboard.waitNotificationIsClosed();
    Assert.assertFalse(stacks.isStackItemExists(NEW_STACK_NAME));
  }
}
