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
package org.eclipse.che.commons.test.tck;

import org.testng.ITestContext;

/**
 * Listener representing fake db server url injection for testing "attributes sharing" using {@link
 * ITestContext} test suite instance.
 *
 * @author Yevhenii Voevodin
 */
public class DBServerListener extends TestListenerAdapter {

  public static final String DB_SERVER_URL_ATTRIBUTE_NAME = "db_server_url";
  public static final String DB_SERVER_URL = "localhost:12345";

  @Override
  public void onStart(ITestContext context) {
    context.setAttribute(DB_SERVER_URL_ATTRIBUTE_NAME, DB_SERVER_URL);
  }
}
