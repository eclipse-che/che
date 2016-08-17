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
package org.eclipse.che.api.core.websocket.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Stores a WEB SOCKET transmission. Transmission contains the protocol and
 * the message. Transmission protocol is defined by <code>protocol</code> field,
 * while transmission message body is stored within <code>message</code> field.
 */
@DTO
public interface WebSocketTransmission {
    String getProtocol();

    WebSocketTransmission withProtocol(final String protocol);

    String getMessage();

    WebSocketTransmission withMessage(final String message);
}
