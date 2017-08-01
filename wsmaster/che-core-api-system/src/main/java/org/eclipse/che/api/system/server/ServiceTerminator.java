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
package org.eclipse.che.api.system.server;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.system.shared.event.service.StoppingSystemServiceEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;

/**
 * Terminates system services.
 *
 * @author Yevhenii Voevodin
 */
class ServiceTerminator {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceTerminator.class);

    private final EventService            eventService;
    private final Set<ServiceTermination> terminations;

    @Inject
    ServiceTerminator(EventService eventService, Set<ServiceTermination> terminations) {
        this.eventService = eventService;
        this.terminations = terminations;
    }

    /**
     * Terminates system services.
     *
     * @throws InterruptedException
     *         when termination is interrupted
     */
    void terminateAll() throws InterruptedException {
        for (ServiceTermination termination : terminations) {
            LOG.info("Shutting down '{}' service", termination.getServiceName());
            eventService.publish(new StoppingSystemServiceEvent(termination.getServiceName()));
            try {
                termination.terminate();
            } catch (InterruptedException x) {
                LOG.error("Interrupted while waiting for '{}' service to shutdown", termination.getServiceName());
                throw x;
            }
            LOG.info("Service '{}' is shut down", termination.getServiceName());
            eventService.publish(new SystemServiceStoppedEvent(termination.getServiceName()));
        }
    }
}
