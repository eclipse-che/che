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
package org.eclipse.che.ide.websocket;

import org.eclipse.che.ide.collections.js.JsoArray;
import org.eclipse.che.ide.util.UUID;
import org.eclipse.che.ide.websocket.rest.Pair;
import com.google.gwt.http.client.RequestBuilder.Method;

/**
 * Builder for constructing {@link Message}.
 *
 * @author Artem Zatsarynnyi
 */
public class MessageBuilder {
    public static final String UUID_FIELD = "uuid";
    /** Message which is constructing and may be send. */
    private final Message message;

    /**
     * Creates a {@link MessageBuilder} using the parameters for configuration.
     *
     * @param method
     *         HTTP method to use for the request
     * @param path
     *         URI
     */
    public MessageBuilder(Method method, String path) {
        message = Message.create();
        message.addField(UUID_FIELD, UUID.uuid());
        message.setMethod((method == null) ? null : method.toString());
        message.setPath(path);
    }

    /**
     * Sets a request header with the given name and value. If a header with the
     * specified name has already been set then the new value overwrites the
     * current value.
     *
     * @param name
     *         the name of the header
     * @param value
     *         the value of the header
     * @return this {@link MessageBuilder}
     */
    public final MessageBuilder header(String name, String value) {
        JsoArray<Pair> headers = message.getHeaders();
        if (headers == null) {
            headers = JsoArray.create();
        }

        for (int i = 0; i < headers.size(); i++) {
            Pair header = headers.get(i);
            if (name.equals(header.getName())) {
                header.setValue(value);
                return this;
            }
        }

        Pair header = Pair.create();
        header.setName(name);
        header.setValue(value);
        headers.add(header);
        message.setHeaders(headers);
        return this;
    }

    /**
     * Sets the data to send as body of this request.
     *
     * @param requestData
     *         the data to send as body of the request
     * @return this {@link MessageBuilder}
     */
    public final MessageBuilder data(String requestData) {
        message.setBody(requestData);
        return this;
    }

    /**
     * Builds message.
     *
     * @return message
     */
    public Message build() {
        return message;
    }
}