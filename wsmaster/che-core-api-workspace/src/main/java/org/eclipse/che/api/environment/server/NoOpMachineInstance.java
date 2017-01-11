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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceNode;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.server.spi.impl.AbstractInstance;

import java.util.Collections;
import java.util.List;

/**
 * @author Alexander Garagatyi
 */
public class NoOpMachineInstance extends AbstractInstance {
    public NoOpMachineInstance(Machine machine) {
        super(machine);
    }

    @Override
    public MachineRuntimeInfoImpl getRuntime() {
        return null;
    }

    @Override
    public LineConsumer getLogger() {
        return null;
    }

    @Override
    public InstanceProcess getProcess(int pid) throws NotFoundException, MachineException {
        return null;
    }

    @Override
    public List<InstanceProcess> getProcesses() throws MachineException {
        return Collections.emptyList();
    }

    @Override
    public InstanceProcess createProcess(Command command, String outputChannel) throws MachineException {
        throw new MachineException("This machine has state that doesn't support process creation");
    }

    @Override
    public MachineSource saveToSnapshot() throws MachineException {
        throw new MachineException("This machine has state that doesn't support saving its state");
    }

    @Override
    public void destroy() throws MachineException {
    }

    @Override
    public InstanceNode getNode() {
        return null;
    }

    @Override
    public String readFileContent(String filePath, int startFrom, int limit) throws MachineException {
        throw new MachineException("This machine has state that doesn't support process reading files");
    }

    @Override
    public void copy(Instance sourceMachine, String sourcePath, String targetPath, boolean overwriteDirNonDir)
            throws MachineException {
        throw new MachineException("This machine has state that doesn't support copying of files");
    }

    @Override
    public void copy(String sourcePath, String targetPath) throws MachineException {
        throw new MachineException("This machine has state that doesn't support copying of files");
    }
}
