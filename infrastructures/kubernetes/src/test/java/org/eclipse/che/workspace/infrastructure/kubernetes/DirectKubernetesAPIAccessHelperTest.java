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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.io.InputStream;
import java.net.URI;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.IoUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class DirectKubernetesAPIAccessHelperTest {

  @Mock private OkHttpClient client;
  @Mock private Call call;
  ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

  @BeforeMethod
  public void setupHttpCall() {
    when(client.newCall(requestCaptor.capture())).thenReturn(call);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testFailsOnAbsoluteUrlSuppliedAsRelative() throws Exception {
    DirectKubernetesAPIAccessHelper.call(
        "https://master/", client, "GET", URI.create("https://not-this-way"), null);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testFailsOnOpaqueUrlSuppliedAsRelative() throws Exception {
    DirectKubernetesAPIAccessHelper.call(
        "https://master/", client, "GET", URI.create("opaque:not-this-way"), null);
  }

  @Test
  public void testSendsDataAsApplicationJson() throws Exception {
    // given
    setupResponse(new Response.Builder().code(200));

    // when
    DirectKubernetesAPIAccessHelper.call(
        "https://master/",
        client,
        "POST",
        URI.create("somewhere/over/the/rainbow"),
        JsonNodeFactory.instance.nullNode());

    // then
    assertEquals(
        requestCaptor.getValue().body().contentType(),
        MediaType.get("application/json; charset=utf-8"));
  }

  @Test
  public void testResponseContainsHeaders() throws Exception {
    // given
    setupResponse(new Response.Builder().code(200).header("header", "value"));

    // when
    javax.ws.rs.core.Response response =
        DirectKubernetesAPIAccessHelper.call(
            "https://master/",
            client,
            "POST",
            URI.create("somewhere/over/the/rainbow"),
            JsonNodeFactory.instance.nullNode());

    // then
    assertEquals(response.getStringHeaders().get("header").get(0), "value");
  }

  @Test
  public void testResponseContainsBody() throws Exception {
    // given
    setupResponse(
        new Response.Builder()
            .code(200)
            .body(ResponseBody.create(MediaType.get("application/json"), "true")));

    // when
    javax.ws.rs.core.Response response =
        DirectKubernetesAPIAccessHelper.call(
            "https://master/",
            client,
            "POST",
            URI.create("somewhere/over/the/rainbow"),
            JsonNodeFactory.instance.nullNode());

    // then
    assertEquals(
        response.getMediaType(),
        javax.ws.rs.core.MediaType.valueOf("application/json; charset=utf-8"));
    assertEquals(IoUtil.readAndCloseQuietly((InputStream) response.getEntity()), "true");
  }

  private void setupResponse(Response.Builder response) throws Exception {
    when(call.execute())
        .thenAnswer(
            inv ->
                response
                    .request(requestCaptor.getValue())
                    .message("")
                    .protocol(Protocol.HTTP_1_1)
                    .build());
  }
}
