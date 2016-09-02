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

import static java.util.Objects.hash;

//TODO move?

/**
 * Data object for {@link MachineSource}.
 *
 * @author Eugene Voevodin
 */
@Deprecated
public class MachineSourceImpl implements MachineSource {

    private String type;
    private String location;
    private String content;

    protected MachineSourceImpl() {

    }

    /**
     * Please use {@link MachineSourceImpl with type and then setLocation or setContent}
     * @param type the source type defined by implementation.
     */
    @Deprecated
    public MachineSourceImpl(String type, String location) {
        this(type);
        setLocation(location);
    }

    public MachineSourceImpl(String type) {
        this.type = type;
    }

    public MachineSourceImpl(MachineSource machineSource) {
        if (machineSource != null) {
            this.type = machineSource.getType();
            this.location = machineSource.getLocation();
            this.content = machineSource.getContent();
        }
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

    /**
     * @return content of the machine source. No need to use an external link.
     */
    @Override
    public String getContent() {
        return this.content;
    }

    /**
     * Defines the new content to use for this machine source.
     * Alternate way is to provide a location
     *
     * @param content
     *         the content instead of an external link like with location
     */
    public MachineSourceImpl setContent(String content) {
        this.content = content;
        return this;
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
        return Objects.equals(type, other.type) && Objects.equals(location, other.location) && Objects.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        return hash(type, location, content);
    }

    @Override
    public String toString() {
        return MachineSourceImpl.class.getSimpleName() + "{" +
               "type='" + type + '\'' +
               ", location='" + location + '\'' +
               ", content='" + content + '\'' +
               '}';
    }
}
