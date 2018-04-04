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
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.api.factory.shared.dto.PoliciesDto;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.PopupDialogsBrowser;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.WarningDialog;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Mihail Kuznyetsov */
public class CheckFactoryWithUntilPolicyTest {
  private static final String FACTORY_NAME = NameGenerator.generate("untilPolicy", 3);
  private static final String EXPIRE_MESSAGE =
      "Unable to load Factory: This Factory has expired due to time restrictions applied by its owner. Please, contact owner for more information.";
  private static final int FACTORY_INACTIVITY_TIME = 120000;
  private static final int ADDITIONAL_TIME = 10000;
  private static long initTime;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private PopupDialogsBrowser popupDialogsBrowser;
  @Inject private Dashboard dashboard;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private WarningDialog warningDialog;
  private TestFactory testFactory;

  @BeforeClass
  public void setUp() throws Exception {
    TestFactoryInitializer.TestFactoryBuilder factoryBuilder =
        testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL);
    initTime = System.currentTimeMillis();
    factoryBuilder.setPolicies(
        newDto(PoliciesDto.class).withUntil(initTime + FACTORY_INACTIVITY_TIME));
    factoryBuilder.setName(FACTORY_NAME);
    testFactory = factoryBuilder.build();
  }

  @AfterClass
  public void tearDown() throws Exception {
    testFactory.delete();
  }

  @Test
  public void checkFactoryAcceptingWithUntilPolicy() {
    dashboard.open();
    testFactory.open(seleniumWebDriver);
    seleniumWebDriver.switchFromDashboardIframeToIde();

    if (System.currentTimeMillis() > initTime + FACTORY_INACTIVITY_TIME) {
      fail("Factory started longer then additional time and next test steps does not make sense");
    }

    // first
    while (System.currentTimeMillis() <= initTime + FACTORY_INACTIVITY_TIME + ADDITIONAL_TIME) {
      if (warningDialog.isPresent()) {
        warningDialog.clickOkBtn();
        fail("Factory expired before the until period");
      }
      projectExplorer.waitProjectExplorer();
      WaitUtils.sleepQuietly(1);
    }

    // second
    testFactory.open(seleniumWebDriver);
    seleniumWebDriver.switchFromDashboardIframeToIde();
    warningDialog.waitWaitWarnDialogWindowWithSpecifiedTextMess(EXPIRE_MESSAGE);
  }
}
