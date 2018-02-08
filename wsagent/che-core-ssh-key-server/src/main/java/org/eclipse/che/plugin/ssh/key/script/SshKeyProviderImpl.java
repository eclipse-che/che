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

import com.google.inject.Inject;
import java.util.Set;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.ssh.key.SshServiceClient;
import org.eclipse.che.plugin.ssh.key.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation {@link SshKeyProvider} that provides private key and upload public
 *
 * @author Anton Korneta
 */
public class SshKeyProviderImpl implements SshKeyProvider {
  private static final Logger LOG = LoggerFactory.getLogger(SshKeyProviderImpl.class);

  private final SshServiceClient sshService;
  private final Set<SshKeyUploader> sshKeyUploaders;

  @Inject
  public SshKeyProviderImpl(SshServiceClient sshService, Set<SshKeyUploader> sshKeyUploaders) {
    this.sshService = sshService;
    this.sshKeyUploaders = sshKeyUploaders;
  }

  /**
   * Get private ssh key and upload public ssh key to repository hosting service.
   *
   * @param url url to the repository
   * @return private ssh key
   * @throws ServerException if an error occurs while generating or uploading keys
   */
  @Override
  public byte[] getPrivateKey(String url) throws ServerException {
    String host = UrlUtils.getHost(url);

    SshPair pair;
    try {
      pair = sshService.getPair("vcs", host);
    } catch (ServerException | NotFoundException e) {
      throw new ServerException(
          DtoFactory.newDto(ExtendedError.class)
              .withMessage("Unable get private ssh key")
              .withErrorCode(ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY));
    }

    // check keys existence
    String privateKey = pair.getPrivateKey();
    if (privateKey == null) {
      throw new ServerException(
          DtoFactory.newDto(ExtendedError.class)
              .withMessage("Unable get private ssh key")
              .withErrorCode(ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY));
    }
    return privateKey.getBytes();
  }
}
