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

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests {@link VersionResolver}.
 *
 * @author Yevhenii Voevodin
 */
public class VersionResolverTest {

    private final Flyway          flyway   = new Flyway();
    private final VersionResolver resolver = new VersionResolver();

    @BeforeMethod
    public void setUpDefaults() {
        flyway.setSqlMigrationSuffix(".sql");
        flyway.setSqlMigrationPrefix("");
        flyway.setSqlMigrationSeparator("__");
    }

    @Test(dataProvider = "validScripts")
    public void resolvesVersion(String dir, String name, String expectedVersion) {
        final SqlScript script = new SqlScript(new FileSystemResource("sql/" + dir + "/" + name),
                                               new Location("filesystem:sql"),
                                               dir,
                                               null,
                                               name);

        assertEquals(resolver.resolve(script, flyway), MigrationVersion.fromVersion(expectedVersion));
    }

    @Test(dataProvider = "invalidScripts", expectedExceptions = FlywayException.class)
    public void failsToResolveVersions(String dir, String name) throws Exception {
        final SqlScript script = new SqlScript(new FileSystemResource("sql/" + dir + "/" + name),
                                               new Location("filesystem:sql"),
                                               dir,
                                               null,
                                               name);
        resolver.resolve(script, flyway);
    }

    @DataProvider
    public static Object[][] invalidScripts() {
        return new String[][] {
                {"1.0", "2016-11-11__init.sql"},
                {"1.0", "one__init.sql"},
                {"1.0", "__init.sql"},
                {"1.0", "version1__script.sql"},
                {"1.0", "1..1__script.sql"},
                {"5..0.0", "1__init.sql"}
        };
    }

    @DataProvider
    public static Object[][] validScripts() {
        return new Object[][] {
                {"5.0.0-M7", "1__init.sql", "5.0.0.7.1"},
                {"5.0.0-M7", "1.1__init_sub_tables.sql", "5.0.0.7.1.1"},
                {"6.0", "0.1__specific_update.sql", "6.0.0.1"},
                {"1.0", "1__simple.sql", "1.0.1"}
        };
    }
}
