/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.db;

import com.google.common.annotations.Beta;
import io.opentracing.contrib.jdbc.ConnectionInfo;
import io.opentracing.contrib.jdbc.TracingConnection;
import io.opentracing.contrib.jdbc.parser.URLParser;
import io.opentracing.util.GlobalTracer;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.slf4j.LoggerFactory;

/**
 * Adding tracing support for existing @{@link javax.sql.DataSource}. DbType and DbUser information
 * omitted in traces. Traced are made only if active span exists. Prerequisites of using this class
 * is that @{@link io.opentracing.Tracer} should be set in @{@link io.opentracing.util.GlobalTracer}
 *
 * @author Sergii Kabashniuk
 */
@Beta
public class TracingDataSource implements DataSource {
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TracingDataSource.class);

  private final DataSource delegate;
  private final ConnectionInfo connectionInfo;

  public TracingDataSource(DataSource delegate) {
    this.delegate = delegate;
    try (Connection connection = delegate.getConnection()) {
      connectionInfo = URLParser.parser(connection.getMetaData().getURL());
      LOG.debug(
          "URL {} connectionInfo {}",
          connection.getMetaData().getURL(),
          connectionInfo.getPeerService());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    return new TracingConnection(
        delegate.getConnection(), connectionInfo, true, null, GlobalTracer.get());
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return new TracingConnection(
        delegate.getConnection(username, password), connectionInfo, true, null, GlobalTracer.get());
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return delegate.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return delegate.isWrapperFor(iface);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return delegate.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    delegate.setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    delegate.setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return delegate.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return delegate.getParentLogger();
  }

  public static DataSource wrapWithTracingIfEnabled(DataSource dataSource) {
    return Boolean.valueOf(System.getenv("CHE_DB_TRACING_ENABLED"))
        ? new TracingDataSource(dataSource)
        : dataSource;
  }
}
