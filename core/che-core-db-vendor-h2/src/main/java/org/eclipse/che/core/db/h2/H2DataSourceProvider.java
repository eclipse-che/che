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
package org.eclipse.che.core.db.h2;

import org.eclipse.che.core.db.JndiDataSourceProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.sql.DataSource;
import java.nio.file.Paths;

/**
 * Provides data source for h2 database.
 *
 * @author Yevhenii Voevodin
 */
public class H2DataSourceProvider implements Provider<DataSource> {

    @Inject
    @Named("che.database")
    private String storageRoot;

    @Inject
    private JndiDataSourceProvider jndiDataSourceProvider;

    @Override
    public DataSource get() {
        System.setProperty("h2.baseDir", Paths.get(storageRoot).resolve("db").toString());
        return jndiDataSourceProvider.get();
    }
}
