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
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Base class for all API errors.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class ApiException extends Exception {
    private final ServiceError serviceError;

    public ApiException(ServiceError serviceError) {
        super(serviceError.getMessage());
        this.serviceError = serviceError;
    }

    public ApiException(String message) {
        super(message);

        this.serviceError = createError(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.serviceError = createError(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
        this.serviceError = createError(cause.getMessage());
    }

    public ServiceError getServiceError() {
        return serviceError;
    }

    private ServiceError createError(String message) {
        return DtoFactory.getInstance().createDto(ServiceError.class).withMessage(message);
    }
}
