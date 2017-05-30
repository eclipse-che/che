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
package org.eclipse.che.api.git.exception;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

import java.util.Collections;
import java.util.Map;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author andrew00x
 */
public class GitException extends ServerException {
    public GitException(String message) {
        super(message);
    }

    public GitException(String message, int errorCode, Map<String, String> attributes) {
        super(newDto(ExtendedError.class).withMessage(message).withErrorCode(errorCode).withAttributes(attributes));
    }

    public GitException(ServiceError serviceError) {
        super(serviceError);
    }

    public GitException(String message, int errorCode) {
        this(message, errorCode, Collections.emptyMap());
    }


    public GitException(Throwable cause) {
        super(cause);
    }

    public GitException(String message, Throwable cause) {
        super(message, cause);
    }
}
