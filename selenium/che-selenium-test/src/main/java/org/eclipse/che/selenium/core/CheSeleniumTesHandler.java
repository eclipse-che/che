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
package org.eclipse.che.selenium.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.che.selenium.core.inject.SeleniumTestHandler;

/** @author Anatolii Bazko */
public class CheSeleniumTesHandler extends SeleniumTestHandler {
  @Override
  public Injector createParentInjector() {
    return Guice.createInjector(new CheSeleniumSuiteModule());
  }
}
