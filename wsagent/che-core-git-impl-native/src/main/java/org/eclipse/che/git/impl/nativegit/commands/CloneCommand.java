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

/**
 * This command used for cloning repositories.
 *
 * @author Eugene Voevodin
 */
public class CloneCommand extends RemoteOperationCommand<Void> {

    private String remoteName;
    private boolean recursiveEnabled;

    public CloneCommand(File repository, SshScriptProvider sshScriptProvider, CredentialsLoader credentialsLoader, GitAskPassScript gitAskPassScript) {
        super(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        reset();
        commandLine.add("clone");
        if (remoteName != null) {
            commandLine.add("--origin", remoteName);
        } //else default origin name
        if (recursiveEnabled) {
            commandLine.add("--recursive");
        }
        commandLine.add(getRemoteUri(), getRepository().getAbsolutePath());
        // Progress not shown if not a terminal. Activating progress output. See git clone man page.
        commandLine.add("--progress");
        start();
        return null;
    }

    /**
     * @param remoteName
     *         name of remote, if it is null than default "origin" name will be used
     * @return CloneCommand with established remoteName
     */
    public CloneCommand setRemoteName(String remoteName) {
        this.remoteName = remoteName;
        return this;
    }

    /**
     * @param recursiveEnabled
     *         returnes true if 'recursive' parameter enabled
     * @return CloneCommand with established 'recursive' parameter
     */
    public CloneCommand setRecursiveEnabled(boolean recursiveEnabled) {
        this.recursiveEnabled = recursiveEnabled;
        return this;
    }
}
