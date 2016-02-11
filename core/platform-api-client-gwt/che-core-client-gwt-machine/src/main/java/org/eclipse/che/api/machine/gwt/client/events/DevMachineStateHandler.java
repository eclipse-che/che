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
package org.eclipse.che.api.machine.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * A handler for handling {@link DevMachineStateEvent}.
 *
 * @author Roman Nikitenko
 */
public interface DevMachineStateHandler extends EventHandler {

    /**
     * Called when dev machine has been started.
     *
     * @param event
     *         the fired {@link DevMachineStateEvent}
     */
    void onMachineStarted(DevMachineStateEvent event);

    /**
     * Called when dev machine has been destroyed.
     *
     * @param event
     *         the fired {@link DevMachineStateEvent}
     */
    void onMachineDestroyed(DevMachineStateEvent event);
}
