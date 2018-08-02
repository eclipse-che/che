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

import com.google.common.io.Files;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of script that provide ssh connection
 *
 * @author Anton Korneta
 * @author Alexander Andrienko
 */
public abstract class SshScript {

  private static final Logger LOG = LoggerFactory.getLogger(SshScript.class);

  private static final String DEFAULT_KEY_NAME = "identity";

  private byte[] sshKey;
  private String host;
  private File rootFolder;
  private File sshScriptFile;

  public SshScript(String host, byte[] sshKey) throws ServerException {
    this.rootFolder = Files.createTempDir();
    this.host = host;
    this.sshKey = sshKey;
    this.sshScriptFile = storeSshScript(writePrivateKeyFile().getPath());
  }

  /**
   * Writes private SSH key into file.
   *
   * @return file that contains SSH key
   * @throws ServerException if other error occurs
   */
  private File writePrivateKeyFile() throws ServerException {
    final File keyDirectory = new File(rootFolder, host);
    if (!keyDirectory.exists()) {
      keyDirectory.mkdirs();
    }

    final File keyFile = new File(keyDirectory, DEFAULT_KEY_NAME);
    try (FileOutputStream fos = new FileOutputStream(keyFile)) {
      fos.write(sshKey);
    } catch (IOException e) {
      LOG.error("Can't store ssh key. ", e);
      throw new ServerException("Can't store ssh key. ");
    }
    protectPrivateKeyFile(keyFile);
    return keyFile;
  }

  /**
   * Stores ssh script that will be executed with all commands that need ssh.
   *
   * @param keyPath path to ssh key
   * @return file that contains script for ssh commands
   * @throws ServerException when any error with ssh script storing occurs
   */
  private File storeSshScript(String keyPath) throws ServerException {
    File sshScriptFile = new File(rootFolder, getSshKeyFileName());
    try (FileOutputStream fos = new FileOutputStream(sshScriptFile)) {
      fos.write(getSshScriptTemplate().replace("$ssh_key", keyPath).getBytes());
    } catch (IOException e) {
      LOG.error("It is not possible to store {} ssh key", keyPath);
      throw new ServerException("Can't store SSH key");
    }
    if (!sshScriptFile.setExecutable(true)) {
      LOG.error("Can't make {} executable", sshScriptFile);
      throw new ServerException("Can't set permissions to SSH key");
    }
    return sshScriptFile;
  }

  /**
   * Get ssh key file name
   *
   * @return ssh key file name
   */
  protected abstract String getSshKeyFileName();

  /**
   * Get ssh script template
   *
   * @return ssh script template for loading loading ssh key
   */
  protected abstract String getSshScriptTemplate();

  /**
   * Set file permission attributes for protection sshKey file
   *
   * @param sshKey ssh key file
   * @throws ServerException
   */
  protected abstract void protectPrivateKeyFile(File sshKey) throws ServerException;

  /**
   * Get sshScript File
   *
   * @return sshScript file
   */
  public File getSshScriptFile() {
    return sshScriptFile;
  }

  /**
   * Remove script folder with sshScript and sshKey
   *
   * @throws ServerException when any error with ssh script deleting occurs
   */
  public void delete() throws ServerException {
    try {
      FileUtils.deleteDirectory(rootFolder);
    } catch (IOException ioEx) {
      throw new ServerException("Can't remove SSH script directory", ioEx);
    }
  }
}
