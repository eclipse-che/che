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
package org.eclipse.che.ide.api.event.ng;

import org.eclipse.che.api.promises.client.Promise;

/**
 * Send file tracking operation calls on server side. There are several types of such calls:
 * <ul>
 *     <li>
 *         START/STOP - tells to start/stop tracking specific file
 *     </li>
 *     <li>
 *         SUSPEND/RESUME - tells to start/stop tracking all files registered for specific endpoint
 *     </li>
 *     <li>
 *         MOVE - tells that file that is being tracked should be moved (renamed)
 *     </li>
 * </ul>
 */
public interface ClientServerEventService {

    /**
     * Sends event on server side which tells to start tracking specific file
     *
     * @param path
     *         the path to the specific file
     */
    Promise<Void> sendFileTrackingStartEvent(String path);

    /**
     * Sends event on server side which tells to stop tracking specific file
     *
     * @param path
     *         the path to the specific file
     */
    Promise<Void> sendFileTrackingStopEvent(String path);

    /** Sends event on server side which tells to suspend tracking all files registered for specific endpoint */
    Promise<Void> sendFileTrackingSuspendEvent();

    /** Sends event on server side which tells to resume tracking all files registered for specific endpoint */
    Promise<Void> sendFileTrackingResumeEvent();

    /**
     * Sends event on server side which tells file that is being tracked should be moved (renamed)
     *
     * @param oldPath
     *         the old path to the specific file
     * @param newPath
     *         the new path to the specific file
     */
    Promise<Void> sendFileTrackingMoveEvent(String oldPath, String newPath);
}
