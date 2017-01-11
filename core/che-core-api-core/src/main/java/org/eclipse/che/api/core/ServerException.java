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
package org.eclipse.che.api.core;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * A {@code ServerException} is thrown as a result of an error that internal server error.
 * <p/>
 * Typically in REST API such errors are converted in HTTP response with status 500.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class ServerException extends ApiException {

    public ServerException(String message) {
        super(message);
    }

    public ServerException(ServiceError serviceError) {
        super(serviceError);
    }

    public ServerException(Throwable cause) {
        super(cause);
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
