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
package org.eclipse.che.api.machine.shared.dto;

import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.MachineProcess;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/**
 * Describes process created from {@link Command} in machine
 *
 * @author andrew00x
 */
@DTO
public interface MachineProcessDto extends MachineProcess, Hyperlinks {
    void setPid(int pid);

    MachineProcessDto withPid(int pid);

    void setCommandLine(String commandLine);

    MachineProcessDto withCommandLine(String commandLine);

    void setAlive(boolean isAlive);

    MachineProcessDto withAlive(boolean isAlive);

    void setName(String commandName);

    MachineProcessDto withName(String commandName);

    void setType(String type);

    MachineProcessDto withType(String type);

    void setOutputChannel(String outputChannel);

    MachineProcessDto withOutputChannel(String outputChannel);

    @Override
    MachineProcessDto withLinks(List<Link> links);

    MachineProcessDto withAttributes(Map<String, String> attributes);
}
