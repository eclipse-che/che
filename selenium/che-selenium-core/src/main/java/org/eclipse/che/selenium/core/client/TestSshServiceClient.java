/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;

/** @author Musienko Maxim */
@Singleton
public class TestSshServiceClient {
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

  public String getPrivateKeyByName(String name) throws Exception {
    HttpJsonResponse request =
        requestFactory
            .fromUrl(String.format("%sssh/%s/?name=%s", apiEndpoint, MACHINE_SERVICE, name))
            .useGetMethod()
            .request();
    List<SshPairDto> sshPair = request.asList(SshPairDto.class);
    return sshPair.isEmpty() ? null : sshPair.get(0).getPrivateKey();
  }

  public void deleteMachineKeyByName(String name) throws Exception {
    requestFactory
        .fromUrl(apiEndpoint + "ssh/" + MACHINE_SERVICE + "/?name=" + name)
        .useDeleteMethod()
        .request();
  }

  public String generateGithubKey() throws Exception {
    GenerateSshPairRequest generateSshKeyData =
        newDto(GenerateSshPairRequest.class).withName("github.com").withService(VCS_SERVICE);

    HttpJsonResponse response =
        requestFactory
            .fromUrl(apiEndpoint + "ssh/generate")
            .usePostMethod()
            .setBody(generateSshKeyData)
            .request();
    return response.asDto(SshPairDto.class).getPublicKey();
  }
}
