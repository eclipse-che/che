/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.core.db.schema.impl.flyway;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FlywayConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * Creates versions for scripts depending on the provided data.
 * <ul>A few examples:
 * <li>5.0.0-M7/v1__init.sql => 5.0.0.7.1</li>
 * <li>5.0.0-M8/v2.1__modify.sql => 5.0.0.8.2.1</li>
 * </ul>
 *
 * @author Yevhenii Voevodin
 */
class VersionResolver {

    private static final Pattern NOT_VERSION_CHARS_PATTERN = Pattern.compile("[^0-9.]");

    private final Map<String, String> normalizedDirs = new HashMap<>();

    /**
     * Creates migration version based on script data.
     *
     * @param script
     *         script for which to resolve the version
     * @param configuration
     *         flyway configuration used for resolution parameters
     */
    MigrationVersion resolve(SqlScript script, FlywayConfiguration configuration) {
        String normalizedDir = normalizedDirs.get(script.dir);
        if (normalizedDir == null) {
            // 5.0.0-M1 -> 5.0.0.M1 -> 5.0.0.1
            normalizedDir = NOT_VERSION_CHARS_PATTERN.matcher(script.dir.replace("-", ".")).replaceAll("");
            normalizedDirs.put(script.dir, normalizedDir);
        }

        // separate version from the other part of the name
        final int sepIdx = script.name.indexOf(configuration.getSqlMigrationSeparator());
        if (sepIdx == -1) {
            throw new FlywayException(format("sql script name '%s' is not valid, name must contain '%s'",
                                             script.name,
                                             configuration.getSqlMigrationSeparator()));
        }

        // check whether part before separator is not empty
        String version = script.name.substring(0, sepIdx);
        if (version.isEmpty()) {
            throw new FlywayException(format("sql script name '%s' is not valid, name must provide version like " +
                                             "'%s4%smigration_description.sql",
                                             configuration.getSqlMigrationPrefix(),
                                             script.name,
                                             configuration.getSqlMigrationSeparator()));
        }

        // extract sql script version without prefix
        final String prefix = configuration.getSqlMigrationPrefix();
        if (!isNullOrEmpty(prefix) && script.name.startsWith(prefix)) {
            version = version.substring(prefix.length());
        }
        return MigrationVersion.fromVersion(normalizedDir + '.' + version);
    }
}
