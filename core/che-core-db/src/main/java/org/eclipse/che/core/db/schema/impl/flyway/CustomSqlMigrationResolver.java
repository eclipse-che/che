/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.BaseMigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationExecutor;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves SQL migrations from the configured locations, allows overriding of default scripts with
 * vendor specific ones.
 *
 * <ul>
 *   Migration scripts must follow the next rules:
 *   <li>It must be placed in the project dir directory e.g. <i>5.0.1</i>
 *   <li>Project dir directory must be placed in dedicated directory e.g. <i>resources/sql</i>
 *   <li>Migration/Initialization script name must start with a number e.g <i>1.init.sql</i>, this
 *       number indicates the subversion of the database migration, e.g. for dir <i>5.0.0</i> and
 *       migration script <i>1.init.sql</i> database migration dir will be <i>5.0.0.1</i>
 *   <li>If a file is not a part of migration it shouldn't end with migration prefix e.g.
 *       <i>.sql</i> then resolver will ignore it
 * </ul>
 *
 * <p>For the structure:
 *
 * <pre>
 *   resources/
 *      sql/
 *        5.0.0/
 *          1.init.sql
 *        5.0.0-M1/
 *          1.rename_fields.sql
 *          2.add_workspace_constraint.sql
 *          postgresql/
 *            2.add_workspace_constraint.sql
 *        5.0.1/
 *          1.stacks_migration.sql
 * </pre>
 *
 * And configuration:
 *
 * <pre>
 *     prefix - ""
 *     suffix - ".sql"
 *     separator - "."
 *     locations - "classpath:sql"
 * </pre>
 *
 * <ul>
 *   4 database migrations will be resolved
 *   <li>5.0.0.1 - initialization script based on file <i>sql/5.0.0/1.init.sql</i>
 *   <li>5.0.0.1.1 - modification script based on file <i>sql/5.0.0-M1/1.rename_fields.sql</i>
 *   <li>5.0.0.1.2 - modification script(if postgresql is current provider) based on file
 *       <i>sql/5.0.0-M1/postgresql/2.add_workspace_constraint.sql</i>
 *   <li>5.0.1.1 - modification script based on file <i>sql/5.0.1/1.stacks_migrations.sql</i>
 * </ul>
 *
 * <p>It is also possible to configure several locations then all of those locations will be
 * analyzed for migration scripts existence. For example:
 *
 * <p>For the structure:
 *
 * <pre>
 *  che/
 *    resources/
 *       che-schema/
 *         5.0.0/
 *          1.init.sql
 *  another-project/
 *    resources/
 *      custom-schema/
 *        5.0.0/
 *          2.init_additional_tables.sql
 * </pre>
 *
 * And configuration:
 *
 * <pre>
 *     prefix - ""
 *     suffix - ".sql"
 *     separator - "."
 *     locations - "classpath:che-schema, classpath:custom-schema"
 * </pre>
 *
 * <ul>
 *   2 database migrations will be resolved
 *   <li>5.0.0.1 - initialization script based on file <i>che-schema/5.0.0/1.init.sql</i>
 *   <li>5.0.0.2 - modification script based on file
 *       <i>custom-schema/5.0.0/2.init_additional_tables.sql</i>
 * </ul>
 *
 * @author Yevhenii Voevodin
 */
public class CustomSqlMigrationResolver extends BaseMigrationResolver {

  private static final Logger LOG = LoggerFactory.getLogger(CustomSqlMigrationResolver.class);

  private final String vendorName;
  private final ResourcesFinder finder;
  private final VersionResolver versionResolver;
  private final SqlScriptCreator scriptsCreator;
  private final DbSupport dbSupport;
  private final PlaceholderReplacer placeholderReplacer;

  public CustomSqlMigrationResolver(
      String dbProviderName, DbSupport dbSupport, PlaceholderReplacer placeholderReplacer) {
    this.vendorName = dbProviderName;
    this.dbSupport = dbSupport;
    this.placeholderReplacer = placeholderReplacer;
    this.finder = new ResourcesFinder();
    this.versionResolver = new VersionResolver();
    this.scriptsCreator = new SqlScriptCreator();
  }

  @Override
  public Collection<ResolvedMigration> resolveMigrations() {
    try {
      return resolveSqlMigrations();
    } catch (IOException | SQLException x) {
      throw new RuntimeException(x.getLocalizedMessage(), x);
    }
  }

  private List<ResolvedMigration> resolveSqlMigrations() throws IOException, SQLException {
    LOG.info(
        "Searching for SQL scripts in locations {}",
        Arrays.toString(flywayConfiguration.getLocations()));
    final Map<Location, List<Resource>> allResources = finder.findResources(flywayConfiguration);
    LOG.debug("Found scripts: {}", allResources);

    final Map<String, Map<String, SqlScript>> scriptsInDir = new HashMap<>();
    for (Location location : allResources.keySet()) {
      final List<Resource> resources = allResources.get(location);
      for (Resource resource : resources) {
        final SqlScript newScript = scriptsCreator.createScript(location, resource);
        if (!scriptsInDir.containsKey(newScript.dir)) {
          scriptsInDir.put(newScript.dir, new HashMap<>(4));
        }
        final Map<String, SqlScript> existingScripts = scriptsInDir.get(newScript.dir);
        final SqlScript existingScript = existingScripts.get(newScript.name);
        if (existingScript == null) {
          existingScripts.put(newScript.name, newScript);
        } else if (Objects.equals(existingScript.vendor, newScript.vendor)) {
          throw new FlywayException(
              format(
                  "More than one script with name '%s' is registered for "
                      + "database vendor '%s', script '%s' conflicts with '%s'",
                  newScript.name, existingScript.vendor, newScript, existingScript));
        } else if (vendorName.equals(newScript.vendor)) {
          existingScripts.put(newScript.name, newScript);
        }
      }
    }

    final Map<MigrationVersion, ResolvedMigration> migrations = new HashMap<>();
    for (SqlScript script :
        scriptsInDir
            .values()
            .stream()
            .flatMap(scripts -> scripts.values().stream())
            .collect(toList())) {
      final ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
      migration.setVersion(versionResolver.resolve(script, flywayConfiguration));
      migration.setScript(script.resource.getLocation());
      migration.setPhysicalLocation(script.resource.getLocationOnDisk());
      migration.setType(MigrationType.SQL);
      migration.setDescription(script.name);
      migration.setChecksum(
          ByteSource.wrap(script.resource.loadAsBytes()).hash(Hashing.crc32()).asInt());
      migration.setExecutor(
          new SqlMigrationExecutor(
              dbSupport, script.resource, placeholderReplacer, flywayConfiguration));
      if (migrations.put(migration.getVersion(), migration) != null) {
        throw new FlywayException("Two migrations with the same version detected");
      }
    }
    return new ArrayList<>(migrations.values());
  }
}
