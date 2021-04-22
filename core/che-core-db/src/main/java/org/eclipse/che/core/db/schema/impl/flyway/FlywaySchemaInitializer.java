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
package org.eclipse.che.core.db.schema.impl.flyway;

import static org.eclipse.che.core.db.DBInitializer.BARE_DB_INIT_PROPERTY_NAME;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import org.eclipse.che.core.db.schema.SchemaInitializationException;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.metadatatable.MetaDataTableImpl;
import org.flywaydb.core.internal.util.PlaceholderReplacer;

/**
 * <a href="https://flywaydb.org/">Flyway</a> based schema initializer.
 *
 * @author Yevhenii Voevodin
 */
public class FlywaySchemaInitializer implements SchemaInitializer {

  private final DataSource dataSource;
  private final String[] locations;
  private final String scriptsPrefix;
  private final String scriptsSuffix;
  private final String versionSeparator;
  private final boolean baselineOnMigrate;
  private final String baselineVersion;
  private final PlaceholderReplacer placeholderReplacer;

  /**
   * Creates a new instance of flyway schema initializer.
   *
   * @param scriptsLocations the locations where to search migration scripts, if locations is not
   *     prefixed or prefixed with <i>classpath:</i> then initializer will try to find scripts in
   *     classpath using {@code Thread.currentThread().}{@link Thread#getContextClassLoader()
   *     getContextClassLoader()}
   * @param scriptsPrefix prefix of migration scripts e.g. 'v' or empty string
   * @param scriptsSuffix suffix of migration scripts e.g. '.sql'
   * @param versionSeparator separate version from the other part of script name e.g. '.' or '__'
   * @param baselineOnMigrate whether to ignore scripts up to the version configured by {@code
   *     baselineVersion}
   * @param baselineVersion up to this version all the scripts ignored, unless schema is initialized
   *     first time, note that scripts with version equal to baseline version are also ignored
   * @param dataSource data source used for migrations
   * @param placeholderReplacer used to replace variables in script with configured values
   */
  @Inject
  public FlywaySchemaInitializer(
      @Named("db.schema.flyway.scripts.locations") String[] scriptsLocations,
      @Named("db.schema.flyway.scripts.prefix") String scriptsPrefix,
      @Named("db.schema.flyway.scripts.suffix") String scriptsSuffix,
      @Named("db.schema.flyway.scripts.version_separator") String versionSeparator,
      @Named("db.schema.flyway.baseline.enabled") boolean baselineOnMigrate,
      @Named("db.schema.flyway.baseline.version") String baselineVersion,
      DataSource dataSource,
      PlaceholderReplacer placeholderReplacer) {
    this.dataSource = dataSource;
    this.locations = scriptsLocations;
    this.scriptsPrefix = scriptsPrefix;
    this.scriptsSuffix = scriptsSuffix;
    this.versionSeparator = versionSeparator;
    this.baselineOnMigrate = baselineOnMigrate;
    this.baselineVersion = baselineVersion;
    this.placeholderReplacer = placeholderReplacer;
  }

  /** Creates a new flyway based initializer with default values. */
  public FlywaySchemaInitializer(DataSource dataSource, String... locations) {
    this(locations, "", ".sql", "__", false, "", dataSource, PlaceholderReplacer.NO_PLACEHOLDERS);
  }

  @Override
  public Map<String, String> init() throws SchemaInitializationException {
    final Map<String, String> initResult = new HashMap<>();
    try (final Connection conn = dataSource.getConnection()) {
      final Flyway flyway = new Flyway();
      flyway.setDataSource(dataSource);
      flyway.setLocations(locations);
      flyway.setClassLoader(Thread.currentThread().getContextClassLoader());
      final DbSupport dbSupport = DbSupportFactory.createDbSupport(conn, true);
      final MetaDataTable mt =
          new MetaDataTableImpl(
              dbSupport,
              dbSupport.getOriginalSchema().getTable(flyway.getTable()),
              flyway.getInstalledBy());
      initResult.put(BARE_DB_INIT_PROPERTY_NAME, String.valueOf(!mt.hasAppliedMigrations()));
      final String productName = conn.getMetaData().getDatabaseProductName().toLowerCase();
      flyway.setResolvers(
          new CustomSqlMigrationResolver(productName, dbSupport, placeholderReplacer));
      flyway.setSkipDefaultResolvers(true);
      flyway.setBaselineOnMigrate(baselineOnMigrate);
      if (baselineOnMigrate) {
        flyway.setBaselineVersionAsString(baselineVersion);
      }
      flyway.setSqlMigrationSeparator(versionSeparator);
      flyway.setSqlMigrationSuffix(scriptsSuffix);
      flyway.setSqlMigrationPrefix(scriptsPrefix);
      flyway.migrate();
    } catch (SQLException | RuntimeException x) {
      throw new SchemaInitializationException(x.getLocalizedMessage(), x);
    }
    return initResult;
  }
}
