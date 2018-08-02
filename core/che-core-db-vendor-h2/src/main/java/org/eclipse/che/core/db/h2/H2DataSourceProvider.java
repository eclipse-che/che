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
package org.eclipse.che.core.db.h2;

import java.nio.file.Paths;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.sql.DataSource;
import org.eclipse.che.core.db.JndiDataSourceProvider;

/**
 * Provides data source for h2 database.
 *
 * @author Yevhenii Voevodin
 */
public class H2DataSourceProvider implements Provider<DataSource> {

  @Inject
  @Named("che.database")
  private String storageRoot;

  @Inject private JndiDataSourceProvider jndiDataSourceProvider;

  @Override
  public DataSource get() {
    System.setProperty("h2.baseDir", Paths.get(storageRoot).resolve("db").toString());
    return jndiDataSourceProvider.get();
  }
}
