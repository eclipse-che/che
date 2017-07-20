/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;

import java.util.List;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Musienko Maxim
 */
@Singleton
public class TestSshServiceClient {
    private final String                 apiEndpoint;
    private final HttpJsonRequestFactory requestFactory;

    @Inject
    public TestSshServiceClient(TestApiEndpointUrlProvider apiEndpointProvider,
                                HttpJsonRequestFactory requestFactory) {
        this.apiEndpoint = apiEndpointProvider.get().toString();
        this.requestFactory = requestFactory;
    }


    public String getPrivateKeyByName(String authToken, String key) throws Exception {
        HttpJsonResponse request = requestFactory.fromUrl(apiEndpoint + "ssh/machine/" + "?name=" + key)
                                                 .setAuthorizationHeader(authToken)
                                                 .useGetMethod()
                                                 .request();
        List<SshPairDto> sshPair = request.asList(SshPairDto.class);
        return sshPair.isEmpty() ? null : sshPair.get(0).getPrivateKey();
    }


    public void deleteMachineKeyByName(String authToken, String key) throws Exception {
        requestFactory.fromUrl(apiEndpoint + "ssh/machine/" + "?name=" + key)
                      .setAuthorizationHeader(authToken)
                      .useDeleteMethod()
                      .request();
    }

    public String generateSshKeys(String authToken) throws Exception {
        GenerateSshPairRequest generateSshKeyData = newDto(GenerateSshPairRequest.class)
                .withName("github.com")
                .withService("vcs");

        HttpJsonResponse response = requestFactory.fromUrl(apiEndpoint + "ssh/generate")
                                                  .usePostMethod()
                                                  .setAuthorizationHeader(authToken)
                                                  .setBody(generateSshKeyData)
                                                  .request();
        return response.asDto(SshPairDto.class).getPublicKey();
    }
}
