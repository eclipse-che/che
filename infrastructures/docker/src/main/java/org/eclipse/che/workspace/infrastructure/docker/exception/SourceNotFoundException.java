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
package org.eclipse.che.workspace.infrastructure.docker.exception;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/**
 * @author Alexander Garagatyi
 */
public class SourceNotFoundException extends InfrastructureException {
    public SourceNotFoundException(String message) {
        super(message);
    }

    public SourceNotFoundException(Exception e) {
        super(e);
    }

    public SourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
