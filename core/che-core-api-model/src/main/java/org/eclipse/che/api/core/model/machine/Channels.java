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
package org.eclipse.che.api.core.model.machine;

/**
 * Websocket channels that provide information about machine.
 *
 * @author Alexander Garagatyi
 */
public interface Channels {
    /**
     * Returns channel of websocket where machine logs should be put.
     */
    String getOutput();

    /**
     * Returns channel of websocket where machine status events should be put.
     */
    String getStatus();
}
