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
package org.eclipse.che.git.impl.nativegit.commands;

import com.google.common.base.Joiner;

import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.git.impl.nativegit.GitAskPassScript;
import org.eclipse.che.plugin.ssh.key.script.SshScriptProvider;

import java.io.File;
import java.util.List;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Update remote refs with associated objects
 *
 * @author Eugene Voevodin
 */
public class PushCommand extends RemoteOperationCommand<Void> {

    private List<String> refSpec;
    private String       remote;
    private boolean      force;
    private PushResponse pushResponse;

    public PushCommand(File repository, SshScriptProvider sshScriptProvider, CredentialsLoader credentialsLoader, GitAskPassScript gitAskPassScript) {
        super(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        remote = remote == null ? "origin" : remote;
        reset();
        commandLine.add("push");
        commandLine.add(remote);
        if (refSpec != null) {
            commandLine.add(refSpec);
        }
        if (force) {
            commandLine.add("--force");
        }
        start();
        pushResponse = newDto(PushResponse.class).withCommandOutput(Joiner.on("\n").join(lines));
        return null;
    }

    /**
     * @param refSpecs
     *         ref specs to push
     * @return PushCommand with established ref specs
     */
    public PushCommand setRefSpec(List<String> refSpecs) {
        this.refSpec = refSpecs;
        return this;
    }


    /**
     * @param force
     *         if <code>true</code> push will be forced
     * @return PushCommand with established force parmeter
     */
    public PushCommand setForce(boolean force) {
        this.force = force;
        return this;
    }

    /**
     * Get push response information
     * @return PushResponse DTO
     */
    public PushResponse getPushResponse() {
        return pushResponse;
    }

    /**
     * If remote name is null "origin" will be used
     *
     * @param remoteName
     *         remote name
     * @return PushCommand with established remote name
     */
    public PushCommand setRemote(String remoteName) {
        this.remote = remoteName;
        return this;
    }
}
