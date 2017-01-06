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

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileMetadataSerializerTest {
    private FileMetadataSerializer metadataSerializer;

    @Before
    public void setUp() throws Exception {
        metadataSerializer = new FileMetadataSerializer();
    }

    @Test
    public void writesProperties() throws Exception {
        DataOutput output = mock(DataOutput.class);
        Map<String,String> properties = ImmutableMap.of("a", "x", "b", "z");

        metadataSerializer.write(output, properties);

        InOrder inOrder = inOrder(output);
        inOrder.verify(output).writeInt(properties.size());
        inOrder.verify(output).writeUTF("a");
        inOrder.verify(output).writeInt(1);
        inOrder.verify(output).writeUTF("b");
        inOrder.verify(output).writeInt(1);
        inOrder.verify(output).writeUTF("z");
    }

    @Test
    public void readsProperties() throws Exception {
        DataInput data = mock(DataInput.class);
        when(data.readInt()).thenReturn(2,
                                        1,
                                        1);
        when(data.readUTF()).thenReturn("a",
                                        "x",
                                        "b",
                                        "z");
        Map<String, String> expected = ImmutableMap.of("a", "x",
                                                       "b", "z");
        assertEquals(expected, metadataSerializer.read(data));
    }

    @Test
    public void readsMultivaluedPropertiesAndJoinsValuesWithComma() throws Exception {
        DataInput data = mock(DataInput.class);
        when(data.readInt()).thenReturn(2,
                                        3,
                                        3);
        when(data.readUTF()).thenReturn("a",
                                        "x", "y", "z",
                                        "b",
                                        "z", "y", "x");
        Map<String, String> expected = ImmutableMap.of("a", "x,y,z",
                                                       "b", "z,y,x");
        assertEquals(expected, metadataSerializer.read(data));
    }
}