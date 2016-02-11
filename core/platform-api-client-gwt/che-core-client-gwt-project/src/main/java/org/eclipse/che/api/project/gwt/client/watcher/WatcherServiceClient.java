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
package org.eclipse.che.api.project.gwt.client.watcher;

import com.google.inject.ImplementedBy;

import org.eclipse.che.api.promises.client.Promise;

import javax.validation.constraints.NotNull;

/**
 * Special client service which allows send requests to special {@link org.eclipse.che.api.project.server.watcher.WatcherService} to
 * register or un register file system changes.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(WatcherServiceClientImpl.class)
public interface WatcherServiceClient {

    /**
     * Sends special request to register file system changes.
     *
     * @param pathToFolder
     *         folder which will be registered  to handle file system changes
     * @return an instance of {@link Promise}
     */
    public Promise<Void> registerRecursiveWatcher(@NotNull String pathToFolder);
}
