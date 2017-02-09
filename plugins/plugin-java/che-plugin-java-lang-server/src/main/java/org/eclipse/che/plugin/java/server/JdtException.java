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
package org.eclipse.che.plugin.java.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * Base exception for all JDT exceptions
 *
 * @author Evgen Vidolob
 */
public class JdtException extends ServerException {
    public JdtException(String message) {
        super(message);
    }

    public JdtException(ServiceError serviceError) {
        super(serviceError);
    }

    public JdtException(Throwable cause) {
        super(cause);
    }

    public JdtException(String message, Throwable cause) {
        super(message, cause);
    }
}
