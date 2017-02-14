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
package org.eclipse.che.plugin.docker.client.connection;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author andrew00x
 */
public interface DockerResponse {
    int getStatus() throws IOException;

    int getContentLength() throws IOException;

    String getContentType() throws IOException;

    String getHeader(String name) throws IOException;

    String[] getHeaders(String name) throws IOException;

    InputStream getInputStream() throws IOException;
}
