/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
