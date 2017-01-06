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
 * A {@code BadRequestException} should be thrown when server receives invalid input parameter or missed it.
 * <p/>
 * Typically in REST API such errors are converted in HTTP response with status 400.
 *
 * @author Sergii Leschenko
 */
public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(ServiceError serviceError) {
        super(serviceError);
    }
}
