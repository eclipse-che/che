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
package org.eclipse.che.api.debug.shared.model.impl.action;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.action.Action;
import org.eclipse.che.api.debug.shared.model.action.StartAction;

import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public class StartActionImpl extends ActionImpl implements StartAction {

    private final List<Breakpoint> breakpoints;

    public StartActionImpl(List<Breakpoint> breakpoints) {
        super(Action.TYPE.START);
        this.breakpoints = breakpoints;
    }

    @Override
    public List<Breakpoint> getBreakpoints() {
        return breakpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StartActionImpl)) return false;
        if (!super.equals(o)) return false;

        StartActionImpl that = (StartActionImpl)o;

        return !(breakpoints != null ? !breakpoints.equals(that.breakpoints) : that.breakpoints != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (breakpoints != null ? breakpoints.hashCode() : 0);
        return result;
    }
}
