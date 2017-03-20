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
package org.eclipse.che.plugin.ssh.key.client;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds available ssh key uploaders.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class SshKeyUploaderRegistry {
    private final Map<String, SshKeyUploader> sshKeyUploaders = new HashMap<>();

    /**
     * Get the list of SSH keys uploaders.
     */
    public Map<String, SshKeyUploader> getUploaders() {
        return new HashMap<>(sshKeyUploaders);
    }

    /**
     * Get the ssh key uploader for given host.
     */
    public SshKeyUploader getUploader(String host) {
        return sshKeyUploaders.get(host);
    }

    /**
     * Register SSH key uploader
     *
     * @param host
     *         host, for which to provide keys
     * @param sshKeyUploader
     *         keys uploader
     */
    public void registerUploader(@NotNull String host, @NotNull SshKeyUploader sshKeyUploader) {
        sshKeyUploaders.put(host, sshKeyUploader);
    }
}
