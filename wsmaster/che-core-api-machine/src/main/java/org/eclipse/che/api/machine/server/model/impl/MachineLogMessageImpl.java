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
package org.eclipse.che.api.machine.server.model.impl;

import org.eclipse.che.api.core.model.machine.MachineLogMessage;

import java.util.Objects;

/**
 * @author Alexander Garagatyi
 */
public class MachineLogMessageImpl implements MachineLogMessage {
    private String machineName;
    private String content;

    public MachineLogMessageImpl() {}

    public MachineLogMessageImpl(String machineName, String content) {
        this.machineName = machineName;
        this.content = content;
    }

    @Override
    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machine) {
        this.machineName = machine;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineLogMessageImpl)) return false;
        MachineLogMessageImpl that = (MachineLogMessageImpl)o;
        return Objects.equals(machineName, that.machineName) &&
               Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(machineName, content);
    }

    @Override
    public String toString() {
        return "MachineLogMessageImpl{machineName='" + machineName +
               "', content='" + content + "'}";
    }
}
