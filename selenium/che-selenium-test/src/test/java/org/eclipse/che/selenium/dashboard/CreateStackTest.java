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

import static org.eclipse.che.commons.lang.NameGenerator.generate;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.dashboard.BuildStackFromRecipe;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.stacks.StackDetails;
import org.eclipse.che.selenium.pageobject.dashboard.stacks.Stacks;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateStackTest {
  private static final String COMPOSE_RECIPE =
      "services:\n"
          + " db:\n"
          + "  image: eclipse/mysql\n"
          + "environment:\n"
          + "  MYSQL_ROOT_PASSWORD: password\n"
          + "MYSQL_DATABASE: petclinic\n"
          + "MYSQL_USER: petclinic\n"
          + "MYSQL_PASSWORD: password\n"
          + "\b\bmem_limit: 1073741824\n"
          + "\b\bdev-machine:\n"
          + "  image: eclipse/ubuntu_jdk8\n"
          + "mem_limit: 2147483648\n"
          + "depends_on:\n"
          + "  - db";
  private static final String DOCKERIMAGE_RECIPE = "eclipse/ubuntu_jdk8";
  private static final String DOCKERFILE_RECIPE = "FROM eclipse/ubuntu_jdk8";
  private static final String KUBERNETES_RECIPE =
      "---\n"
          + "kind: List\n"
          + "items:\n"
          + "-\n"
          + "  apiVersion: v1\n"
          + "kind: Pod\n"
          + "metadata:\n"
          + "  name: ws\n"
          + "\b\bspec:\n"
          + "  containers:\n"
          + "  -\n"
          + "  image: eclipse/ubuntu_jdk8\n"
          + "name: dev-machine";

  @Inject private Stacks stacks;
  @Inject private Dashboard dashboard;
  @Inject private StackDetails stackDetails;
  @Inject private BuildStackFromRecipe buildStackFromRecipe;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();
  }

  @BeforeMethod
  public void openStacksListPage() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectStacksItemOnDashboard();
    stacks.waitToolbarTitleName();

    stacks.clickOnAddStackButton();
    buildStackFromRecipe.waitCreateStackDialogVisible();
  }

  @AfterClass
  public void deleteCreatedStacks() {
    dashboard.selectStacksItemOnDashboard();
    stacks.waitToolbarTitleName();

    stacks.selectAllStacksByBulk();

    if (stacks.isDeleteStackButtonEnabled()) {
      stacks.clickOnDeleteStackButton();
      stacks.clickOnDeleteDialogButton();

      dashboard.waitNotificationMessage("Selected stacks have been successfully removed.");
      dashboard.waitNotificationIsClosed();
    }
  }

  @Test(groups = {TestGroup.DOCKER})
  public void createStackFromComposeRecipe() {
    createStack("compose", COMPOSE_RECIPE);
  }

  @Test
  public void createStackFromDockerimageRecipe() {
    createStack("dockerimage", DOCKERIMAGE_RECIPE);
  }

  @Test(groups = {TestGroup.DOCKER})
  public void createStackFromDockerfileRecipe() {
    createStack("dockerfile", DOCKERFILE_RECIPE);
  }

  @Test(groups = {TestGroup.OPENSHIFT})
  public void createStackFromKubernetesRecipe() {
    createStack("kubernetes", KUBERNETES_RECIPE);
  }

  @Test(groups = {TestGroup.OPENSHIFT})
  public void createStackFromOpenshiftRecipe() {
    createStack("openshift", KUBERNETES_RECIPE);
  }

  private void createStack(String stackName, String recipe) {
    String name = generate(stackName, 5);

    buildStackFromRecipe.selectTabByName(stackName);

    // wait for editor to be active for typing after tab selection
    WaitUtils.sleepQuietly(1);
    buildStackFromRecipe.enterRecipe(recipe);

    // wait that recipe is valid and create stack
    buildStackFromRecipe.checkRecipeIsCorrect();
    buildStackFromRecipe.waitOkButtonEnabled();
    buildStackFromRecipe.clickOnOkButton();
    buildStackFromRecipe.waitCreateStackDialogClosed();

    // check that stack was created correctly and save changes
    stackDetails.waitToolbar();
    stackDetails.setStackName(name);
    stackDetails.clickOnSaveChangesButton();
    stackDetails.waitToolbarWithStackName(name);
  }
}
