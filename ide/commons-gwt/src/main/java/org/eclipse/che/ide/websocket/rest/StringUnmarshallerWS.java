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
package org.eclipse.che.ide.websocket.rest;

import org.eclipse.che.ide.websocket.Message;

/**
 * String unmarshaller for websocket messages.
 *
 * @author Artem Zatsarynnyi
 */
public class StringUnmarshallerWS implements Unmarshallable<String> {
    protected String builder;

    /** {@inheritDoc} */
    @Override
    public void unmarshal(Message message) {
        builder = message.getBody();
    }

    /** {@inheritDoc} */
    @Override
    public String getPayload() {
        return builder;
    }
}
