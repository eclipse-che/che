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
package org.eclipse.che.git.impl;

import org.testng.annotations.DataProvider;

/**
 * Implementations have to provide DataProvider with real GitConnectionFactoryProvider. This class
 * are NOT packaged to the test-jar.
 *
 * @author Sergii Kabashniuk
 */
public class GitConnectionFactoryProvider {

  @DataProvider(name = "GitConnectionFactory")
  public static Object[][] createConnection() {
    throw new UnsupportedOperationException();
  }
}
