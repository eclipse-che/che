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
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardFactories;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FactoriesListTest {
  private static final String FACTORY1_NAME = generate("factory1", 4);
  private static final String FACTORY2_NAME = generate("factory2", 4);
  private static final String FACTORY3_NAME = generate("factory3", 4);

  @Inject private TestFactoryServiceClient factoryServiceClient;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private DashboardFactories dashboardFactories;
  @Inject private Dashboard dashboard;

  @BeforeClass
  public void setUp() throws Exception {
    createFactoryByApi(FACTORY1_NAME);
    createFactoryByApi(FACTORY2_NAME);
    createFactoryByApi(FACTORY3_NAME);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    factoryServiceClient.deleteFactory(FACTORY1_NAME);
    factoryServiceClient.deleteFactory(FACTORY2_NAME);
    factoryServiceClient.deleteFactory(FACTORY3_NAME);
  }

  @Test
  public void checkAllFactoriesPage() {
    dashboardFactories.selectFactoriesOnNavBar();
    dashboardFactories.waitAllFactoriesPage();

    dashboardFactories.waitBulkCheckbox();
    dashboardFactories.waitFactoryName(FACTORY1_NAME);

    dashboardFactories.waitAddFactoryBtn();

    // check selecting factories by Bulk
    dashboardFactories.selectAllFactoriesByBulk();
    assertTrue(dashboardFactories.isFactoryChecked(FACTORY1_NAME));
    assertTrue(dashboardFactories.isFactoryChecked(FACTORY2_NAME));
    dashboardFactories.selectAllFactoriesByBulk();
    Assert.assertFalse(dashboardFactories.isFactoryChecked(FACTORY1_NAME));
    Assert.assertFalse(dashboardFactories.isFactoryChecked(FACTORY2_NAME));

    // check selecting factories by checkbox
    dashboardFactories.selectFactoryByCheckbox(FACTORY1_NAME);
    assertTrue(dashboardFactories.isFactoryChecked(FACTORY1_NAME));
    dashboardFactories.selectFactoryByCheckbox(FACTORY1_NAME);
    Assert.assertFalse(dashboardFactories.isFactoryChecked(FACTORY1_NAME));
  }

  @Test
  public void checkFactoriesFiltering() {
    dashboardFactories.selectFactoriesOnNavBar();
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.waitSearchFactoryByNameField();

    // filter the list by a full factory name
    dashboardFactories.typeToSearchInput(FACTORY1_NAME);
    dashboardFactories.waitFactoryName(FACTORY1_NAME);
    dashboardFactories.waitFactoryNotExists(FACTORY2_NAME);

    // filter the list by a part factory name
    dashboardFactories.typeToSearchInput("factory");
    dashboardFactories.waitFactoryName(FACTORY1_NAME);
    dashboardFactories.waitFactoryName(FACTORY2_NAME);

    // filter the list by a nonexistent factory name
    dashboardFactories.typeToSearchInput(generate("", 20));
    dashboardFactories.waitFactoryNotExists(FACTORY1_NAME);
    dashboardFactories.waitFactoryNotExists(FACTORY2_NAME);
  }

  @Test
  public void checkFactoriesDeleting() {
    dashboardFactories.selectFactoriesOnNavBar();
    dashboardFactories.waitAllFactoriesPage();

    // delete factory selected by checkbox
    dashboardFactories.selectFactoryByCheckbox(FACTORY3_NAME);
    assertTrue(dashboardFactories.isFactoryChecked(FACTORY3_NAME));
    dashboardFactories.clickOnDeleteFactoryBtn();
    dashboardFactories.clickOnDeleteButtonInDialogWindow();
    dashboardFactories.waitFactoryNotExists(FACTORY3_NAME);
  }

  private void createFactoryByApi(String factoryName) throws Exception {
    TestFactoryInitializer.TestFactoryBuilder factoryBuilder =
        testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL);
    factoryBuilder.setName(factoryName);
    factoryBuilder.build();
  }
}
