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
package org.eclipse.che.runner.sdk;

import org.eclipse.che.api.runner.internal.ApplicationProcess;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for {@link ApplicationUpdater}s.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class ApplicationUpdaterRegistry {
    private final Map<Long, ApplicationUpdater> applicationUpdaters;

    public ApplicationUpdaterRegistry() {
        applicationUpdaters = new ConcurrentHashMap<>();
    }

    /**
     * Register {@link ApplicationUpdater}.
     *
     * @param process
     * @param updater
     *         {@link ApplicationUpdater} to register
     */
    public void registerUpdater(ApplicationProcess process, ApplicationUpdater updater) {
        applicationUpdaters.put(process.getId(), updater);
    }

    /**
     * Unregister {@link ApplicationUpdater} for {@link ApplicationProcess} with the specified id.
     *
     * @param id
     *         id of the {@link ApplicationProcess}
     * @return removed {@link ApplicationUpdater}
     */
    public ApplicationUpdater unregisterUpdater(long id) {
        return applicationUpdaters.remove(id);
    }

    /**
     * Get {@link ApplicationUpdater} by the specified {@link ApplicationProcess}'s id.
     *
     * @param id
     *         id of the {@link ApplicationProcess}
     * @return {@link ApplicationUpdater}
     */
    public ApplicationUpdater getUpdaterByApplicationProcessId(long id) {
        return applicationUpdaters.get(id);
    }
}
