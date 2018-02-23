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

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardFactory;
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
  @Inject private DashboardFactory dashboardFactory;
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
  public void checkFactoryFiltering() {
    dashboardFactory.selectFactoriesOnNavBar();
    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.waitSearchFactoryByNameField();

    dashboardFactory.waitFactoryName(FACTORY1_NAME);
    dashboardFactory.waitFactoryName(FACTORY2_NAME);

    // filter list by full factory name
    dashboardFactory.typeToSearchInput(FACTORY1_NAME);
    dashboardFactory.waitFactoryName(FACTORY1_NAME);
    dashboardFactory.waitFactoryNotExists(FACTORY2_NAME);

    // filter list by part factory name
    dashboardFactory.typeToSearchInput("factory");
    dashboardFactory.waitFactoryName(FACTORY1_NAME);
    dashboardFactory.waitFactoryName(FACTORY2_NAME);

    // filter by a nonexistent factory name
    dashboardFactory.typeToSearchInput(generate("", 20));
    dashboardFactory.waitFactoryNotExists(FACTORY1_NAME);
    dashboardFactory.waitFactoryNotExists(FACTORY2_NAME);
  }

  @Test
  public void checkAllFactoriesPage() {
    dashboardFactory.selectFactoriesOnNavBar();
    dashboardFactory.waitAllFactoriesPage();

    dashboardFactory.waitBulkCheckbox();
    dashboardFactory.waitFactoryName(FACTORY1_NAME);

    dashboardFactory.waitAddFactoryBtn();

    // check select and unselect factories by Bulk
    dashboardFactory.selectAllFactoriesByBulk();
    Assert.assertTrue(dashboardFactory.isFactoryChecked(FACTORY1_NAME));
    Assert.assertTrue(dashboardFactory.isFactoryChecked(FACTORY2_NAME));
    dashboardFactory.selectAllFactoriesByBulk();
    Assert.assertFalse(dashboardFactory.isFactoryChecked(FACTORY1_NAME));
    Assert.assertFalse(dashboardFactory.isFactoryChecked(FACTORY2_NAME));

    // check selecting factory by checkbox
    dashboardFactory.selectFactoryByCheckbox(FACTORY1_NAME);
    Assert.assertTrue(dashboardFactory.isFactoryChecked(FACTORY1_NAME));
    dashboardFactory.selectFactoryByCheckbox(FACTORY1_NAME);
    Assert.assertFalse(dashboardFactory.isFactoryChecked(FACTORY1_NAME));
  }

  @Test(priority = 1)
  public void checkFactoryDeleting() {
    dashboardFactory.selectFactoriesOnNavBar();
    dashboardFactory.waitAllFactoriesPage();

    // delete factory that is selected be checkbox
    dashboardFactory.selectFactoryByCheckbox(FACTORY3_NAME);
    Assert.assertTrue(dashboardFactory.isFactoryChecked(FACTORY3_NAME));
    dashboardFactory.clickOnDeleteFactoryBtn();
    dashboardFactory.clickOnDeleteButtonInDialogWindow();
    dashboardFactory.waitFactoryNotExists(FACTORY3_NAME);

    // select all factories and delete them
    dashboardFactory.selectAllFactoriesByBulk();
    dashboardFactory.clickOnDeleteFactoryBtn();
    dashboardFactory.clickOnDeleteButtonInDialogWindow();

    dashboardFactory.waitFactoryNotExists(FACTORY1_NAME);
    dashboardFactory.waitFactoryNotExists(FACTORY2_NAME);

    // TODO check this method not delete others selenium tests factories
  }

  private void createFactoryByApi(String factoryName) throws Exception {
    TestFactoryInitializer.TestFactoryBuilder factoryBuilder =
        testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL);
    factoryBuilder.setName(factoryName);
    factoryBuilder.build();
  }
}
