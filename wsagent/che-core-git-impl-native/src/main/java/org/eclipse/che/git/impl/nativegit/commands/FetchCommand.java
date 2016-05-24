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

import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.git.impl.nativegit.GitAskPassScript;
import org.eclipse.che.plugin.ssh.key.script.SshScriptProvider;

import java.io.File;
import java.util.List;

/**
 * Download objects and refs from other repository
 *
 * @author Eugene Voevodin
 */
public class FetchCommand extends RemoteOperationCommand<Void> {

    private List<String> refSpec;
    private String   remote;
    private boolean  prune;

    public FetchCommand(File repository, SshScriptProvider sshScriptProvider, CredentialsLoader credentialsLoader, GitAskPassScript gitAskPassScript) {
        super(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        remote = remote == null ? "origin" : remote;
        reset();
        commandLine.add("fetch", remote);
        commandLine.add(refSpec);
        if (prune) {
            commandLine.add("--prune");
        }
        // Progress not shown if not a terminal. Activating progress output. See git fetch man page.
        commandLine.add("--progress");
        start();
        return null;
    }

    /**
     * @param refSpec
     *         ref spec to fetch
     * @return FetchCommand with established ref spec
     */
    public FetchCommand setRefSpec(List<String> refSpec) {
        this.refSpec = refSpec;
        return this;
    }

    /**
     * @param prune
     *         if <code>true</code> not existing remote branches will be removed
     * @return FetchCommand with established prune parameter
     */
    public FetchCommand setPrune(boolean prune) {
        this.prune = prune;
        return this;
    }

    /**
     * @param remote
     *         remote name
     * @return FetchCommand with established remote fetch
     */
    public FetchCommand setRemote(String remote) {
        this.remote = remote;
        return this;
    }
}
