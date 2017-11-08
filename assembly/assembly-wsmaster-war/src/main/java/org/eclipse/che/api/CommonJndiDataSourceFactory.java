/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api;

import static com.google.common.base.MoreObjects.firstNonNull;

import org.eclipse.che.core.db.JNDIDataSourceFactory;
import org.eclipse.che.core.db.h2.H2SQLJndiDataSourceFactory;
import org.eclipse.che.core.db.postgresql.PostgreSQLJndiDataSourceFactory;

/**
 * Creates appropriate JNDI data source factory depending on system variable.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class CommonJndiDataSourceFactory extends JNDIDataSourceFactory {

  public CommonJndiDataSourceFactory() throws Exception {
    super(
        firstNonNull(
            System.getenv("CHE_JDBC_USERNAME"),
            isMultiuser()
                ? PostgreSQLJndiDataSourceFactory.DEFAULT_USERNAME
                : H2SQLJndiDataSourceFactory.DEFAULT_USERNAME),
        firstNonNull(
            System.getenv("CHE_JDBC_PASSWORD"),
            isMultiuser()
                ? PostgreSQLJndiDataSourceFactory.DEFAULT_PASSWORD
                : H2SQLJndiDataSourceFactory.DEFAULT_PASSWORD),
        firstNonNull(
            System.getenv("CHE_JDBC_URL"),
            isMultiuser()
                ? PostgreSQLJndiDataSourceFactory.DEFAULT_URL
                : H2SQLJndiDataSourceFactory.DEFAULT_URL),
        firstNonNull(
            System.getenv("CHE_JDBC_DRIVER__CLASS__NAME"),
            isMultiuser()
                ? PostgreSQLJndiDataSourceFactory.DEFAULT_DRIVER__CLASS__NAME
                : H2SQLJndiDataSourceFactory.DEFAULT_DRIVER__CLASS__NAME),
        firstNonNull(
            System.getenv("CHE_JDBC_MAX__TOTAL"),
            isMultiuser()
                ? PostgreSQLJndiDataSourceFactory.DEFAULT_MAX__TOTAL
                : H2SQLJndiDataSourceFactory.DEFAULT_MAX__TOTAL),
        firstNonNull(
            System.getenv("CHE_JDBC_MAX__IDLE"),
            isMultiuser()
                ? PostgreSQLJndiDataSourceFactory.DEFAULT_MAX__IDLE
                : H2SQLJndiDataSourceFactory.DEFAULT_MAX__IDLE),
        firstNonNull(
            System.getenv("CHE_JDBC_MAX__WAIT__MILLIS"),
            isMultiuser()
                ? PostgreSQLJndiDataSourceFactory.DEFAULT_MAX__WAIT__MILLIS
                : H2SQLJndiDataSourceFactory.DEFAULT_MAX__WAIT__MILLIS));
  }

  private static Boolean isMultiuser() {
    return Boolean.valueOf(System.getenv("CHE_MULTIUSER"));
  }
}
