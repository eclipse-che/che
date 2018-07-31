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
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;

/**
 * In-memory h2 based implementation of test server.
 *
 * @author Yevhenii Voevodin
 */
public class H2DBTestServer implements DBTestServer {

  public static H2DBTestServer startDefault() {
    H2DBTestServer server = new H2DBTestServer();
    server.start();
    return server;
  }

  @Override
  public String getUrl() {
    return "jdbc:h2:mem:test";
  }

  @Override
  public String getUser() {
    return "";
  }

  @Override
  public String getPassword() {
    return "";
  }

  @Override
  public DataSource getDataSource() {
    final JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setUrl(getUrl());
    return dataSource;
  }

  @Override
  public void start() {
    final JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setUrl(getUrl() + ";DB_CLOSE_DELAY=-1");
    try (Connection conn = dataSource.getConnection()) {
      RunScript.execute(conn, new StringReader("SELECT 1"));
    } catch (SQLException x) {
      throw new RuntimeException(x);
    }
  }

  @Override
  public void shutdown() {
    try (Connection conn = getDataSource().getConnection()) {
      RunScript.execute(conn, new StringReader("SHUTDOWN"));
    } catch (SQLException x) {
      throw new RuntimeException(x);
    }
  }
}
