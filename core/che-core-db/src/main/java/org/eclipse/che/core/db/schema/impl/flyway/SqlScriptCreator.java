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
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.Resource;

import java.io.File;

import static java.lang.String.format;

/**
 * Creates new {@link SqlScript} instance from given resource.
 *
 * @author Yevhenii Voevodin
 */
class SqlScriptCreator {

    /**
     * Create a new instance of script based on location and resource.
     *
     * @param location
     *         root location of the given resource
     * @param resource
     *         script resource
     * @return a new instance of sql script based on location and resource
     * @throws FlywayException
     *         when script can't be created from the resource
     */
    SqlScript createScript(Location location, Resource resource) {
        final String separator = location.isClassPath() ? "/" : File.separator;
        // '/root-location/5.0.0-M7/v1__init.sql' -> '5.0.0-M7/v1__init.sql'
        final String relLocation = resource.getLocation().substring(location.getPath().length() + 1);
        final String[] paths = relLocation.split(separator);
        // 5.0.0-M1/v1__init.sql
        if (paths.length == 2) {
            return new SqlScript(resource, location, paths[0], null, paths[1]);
        }
        // 5.0.0-M1/postgresql/v1__init.sql
        if (paths.length == 3) {
            return new SqlScript(resource, location, paths[0], paths[1], paths[2]);
        }
        throw new FlywayException(format("Sql script location must be either in 'location-root/version-dir' " +
                                         "or in 'location-root/version-dir/provider-name', but script '%s' is not in root '%s'",
                                         resource.getLocation(),
                                         location.getPath()));
    }
}
