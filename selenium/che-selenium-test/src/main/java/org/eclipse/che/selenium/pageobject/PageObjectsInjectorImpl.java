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
package org.eclipse.che.selenium.pageobject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.selenium.core.CheSeleniumWebDriverRelatedModule;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.pageobject.PageObjectsInjector;
import org.eclipse.che.selenium.pageobject.site.CheLoginPage;

/** @author Dmytro Nochevnov */
@Singleton
public class PageObjectsInjectorImpl extends PageObjectsInjector {

  private static final String CHE_MULTIUSER = "che.multiuser";

  @Inject private CheSeleniumWebDriverRelatedModule cheSeleniumWebDriverRelatedModule;

  @Inject
  @javax.inject.Named(CHE_MULTIUSER)
  boolean isMultiuser;

  @Override
  public Map<Class<?>, Object> getDependenciesWithWebdriver(SeleniumWebDriver seleniumWebDriver) {
    Map<Class<?>, Object> dependencies = new HashMap<>();
    dependencies.put(
        Entrance.class,
        cheSeleniumWebDriverRelatedModule.getEntrance(
            isMultiuser, new CheLoginPage(seleniumWebDriver), seleniumWebDriver));

    return dependencies;
  }
}
