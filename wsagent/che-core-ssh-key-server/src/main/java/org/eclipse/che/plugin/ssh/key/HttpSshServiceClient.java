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
package org.eclipse.che.plugin.ssh.key;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
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

/**
 * Implementation of {@link SshServiceClient} that provide access to Ssh Service via http.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class HttpSshServiceClient implements SshServiceClient {
  private final String sshUrl;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public HttpSshServiceClient(
      @Named("che.api") String apiUrl, HttpJsonRequestFactory requestFactory) {
    this.sshUrl = apiUrl + "/ssh";
    this.requestFactory = requestFactory;
  }

  @Override
  public SshPairDto generatePair(GenerateSshPairRequest request) throws ServerException {
    try {
      return requestFactory
          .fromUrl(sshUrl + "/generate")
          .usePostMethod()
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
      requestFactory
          .fromUrl(sshUrl)
          .usePostMethod()
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
      return requestFactory
          .fromUrl(sshUrl + "/" + service + "/find")
          .useGetMethod()
          .addQueryParam("name", name)
          .request()
          .asDto(SshPairDto.class);
    } catch (IOException
        | ForbiddenException
        | BadRequestException
        | ConflictException
        | UnauthorizedException e) {
      throw new ServerException(e);
    }
  }

  @Override
  public void removePair(String service, String name) throws ServerException, NotFoundException {
    try {
      requestFactory
          .fromUrl(sshUrl + "/" + service)
          .useDeleteMethod()
          .addQueryParam("name", name)
          .request();
    } catch (IOException
        | ForbiddenException
        | BadRequestException
        | ConflictException
        | UnauthorizedException e) {
      throw new ServerException(e);
    }
  }
}
