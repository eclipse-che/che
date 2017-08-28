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
package org.eclipse.che.datasource.ide.newDatasource.connector;

import java.util.Collection;

/**
 * Provides DB registered connectors
 */
public interface NewDatasourceConnectorAgent {

    /**
     * Register a new connector.
     * 
     * @param connector the connector to register
     */
    void register(NewDatasourceConnector connector);

    /**
     * Returns all registered connectors.
     * 
     * @return the connectors
     */
    Collection<NewDatasourceConnector> getConnectors();

    /**
     * Retrieve a connector by id.
     * 
     * @param id the id of the connector we seek
     * @return the connector if found, null otherwise
     */
    NewDatasourceConnector getConnector(String id);
}
