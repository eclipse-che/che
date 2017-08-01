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

import org.eclipse.che.api.system.shared.event.service.StoppingSystemServiceEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceItemStoppedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;

/**
 * Defines an interface for implementing termination
 * process for a certain service.
 *
 * @author Yevhenii Voevodin
 */
public interface ServiceTermination {

    /**
     * Terminates a certain service.
     * It's expected that termination is synchronous.
     *
     * @throws InterruptedException
     *         as termination is synchronous some of the implementations
     *         may need to wait for asynchronous jobs to finish their execution,
     *         so if termination is interrupted and implementation supports termination
     *         it should throw an interrupted exception
     */
    void terminate() throws InterruptedException;

    /**
     * Returns the name of the service which is terminated by this termination.
     * The name is used for logging/sending events like {@link StoppingSystemServiceEvent},
     * {@link SystemServiceItemStoppedEvent} or {@link SystemServiceStoppedEvent}.
     */
    String getServiceName();
}
