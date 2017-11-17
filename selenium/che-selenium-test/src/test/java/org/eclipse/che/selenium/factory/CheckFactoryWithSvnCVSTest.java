/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.factory.FactoryTemplate.SVN;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.provider.TestSvnPasswordProvider;
import org.eclipse.che.selenium.core.provider.TestSvnRepo1Provider;
import org.eclipse.che.selenium.core.provider.TestSvnUsernameProvider;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.subversion.Subversion;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckFactoryWithSvnCVSTest {
  private static final Logger LOG = LoggerFactory.getLogger(CheckFactoryWithSvnCVSTest.class);

  @Inject private Ide ide;
  @Inject private TestWorkspace ws;
  @Inject private TestUser user;

  @Inject private TestSvnRepo1Provider svnRepo1UrlProvider;
  @Inject private TestSvnUsernameProvider svnUsernameProvider;
  @Inject private TestSvnPasswordProvider svnPasswordProvider;
  @Inject private TestFactoryInitializer testFactoryInitializer;

  private TestFactory testFactory;

  @Inject private Menu menu;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Wizard wizard;
  @Inject private ImportProjectFromLocation importProjectFromLocation;
  @Inject private Loader loader;
  @Inject private Subversion subversion;
  @Inject private Dashboard dashboard;
  @Inject private NotificationsPopupPanel notifications;
  @Inject private Events events;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  @BeforeClass
  public void setUp() throws Exception {
    // setup test factory
    TestFactoryInitializer.TestFactoryBuilder testFactoryBuilder =
        testFactoryInitializer.fromTemplate(SVN);
    testFactoryBuilder
        .getWorkspace()
        .getProjects()
        .get(0)
        .getSource()
        .setLocation(svnRepo1UrlProvider.get() + "/trunk");
    testFactory = testFactoryBuilder.build();
  }

  @AfterClass
  public void tearDown() throws Exception {
    testFactory.delete();
  }

  @Test
  public void checkFactoryProcessing() throws Exception {
    // given
    String expectedProject = "SvnFactory";
    List<String> expectedItemsInProjectTree =
        Arrays.asList("commit-test", "copy", "move", "properties-test", "newfile", "test");

    // when
    testFactory.authenticateAndOpen();

    // then
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[text()='SVN Authentication']")));

    subversion.svnLogin(svnUsernameProvider.get(), svnPasswordProvider.get());

    try {
      notifications.waitExpectedMessageOnProgressPanelAndClosed(
          "Project " + expectedProject + " imported");
    } catch (org.openqa.selenium.TimeoutException e) {
      events.clickProjectEventsTab();
      events.waitExpectedMessage("Project " + expectedProject + " imported");
    }

    projectExplorer.openItemByPath(expectedProject);
    for (String item : expectedItemsInProjectTree) {
      projectExplorer.waitItem(expectedProject + "/" + item);
    }
  }
}
