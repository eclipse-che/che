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
package org.eclipse.che.ide.commons.exception;

import com.google.gwt.http.client.Response;

import org.eclipse.che.ide.rest.AsyncRequest;


/**
 * @author Vitaliy Gulyy
 * @author Sergii Leschenko
 */
@SuppressWarnings("serial")
public class UnauthorizedException extends ServerException {

    private AsyncRequest request;

    public UnauthorizedException(Response response, AsyncRequest request) {
        super(response);
        this.request = request;
    }

    public AsyncRequest getRequest() {
        return request;
    }
}
