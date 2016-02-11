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

/**
 * Serializer for AccessControlList.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public class AccessControlListSerializer implements DataSerializer<AccessControlList> {
    @Override
    public void write(DataOutput output, AccessControlList value) throws IOException {
        value.write(output);
    }

    @Override
    public AccessControlList read(DataInput input) throws IOException {
        return AccessControlList.read(input);
    }
}
