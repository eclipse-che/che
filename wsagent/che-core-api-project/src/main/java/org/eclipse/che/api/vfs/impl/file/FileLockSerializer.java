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
import java.io.EOFException;
import java.io.IOException;

/**
 * Serializer for locks of VirtualFile.
 *
 * @author andrew00x
 * @see FileLock
 */
public class FileLockSerializer implements DataSerializer<FileLock> {
    @Override
    public void write(DataOutput output, FileLock lock) throws IOException {
        output.writeUTF(lock.getLockToken());
        output.writeLong(lock.getExpired());
    }

    @Override
    public FileLock read(DataInput input) throws IOException {
        String lockToken = input.readUTF();
        long expired = Long.MAX_VALUE;
        try {
            expired = input.readLong();
        } catch (EOFException ignored) {
        }
        return new FileLock(lockToken, expired);
    }
}
