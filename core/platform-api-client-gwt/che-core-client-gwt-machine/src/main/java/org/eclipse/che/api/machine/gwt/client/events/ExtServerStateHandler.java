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
 * A handler for handling {@link ExtServerStateEvent}.
 *
 * @author Roman Nikitenko
 */
public interface ExtServerStateHandler extends EventHandler {

    /**
     * Called when extension server has been started.
     *
     * @param event
     *         the fired {@link ExtServerStateEvent}
     */
    void onExtServerStarted(ExtServerStateEvent event);

    /**
     * Called when extension server has been stopped.
     *
     * @param event
     *         the fired {@link ExtServerStateEvent}
     */
    void onExtServerStopped(ExtServerStateEvent event);
}
