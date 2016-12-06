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
package org.eclipse.che.commons.test.db;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides utility methods to work with h2 in tests.
 *
 * @author Yevhenii Voevodin
 */
public final class H2TestHelper {

    public static final String DEFAULT_IN_MEMORY_DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

    /**
     * Creates new default datasource to in memory database
     * with url {@value #DEFAULT_IN_MEMORY_DB_URL}.
     * Boots database if this is invoked first time, database
     * won't be shutdown until 'SHUTDOWN' query is executed
     * or {@link #shutdownDefault()} is called directly.
     *
     * @return datasource to the in memory database
     */
    public static DataSource inMemoryDefault() {
        final JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(DEFAULT_IN_MEMORY_DB_URL);
        return dataSource;
    }

    /**
     * Shutdowns default in memory database with url {@value #DEFAULT_IN_MEMORY_DB_URL}.
     *
     * @throws SQLException
     *         when any sql error occurs
     */
    public static void shutdownDefault() throws SQLException {
        try (Connection conn = inMemoryDefault().getConnection()) {
            RunScript.execute(conn, new StringReader("SHUTDOWN"));
        }
    }
}
