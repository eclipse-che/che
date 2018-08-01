/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.ssh.key.script;

import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;
import org.eclipse.che.api.core.ServerException;

/**
 * Implementation of script that provide ssh connection on Unix
 *
 * @author Anton Korneta
 * @author Alexander Andrienko
 */
public class UnixSshScript extends SshScript {

  public UnixSshScript(String host, byte[] sshKey) throws ServerException {
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
  protected void protectPrivateKeyFile(File sshKey) throws ServerException {
    try {
      // set permission to -rw-------
      Set<PosixFilePermission> permissions = EnumSet.of(OWNER_READ, OWNER_WRITE);
      Files.setPosixFilePermissions(sshKey.toPath(), permissions);
    } catch (IOException e) {
      throw new ServerException("Failed to set file permissions");
    }
  }
}
