/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import org.eclipse.che.api.factory.shared.dto.PoliciesDto;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Mihail Kuznyetsov */
public class CheckFactoryWithPerClickCreatePolicyTest {
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Dashboard dashboard;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  private TestFactory testFactory;

  @BeforeClass
  public void setUp() throws Exception {
    TestFactoryInitializer.TestFactoryBuilder factoryBuilder =
        testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL);
    factoryBuilder.setPolicies(newDto(PoliciesDto.class).withCreate("perClick"));
    testFactory = factoryBuilder.build();
  }

  @AfterClass
  public void tearDown() throws Exception {
    testFactory.delete();
  }

  @Test
  public void checkFactoryAcceptingWithPerClickPolicy() {
    // accept factory
    dashboard.open();
    testFactory.open(seleniumWebDriver);

    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    projectExplorer.waitProjectExplorer();
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed("Project Spring imported");

    String workspaceUrl = seleniumWebDriver.getCurrentUrl();

    // accept factory
    testFactory.open(seleniumWebDriver);
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    projectExplorer.waitProjectExplorer();

    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed("Project Spring imported");

    // factory has been accepted in another workspace
    assertEquals(workspaceUrl + "_1", seleniumWebDriver.getCurrentUrl());
  }
}
