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
 * A {@code ForbiddenException} is thrown when required operation is forbidden. For example, if caller doesn't have rights or required
 * operation is not allowed for some resource.
 * <p/>
 * Typically in REST API such errors are converted in HTTP response with status 403.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(ServiceError serviceError) {
        super(serviceError);
    }
}
