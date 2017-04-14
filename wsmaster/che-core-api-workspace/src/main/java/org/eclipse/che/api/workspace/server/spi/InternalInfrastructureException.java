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
package org.eclipse.che.api.workspace.server.spi;

/**
 * Thrown by {@link RuntimeInfrastructure} and related components
 * when unexpected internal error occurs.
 *
 * @author Yevhenii Voevodin
 */
public class InternalInfrastructureException extends InfrastructureException {

    public InternalInfrastructureException(String message) {
        super(message);
    }

    public InternalInfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalInfrastructureException(Exception e) {
        super(e);
    }
}
