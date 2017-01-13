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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * @author andrew00x
 * @author Alexander Garagatyi
 */
@SuppressWarnings("serial")
public class MachineException extends ServerException {
    public MachineException(String message) {
        super(message);
    }

    public MachineException(ServiceError serviceError) {
        super(serviceError);
    }

    public MachineException(Throwable cause) {
        super(cause);
    }

    public MachineException(String message, Throwable cause) {
        super(message, cause);
    }
}
