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
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests {@link ResourcesFinder}.
 *
 * @author Yevhenii Voevodin
 */
public class ResourcesFinderTest {

    private final List<Path> cleanAfter = new ArrayList<>();
    private final Flyway     flyway     = new Flyway();

    @BeforeMethod
    public void setUpDefaults() {
        flyway.setSqlMigrationSuffix(".sql");
        flyway.setSqlMigrationPrefix("");
    }

    @AfterMethod
    public void cleanup() throws IOException {
        for (Path path : cleanAfter) {
            Files.delete(path);
        }
        cleanAfter.clear();
    }

    @Test
    public void findsScriptsInClassPath() throws Exception {
        flyway.setLocations("classpath:finder-sql-files");
        cleanAfter.addAll(createFiles("finder-sql-files/1.0/1.sql",
                                      "finder-sql-files/1.0/2.sql",
                                      "finder-sql-files/2.0/1.sql",
                                      "finder-sql-files/2.0/postgresql/1.sql"));

        final Set<String> locations = findResources(flyway).get("classpath:finder-sql-files");

        assertEquals(locations, newHashSet("finder-sql-files/1.0/1.sql",
                                           "finder-sql-files/1.0/2.sql",
                                           "finder-sql-files/2.0/1.sql",
                                           "finder-sql-files/2.0/postgresql/1.sql"));
    }

    @Test
    public void findsScriptsOnFileSystem() throws Exception {
        final List<Path> paths = createFiles("finder-sql-files/1.0/1.sql",
                                             "finder-sql-files/1.0/2.sql",
                                             "finder-sql-files/2.0/1.sql",
                                             "finder-sql-files/2.0/postgresql/1.sql");
        cleanAfter.addAll(paths);
        final Path finderSqlFiles = paths.get(0).getParent().getParent();
        final String fsLocation = "filesystem:" + finderSqlFiles.toAbsolutePath();
        flyway.setLocations(fsLocation);

        final Set<String> locations = findResources(flyway).get(fsLocation);

        assertEquals(locations,
                     newHashSet(finderSqlFiles.resolve("1.0").resolve("1.sql").toString(),
                                finderSqlFiles.resolve("1.0").resolve("2.sql").toString(),
                                finderSqlFiles.resolve("2.0").resolve("1.sql").toString(),
                                finderSqlFiles.resolve("2.0").resolve("postgresql").resolve("1.sql").toString()));
    }

    @Test
    public void findsFileSystemAndClassPathScripts() throws Exception {
        final List<Path> paths = createFiles("finder-fs-sql-files/1.0/1.sql",
                                             "finder-fs-sql-files/2.0/2.sql",
                                             "finder-cp-sql-files/1.0/2.sql",
                                             "finder-cp-sql-files/2.0/postgresql/1.sql");
        cleanAfter.addAll(paths);
        final Path finderFsSqlFiles = paths.get(0).getParent().getParent();
        final String fsLocation = "filesystem:" + finderFsSqlFiles.toAbsolutePath();
        final String cpLocation = "classpath:finder-cp-sql-files";
        flyway.setLocations(fsLocation, cpLocation);

        final Map<String, Set<String>> locations = findResources(flyway);

        assertEquals(locations.get(fsLocation), newHashSet(finderFsSqlFiles.resolve("1.0")
                                                                           .resolve("1.sql")
                                                                           .toString(),
                                                           finderFsSqlFiles.resolve("2.0")
                                                                           .resolve("2.sql")
                                                                           .toString()));
        assertEquals(locations.get(cpLocation), newHashSet("finder-cp-sql-files/1.0/2.sql",
                                                           "finder-cp-sql-files/2.0/postgresql/1.sql"));
    }

    private static Map<String, Set<String>> findResources(FlywayConfiguration configuration) throws IOException {
        final Map<Location, List<Resource>> resources = new ResourcesFinder().findResources(configuration);
        final Map<String, Set<String>> locations = new HashMap<>();
        for (Map.Entry<Location, List<Resource>> entry : resources.entrySet()) {
            locations.put(entry.getKey().toString(), entry.getValue()
                                                          .stream()
                                                          .map(Resource::getLocation)
                                                          .collect(Collectors.toSet()));
        }
        return locations;
    }

    private static List<Path> createFiles(String... paths) throws URISyntaxException, IOException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        final Path classesDir = Paths.get(url.toURI());
        final List<Path> createdFiles = new ArrayList<>(paths.length);
        for (String stringPath : paths) {
            final Path path = classesDir.resolve(Paths.get(stringPath));
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, path.toString().getBytes(StandardCharsets.UTF_8));
            createdFiles.add(path);
        }
        return createdFiles;
    }
}
