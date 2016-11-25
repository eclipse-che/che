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
package org.eclipse.che.api.core.jsonrpc;


/**
 * Request handlers are the key elements of json rpc request processing
 * routines. You must extend this class if you want to handle a request to a
 * specific method. In order to apply this handler you must register it inside
 * dependency injection infrastructure (e.g. guice) The example:
 * <p><code>
 * MapBinder.newMapBinder(binder(), String.class, RequestHandler.class)
 * .addBinding("event:project-tree-status-changed")
 * .to(ProjectTreeStatusHandler.class);
 * </code></p>
 * where
 * <ul>
 * <li>
 * <code>event:project-tree-status-changed</code>: corresponding method
 * </li>
 * <li>
 * <code>ProjectTreeStatusHandler</code>: concrete implementation of
 * request handler
 * </li>
 * </ul>
 * Obvious limitation there can be only one handler for a combination of
 * endpoint and method.
 * <p>
 * Class has two generic parameters - <code>P</code> and <code>R</code> which
 * corresponds to parameters DTO type and result DTO type. If handler is
 * designed to process request without parameters you must specify that fact
 * explicitly by defining a <code>P</code> parameter as {@link Void}. On the
 * other hand handler can process a notification that means that it is not
 * planned to send back a response in this situation you must similary define
 * <code>R</code> as {@link Void}.
 * </p>
 * <p>
 * All handling methods are throwing an {@link UnsupportedOperationException}
 * so you must override method that mostly correspond the type of you handler.
 * For example, if you are going to handle a notification without parameters
 * you must override {@link RequestHandler#handleNotification(String)} method,
 * while the implementation will independently (analyzing the request) define
 * which handler and which method to call.
 * </p>
 *
 * @author Dmitry Kuleshov
 */
abstract public class RequestHandler<P, R> {

    final private Class<P> paramsClass;
    final private Class<R> resultClass;

    protected RequestHandler(Class<P> paramsClass, Class<R> resultClass) {
        this.paramsClass = paramsClass;
        this.resultClass = resultClass;
    }

    public Class<P> getParamsClass() {
        return paramsClass;
    }

    public Class<R> getResultClass() {
        return resultClass;
    }

    /**
     * Handle a notification without parameters
     *
     * @param endpointId
     *         endpoint identifier that a notification comes from
     */
    public void handleNotification(String endpointId) {

        throw new UnsupportedOperationException();
    }

    /**
     * Handle a notification with parameters
     *
     * @param endpointId
     *         endpoint identifier that a notification comes from
     * @param params
     *         parameters represented by DTO
     */
    public void handleNotification(String endpointId, P params) {
        throw new UnsupportedOperationException();
    }

    /**
     * Handle a request without parameters
     *
     * @param endpointId
     *         endpoint identifier that a notification comes from
     *
     * @return result of handling of a request
     */
    public R handleRequest(String endpointId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Handle a request with parameters
     *
     * @param endpointId
     *         endpoint identifier that a notification comes from
     * @param params
     *         parameters represented by DTO
     *
     * @return result of handling of a request
     */
    public R handleRequest(String endpointId, P params) {
        throw new UnsupportedOperationException();
    }
}
