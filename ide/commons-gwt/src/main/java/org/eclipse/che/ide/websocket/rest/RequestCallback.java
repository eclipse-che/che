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

import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.rest.HTTPStatus;
import org.eclipse.che.ide.rest.RequestStatusHandler;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.rest.exceptions.ServerException;
import org.eclipse.che.ide.websocket.rest.exceptions.UnauthorizedException;
import com.google.gwt.http.client.Response;

import java.util.List;


/**
 * Callback to receive a {@link Message}.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class RequestCallback<T> {

    // http code 207 is "Multi-Status"
    // IE misinterpreting HTTP status code 204 as 1223 (http://www.mail-archive.com/jquery-en@googlegroups.com/msg13093.html)
    private static final int[] DEFAULT_SUCCESS_CODES = {Response.SC_OK, Response.SC_CREATED, Response.SC_NO_CONTENT, 207, 1223};
    /** Deserializer for the body of the {@link Message}. */
    private final Unmarshallable<T>    unmarshaller;
    /** Status codes of the successful responses. */
    private       int[]                successCodes;
    /** An object deserialized from the response. */
    private       T                    payload;
    /** Handler to show an execution state of operation. */
    private       RequestStatusHandler statusHandler;
    /** Loader to show while request is calling. */
    private       AsyncRequestLoader   loader;

    public RequestCallback() {
        this.successCodes = DEFAULT_SUCCESS_CODES;
        this.unmarshaller = null;
    }

    /**
     * Constructor retrieves unmarshaller with initialized (this is important!) object.
     * When response comes then callback calls {@link Unmarshallable#unmarshal(com.codenvy.ide.websocket.Message)}
     * which populates the object.
     *
     * @param unmarshaller
     *         {@link Unmarshallable}
     */
    public RequestCallback(Unmarshallable<T> unmarshaller) {
        this.successCodes = DEFAULT_SUCCESS_CODES;
        this.unmarshaller = unmarshaller;
    }

    /**
     * Perform actions when response message was received.
     *
     * @param message
     *         message
     */
    public void onReply(Message message) {
        if (loader != null) {
            loader.hide();
        }

        final String uuid = message.getStringField(MessageBuilder.UUID_FIELD);
        if (message.getResponseCode() == HTTPStatus.UNAUTHORIZED) {
            UnauthorizedException exception = new UnauthorizedException(message);
            if (statusHandler != null) {
                statusHandler.requestError(uuid, exception);
            }
            onFailure(exception);
            return;
        }

        if (isSuccessful(message)) {
            try {
                if (unmarshaller != null) {
                    unmarshaller.unmarshal(message);
                    payload = unmarshaller.getPayload();
                }
                if (statusHandler != null) {
                    statusHandler.requestFinished(uuid);
                }
                onSuccess(payload);
            } catch (UnmarshallerException e) {
                if (statusHandler != null) {
                    statusHandler.requestError(uuid, e);
                }
                onFailure(e);
            }
        } else {
            ServerException exception = new ServerException(message);
            if (statusHandler != null) {
                statusHandler.requestError(uuid, exception);
            }
            onFailure(exception);
        }
    }

    /**
     * Is response successful?
     *
     * @param response
     *         {@link Message}
     * @return <code>true</code> if response is successful and <code>false</code> if response is not successful
     */
    protected final boolean isSuccessful(Message response) {
        if (successCodes == null) {
            successCodes = DEFAULT_SUCCESS_CODES;
        }

        List<Pair> headers = response.getHeaders().toList();
        if (headers != null) {
            for (Pair header : headers) {
                if (HTTPHeader.JAXRS_BODY_PROVIDED.equals(header.getName()) && "Authentication-required".equals(header.getValue())) {
                    return false;
                }
            }
        }

        for (int code : successCodes)
            if (response.getResponseCode() == code)
                return true;

        return false;
    }

    /**
     * Set the array of successful HTTP status codes.
     *
     * @param successCodes
     *         the successCodes to set
     */
    public void setSuccessCodes(int[] successCodes) {
        this.successCodes = successCodes;
    }

    /** Get handler to show an execution state of request. */
    public final RequestStatusHandler getStatusHandler() {
        return statusHandler;
    }

    /**
     * Set handler to show an execution state of request.
     *
     * @param handler
     *         status handler
     */
    public final void setStatusHandler(RequestStatusHandler handler) {
        this.statusHandler = handler;
    }

    /** Get the loader to show while request is calling. */
    public final AsyncRequestLoader getLoader() {
        return loader;
    }

    /**
     * Set the loader to show while request is calling.
     *
     * @param loader
     *         loader to show while request is calling
     */
    public final void setLoader(AsyncRequestLoader loader) {
        this.loader = loader;
    }

    /**
     * Invokes if response is successfully received and
     * response status code is in set of success codes.
     *
     * @param result
     */
    protected abstract void onSuccess(T result);

    /**
     * Invokes if an error received from the server.
     *
     * @param exception
     *         caused failure
     */
    protected abstract void onFailure(Throwable exception);
}
