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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.core.db.schema.SchemaInitializationException;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests {@link FlywaySchemaInitializer}.
 *
 * @author Yevhenii Voevodin
 */
public class FlywaySchemaInitializerTest {

    private static final String SCRIPTS_ROOT = "flyway/sql";

    private JdbcDataSource dataSource;

    @BeforeMethod
    public void setUp() throws URISyntaxException {
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:flyway_test;DB_CLOSE_DELAY=-1");
    }

    @AfterMethod
    public void cleanup() throws SQLException, URISyntaxException {
        try (Connection conn = dataSource.getConnection()) {
            RunScript.execute(conn, new StringReader("SHUTDOWN"));
        }
        IoUtil.deleteRecursive(targetDir().resolve(Paths.get(SCRIPTS_ROOT)).toFile());
    }

    @Test
    public void initializesSchemaWhenDatabaseIsEmpty() throws Exception {
        createScript("1.0/1__init.sql", "CREATE TABLE test (id INT, text TEXT, PRIMARY KEY (id));");
        createScript("1.0/2__add_data.sql", "INSERT INTO test VALUES(1, 'test1');" +
                                            "INSERT INTO test VALUES(2, 'test2');" +
                                            "INSERT INTO test VALUES(3, 'test3');");
        createScript("2.0/1__add_more_data.sql", "INSERT INTO test VALUES(4, 'test4');");
        createScript("2.0/postgresql/1__add_more_data.sql", "INSERT INTO test VALUES(4, 'postgresql-data');");

        final SchemaInitializer initializer = FlywayInitializerBuilder.from(dataSource).build();
        initializer.init();

        assertEquals(queryEntities(), Sets.newHashSet(new TestEntity(1, "test1"),
                                                      new TestEntity(2, "test2"),
                                                      new TestEntity(3, "test3"),
                                                      new TestEntity(4, "test4")));

        // second init must do nothing, so there are no conflicts
        initializer.init();
    }

    @Test(expectedExceptions = SchemaInitializationException.class)
    public void failsIfBaseLineIsNotConfiguredProperly() throws Exception {
        execQuery("CREATE TABLE test (id INT, text TEXT, PRIMARY KEY (id));" +
                  "INSERT INTO test VALUES(1, 'test1');" +
                  "INSERT INTO test VALUES(2, 'test2');" +
                  "INSERT INTO test VALUES(3, 'test3');");
        createScript("1.0/1__init.sql", "CREATE TABLE test (id INT, text TEXT, PRIMARY KEY (id));");

        FlywayInitializerBuilder.from(dataSource)
                                .setBaselineOnMigrate(true)
                                .setBaselineVersion("1.0")
                                .build()
                                .init();
    }

    @Test
    public void executesOnlyThoseMigrationsWhichGoAfterBaseline() throws Exception {
        execQuery("CREATE TABLE test (id INT, text TEXT, PRIMARY KEY (id));");
        createScript("1.0/1__init.sql", "CREATE TABLE test (id INT, text TEXT, PRIMARY KEY (id));");
        createScript("2.0/1__add_data.sql", "INSERT INTO test VALUES(1, 'test1');" +
                                            "INSERT INTO test VALUES(2, 'test2');" +
                                            "INSERT INTO test VALUES(3, 'test3');");
        final FlywaySchemaInitializer initializer = FlywayInitializerBuilder.from(dataSource)
                                                                            .setBaselineOnMigrate(true)
                                                                            .setBaselineVersion("1.0.1")
                                                                            .build();

        initializer.init();

        assertEquals(queryEntities(), Sets.newHashSet(new TestEntity(1, "test1"),
                                                      new TestEntity(2, "test2"),
                                                      new TestEntity(3, "test3")));

        // second init must do nothing, so there are no conflicts
        initializer.init();
    }

    @Test
    public void initializesSchemaWhenDatabaseIsEmptyAndBaselineIsConfigured() throws Exception {
        createScript("1.0/1__init.sql", "CREATE TABLE test (id INT, text TEXT, PRIMARY KEY (id));");
        createScript("2.0/1__add_data.sql", "INSERT INTO test VALUES(1, 'test1');" +
                                            "INSERT INTO test VALUES(2, 'test2');" +
                                            "INSERT INTO test VALUES(3, 'test3');");

        final FlywaySchemaInitializer initializer = FlywayInitializerBuilder.from(dataSource)
                                                                            .setBaselineOnMigrate(true)
                                                                            .setBaselineVersion("1.0.1")
                                                                            .build();
        initializer.init();

        assertEquals(queryEntities(), Sets.newHashSet(new TestEntity(1, "test1"),
                                                      new TestEntity(2, "test2"),
                                                      new TestEntity(3, "test3")));

        // second init must do nothing, so there are no conflicts
        initializer.init();
    }

    @Test
    public void selectsProviderSpecificScriptsInPreferenceToDefaultOnes() throws Exception {
        createScript("1.0/1__init.sql", "CREATE TABLE test (id INT, text TEXT, PRIMARY KEY (id));");
        createScript("2.0/1__add_data.sql", "INSERT INTO test VALUES(1, 'default data');");
        createScript("2.0/h2/1__add_data.sql", "INSERT INTO test VALUES(1, 'h2 data');");

        final FlywaySchemaInitializer initializer = FlywayInitializerBuilder.from(dataSource).build();
        initializer.init();

        assertEquals(queryEntities(), Sets.newHashSet(new TestEntity(1, "h2 data")));

        // second init must do nothing, so there are no conflicts
        initializer.init();
    }

    @Test
    public void replacesVariablesWhenPlaceholderReplacerIsConfigured() throws Exception {
        createScript("1.0/1__init.sql", "CREATE TABLE test (id INT, text TEXT, PRIMARY KEY (id));" +
                                        "INSERT INTO test VALUES(1, '${variable}');");

        FlywayInitializerBuilder.from(dataSource)
                                .setReplacer(new PlaceholderReplacer(ImmutableMap.of("variable", "test"), "${", "}"))
                                .build()
                                .init();

        assertEquals(queryEntities(), Sets.newHashSet(new TestEntity(1, "test")));
    }

    private Set<TestEntity> queryEntities() throws SQLException {
        final Set<TestEntity> entities = new HashSet<>();
        try (Connection conn = dataSource.getConnection()) {
            final ResultSet result = RunScript.execute(conn, new StringReader("SELECT * FROM test"));
            while (result.next()) {
                entities.add(new TestEntity(result.getLong("id"), result.getString("text")));
            }
        }
        return entities;
    }

    private ResultSet execQuery(String query) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return RunScript.execute(conn, new StringReader(query));
        }
    }

    private static Path createScript(String relativePath, String content) throws URISyntaxException, IOException {
        return createFile(targetDir().resolve(Paths.get(SCRIPTS_ROOT))
                                     .resolve(relativePath).toString(),
                          content);
    }

    private static Path createFile(String filepath, String content) throws URISyntaxException, IOException {
        final Path path = targetDir().resolve(Paths.get(filepath));
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }

    private static Path targetDir() throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        return Paths.get(url.toURI()).getParent();
    }

    private static class TestEntity {
        final long   id;
        final String text;

        private TestEntity(long id, String text) {
            this.id = id;
            this.text = text;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TestEntity)) {
                return false;
            }
            final TestEntity that = (TestEntity)obj;
            return id == that.id
                   && Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + Long.hashCode(id);
            hash = 31 * hash + Objects.hashCode(text);
            return hash;
        }

        @Override
        public String toString() {
            return "TestEntity{" +
                   "id=" + id +
                   ", text='" + text + '\'' +
                   '}';
        }
    }

    private static class FlywayInitializerBuilder {

        public static FlywayInitializerBuilder from(DataSource dataSource) {
            try {
                final String scriptsRoot = targetDir().resolve(Paths.get(SCRIPTS_ROOT)).toString();
                return new FlywayInitializerBuilder().setDataSource(dataSource)
                                                     .setScriptsPrefix("")
                                                     .setScriptsSuffix(".sql")
                                                     .setVersionSeparator("__")
                                                     .setReplacer(PlaceholderReplacer.NO_PLACEHOLDERS)
                                                     .setBaselineOnMigrate(false)
                                                     .addLocation("filesystem:" + scriptsRoot);
            } catch (Exception x) {
                throw new RuntimeException(x.getMessage(), x);
            }
        }

        private DataSource          dataSource;
        private List<String>        locations;
        private String              scriptsPrefix;
        private String              scriptsSuffix;
        private String              versionSeparator;
        private boolean             baselineOnMigrate;
        private String              baselineVersion;
        private PlaceholderReplacer replacer;

        public FlywayInitializerBuilder setDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public FlywayInitializerBuilder setReplacer(PlaceholderReplacer replacer) {
            this.replacer = replacer;
            return this;
        }

        public FlywayInitializerBuilder addLocation(String location) {
            if (locations == null) {
                locations = new ArrayList<>();
            }
            locations.add(location);
            return this;
        }

        public FlywayInitializerBuilder setScriptsPrefix(String scriptsPrefix) {
            this.scriptsPrefix = scriptsPrefix;
            return this;
        }

        public FlywayInitializerBuilder setScriptsSuffix(String scriptsSuffix) {
            this.scriptsSuffix = scriptsSuffix;
            return this;
        }

        public FlywayInitializerBuilder setVersionSeparator(String versionSeparator) {
            this.versionSeparator = versionSeparator;
            return this;
        }

        public FlywayInitializerBuilder setBaselineOnMigrate(boolean baselineOnMigrate) {
            this.baselineOnMigrate = baselineOnMigrate;
            return this;
        }

        public FlywayInitializerBuilder setBaselineVersion(String baselineVersion) {
            this.baselineVersion = baselineVersion;
            return this;
        }

        public FlywaySchemaInitializer build() {
            if (locations == null) {
                throw new IllegalStateException("locations required");
            }
            return new FlywaySchemaInitializer(locations.toArray(new String[locations.size()]),
                                               scriptsPrefix,
                                               scriptsSuffix,
                                               versionSeparator,
                                               baselineOnMigrate,
                                               baselineVersion,
                                               dataSource,
                                               replacer);
        }
    }
}
