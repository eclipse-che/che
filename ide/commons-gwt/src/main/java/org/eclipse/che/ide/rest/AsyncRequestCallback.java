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
package org.eclipse.che.ide.rest;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

import org.eclipse.che.ide.commons.exception.ServerDisconnectedException;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * Callback class for receiving the {@link Response}.
 *
 * @param <T>
 *         the return type of the response the callback expects.
 *         Use {@link Void} for methods returning {@code void}.
 *
 * @author Valeriy Svydenko
 */
public abstract class AsyncRequestCallback<T> implements RequestCallback {

    // HTTP code 207 is "Multi-Status"
    // IE misinterpreting HTTP status code 204 as 1223 (http://www.mail-archive.com/jquery-en@googlegroups.com/msg13093.html)
    private static final int[] DEFAULT_SUCCESS_CODES = {Response.SC_OK, Response.SC_CREATED, Response.SC_NO_CONTENT, 207, 1223};
    private final Unmarshallable<T>  unmarshaller;
    private       int[]              successCodes;
    private       AsyncRequestLoader loader;
    private       T                  payload;
    private       AsyncRequest       request;

    public AsyncRequestCallback() {
        this(null);
    }

    /**
     * Constructor retrieves unmarshaller with initialized (this is important!) object.
     * When response comes callback calls {@link Unmarshallable#unmarshal(com.google.gwt.http.client.Response)}
     * which populates the object.
     *
     * @param unmarshaller
     */
    public AsyncRequestCallback(Unmarshallable<T> unmarshaller) {
        this.successCodes = DEFAULT_SUCCESS_CODES;
        this.unmarshaller = unmarshaller;
    }

    /** @return the result */
    public T getPayload() {
        return payload;
    }

    /**
     * @param successCodes
     *         the successCodes to set
     */
    public void setSuccessCodes(int[] successCodes) {
        this.successCodes = successCodes;
    }

    public final void setLoader(AsyncRequestLoader loader) {
        this.loader = loader;
    }

    /** @see com.google.gwt.http.client.RequestCallback#onError(com.google.gwt.http.client.Request, java.lang.Throwable) */
    @Override
    public final void onError(Request request, Throwable exception) {
        if (loader != null) {
            loader.hide();
        }

        onFailure(exception);
    }

    /**
     * @see com.google.gwt.http.client.RequestCallback#onResponseReceived(com.google.gwt.http.client.Request,
     * com.google.gwt.http.client.Response)
     */
    @Override
    public final void onResponseReceived(Request request, Response response) {
        if (loader != null) {
            loader.hide();
        }

        // If there is no connection to the server then status equals 0 ( In Internet Explorer status is 12029 )
        if (response.getStatusCode() == 0 || response.getStatusCode() == 12029) {
            onServerDisconnected();
            return;
        }

        if (response.getStatusCode() == HTTPStatus.UNAUTHORIZED) {
            onUnauthorized(response);
            return;
        }

        if (isSuccessful(response)) {
            handleSuccess(response);
        } else {
            handleFailure(response);
        }
    }

    private void handleFailure(Response response) {
        Exception exception;
        String contentType = response.getHeader(CONTENT_TYPE);

        if (contentType != null && !contentType.contains(APPLICATION_JSON)) {
            String message = generateErrorMessage(response);
            exception = new Exception(message);
        } else {
            exception = new ServerException(response);
        }
        onFailure(exception);
    }

    private void handleSuccess(Response response) {
        try {
            if (unmarshaller != null) {
                //It's needed for handling a situation when response DTO object is NULL
                if (response.getStatusCode() != Response.SC_NO_CONTENT) {
                    unmarshaller.unmarshal(response);
                }
                payload = unmarshaller.getPayload();
            }

            onSuccess(payload);
        } catch (Exception e) {
            onFailure(e);
        }
    }


    private String generateErrorMessage(Response response) {
        StringBuilder message = new StringBuilder();
        String protocol = Window.Location.getProtocol();
        String host = Window.Location.getHost();
        String url = this.request.getRequestBuilder().getUrl();

        //deletes query params
        url = url.substring(0, url.indexOf('?'));

        message.append(response.getStatusCode())
               .append(" ")
               .append(response.getStatusText())
               .append(" ")
               .append(protocol)
               .append("//")
               .append(host)
               .append(url);

        return message.toString();
    }

    protected final boolean isSuccessful(Response response) {
        if (successCodes == null) {
            successCodes = DEFAULT_SUCCESS_CODES;
        }

        if ("Authentication-required".equals(response.getHeader(HTTPHeader.JAXRS_BODY_PROVIDED))) {
            return false;
        }

        for (int code : successCodes) {
            if (response.getStatusCode() == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * If response is successfully received and response status code is in set of success codes.
     *
     * @param result
     *         the response returned from the request. Will be {@code null} if
     *         {@code unmarshaller} wasn't set
     */
    protected abstract void onSuccess(T result);

    /**
     * Called when an error received from the server or when request was failed.
     *
     * @param exception
     *         the exception thrown
     */
    protected abstract void onFailure(Throwable exception);

    /** If server disconnected. */
    protected void onServerDisconnected() {
        onFailure(new ServerDisconnectedException(request));
    }

    /**
     * If unauthorized.
     *
     * @param response
     */
    protected void onUnauthorized(Response response) {
        onFailure(new UnauthorizedException(response, request));
    }

    public AsyncRequest getRequest() {
        return request;
    }

    public void setRequest(AsyncRequest request) {
        this.request = request;
    }
}
