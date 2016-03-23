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

import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.git.impl.nativegit.GitAskPassScript;
import org.eclipse.che.git.impl.nativegit.GitUrl;
import org.eclipse.che.api.git.UserCredential;
import org.eclipse.che.git.impl.nativegit.ssh.GitSshScript;
import org.eclipse.che.git.impl.nativegit.ssh.GitSshScriptProvider;

import java.io.File;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * @author Sergii Kabashniuk
 */
public abstract class RemoteOperationCommand<T> extends GitCommand<T> {

    private final GitSshScriptProvider gitSshScriptProvider;
    private final CredentialsLoader    credentialsLoader;
    private final GitAskPassScript     gitAskPassScript;
    private       String               remoteUri;

    /**
     * @param repository
     *         directory where command will be executed
     */
    public RemoteOperationCommand(File repository,
                                  GitSshScriptProvider gitSshScriptProvider,
                                  CredentialsLoader credentialsLoader,
                                  GitAskPassScript gitAskPassScript) {
        super(repository);
        this.gitSshScriptProvider = gitSshScriptProvider;
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
        if (GitUrl.isSSH(remoteUri)) {
            GitSshScript sshScript = gitSshScriptProvider.gitSshScript(remoteUri);
            setCommandEnvironment("GIT_SSH", sshScript.getSshScriptFile().getAbsolutePath());

            try {
                super.start();
            } finally {
                sshScript.delete();
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
}
