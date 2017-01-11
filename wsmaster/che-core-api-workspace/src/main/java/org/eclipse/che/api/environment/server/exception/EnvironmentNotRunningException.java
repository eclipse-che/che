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
package org.eclipse.che.api.environment.server.exception;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * Exception thrown in case environment stop is called but no matching environment is running.
 *
 * @author Alexander Garagatyi
 */
public class EnvironmentNotRunningException extends NotFoundException {
    public EnvironmentNotRunningException(String message) {
        super(message);
    }

    public EnvironmentNotRunningException(ServiceError serviceError) {
        super(serviceError);
    }
}
