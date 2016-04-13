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
package org.eclipse.che.api.machine.server.spi.impl;

import org.eclipse.che.api.machine.server.spi.InstanceKey;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Map based implementation of {@link org.eclipse.che.api.machine.server.spi.InstanceKey}.
 *
 * @author Sergii Kabashniuk
 */
public class InstanceKeyImpl implements InstanceKey {

    private final Map<String, String> fields;

    public InstanceKeyImpl(Map<String, String> fields) {
        this.fields = Collections.unmodifiableMap(new HashMap<>(fields));
    }

    public InstanceKeyImpl(InstanceKey instanceKey) {
        this(instanceKey.getFields());
    }

    /**
     * @return unmodifiable copy of fields.
     */
    @Override
    public Map<String, String> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstanceKeyImpl)) return false;

        InstanceKeyImpl that = (InstanceKeyImpl)o;

        return !(getFields() != null ? !getFields().equals(that.getFields()) : that.getFields() != null);

    }

    @Override
    public int hashCode() {
        return getFields() != null ? getFields().hashCode() : 0;
    }
}
