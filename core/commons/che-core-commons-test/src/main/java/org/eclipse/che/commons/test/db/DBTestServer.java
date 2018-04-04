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
