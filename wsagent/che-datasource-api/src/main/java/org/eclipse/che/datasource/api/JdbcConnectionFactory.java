/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.datasource.api;

import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;
import org.eclipse.che.datasource.shared.exception.DatabaseDefinitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

/**
 * Creates JDBC Connections
 */
public class JdbcConnectionFactory {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JdbcConnectionFactory.class);

    /**
     * URL pattern for MySQL databases.
     */
    private static final String URL_TEMPLATE_MYSQL = "jdbc:mysql://{0}:{1}/{2}";
    protected String profileApiUrl;


    public JdbcConnectionFactory() {
        profileApiUrl = "http://192.168.1.35:8080" + "/profile";
        //TODO:Have to replace API URL
    }

    /**
     * builds a JDBC {@link Connection} for a datasource.
     *
     * @param configuration the datasource configuration
     * @return a connection
     * @throws SQLException                if the creation of the connection failed
     * @throws DatabaseDefinitionException if the configuration is incorrect
     */
    public Connection getDatabaseConnection(final DatabaseConfigurationDTO configuration) throws SQLException, DatabaseDefinitionException {
        if (LOG.isInfoEnabled()) {
            Driver[] drivers = Collections.list(DriverManager.getDrivers()).toArray(new Driver[0]);
            LOG.info("Available jdbc drivers : {}", Arrays.toString(drivers));
        }

        Properties info = new Properties();
        info.setProperty("user", configuration.getUsername());

        final String password = configuration.getPassword();
        if (password != null && !password.isEmpty()) {
            info.setProperty("password", password);
        } else {
            info.setProperty("password", "");
        }

        String jdbcUrl = getJdbcUrl(configuration);
        final Connection connection = DriverManager.getConnection(jdbcUrl, info.getProperty("user"),info.getProperty("password"));
        return connection;
    }

    /**
     * Builds a JDBC URL for a datasource.
     *
     * @param configuration the datasource configuration
     * @return the URL
     * @throws DatabaseDefinitionException in case the datasource configuration is incorrect
     */
    private String getJdbcUrl(final DatabaseConfigurationDTO configuration) throws DatabaseDefinitionException {
        // Should we check and sanitize input values ?
        if (configuration.getDatabaseType() == null) {
            throw new DatabaseDefinitionException("Database type is null in " + configuration.toString());
        }
        switch (configuration.getDatabaseType()) {
            case MYSQL:
                return getMySQLJdbcUrl(configuration);
            default:
                throw new DatabaseDefinitionException("Unknown database type "
                        + configuration.getDatabaseType()
                        + " in "
                        + configuration.toString());
        }
    }

    /**
     * Builds a JDBC URL for a MySQL datasource.
     *
     * @param configuration the datasource configuration
     * @return the URL
     * @throws DatabaseDefinitionException in case the datasource configuration is incorrect
     */
    private String getMySQLJdbcUrl(final DatabaseConfigurationDTO configuration) {
        String url = MessageFormat.format(URL_TEMPLATE_MYSQL,
                configuration.getHostName(),
                Integer.toString(configuration.getPort()),
                configuration.getDatabaseName());
        return url;
    }


}
