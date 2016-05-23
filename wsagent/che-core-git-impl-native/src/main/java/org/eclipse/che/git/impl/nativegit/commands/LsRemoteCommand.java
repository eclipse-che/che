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
import org.eclipse.che.api.git.shared.RemoteReference;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.git.impl.nativegit.GitAskPassScript;
import org.eclipse.che.plugin.ssh.key.script.SshScriptProvider;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vladyslav Zhukovskii
 */
public class LsRemoteCommand extends RemoteOperationCommand<Void> {
    private String url;

    public LsRemoteCommand(File repository, SshScriptProvider sshScriptProvider, CredentialsLoader credentialsLoader, GitAskPassScript gitAskPassScript) {
        super(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
    }

    /** {@inheritDoc} */
    @Override
    public Void execute() throws GitException {
        if (url == null) {
            throw new GitException("Remote repository URL wasn't set.");
        }
        reset();
        commandLine.add("ls-remote", url);
        start();
        return null;
    }

    /**
     * @param url
     *         url of remote repository to get references list
     * @return LsRemoteCommand with established url
     */
    public LsRemoteCommand setRemoteUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get list of remote references.
     */
    public List<RemoteReference> getRemoteReferences() {
        List<RemoteReference> references = new LinkedList<>();
        final DtoFactory dtoFactory = DtoFactory.getInstance();
        for (String outLine : lines) {
            String[] parts = outLine.trim().split("\\s");
            String commitId = parts[0];
            String referenceName = parts[1];
            references.add(dtoFactory.createDto(RemoteReference.class).withCommitId(commitId).withReferenceName(referenceName));
        }
        return references;
    }
}
