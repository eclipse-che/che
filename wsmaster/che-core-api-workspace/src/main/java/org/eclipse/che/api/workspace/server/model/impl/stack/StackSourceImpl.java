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


import org.eclipse.che.api.workspace.shared.stack.StackSource;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * Implementation of {@link StackSource}
 *
 * @author Alexander Andrienko
 */
@Embeddable
public class StackSourceImpl implements StackSource {

    @Column(name = "type")
    private String type;

    @Column(name = "origin")
    private String origin;

    public StackSourceImpl() {}

    public StackSourceImpl(StackSource stackSource) {
        this(stackSource.getType(), stackSource.getOrigin());
    }

    public StackSourceImpl(String type, String origin) {
        this.type = type;
        this.origin = origin;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return "StackSourceImpl{" +
               "type='" + type + '\'' +
               ", origin='" + origin + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StackSourceImpl)) {
            return false;
        }
        StackSourceImpl another = (StackSourceImpl)obj;
        return Objects.equals(type, another.type) && Objects.equals(origin, another.origin);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(type);
        hash = 31 * hash + Objects.hashCode(origin);
        return hash;
    }
}
