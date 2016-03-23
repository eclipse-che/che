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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

/**
 * Implementation of script that provide ssh connection on Unix
 *
 * @author Anton Korneta
 * @author Alexander Andrienko
 */
public class UnixGitSshScript extends GitSshScript {

    public UnixGitSshScript(String host, byte[] sshKey) throws GitException {
        super(host, sshKey);
    }

    @Override
    protected String getSshKeyFileName() {
        return "ssh_script";
    }

    @Override
    protected String getSshScriptTemplate() {
        return "exec ssh -o IdentitiesOnly=yes -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i \"$ssh_key\" $@";
    }

    @Override
    protected void protectPrivateKeyFile(File sshKey) throws GitException {
        try {
            //set permission to -rw-------
            Set<PosixFilePermission> permissions = EnumSet.of(OWNER_READ, OWNER_WRITE);
            Files.setPosixFilePermissions(sshKey.toPath(), permissions);
        } catch (IOException e) {
            throw new GitException("Failed to set file permissions");
        }
    }
}
