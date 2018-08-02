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

import com.google.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Provides data source based on jndi resource name.
 *
 * @author Yevhenii Voevodin
 */
public class JndiDataSourceProvider implements Provider<DataSource> {

  @Inject
  @Named("jndi.datasource.name")
  private String name;

  @Override
  public DataSource get() {
    try {
      final InitialContext context = new InitialContext();
      return (DataSource) context.lookup(name);
    } catch (NamingException x) {
      throw new IllegalStateException(x.getLocalizedMessage(), x);
    }
  }
}
