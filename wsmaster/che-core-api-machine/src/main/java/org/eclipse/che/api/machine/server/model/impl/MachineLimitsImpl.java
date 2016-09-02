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

import org.eclipse.che.api.core.model.machine.MachineLimits;

/**
 * @author Alexander Garagatyi
 */
@Deprecated
public class MachineLimitsImpl implements MachineLimits {
    private final int memory;

    public MachineLimitsImpl(MachineLimits machineLimits) {
        if(machineLimits != null) {
            memory = machineLimits.getRam();
        } else {
            memory = 0;
        }
    }

    public MachineLimitsImpl(int memory) {
        this.memory = memory;
    }

    @Override
    public int getRam() {
        return memory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineLimitsImpl)) return false;

        MachineLimitsImpl limits = (MachineLimitsImpl)o;

        return memory == limits.memory;

    }

    @Override
    public int hashCode() {
        return memory;
    }
}
