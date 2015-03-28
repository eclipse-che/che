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
package org.eclipse.che.ide.ext.tutorials.client.update;

import org.eclipse.che.ide.ext.runner.client.models.Runner;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;

/**
 * Client for service for updating launched Codenvy Extension on SDK runner.
 *
 * @author Artem Zatsarynnyy
 * @author Valeriy Svydenko
 */
public interface UpdateServiceClient {
    /**
     * Update launched extension.
     *
     * @param runner
     *         {@link Runner} that represents a launched extension
     * @param callback
     *         the callback to use for the response
     * @throws WebSocketException
     */
    public void update(@NotNull Runner runner, @NotNull RequestCallback<Void> callback)
            throws WebSocketException;
}