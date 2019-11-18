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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.db.DatabaseTableMetrics;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Singleton
public class DbTablesMetrics implements MeterBinder {

  @Inject
  public DbTablesMetrics(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  private final DataSource dataSource;

  @Override
  public void bindTo(MeterRegistry registry) {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData md = connection.getMetaData();
      try (ResultSet rs = md.getTables(null, null, "%", null)) {
        while (rs.next()) {
          String tableName = rs.getString(3);
          if (!tableName.startsWith("pg_")) {
            DatabaseTableMetrics.monitor(registry, rs.getString(3), "che-datasource", dataSource);
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
