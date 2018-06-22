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

import static java.lang.String.format;
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
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.stacks.StackDetails;
import org.eclipse.che.selenium.pageobject.dashboard.stacks.Stacks;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class StacksListTest {

  private static final String NEW_STACK_NAME = generate("", 8);

  @Inject private StackDetails stackDetails;
  @Inject private Dashboard dashboard;
  @Inject private Stacks stacks;

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

  @AfterClass
  public void deleteCreatedStacks() {
    dashboard.selectStacksItemOnDashboard();
    stacks.waitToolbarTitleName();
    stacks.selectAllStacksByBulk();
    deleteStack();
  }

  @Test
  public void checkStacksList() {
    // check UI views of Stacks list
    stacks.waitToolbarTitleName();
    stacks.waitDocumentationLink();
    stacks.waitAddStackButton();
    stacks.waitBuildStackFromRecipeButton();
    stacks.waitFilterStacksField();

    // check that all Stack list headers are present
    ArrayList<String> headers = stacks.getStacksListHeaders();
    assertTrue(headers.contains("NAME"));
    assertTrue(headers.contains("DESCRIPTION"));
    assertTrue(headers.contains("COMPONENTS"));
    assertTrue(headers.contains("ACTIONS"));

    // check JAVA stack info
    assertTrue(stacks.isStackItemExisted(JAVA.getName()));
    assertEquals(
        stacks.getStackDescription(JAVA.getName()),
        "Default Java Stack with JDK 8, Maven and Tomcat.");
    assertEquals(stacks.getStackComponents(JAVA.getName()), "Ubuntu, JDK, Maven, Tomcat");
  }

  @Test
  public void checkStacksSelectingByCheckbox() {
    createStack(NEW_STACK_NAME);

    // select stacks by checkbox and check it is selected
    stacks.selectStackByCheckbox(NEW_STACK_NAME);
    assertTrue(stacks.isStackChecked(NEW_STACK_NAME));
    stacks.selectStackByCheckbox(NEW_STACK_NAME);
    assertFalse(stacks.isStackChecked(NEW_STACK_NAME));

    // click on the Bulk button and check that created stack is checked
    stacks.selectAllStacksByBulk();
    assertTrue(stacks.isStackChecked(NEW_STACK_NAME));
  }

  @Test
  public void checkStacksFiltering() {
    // filter stacks by nonexistent name
    stacks.typeToSearchInput("*");
    stacks.waitNoStacksFound();

    // search stacks by a full name
    stacks.typeToSearchInput(JAVA.getName());
    assertTrue(stacks.isStackItemExisted(JAVA.getName()));
    assertTrue(stacks.isStackItemExisted(JAVA_MYSQL.getName()));
    assertFalse(stacks.isStackItemExisted(BLANK.getName()));

    stacks.typeToSearchInput(BLANK.getName());
    assertTrue(stacks.isStackItemExisted(BLANK.getName()));
    assertFalse(stacks.isStackItemExisted(JAVA.getName()));
    assertFalse(stacks.isStackItemExisted(JAVA_MYSQL.getName()));

    // search stacks by a part name
    stacks.typeToSearchInput(BLANK.getName().substring(BLANK.getName().length() / 2));
    assertTrue(stacks.isStackItemExisted(BLANK.getName()));
    assertFalse(stacks.isStackItemExisted(JAVA.getName()));
    assertFalse(stacks.isStackItemExisted(JAVA_MYSQL.getName()));
  }

  @Test
  public void checkStacksSorting() {
    ArrayList<String> stackNamesListBeforeSorting, stackNamesListAfterSorting;
    // click on sort button to initialize it
    stacks.clickOnSortStacksByNameButton();

    // get stacks names list and click on sort stacks button
    stackNamesListBeforeSorting = stacks.getStacksNamesList();
    stacks.clickOnSortStacksByNameButton();

    // check that Stacks list reverted
    stackNamesListAfterSorting = stacks.getStacksNamesList();
    Collections.reverse(stackNamesListBeforeSorting);
    assertEquals(stackNamesListBeforeSorting, stackNamesListAfterSorting);

    stacks.clickOnSortStacksByNameButton();
    stackNamesListBeforeSorting = stacks.getStacksNamesList();
    Collections.reverse(stackNamesListAfterSorting);
    assertEquals(stackNamesListBeforeSorting, stackNamesListAfterSorting);
  }

  @Test
  public void checkStackActionButtons() {
    String stackName = generate("", 8);

    // create stack duplicate by Duplicate Stack button
    stacks.clickOnDuplicateStackButton(JAVA.getName());
    assertTrue(stacks.isDuplicatedStackExisted(JAVA.getName() + "-copy-"));
    stacks.clickOnDuplicateStackButton(BLANK.getName());
    assertTrue(stacks.isDuplicatedStackExisted(BLANK.getName() + "-copy-"));

    // delete stack by the Action delete stack button
    createStack(stackName);
    stacks.clickOnDeleteActionButton(stackName);
    stacks.clickOnDeleteDialogButton();
    dashboard.waitNotificationMessage(format("Stack %s has been successfully removed.", stackName));
    dashboard.waitNotificationIsClosed();
    assertFalse(stacks.isStackItemExisted(stackName));
  }

  private void deleteStack() {
    stacks.clickOnDeleteStackButton();
    stacks.clickOnDeleteDialogButton();
    dashboard.waitNotificationMessage("Selected stacks have been successfully removed.");
    dashboard.waitNotificationIsClosed();
  }

  private void createStack(String stackName) {
    dashboard.selectStacksItemOnDashboard();
    stacks.waitToolbarTitleName();

    stacks.clickOnAddStackButton();
    stackDetails.setStackName(stackName);
    stackDetails.clickOnSaveChangesButton();
    stackDetails.waitToolbar(stackName);
    stackDetails.clickOnAllStacksButton();
    stacks.waitToolbarTitleName();
    stacks.waitStackItem(stackName);
  }
}
