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

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.BLANK;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA_MYSQL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.stacks.StackDetails;
import org.eclipse.che.selenium.pageobject.dashboard.stacks.Stacks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StacksListTest {
  @Inject private Dashboard dashboard;
  @Inject private Stacks stacks;
  @Inject private StackDetails stackDetails;

  private static final String NEW_STACK_NAME = generate("", 8);

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
    // check all headers are present
    ArrayList<String> headers = stacks.getStacksListHeaders();
    assertTrue(headers.contains("NAME"));
    assertTrue(headers.contains("DESCRIPTION"));
    assertTrue(headers.contains("COMPONENTS"));
    assertTrue(headers.contains("ACTIONS"));

    // check stack info
    assertTrue(stacks.isStackItemExists(JAVA.getName()));
    assertEquals(
        stacks.getStackDescription(JAVA.getName()),
        "Default Java Stack with JDK 8, Maven and Tomcat.");
    assertEquals(stacks.getStackComponents(JAVA.getName()), "JDK, Maven, Tomcat");

    // Assert.assertFalse(stacks.isDeleteStackButtonEnabled(TestStacksConstants.JAVA.getName()));
    stacks.openStackDetails(JAVA.getName());
    stackDetails.waitToolbar(JAVA.getName());
  }

  @Test
  public void createNewStack() { // TODO change method name
    stacks.clickOnAddStackButton();
    stackDetails.setStackName(NEW_STACK_NAME);
    stackDetails.clickOnSaveChangesButton();
    stackDetails.backToStacksList();
    WaitUtils.sleepQuietly(1); // TODO wait Stacks list page is loaded
    assertTrue(stacks.isStackItemExists(NEW_STACK_NAME));

    stacks.selectAllStacksByBulk();
    deleteStack();

    assertFalse(stacks.isStackItemExists(NEW_STACK_NAME));
  }

  @Test
  public void checkDuplicateStackButton() {
    // create stack duplicate by Duplicate Stack button
    stacks.clickOnDuplicateStackActionButton(JAVA.getName());
    assertTrue(stacks.isStackDuplicateExists("Java-copy-"));
    WaitUtils.sleepQuietly(1);
    stacks.clickOnDuplicateStackActionButton(BLANK.getName());
    assertTrue(stacks.isStackDuplicateExists("Blank-copy-"));

    // TODO delete duplicated stacks
  }

  @Test
  public void checkStacksFiltering() {
    // filter stacks by nonexistent name
    stacks.typeToSearchInput("*");
    stacks.waitNoStacksFound();

    // search a workspace by a full name
    stacks.typeToSearchInput(JAVA.getName());
    assertTrue(stacks.isStackItemExists(JAVA.getName()));
    assertTrue(stacks.isStackItemExists(JAVA_MYSQL.getName()));
    assertFalse(stacks.isStackItemExists(BLANK.getName()));

    stacks.typeToSearchInput(BLANK.getName());
    assertTrue(stacks.isStackItemExists(BLANK.getName()));
    assertFalse(stacks.isStackItemExists(JAVA.getName()));
    assertFalse(stacks.isStackItemExists(JAVA_MYSQL.getName()));

    // search a workspace by a part name
    stacks.typeToSearchInput(BLANK.getName().substring(BLANK.getName().length() / 2));
    assertTrue(stacks.isStackItemExists(BLANK.getName()));
    assertFalse(stacks.isStackItemExists(JAVA.getName()));
    assertFalse(stacks.isStackItemExists(JAVA_MYSQL.getName()));
  }

  @Test
  public void checkStacksSorting() {
    ArrayList<String> listAfterSorting, listBeforeSorting;

    // get stacks names list and click on sort stacks button
    listBeforeSorting = stacks.getStacksNamesList();
    // TODO fix need to click on the sort button twice
    stacks.clickOnSortStacksByNameButton();
    stacks.clickOnSortStacksByNameButton();

    // get stacks name list after sorting and check it with reverted list before sorting
    listAfterSorting = stacks.getStacksNamesList();
    Collections.reverse(listBeforeSorting);
    assertEquals(listBeforeSorting, listAfterSorting);

    stacks.clickOnSortStacksByNameButton();
    listBeforeSorting = stacks.getStacksNamesList();
    Collections.reverse(listAfterSorting);
    assertEquals(listBeforeSorting, listAfterSorting);
  }

  private void deleteStack() {
    stacks.clickOnDeleteStackButton();
    stacks.clickOnDeleteDialogButton();
    dashboard.waitNotificationMessage("Selected stacks have been successfully removed.");
    dashboard.waitNotificationIsClosed();
  }
}
