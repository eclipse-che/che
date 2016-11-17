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
package org.eclipse.che.api.ssh.server;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/**
 * Implementation of {@link SshServiceClient} that provide access to Ssh Service via http.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class HttpSshServiceClient implements SshServiceClient {
    private final String                 sshUrl;
    private final HttpJsonRequestFactory requestFactory;

    @Inject
    public HttpSshServiceClient(@Named("che.api") String apiUrl,
                                HttpJsonRequestFactory requestFactory) {
        this.sshUrl = UriBuilder.fromUri(apiUrl)
                                .path(SshService.class)
                                .build()
                                .toString();
        this.requestFactory = requestFactory;
    }

    @Override
    public SshPairDto generatePair(GenerateSshPairRequest request) throws ServerException {
        try {
            final String url = UriBuilder.fromUri(sshUrl)
                                         .path(SshService.class, "generatePair")
                                         .build()
                                         .toString();
            return requestFactory.fromUrl(url)
                                 .useGetMethod()
                                 .setBody(request)
                                 .request()
                                 .asDto(SshPairDto.class);
        } catch (IOException | ApiException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public void createPair(SshPairDto sshPair) throws ServerException {
        try {
            final String url = UriBuilder.fromUri(sshUrl)
                                         .path(SshService.class, "createPair")
                                         .build()
                                         .toString();
            requestFactory.fromUrl(url)
                          .useGetMethod()
                          .setBody(sshPair)
                          .request()
                          .asDto(SshPairDto.class);
        } catch (IOException | ApiException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public SshPairDto getPair(String service, String name) throws ServerException, NotFoundException {
        try {
            final String url = UriBuilder.fromUri(sshUrl)
                                         .path(SshService.class, "getPair")
                                         .build(service)
                                         .toString();

            return requestFactory.fromUrl(url)
                                 .useGetMethod()
                                 .addQueryParam("name", name)
                                 .request()
                                 .asDto(SshPairDto.class);
        } catch (IOException | ForbiddenException | BadRequestException | ConflictException | UnauthorizedException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public void removePair(String service, String name) throws ServerException, NotFoundException {
        try {
            final String url = UriBuilder.fromUri(sshUrl)
                                         .path(SshService.class, "removePair")
                                         .build(service)
                                         .toString();

            requestFactory.fromUrl(url)
                          .useDeleteMethod()
                          .addQueryParam("name", name)
                          .request();
        } catch (IOException | ForbiddenException | BadRequestException | ConflictException | UnauthorizedException e) {
            throw new ServerException(e);
        }
    }
}
