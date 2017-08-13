/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.datasource.ide.newDatasource.connector;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public class NewDatasourceConnectorAgentImpl implements NewDatasourceConnectorAgent {

    private final SortedSet<NewDatasourceConnector> registeredConnectors;

    @Inject
    public NewDatasourceConnectorAgentImpl() {
        registeredConnectors = new TreeSet<>();
    }

    @Override
    public void register(final NewDatasourceConnector connector) {
        if (registeredConnectors.contains(connector)) {
            // TODO this shouldn't happen: notification error instead of the alert ?
            Window.alert("Datasource connector with " + connector.getId() + " id already exists");
            return;
        }

        registeredConnectors.add(connector);
    }

    @Override
    public Collection<NewDatasourceConnector> getConnectors() {
        return registeredConnectors;
    }

    @Override
    public NewDatasourceConnector getConnector(final String id) {
        if (id == null) {
            return null;
        }
        for (final NewDatasourceConnector connector : registeredConnectors) {
            if (id.equals(connector.getId())) {
                return connector;
            }
        }
        return null;
    }
}
