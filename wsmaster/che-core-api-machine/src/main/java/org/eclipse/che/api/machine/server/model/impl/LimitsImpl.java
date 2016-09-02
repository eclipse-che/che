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

/**
 * @author Alexander Garagatyi
 */
@Deprecated
public class LimitsImpl implements Limits {
    private final int memory;

    public LimitsImpl(Limits limits) {
        if(limits != null) {
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
}
