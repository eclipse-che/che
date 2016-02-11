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
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.model.machine.MachineSource;

import java.util.Objects;

//TODO move?

/**
 * Data object for {@link MachineSource}.
 *
 * @author Eugene Voevodin
 */
public class MachineSourceImpl implements MachineSource {

    private String type;
    private String location;

    public MachineSourceImpl(String type, String location) {
        this.type = type;
        this.location = location;
    }

    @Override
    public String getType() {
        return type;
    }

    public MachineSourceImpl setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public MachineSourceImpl setLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MachineSourceImpl)) return false;
        final MachineSourceImpl other = (MachineSourceImpl)obj;
        return Objects.equals(type, other.type) && Objects.equals(location, other.location);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(type);
        hash = hash * 31 + Objects.hashCode(location);
        return hash;
    }

    @Override
    public String toString() {
        return "MachineSourceImpl{" +
               "type='" + type + '\'' +
               ", location='" + location + '\'' +
               '}';
    }
}
