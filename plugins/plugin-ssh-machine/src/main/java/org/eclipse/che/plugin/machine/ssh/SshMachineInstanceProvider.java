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
package org.eclipse.che.plugin.machine.ssh;

import com.google.gson.Gson;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link InstanceProvider} based on communication with machine over ssh protocol.
 *
 * <p>Ssh machine can't be actually created and exists somewhere outside of the control.<br>
 * So this implementation just performs command execution in such machines.<br>
 * This implementation ignores machine limits {@link MachineConfig#getLimits()}.
 *
 * @author Alexander Garagatyi
 */
// todo tests
public class SshMachineInstanceProvider implements InstanceProvider {
    private static final Gson GSON = new Gson();

    private final Set<String>       supportedRecipeTypes;
    private final SshMachineFactory sshMachineFactory;

    @Inject
    public SshMachineInstanceProvider(SshMachineFactory sshMachineFactory) throws IOException {
        this.sshMachineFactory = sshMachineFactory;
        this.supportedRecipeTypes = Collections.singleton("ssh-config");
    }

    @Override
    public String getType() {
        return "ssh";
    }

    @Override
    public Set<String> getRecipeTypes() {
        return supportedRecipeTypes;
    }

    @Override
    public Instance createInstance(Recipe recipe,
                                   Machine machine,
                                   LineConsumer machineLogsConsumer) throws MachineException {
        requireNonNull(machine, "Non null machine required");
        requireNonNull(machineLogsConsumer, "Non null logs consumer required");

        if (machine.getConfig().isDev()) {
            throw new MachineException("Dev machine is not supported for Ssh machine implementation");
        }

        SshMachineRecipe sshMachineRecipe = parseRecipe(recipe);

        SshClient sshClient = sshMachineFactory.createSshClient(sshMachineRecipe,
                                                                machine.getConfig().getEnvVariables());
        sshClient.start();

        return sshMachineFactory.createInstance(machine,
                                                sshClient,
                                                machineLogsConsumer);
    }

    @Override
    public Instance createInstance(InstanceKey instanceKey,
                                   Machine machine,
                                   LineConsumer creationLogsOutput) throws NotFoundException, MachineException {
        throw new MachineException("Snapshot feature is unsupported for ssh machine implementation");
    }

    @Override
    public void removeInstanceSnapshot(InstanceKey instanceKey) throws SnapshotException {
        throw new SnapshotException("Snapshot feature is unsupported for ssh machine implementation");
    }

    private SshMachineRecipe parseRecipe(Recipe recipe) {
        return GSON.fromJson(recipe.getScript(), SshMachineRecipe.class);
    }
}
