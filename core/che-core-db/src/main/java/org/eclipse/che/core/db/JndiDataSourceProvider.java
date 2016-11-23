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
    @Named("db.jndi.datasource.name")
    private String name;

    @Override
    public DataSource get() {
        try {
            final InitialContext context = new InitialContext();
            return (DataSource)context.lookup(name);
        } catch (NamingException x) {
            throw new IllegalStateException(x.getLocalizedMessage(), x);
        }
    }
}
