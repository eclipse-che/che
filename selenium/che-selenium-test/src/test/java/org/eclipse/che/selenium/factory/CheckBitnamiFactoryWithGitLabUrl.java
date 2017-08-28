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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestWorkspaceConstants.RUNNING_WORKSPACE_MESS;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckBitnamiFactoryWithGitLabUrl {
  @Inject private Ide ide;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Events events;
  @Inject private TestFactoryInitializer testFactoryInitializer;

  private TestFactory testFactory;

  @BeforeClass
  public void setUp() throws Exception {
    testFactory = testFactoryInitializer.fromUrl("https://gitlab.com/benoitf/simple-project");
  }

  @AfterClass
  public void tearDown() throws Exception {
    testFactory.delete();
  }

  @Test
  public void checkGitLabUrl() throws Exception {
    testFactory.authenticateAndOpen(ide.driver());
    ide.switchFromDashboard();
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed(
        RUNNING_WORKSPACE_MESS, UPDATING_PROJECT_TIMEOUT_SEC);
    events.clickProjectEventsTab();
    events.waitExpectedMessage(
        "Successfully configured and cloned source code of spring-petclinic.");
  }
}
