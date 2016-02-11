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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * Thrown when input value is invalid by some reason.
 *
 * @author gazarenkov
 */
public class InvalidValueException extends ForbiddenException {

    public InvalidValueException(String message) {
        super(message);
    }

    public InvalidValueException(ServiceError serviceError) {
        super(serviceError);
    }
}
