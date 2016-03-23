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
package org.eclipse.che.git.impl.nativegit.ssh;

import org.eclipse.che.api.git.GitException;

import static org.eclipse.che.api.core.util.SystemInfo.isUnix;
import static org.eclipse.che.api.core.util.SystemInfo.isWindows;
import static org.eclipse.che.git.impl.nativegit.GitUrl.getHost;

import javax.inject.Inject;

/**
 * Provides GitSshScript
 *
 * @author Sergii Kabashniuk
 * @author Anton Korneta
 */
public class GitSshScriptProvider {

    private final SshKeyProvider sshKeyProvider;

    @Inject
    public GitSshScriptProvider(SshKeyProvider sshKeyProvider) {
        this.sshKeyProvider = sshKeyProvider;
    }

    /**
     * Get GitSshScript object
     *
     * @param url
     *         url to git repository
     * @throws GitException
     *         if an error occurs when creating a script file
     */
    public GitSshScript gitSshScript(String url) throws GitException {
        String host = getHost(url);
        if (host == null) {
            throw new GitException("URL does not have a host");
        }
        if (isWindows()) {
            return new WindowsGitSshScript(host, sshKeyProvider.getPrivateKey(url));
        }
        if (isUnix()) {
            return new UnixGitSshScript(host, sshKeyProvider.getPrivateKey(url));
        }
        throw new GitException("Unsupported OS.");
    }
}
