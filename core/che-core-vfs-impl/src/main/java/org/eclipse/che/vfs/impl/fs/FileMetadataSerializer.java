/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.vfs.impl.fs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Serializer for properties of VirtualFile.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public class FileMetadataSerializer implements DataSerializer<Map<String, String[]>> {
    @Override
    public void write(DataOutput output, Map<String, String[]> props) throws IOException {
        output.writeInt(props.size());
        for (Map.Entry<String, String[]> entry : props.entrySet()) {
            String[] values = entry.getValue();
            if (values != null) {
                output.writeUTF(entry.getKey());
                output.writeInt(values.length);
                for (String v : values) {
                    output.writeUTF(v);
                }
            }
        }
    }

    @Override
    public Map<String, String[]> read(DataInput input) throws IOException {
        final int recordsNum = input.readInt();
        final Map<String, String[]> props = new HashMap<>(recordsNum);
        int readRecords = 0;
        while (readRecords < recordsNum) {
            String name = input.readUTF();
            String[] values = new String[input.readInt()];
            for (int i = 0; i < values.length; i++) {
                values[i] = input.readUTF();
            }
            props.put(name, values);
            ++readRecords;
        }
        return props;
    }
}
