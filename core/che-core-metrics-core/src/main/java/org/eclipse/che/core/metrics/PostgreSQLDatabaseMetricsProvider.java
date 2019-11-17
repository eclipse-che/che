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
package org.eclipse.che.core.metrics;

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.db.PostgreSQLDatabaseMetrics;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PostgreSQLDatabaseMetricsProvider implements Provider<MeterBinder> {
  private final PostgreSQLDatabaseMetrics postgreSQLDatabaseMetrics;

  @Inject
  public PostgreSQLDatabaseMetricsProvider(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      this.postgreSQLDatabaseMetrics = new PostgreSQLDatabaseMetrics(dataSource, connection.getCatalog());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PostgreSQLDatabaseMetrics get() {
    return postgreSQLDatabaseMetrics;
  }
}
