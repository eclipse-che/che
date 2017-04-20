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
package org.eclipse.che.plugin.machine.ssh;

import com.google.gson.Gson;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.InvalidRecipeException;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.UnsupportedRecipeException;
import org.eclipse.che.api.workspace.server.RecipeDownloader;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Instance provider based on communication with machine over ssh protocol.
 *
 * <p>Ssh machine can't be actually created and exists somewhere outside of the control.<br>
 * So this implementation just performs command execution in such machines.<br>
 * This implementation ignores machine limits {@link MachineConfig#getLimits()}.
 *
 * @author Alexander Garagatyi
 */
// todo tests
public class SshMachineInstanceProvider  {
    private static final Gson GSON = new Gson();

    private final Set<String>       supportedRecipeTypes;
    private final SshMachineFactory sshMachineFactory;
    private final RecipeDownloader  recipeDownloader;

    @Inject
    public SshMachineInstanceProvider(SshMachineFactory sshMachineFactory, RecipeDownloader recipeDownloader) throws IOException {
        this.sshMachineFactory = sshMachineFactory;
        this.recipeDownloader = recipeDownloader;
        this.supportedRecipeTypes = Collections.singleton("ssh-config");
    }

    public String getType() {
        return "ssh";
    }

    public Set<String> getRecipeTypes() {
        return supportedRecipeTypes;
    }

    /**
     * Creates instance from scratch or by reusing a previously one by using specified {@link MachineSource}
     * data in {@link MachineConfig}.
     *
     * @param machine
     *         machine description
     * @param lineConsumer
     *         output for instance creation logs
     * @return newly created {@link SshMachineInstance}
     * @throws UnsupportedRecipeException
     *         if specified {@code recipe} is not supported
     * @throws InvalidRecipeException
     *         if {@code recipe} is invalid
     * @throws NotFoundException
     *         if instance described by {@link MachineSource} doesn't exists
     * @throws MachineException
     *         if other error occurs
     */
    public SshMachineInstance createInstance(Machine machine, LineConsumer lineConsumer) throws NotFoundException, MachineException {
        requireNonNull(machine, "Non null machine required");
        requireNonNull(lineConsumer, "Non null logs consumer required");
        requireNonNull(machine.getConfig().getSource().getLocation(), "Location in machine source is required");

        if (machine.getConfig().isDev()) {
            throw new MachineException("Dev machine is not supported for Ssh machine implementation");
        }

        Recipe recipe = recipeDownloader.getRecipe(machine.getConfig());
        SshMachineRecipe sshMachineRecipe = GSON.fromJson(recipe.getScript(), SshMachineRecipe.class);

        SshClient sshClient = sshMachineFactory.createSshClient(sshMachineRecipe,
                                                                machine.getConfig().getEnvVariables());
        sshClient.start();

        SshMachineInstance instance = sshMachineFactory.createInstance(machine,
                                                                       sshClient,
                                                                       lineConsumer);

        instance.setStatus(MachineStatus.RUNNING);
        return instance;
    }
}
