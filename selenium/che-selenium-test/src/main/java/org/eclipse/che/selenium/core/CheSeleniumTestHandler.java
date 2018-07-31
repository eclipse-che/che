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
package org.eclipse.che.selenium.core;

import com.google.inject.Module;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.selenium.core.inject.SeleniumTestHandler;

/** @author Anatolii Bazko */
public class CheSeleniumTestHandler extends SeleniumTestHandler {
  @Override
  public List<Module> getParentModules() {
    List<Module> modules = new ArrayList<>();
    modules.add(new CheSeleniumSuiteModule());
    return modules;
  }

  @Override
  public List<Module> getChildModules() {
    List<Module> modules = new ArrayList<>();
    modules.add(new CheSeleniumWebDriverRelatedModule());
    return modules;
  }
}
