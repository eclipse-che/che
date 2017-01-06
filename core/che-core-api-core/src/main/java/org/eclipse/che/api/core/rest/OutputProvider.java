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
package org.eclipse.che.api.core.rest;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Factory for writable output.
 *
 * @author andrew00x
 */
public interface OutputProvider {
    /**
     * Get writable output.
     *
     * @return writable output
     * @throws IOException
     *         if an i/o error occurs
     */
    OutputStream getOutputStream() throws IOException;
}
