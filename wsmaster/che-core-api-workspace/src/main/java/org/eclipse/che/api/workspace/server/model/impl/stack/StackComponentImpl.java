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
package org.eclipse.che.api.workspace.server.model.impl.stack;

import org.eclipse.che.api.workspace.shared.stack.StackComponent;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * Server implementation of {@link StackComponent}
 *
 * @author Alexander Andrienko
 */
@Embeddable
public class StackComponentImpl implements StackComponent {

    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private String version;

    public StackComponentImpl() {}

    public StackComponentImpl(StackComponent stackComponent) {
        this(stackComponent.getName(), stackComponent.getVersion());
    }

    public StackComponentImpl(String name, String version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StackComponentImpl)) {
            return false;
        }
        StackComponentImpl another = (StackComponentImpl)obj;
        return Objects.equals(name, another.name) && Objects.equals(version, another.version);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(name);
        hash = hash * 31 + Objects.hashCode(version);
        return hash;
    }

    @Override
    public String toString() {
        return "StackComponentImpl{" +
               "name='" + name + '\'' +
               ", version='" + version + '\'' +
               '}';
    }
}
