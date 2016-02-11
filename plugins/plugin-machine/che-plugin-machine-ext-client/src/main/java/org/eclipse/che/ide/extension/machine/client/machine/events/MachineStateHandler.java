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
package org.eclipse.che.ide.extension.machine.client.machine.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * A handler for handling {@link MachineStateEvent}.
 *
 * @author Artem Zatsarynnyi
 */
public interface MachineStateHandler extends EventHandler {

    /**
     * Called when machine has been run.
     *
     * @param event
     *         the fired {@link MachineStateEvent}
     */
    void onMachineRunning(MachineStateEvent event);

    /**
     * Called when machine has been destroyed.
     *
     * @param event
     *         the fired {@link MachineStateEvent}
     */
    void onMachineDestroyed(MachineStateEvent event);
}
