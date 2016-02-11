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

import com.google.inject.Inject;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.ssh.server.SshServiceClient;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.git.impl.nativegit.GitUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation {@link SshKeyProvider} that provides private key and upload public
 *
 * @author Anton Korneta
 */
public class SshKeyProviderImpl implements SshKeyProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SshKeyProviderImpl.class);

    private final SshServiceClient    sshService;
    private final Set<SshKeyUploader> sshKeyUploaders;

    @Inject
    public SshKeyProviderImpl(SshServiceClient sshService, Set<SshKeyUploader> sshKeyUploaders) {
        this.sshService = sshService;
        this.sshKeyUploaders = sshKeyUploaders;
    }

    /**
     * Get private ssh key and upload public ssh key to repository hosting service.
     *
     * @param url
     *         url to git repository
     * @return private ssh key
     * @throws GitException
     *         if an error occurs while generating or uploading keys
     */
    @Override
    public byte[] getPrivateKey(String url) throws GitException {
        String host = GitUrl.getHost(url);
        SshPair pair;
        try {
            pair = sshService.getPair("git", host);
        } catch (ServerException | NotFoundException e) {
            throw new GitException("Unable get private ssh key");
        }

        // check keys existence
        String privateKey = pair.getPrivateKey();
        if (privateKey == null) {
            throw new GitException("Unable get private ssh key");
        }

        final String publicKey = pair.getPublicKey();
        if (publicKey != null) {
            final Optional<SshKeyUploader> optionalKeyUploader = sshKeyUploaders.stream()
                                                                                .filter(keyUploader -> keyUploader.match(url))
                                                                                .findFirst();
            if (optionalKeyUploader.isPresent()) {
                final SshKeyUploader uploader = optionalKeyUploader.get();
                try {
                    uploader.uploadKey(publicKey);
                } catch (IOException e) {
                    throw new GitException(e.getMessage(), e);
                } catch (UnauthorizedException e) {
                    // Git action might fail without uploaded public SSH key.
                    LOG.warn(String.format("Unable upload public SSH key with %s", uploader.getClass().getSimpleName()), e);
                }
            } else {
                // Git action might fail without uploaded public SSH key.
                LOG.warn(String.format("Not found ssh key uploader for %s", host));
            }
        }
        return privateKey.getBytes();
    }
}
