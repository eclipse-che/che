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
package org.eclipse.che.api.machine.server.exception;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * Occurs when machine sources is not found or is not available.
 *
 * @author Anton Korneta
 */
public class SourceNotFoundException extends MachineException {
    public SourceNotFoundException(String message) {
        super(message);
    }

    public SourceNotFoundException(ServiceError serviceError) {
        super(serviceError);
    }

    public SourceNotFoundException(Throwable cause) {
        super(cause);
    }

    public SourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
