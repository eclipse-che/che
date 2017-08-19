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
package org.eclipse.che.plugin.testing.ide.view.navigation;

/**
 * Enables navigation to the failed test class.
 *
 * @author Mirage Abeysekara
 */
@Deprecated
public interface TestClassNavigation {

  /**
   * Navigates to the failed test class.
   *
   * @param packagePath
   * @param line
   */
  @Deprecated
  void gotoClass(String packagePath, String className, String methodName, int line);
}
