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
package org.eclipse.che.commons.test.db;

import javax.sql.DataSource;

/**
 * Defines a simple model of database test server.
 *
 * @author Yevhenii Voevodin
 */
public interface DBTestServer {

  /** Returns jdbc url to this server. */
  String getUrl();

  /** Returns the name of the user who can access this db server. */
  String getUser();

  /** Returns the password of the user returned by {@link #getUser()}. */
  String getPassword();

  /** Returns server data source. */
  DataSource getDataSource();

  /** Starts this test server. */
  void start();

  /** Shuts down this test server. */
  void shutdown();
}
