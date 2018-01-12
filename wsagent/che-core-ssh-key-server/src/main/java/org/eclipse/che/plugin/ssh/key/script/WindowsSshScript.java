/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.ssh.key.script;

import static java.nio.file.attribute.AclEntryPermission.APPEND_DATA;
import static java.nio.file.attribute.AclEntryPermission.DELETE;
import static java.nio.file.attribute.AclEntryPermission.READ_ACL;
import static java.nio.file.attribute.AclEntryPermission.READ_ATTRIBUTES;
import static java.nio.file.attribute.AclEntryPermission.READ_DATA;
import static java.nio.file.attribute.AclEntryPermission.READ_NAMED_ATTRS;
import static java.nio.file.attribute.AclEntryPermission.SYNCHRONIZE;
import static java.nio.file.attribute.AclEntryType.ALLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.ServerException;

/**
 * Implementation of script that provide ssh connection on Windows
 *
 * @author Alexander Andrienko
 */
public class WindowsSshScript extends SshScript {

  private static final String OWNER_NAME_PROPERTY = "user.name";

  public WindowsSshScript(String host, byte[] sshKey) throws ServerException {
    super(host, sshKey);
  }

  @Override
  protected String getSshKeyFileName() {
    return "ssh_script.bat";
  }

  @Override
  protected String getSshScriptTemplate() {
    return "@echo off\n ssh -o IdentitiesOnly=yes -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i \"$ssh_key\" %*";
  }

  @Override
  protected void protectPrivateKeyFile(File sshKey) throws ServerException {
    try {
      AclFileAttributeView attributes =
          Files.getFileAttributeView(sshKey.toPath(), AclFileAttributeView.class);

      AclEntry.Builder builder = AclEntry.newBuilder();
      builder.setType(ALLOW);

      String ownerName = System.getProperty(OWNER_NAME_PROPERTY);
      UserPrincipal userPrincipal =
          FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(ownerName);

      builder.setPrincipal(userPrincipal);
      builder.setPermissions(
          READ_DATA, APPEND_DATA, READ_NAMED_ATTRS, READ_ATTRIBUTES, DELETE, READ_ACL, SYNCHRONIZE);

      AclEntry entry = builder.build();
      List<AclEntry> aclEntryList = new ArrayList<>();
      aclEntryList.add(entry);
      attributes.setAcl(aclEntryList);
    } catch (IOException e) {
      throw new ServerException("Failed to set file permissions");
    }
  }
}
