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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.UserCredential;
import org.eclipse.che.git.impl.nativegit.GitAskPassScript;
import org.eclipse.che.api.git.GitUrlUtils;
import org.eclipse.che.plugin.ssh.key.script.SshScript;
import org.eclipse.che.plugin.ssh.key.script.SshScriptProvider;

import java.io.File;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * @author Sergii Kabashniuk
 */
public abstract class RemoteOperationCommand<T> extends GitCommand<T> {

    private final SshScriptProvider sshScriptProvider;
    private final CredentialsLoader credentialsLoader;
    private final GitAskPassScript  gitAskPassScript;
    private       String            remoteUri;

    /**
     * @param repository
     *         directory where command will be executed
     */
    public RemoteOperationCommand(File repository,
                                  SshScriptProvider sshScriptProvider,
                                  CredentialsLoader credentialsLoader,
                                  GitAskPassScript gitAskPassScript) {
        super(repository);
        this.sshScriptProvider = sshScriptProvider;
        this.credentialsLoader = credentialsLoader;
        this.gitAskPassScript = gitAskPassScript;
    }

    public String getRemoteUri() {
        return remoteUri;
    }

    /**
     * @param remoteUri
     *         remote repository uri
     */
    public RemoteOperationCommand setRemoteUri(String remoteUri) {
        this.remoteUri = remoteUri;
        return this;
    }

    @Override
    protected void start() throws GitException {
        if (GitUrlUtils.isSSH(remoteUri)) {
            SshScript sshScript = getSshScript();
            setCommandEnvironment("GIT_SSH", sshScript.getSshScriptFile().getAbsolutePath());

            try {
                super.start();
            } finally {
                deleteSshScript(sshScript);
            }
        } else {
            UserCredential credentials = firstNonNull(credentialsLoader.getUserCredential(remoteUri), UserCredential.EMPTY_CREDENTIALS);
            setCommandEnvironment("GIT_ASKPASS", gitAskPassScript.build(credentials).toString());

            try {
                super.start();
            } finally {
                gitAskPassScript.remove();
            }
        }
    }

    private SshScript getSshScript() throws GitException {
        try {
            return sshScriptProvider.getSshScript(remoteUri);
        } catch (ServerException e) {
            throw new GitException(e.getServiceError());
        }
    }

    private void deleteSshScript(SshScript sshScript) throws GitException {
        try {
            sshScript.delete();
        } catch (ServerException e) {
            throw new GitException(e.getServiceError());
        }
    }
}
