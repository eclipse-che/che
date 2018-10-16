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
package org.eclipse.che.selenium.dashboard;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.ANDROID;
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

    // check Android stack info
    assertTrue(stacks.isStackItemExisted(ANDROID.getName()));
    assertEquals(
        stacks.getStackDescription(ANDROID.getName()),
        "Default Android Stack with Java 1.8 and Android SDK");
    assertEquals(stacks.getStackComponents(ANDROID.getName()), "Centos, JDK, Maven, Android API");
  }

  @Test
  public void checkStacksSelectingByCheckbox() {
    String stackName = createDuplicatedStack(JAVA_MYSQL.getName());

    // select stacks by checkbox and check it is selected
    stacks.selectStackByCheckbox(stackName);
    assertTrue(stacks.isStackChecked(stackName));
    stacks.selectStackByCheckbox(stackName);
    assertFalse(stacks.isStackChecked(stackName));

    // click on the Bulk button and check that created stack is checked
    stacks.selectAllStacksByBulk();
    assertTrue(stacks.isStackChecked(stackName));
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
    String stackName;

    // create stack duplicate by Duplicate Stack button
    stackName = createDuplicatedStack(JAVA.getName());
    assertTrue(stacks.isDuplicatedStackExisted(stackName));

    // delete stack by the Action delete stack button
    deleteStackByActionDeleteButton(stackName);

    stackName = createDuplicatedStack(BLANK.getName());
    assertTrue(stacks.isDuplicatedStackExisted(stackName));
    deleteStackByActionDeleteButton(stackName);
  }

  private void deleteStack() {
    stacks.clickOnDeleteStackButton();
    stacks.clickOnDeleteDialogButton();
    dashboard.waitNotificationMessage("Selected stacks have been successfully removed.");
    dashboard.waitNotificationIsClosed();
  }

  private String createDuplicatedStack(String stack) {
    String createdStackName = "";

    dashboard.selectStacksItemOnDashboard();
    stacks.waitToolbarTitleName();
    stacks.clickOnDuplicateStackButton(stack);

    for (String name : stacks.getStacksNamesList()) {
      if (name.contains(stack + "-copy-")) {
        createdStackName = name;
      }
    }

    return createdStackName;
  }

  private void deleteStackByActionDeleteButton(String name) {
    // delete stack by the Action delete stack button
    stacks.clickOnDeleteActionButton(name);
    stacks.clickOnDeleteDialogButton();
    dashboard.waitNotificationMessage(format("Stack %s has been successfully removed.", name));
    dashboard.waitNotificationIsClosed();
    assertFalse(stacks.isStackItemExisted(name));
  }
}
