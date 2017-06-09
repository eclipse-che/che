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
package org.eclipse.che.api.system.shared.event.service;

import org.eclipse.che.api.system.shared.event.EventType;

/**
 * See {@link EventType#SERVICE_STOPPED} description.
 *
 * @author Yevhenii Voevodin
 */
public class SystemServiceStoppedEvent extends SystemServiceEvent {

    public SystemServiceStoppedEvent(String serviceName) {
        super(serviceName);
    }

    @Override
    public EventType getType() {
        return EventType.SERVICE_STOPPED;
    }
}
