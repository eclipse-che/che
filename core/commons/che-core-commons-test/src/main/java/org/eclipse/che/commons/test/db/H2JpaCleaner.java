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

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.eclipse.che.commons.test.tck.JpaCleaner;
import org.h2.tools.RunScript;

/** @author Yevhenii Voevodin */
public class H2JpaCleaner extends JpaCleaner {

  private final DataSource dataSource;

  private H2DBTestServer server;

  public H2JpaCleaner(H2DBTestServer server) {
    this(server.getDataSource());
    this.server = server;
  }

  public H2JpaCleaner(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void clean() {
    super.clean();
    if (server != null) {
      server.shutdown();
    } else {
      try (Connection conn = dataSource.getConnection()) {
        RunScript.execute(conn, new StringReader("SHUTDOWN"));
      } catch (SQLException x) {
        throw new RuntimeException(x.getMessage(), x);
      }
    }
  }
}
