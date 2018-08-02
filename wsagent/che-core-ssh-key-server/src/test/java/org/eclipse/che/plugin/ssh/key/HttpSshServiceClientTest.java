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

import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.ssh.server.SshService;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link HttpSshServiceClient}
 *
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class HttpSshServiceClientTest {
  private static final String SSH_KEY_SERVICE = "service";
  private static final String SSH_SERVICE_URL = "apiUrl/ssh";
  private static final String SSH_KEY_NAME = "name";
  private static final String API_URL = "apiUrl";

  @Mock private HttpJsonRequestFactory requestFactory;
  @Mock private HttpJsonResponse jsonResponse;

  private HttpJsonRequest jsonRequest;
  private HttpSshServiceClient client;

  @BeforeMethod
  public void setup() throws Exception {
    jsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
    when(jsonRequest.request()).thenReturn(jsonResponse);
    when(requestFactory.fromUrl(anyString())).thenReturn(jsonRequest);

    client = new HttpSshServiceClient(API_URL, requestFactory);
  }

  @Test
  public void shouldMakeGeneratePairRequest() throws Exception {
    // given
    GenerateSshPairRequest sshPairRequest = mock(GenerateSshPairRequest.class);

    // when
    client.generatePair(sshPairRequest);

    // then
    String url = fromUri(SSH_SERVICE_URL).path(SshService.class, "generatePair").build().toString();
    verify(requestFactory).fromUrl(eq(url));
    verify(jsonRequest).usePostMethod();
    verify(jsonRequest).setBody(eq(sshPairRequest));
    verify(jsonRequest).request();
    verify(jsonResponse).asDto(eq(SshPairDto.class));
  }

  @Test
  public void shouldMakeCreatePairRequest() throws Exception {
    // given
    SshPairDto sshPairDto = mock(SshPairDto.class);

    // when
    client.createPair(sshPairDto);

    // then
    String url =
        fromUri(SSH_SERVICE_URL)
            .path(SshService.class.getMethod("createPair", SshPairDto.class))
            .build()
            .toString();
    verify(requestFactory).fromUrl(eq(url));
    verify(jsonRequest).usePostMethod();
    verify(jsonRequest).setBody(eq(sshPairDto));
    verify(jsonRequest).request();
    verify(jsonResponse).asDto(eq(SshPairDto.class));
  }

  @Test
  public void shouldMakeGetPairRequest() throws Exception {
    // when
    client.getPair(SSH_KEY_SERVICE, SSH_KEY_NAME);

    // then
    String url =
        fromUri(SSH_SERVICE_URL)
            .path(SshService.class, "getPair")
            .build(SSH_KEY_SERVICE)
            .toString();
    verify(requestFactory).fromUrl(eq(url));
    verify(jsonRequest).useGetMethod();
    verify(jsonRequest).addQueryParam(eq("name"), eq(SSH_KEY_NAME));
    verify(jsonRequest).request();
    verify(jsonResponse).asDto(eq(SshPairDto.class));
  }

  @Test
  public void shouldMakeRemovePairRequest() throws Exception {
    // when
    client.removePair(SSH_KEY_SERVICE, SSH_KEY_NAME);

    // then
    String url =
        fromUri(SSH_SERVICE_URL)
            .path(SshService.class, "removePair")
            .build(SSH_KEY_SERVICE)
            .toString();
    verify(requestFactory).fromUrl(eq(url));
    verify(jsonRequest).useDeleteMethod();
    verify(jsonRequest).addQueryParam(eq("name"), eq(SSH_KEY_NAME));
    verify(jsonRequest).request();
  }
}
