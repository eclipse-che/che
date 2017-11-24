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
package org.eclipse.che.selenium.pageobject.dashboard.workspacedetails;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.support.PageFactory;

public class WorkspaceDetailsConfig {

  @Inject
  public WorkspaceDetailsConfig(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String PROJECT_BY_NAME = "//div[contains(@ng-click, 'projectItem')]/span[text()='%s']";
    String DELETE_PROJECT = "//button/span[text()='Delete']";
    String DELETE_IT_PROJECT = "//che-button-primary[@che-button-title='Delete']/button";
    String ADD_NEW_PROJECT_BUTTON = "//che-button-primary[@che-button-title='Add Project']/button";
  }
}
