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
package org.eclipse.che.selenium.core.client;

import static java.lang.String.format;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.slf4j.Logger;

/** @author Musienko Maxim */
@Singleton
public class TestSshServiceClient {
  private static final Logger LOG = getLogger(TestSshServiceClient.class);
  private static final String MACHINE_SERVICE = "machine";
  private static final String VCS_SERVICE = "vcs";

  private final String apiEndpoint;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public TestSshServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider, HttpJsonRequestFactory requestFactory) {
    this.apiEndpoint = apiEndpointProvider.get().toString();
    this.requestFactory = requestFactory;
  }

  public String getPrivateMachineKey(String keyName) throws Exception {
    HttpJsonResponse request =
        requestFactory
            .fromUrl(format("%sssh/%s/?name=%s", apiEndpoint, MACHINE_SERVICE, keyName))
            .useGetMethod()
            .request();
    List<SshPairDto> sshPair = request.asList(SshPairDto.class);
    return sshPair.isEmpty() ? null : sshPair.get(0).getPrivateKey();
  }

  public String generateVCSKey(String keyName) throws Exception {
    GenerateSshPairRequest generateSshKeyData =
        newDto(GenerateSshPairRequest.class).withName(keyName).withService(VCS_SERVICE);

    HttpJsonResponse response =
        requestFactory
            .fromUrl(apiEndpoint + "ssh/generate")
            .usePostMethod()
            .setBody(generateSshKeyData)
            .request();
    return response.asDto(SshPairDto.class).getPublicKey();
  }

  public void deleteVCSKey(String keyName) throws Exception {
    deleteKey(VCS_SERVICE, keyName);
  }

  private void deleteKey(String serviceName, String keyName) throws Exception {
    try {
      requestFactory
          .fromUrl(format("%sssh/%s/?name=%s", apiEndpoint, serviceName, keyName))
          .useDeleteMethod()
          .request();
    } catch (NotFoundException e) {
      // ignore absence of key
      LOG.debug("Ssh key for '{}' with name '{}' is absent.", serviceName, keyName);
    }
  }
}
