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
package org.eclipse.che.api.machine.server.spi.impl;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;

import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
public abstract class AbstractMachineProcess implements InstanceProcess {
    private final String              name;
    private final String              commandLine;
    private final String              type;
    private final Map<String, String> attributes;
    private final int                 pid;
    private final String              outputChannel;

    public AbstractMachineProcess(String name,
                                  String commandLine,
                                  String type,
                                  Map<String, String> attributes,
                                  int pid,
                                  String outputChannel) {
        this.name = name;
        this.commandLine = commandLine;
        this.type = type;
        this.attributes = attributes;
        this.outputChannel = outputChannel;
        this.pid = pid;
    }

    public AbstractMachineProcess(Command command,
                                  int pid,
                                  String outputChannel) {
        this.name = command.getName();
        this.commandLine = command.getCommandLine();
        this.type = command.getType();
        this.attributes = command.getAttributes();
        this.pid = pid;
        this.outputChannel = outputChannel;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public String getOutputChannel() {
        return outputChannel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCommandLine() {
        return commandLine;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
