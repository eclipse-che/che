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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.api.factory.shared.dto.IdeActionDto;
import org.eclipse.che.api.factory.shared.dto.IdeDto;
import org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckWelcomePanelOnCodenvyTest {
  @Inject private Ide ide;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Dashboard dashboard;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  private TestFactory testFactory;
  private WebDriverWait webDriverWait;

  @BeforeClass
  public void setUp() throws Exception {
    String urlToContentWelcomePanel = "https://codenvy.io/docs/admin-guide/runbook/index.html";
    webDriverWait =
        new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC);
    TestFactoryInitializer.TestFactoryBuilder factoryBuilder =
        testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL);
    factoryBuilder.withIde(
        newDto(IdeDto.class)
            .withOnAppLoaded(
                newDto(OnAppLoadedDto.class)
                    .withActions(
                        Collections.singletonList(
                            newDto(IdeActionDto.class)
                                .withId("openWelcomePage")
                                .withProperties(
                                    ImmutableMap.of(
                                        "greetingTitle",
                                        "greetingTitle",
                                        "greetingContentUrl",
                                        urlToContentWelcomePanel))))));
    testFactory = factoryBuilder.build();
  }

  @AfterClass
  public void tearDown() throws Exception {
    testFactory.delete();
  }

  @Test
  public void shouldAppearsWelcomePanelAfterUsingFactory() throws Exception {
    dashboard.open();
    testFactory.open(seleniumWebDriver);
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    projectExplorer.waitItem("Spring");
    checkWelcomePanel();
  }

  private void checkWelcomePanel() {
    String expectedTextFragment =
        "Runbook\n"
            + "This article provides specific performance and security guidance for Codenvy on-premises installations based on our experience running";
    webDriverWait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("greetingFrame")));
    webDriverWait.until(
        ExpectedConditions.textToBePresentInElementLocated(
            By.tagName("body"), expectedTextFragment));
  }
}
