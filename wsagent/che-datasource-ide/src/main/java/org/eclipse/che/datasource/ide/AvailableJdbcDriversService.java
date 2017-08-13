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
package org.eclipse.che.datasource.ide;

import org.eclipse.che.datasource.ide.events.JdbcDriversFetchedEvent;

import java.util.List;

public interface AvailableJdbcDriversService {

    /**
     * will retrieve the list of drivers from the server and keep it. This method is asynchronous. Viewers can get notified once the drivers
     * are fetched with {@link JdbcDriversFetchedEvent} event.
     */
    void fetch();

    /**
     * get the list of JDBC drivers that has been fetched previously
     */
    List<String> getDrivers();

}
