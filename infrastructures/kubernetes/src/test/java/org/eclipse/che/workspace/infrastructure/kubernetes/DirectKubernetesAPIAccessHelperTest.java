/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
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
  @Mock private HttpHeaders headers;
  ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

  @BeforeMethod
  public void setup() {
    when(headers.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());
    when(client.newCall(requestCaptor.capture())).thenReturn(call);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testFailsOnAbsoluteUrlSuppliedAsRelative() throws Exception {
    DirectKubernetesAPIAccessHelper.call(
        "https://master/", client, "GET", URI.create("https://not-this-way"), headers, null);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testFailsOnOpaqueUrlSuppliedAsRelative() throws Exception {
    DirectKubernetesAPIAccessHelper.call(
        "https://master/", client, "GET", URI.create("opaque:not-this-way"), headers, null);
  }

  @Test
  public void testSendsDataAsApplicationJsonUtf8IfNotSpecifiedInRequest() throws Exception {
    // given
    setupResponse(new Response.Builder().code(200));

    // when
    DirectKubernetesAPIAccessHelper.call(
        "https://master/",
        client,
        "POST",
        URI.create("somewhere/over/the/rainbow"),
        headers,
        new ByteArrayInputStream(
            "Žluťoučký kůň úpěl ďábelské ódy.".getBytes(StandardCharsets.UTF_8)));

    // then
    assertEquals(
        requestCaptor.getValue().body().contentType(),
        MediaType.get("application/json;charset=UTF-8"));

    Buffer expectedBody = new Buffer();
    expectedBody.write(StandardCharsets.UTF_8.encode("Žluťoučký kůň úpěl ďábelské ódy."));

    Buffer body = new Buffer();
    requestCaptor.getValue().body().writeTo(body);

    assertEquals(body, expectedBody);
  }

  @Test
  public void testSendsRequestHeaders() throws Exception {
    // given
    when(headers.getRequestHeaders())
        .thenReturn(
            new MultivaluedHashMap<>(
                ImmutableMap.of(
                    "ducks", "many", "geese", "volumes", "Content-Type", "text/literary")));
    setupResponse(new Response.Builder().code(200));

    // when
    javax.ws.rs.core.Response response =
        DirectKubernetesAPIAccessHelper.call(
            "https://master/",
            client,
            "POST",
            URI.create("somewhere/over/the/rainbow"),
            headers,
            new ByteArrayInputStream("null".getBytes(StandardCharsets.UTF_8)));

    // then
    assertEquals(requestCaptor.getValue().header("ducks"), "many");
    assertEquals(requestCaptor.getValue().header("geese"), "volumes");
    assertEquals(requestCaptor.getValue().header("Content-Type"), "text/literary");
  }

  @Test
  public void testBodySentIntact() throws Exception {
    // given
    when(headers.getRequestHeaders())
        .thenReturn(
            new MultivaluedHashMap<>(
                ImmutableMap.of(
                    "ducks", "many", "geese", "volumes", "Content-Type", "text/literary")));
    setupResponse(new Response.Builder().code(200));

    // when
    javax.ws.rs.core.Response response =
        DirectKubernetesAPIAccessHelper.call(
            "https://master/",
            client,
            "POST",
            URI.create("somewhere/over/the/rainbow"),
            headers,
            new ByteArrayInputStream(
                "Žluťoučký kůň úpěl ďábelské ódy.".getBytes(StandardCharsets.UTF_16BE)));

    // then
    Buffer expectedBody = new Buffer();
    expectedBody.write(StandardCharsets.UTF_16BE.encode("Žluťoučký kůň úpěl ďábelské ódy."));

    Buffer body = new Buffer();
    requestCaptor.getValue().body().writeTo(body);

    assertEquals(body, expectedBody);
  }

  @Test
  public void testHonorsRequestCharset() throws Exception {
    // given
    when(headers.getRequestHeaders())
        .thenReturn(
            new MultivaluedHashMap<>(
                ImmutableMap.of("Content-Type", "text/plain;charset=utf-16be")));
    when(headers.getMediaType())
        .thenReturn(javax.ws.rs.core.MediaType.valueOf("text/plain;charset=utf-16be"));

    setupResponse(new Response.Builder().code(200));

    // when
    DirectKubernetesAPIAccessHelper.call(
        "https://master/",
        client,
        "POST",
        URI.create("somewhere/over/the/rainbow"),
        headers,
        new ByteArrayInputStream(
            "Žluťoučký kůň úpěl ďábelské ódy.".getBytes(StandardCharsets.UTF_16BE)));

    // then
    Request req = requestCaptor.getValue();

    assertEquals(req.header("Content-Type"), "text/plain;charset=utf-16be");
    assertEquals(req.body().contentType(), MediaType.parse("text/plain;charset=utf-16be"));

    Buffer expectedBody = new Buffer();
    expectedBody.write(StandardCharsets.UTF_16BE.encode("Žluťoučký kůň úpěl ďábelské ódy."));

    Buffer body = new Buffer();
    req.body().writeTo(body);

    assertEquals(body, expectedBody);
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
            headers,
            new ByteArrayInputStream("null".getBytes(StandardCharsets.UTF_8)));

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
            headers,
            new ByteArrayInputStream("null".getBytes(StandardCharsets.UTF_8)));

    // then
    assertEquals(
        response.getMediaType(),
        javax.ws.rs.core.MediaType.valueOf("application/json; charset=utf-8"));
    assertEquals(IoUtil.readAndCloseQuietly((InputStream) response.getEntity()), "true");
  }

  @Test
  public void testEmptyHeadersHandled() throws Exception {
    setupResponse(new Response.Builder().code(200));

    // when
    javax.ws.rs.core.Response response =
        DirectKubernetesAPIAccessHelper.call(
            "https://master/", client, "GET", URI.create("somewhere/over/the/rainbow"), null, null);

    // then
    assertEquals(200, response.getStatus());
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
