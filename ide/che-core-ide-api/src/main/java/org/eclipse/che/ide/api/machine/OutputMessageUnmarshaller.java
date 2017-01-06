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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

/**
 * Unmarshaller for websocket messages from machine.
 *
 * @author Artem Zatsarynnyi
 */
public class OutputMessageUnmarshaller implements Unmarshallable<String> {
    private String payload;

    @Override
    public void unmarshal(Message message) {
        payload = message.getBody();
        if (payload.startsWith("[STDOUT]") || payload.startsWith("[STDERR]")) {
            payload = payload.substring(9);
        }
    }

    @Override
    public String getPayload() {
        return payload;
    }
}
