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

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getenv;
import static org.eclipse.che.selenium.core.CheSeleniumSuiteModule.CHE_MULTIUSER_VARIABLE;

import com.google.inject.Module;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.selenium.core.annotation.Multiuser;
import org.eclipse.che.selenium.core.inject.SeleniumTestHandler;
import org.testng.annotations.ITestAnnotation;

/** @author Anatolii Bazko */
public class CheSeleniumTestHandler extends SeleniumTestHandler {
  private static final boolean isMultiuser = parseBoolean(getenv().get(CHE_MULTIUSER_VARIABLE));

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

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    // disable Multi User tests if this test execution is for Single User Eclipse Che
    if (!isMultiuser) {
      if (testMethod.getDeclaringClass().isAnnotationPresent(Multiuser.class)) {
        annotation.setEnabled(false);
      }
    }
  }
}
