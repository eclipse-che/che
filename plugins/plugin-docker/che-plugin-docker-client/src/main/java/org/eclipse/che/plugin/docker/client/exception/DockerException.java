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
package org.eclipse.che.plugin.docker.client.exception;

import java.io.IOException;

/**
 * @author andrew00x
 */
public class DockerException extends IOException {
    private final int    status;
    private final String originError;

    public DockerException(String message, int status) {
        super(message);
        this.status = status;
        this.originError = null;
    }

    public DockerException(String message, String originError, int status) {
        super(message);
        this.status = status;
        this.originError = originError;
    }

    public int getStatus() {
        return status;
    }

    public String getOriginError() {
        return originError;
    }
}
