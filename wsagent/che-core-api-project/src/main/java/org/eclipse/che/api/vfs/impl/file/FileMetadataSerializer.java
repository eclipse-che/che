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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.collect.Maps.newLinkedHashMap;

/**
 * Serializer for properties of VirtualFile.
 *
 * @author andrew00x
 */
public class FileMetadataSerializer implements DataSerializer<Map<String, String>> {
    @Override
    public void write(DataOutput output, Map<String, String> props) throws IOException {
        output.writeInt(props.size());
        for (Map.Entry<String, String> entry : props.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                final String name = entry.getKey();
                output.writeUTF(name);
                final List<String> asList = Splitter.on(',').splitToList(value);
                output.writeInt(asList.size());
                for (String single : asList) {
                    output.writeUTF(single);
                }
            }
        }
    }

    @Override
    public Map<String, String> read(DataInput input) throws IOException {
        final int recordsNum = input.readInt();
        if (recordsNum == 0) {
            return newLinkedHashMap();
        }
        final Map<String, String> properties = newHashMapWithExpectedSize(recordsNum);
        final List<String> valuesList = newArrayList();
        int readRecords = 0;
        while (readRecords < recordsNum) {
            String name = input.readUTF();
            final int valueItemNum = input.readInt();
            valuesList.clear();
            for (int i = 0; i < valueItemNum; i++) {
                valuesList.add(input.readUTF());
            }
            properties.put(name, Joiner.on(',').join(valuesList));
            ++readRecords;
        }
        return properties;
    }
}
