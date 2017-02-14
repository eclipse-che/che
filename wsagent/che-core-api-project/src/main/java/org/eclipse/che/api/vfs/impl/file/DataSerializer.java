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
package org.eclipse.che.api.vfs.impl.file;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Writes object to stream and restores object from stream. Implementation has full control over format of serialization.
 *
 * @author andrew00x
 */
public interface DataSerializer<T> {
    /**
     * Writes <code>value</code> to <code>output</code>.
     *
     * @param output
     *         serialization stream
     * @param value
     *         instance for serialization
     * @throws IOException
     *         if an i/o error occurs
     */
    void write(DataOutput output, T value) throws IOException;

    /**
     * Restores object from <code>input</code>.
     *
     * @param input
     *         stream which contains serialized object
     * @return restored instance
     * @throws IOException
     *         if an i/o error occurs
     */
    T read(DataInput input) throws IOException;
}
