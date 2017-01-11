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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileLockSerializerTest {
    private FileLockSerializer lockSerializer;

    @Before
    public void setUp() throws Exception {
        lockSerializer = new FileLockSerializer();
    }

    @Test
    public void readsLockObjectWithoutExpirationData() throws Exception {
        String token = Long.toString(System.currentTimeMillis());

        DataInput data = mock(DataInput.class);
        when(data.readUTF()).thenReturn(token);
        when(data.readLong()).thenThrow(new EOFException());

        FileLock lock = lockSerializer.read(data);
        assertEquals(new FileLock(token, Long.MAX_VALUE), lock);
    }

    @Test
    public void readsLockObjectWithExpirationData() throws Exception {
        String token = Long.toString(System.currentTimeMillis());
        long expired = System.currentTimeMillis() + 10000;

        DataInput data = mock(DataInput.class);
        when(data.readUTF()).thenReturn(token);
        when(data.readLong()).thenReturn(expired);

        FileLock lock = lockSerializer.read(data);
        assertEquals(new FileLock(token, expired), lock);
    }

    @Test
    public void writesLockObject() throws Exception {
        String token = Long.toString(System.currentTimeMillis());
        long expired = System.currentTimeMillis() + 10000;
        FileLock lock = new FileLock(token, expired);

        DataOutput data = mock(DataOutput.class);
        lockSerializer.write(data, lock);

        InOrder inOrder = inOrder(data);
        inOrder.verify(data).writeUTF(token);
        inOrder.verify(data).writeLong(expired);
    }
}