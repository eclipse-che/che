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
package org.eclipse.che.selenium.core.webdriver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestBrowser;
import org.eclipse.che.selenium.core.utils.DockerUtil;

/** @author Dmytro Nochevnov */
@Singleton
public class SeleniumWebDriverFactory {

  @Inject
  @Named("sys.browser")
  TestBrowser browser;

  @Inject
  @Named("sys.driver.port")
  private String webDriverPort;

  @Inject
  @Named("sys.grid.mode")
  private boolean gridMode;

  @Inject private HttpJsonRequestFactory httpJsonRequestFactory;

  @Inject private DockerUtil dockerUtil;

  public SeleniumWebDriver create() {
    return new SeleniumWebDriver(
        browser, webDriverPort, gridMode, httpJsonRequestFactory, dockerUtil);
  }
}
