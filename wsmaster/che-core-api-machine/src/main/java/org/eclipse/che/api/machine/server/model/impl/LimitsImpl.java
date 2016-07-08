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

import org.eclipse.che.api.core.model.machine.Limits;

import javax.persistence.Basic;
import javax.persistence.Embeddable;

/**
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 */
@Embeddable
public class LimitsImpl implements Limits {

    @Basic
    private int memory;

    public LimitsImpl() {}

    public LimitsImpl(Limits limits) {
        if (limits != null) {
            memory = limits.getRam();
        } else {
            memory = 0;
        }
    }

    public LimitsImpl(int memory) {
        this.memory = memory;
    }

    @Override
    public int getRam() {
        return memory;
    }

    public void setRam(int memory) {
        this.memory = memory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LimitsImpl)) return false;

        LimitsImpl limits = (LimitsImpl)o;

        return memory == limits.memory;

    }

    @Override
    public int hashCode() {
        return memory;
    }

    @Override
    public String toString() {
        return "LimitsImpl{" +
               "memory=" + memory +
               '}';
    }
}
