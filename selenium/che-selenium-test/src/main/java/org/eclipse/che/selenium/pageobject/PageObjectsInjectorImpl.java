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
package org.eclipse.che.selenium.pageobject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.selenium.core.CheSeleniumWebDriverRelatedModule;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.pageobject.PageObjectsInjector;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.UploadUtil;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.eclipse.che.selenium.pageobject.site.CheLoginPage;

/** @author Dmytro Nochevnov */
@Singleton
public class PageObjectsInjectorImpl extends PageObjectsInjector {
  @Inject private CheSeleniumWebDriverRelatedModule cheSeleniumWebDriverRelatedModule;
  @Inject private ActionsFactory actionsFactory;

  @Inject
  @Named("che.multiuser")
  private boolean isMultiuser;

  @Inject private UploadUtil uploadUtil;

  @Override
  public Map<Class<?>, Object> getDependenciesWithWebdriver(SeleniumWebDriver seleniumWebDriver) {
    SeleniumWebDriverHelper seleniumWebDriverHelper =
        new SeleniumWebDriverHelper(
            seleniumWebDriver,
            new WebDriverWaitFactory(seleniumWebDriver),
            actionsFactory,
            uploadUtil);

    Map<Class<?>, Object> dependencies = new HashMap<>();
    dependencies.put(
        Entrance.class,
        cheSeleniumWebDriverRelatedModule.getEntrance(
            isMultiuser,
            new CheLoginPage(seleniumWebDriver, seleniumWebDriverHelper),
            seleniumWebDriver));

    return dependencies;
  }
}
