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
package org.eclipse.che.plugin.svn.server.utils;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.plugin.ssh.key.script.SshScript;
import org.eclipse.che.plugin.ssh.key.script.SshScriptProvider;
import org.eclipse.che.plugin.svn.server.SubversionException;

import java.util.Collections;
import java.util.Map;

/**
 * Ssh environment for svn commands.
 *
 * @author Anatoliy Bazko
 */
public class SshEnvironment {

    private final SshScript sshScript;

    public SshEnvironment(SshScriptProvider sshScriptProvider, String repoUrl) throws SubversionException {
        try {
            sshScript = sshScriptProvider.getSshScript(repoUrl);
        } catch (ServerException e) {
            throw new SubversionException(e);
        }
    }

    /**
     * Indicates if uri represents ssh connection.
     *
     * @param uri
     *      the url to svn repository
     */
    public static boolean isSSH(String uri) {
        return uri != null && uri.startsWith("svn+ssh://");
    }

    /**
     * Prepares ssh environment.
     */
    public Map<String, String> get() {
        if (sshScript != null) {
            return ImmutableMap.of("SVN_SSH", sshScript.getSshScriptFile().getAbsolutePath());
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Cleanups ssh environment.
     */
    public void cleanUp() throws SubversionException {
        if (sshScript != null) {
            try {
                sshScript.delete();
            } catch (ServerException e) {
                throw new SubversionException(e);
            }
        }
    }
}
