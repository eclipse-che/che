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
package org.eclipse.che.core.db;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract JNDI factory that constructs {@link BasicDataSource} objects from the given params.
 * Should not be used directly and must be subclassed to provide instantiation params from needful
 * source.
 *
 * @author Sergii Kabashniuk
 */
public abstract class JNDIDataSourceFactory implements ObjectFactory {

  private static final Logger LOG = LoggerFactory.getLogger(JNDIDataSourceFactory.class);

  private final BasicDataSource dataSource;

  public JNDIDataSourceFactory(
      String userName,
      String password,
      String url,
      String driverClassName,
      String maxTotal,
      String maxIdle,
      String maxWaitMillis)
      throws Exception {
    Properties poolConfigurationProperties = new Properties();
    poolConfigurationProperties.setProperty("username", userName);
    poolConfigurationProperties.setProperty("password", password);
    poolConfigurationProperties.setProperty("url", url);
    poolConfigurationProperties.setProperty("driverClassName", driverClassName);
    poolConfigurationProperties.setProperty("maxTotal", maxTotal);
    poolConfigurationProperties.setProperty("maxIdle", maxIdle);
    poolConfigurationProperties.setProperty("maxWaitMillis", maxWaitMillis);
    dataSource = BasicDataSourceFactory.createDataSource(poolConfigurationProperties);
  }

  @Override
  public Object getObjectInstance(
      Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
    LOG.info(
        "This={} obj={} name={} Context={} environment={}", this, obj, name, nameCtx, environment);
    return dataSource;
  }

  /**
   * Util method to convert string {@code "NULL"} to null reference. Allows to set string {@code
   * "NULL"} as a value of the property instead of making sure it is unset as it is done in {@link
   * org.eclipse.che.inject.CheBootstrap}
   *
   * @param value value to transform if needed
   * @return null or passed value
   */
  protected static String nullStringToNullReference(String value) {
    return "NULL".equals(value) ? null : value;
  }
}
