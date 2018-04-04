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
package org.eclipse.che.api.core.rest;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.JsonArrayImpl;
import org.eclipse.che.dto.shared.JsonArray;
import org.eclipse.che.dto.shared.JsonStringMap;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests of {@link DefaultHttpJsonRequest}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners({MockitoTestNGListener.class, EverrestJetty.class})
public class DefaultHttpJsonRequestTest {

  @SuppressWarnings("unused") // used by EverrestJetty
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @SuppressWarnings("unused") // used by EverrestJetty
  private static final ApiExceptionMapper EXCEPTION_MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused") // used by EverrestJetty
  private static final TestService TEST_SERVICE = new TestService();

  private static final Subject TEST_SUBJECT = new SubjectImpl("name", "id", "token", false);
  private static final String DEFAULT_URL = "http://localhost:8080";

  @Captor private ArgumentCaptor<Map<String, String>> mapCaptor;

  @Captor private ArgumentCaptor<List<Link>> listCaptor;

  private DefaultHttpJsonRequest request;

  @BeforeMethod
  private void prepareDefaultResponse() throws Exception {
    request = spy(new DefaultHttpJsonRequest(DEFAULT_URL));
    request.setMethod("GET");
    prepareResponse("");
  }

  @Test
  public void shouldUseUrlAndMethodFromTheLinks() throws Exception {
    final Link link = createLink("POST", DEFAULT_URL, "rel");
    final DefaultHttpJsonRequest request = spy(new DefaultHttpJsonRequest(link));
    doReturn(new DefaultHttpJsonResponse("", 200))
        .when(request)
        .doRequest(
            anyInt(),
            anyString(),
            anyString(),
            nullable(Object.class),
            nullable(List.class),
            nullable(String.class),
            nullable(List.class));
    request.request();

    verify(request).doRequest(0, DEFAULT_URL, "POST", null, null, null, null);
  }

  @Test
  public void shouldBeAbleToMakeRequest() throws Exception {
    final Object body = new JsonArrayImpl<>(singletonList("element"));

    request
        .setMethod("PUT")
        .setBody(body)
        .setTimeout(10_000_000)
        .addQueryParam("name", "value")
        .addQueryParam("name2", "value2")
        .addHeader("Connection", "close")
        .request();

    verify(request)
        .doRequest(
            10_000_000,
            "http://localhost:8080",
            "PUT",
            body,
            asList(Pair.of("name", "value"), Pair.of("name2", "value2")),
            null,
            asList(Pair.of("Connection", "close")));
  }

  @Test
  public void shouldBeAbleToUseStringMapAsRequestBody() throws Exception {
    final Map<String, String> body = new HashMap<>();
    body.put("name", "value");
    body.put("name2", "value2");

    request.setMethod("POST").setBody(body).request();

    verify(request)
        .doRequest(
            eq(0),
            eq("http://localhost:8080"),
            eq("POST"),
            mapCaptor.capture(),
            eq(null),
            eq(null),
            eq(null));
    assertTrue(mapCaptor.getValue() instanceof JsonStringMap);
    assertEquals(mapCaptor.getValue(), body);
  }

  @Test
  public void shouldBeAbleToUseListOfJsonSerializableElementsAsRequestBody() throws Exception {
    final List<Link> body = singletonList(createLink("POST", "http://localhost:8080", "rel"));

    request.setMethod("POST").setBody(body).request();

    verify(request)
        .doRequest(
            eq(0),
            eq("http://localhost:8080"),
            eq("POST"),
            listCaptor.capture(),
            eq(null),
            eq(null),
            eq(null));
    assertTrue(listCaptor.getValue() instanceof JsonArray);
    assertEquals(listCaptor.getValue(), body);
  }

  @Test
  public void defaultMethodIsGet() throws Exception {
    request.request();
    verify(request).doRequest(0, DEFAULT_URL, HttpMethod.GET, null, null, null, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfSetMethodArgumentIsNull() throws Exception {
    new DefaultHttpJsonRequest("http://localhost:8080").setMethod(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenObjectBodyIsNull() throws Exception {
    final Object obj = null;

    new DefaultHttpJsonRequest("http://localhost:8080").setBody(obj);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenListBodyIsNull() throws Exception {
    final List<String> list = null;

    new DefaultHttpJsonRequest("http://localhost:8080").setBody(list);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenMapBodyIsNull() throws Exception {
    final Map<String, String> map = null;

    new DefaultHttpJsonRequest("http://localhost:8080").setBody(map);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenQueryParamNameIsNull() throws Exception {
    new DefaultHttpJsonRequest("http://localhost:8080").addQueryParam(null, "value");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenQueryParamValueIsNull() throws Exception {
    new DefaultHttpJsonRequest("http://localhost:8080").addQueryParam("name", null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenQueryParamsAreNull() throws Exception {
    new DefaultHttpJsonRequest("http://localhost:8080").addQueryParams(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenHeaderNameIsNull() throws Exception {
    new DefaultHttpJsonRequest("http://localhost:8080").addHeader(null, "close");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenHeaderValueIsNull() throws Exception {
    new DefaultHttpJsonRequest("http://localhost:8080").addHeader("Connection", null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenHeadersAreNull() throws Exception {
    new DefaultHttpJsonRequest("http://localhost:8080").addHeaders(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenLinkHrefIsNull() throws Exception {
    new DefaultHttpJsonRequest(createLink("GET", null, null));
  }

  @Test(expectedExceptions = BadRequestException.class)
  public void shouldThrowBadRequestExceptionWhenResponseCodeIs400(ITestContext ctx)
      throws Exception {
    new DefaultHttpJsonRequest(getUrl(ctx) + "/400/response-code-test").useGetMethod().request();
  }

  @Test(expectedExceptions = UnauthorizedException.class)
  public void shouldThrowUnauthorizedExceptionWhenResponseCodeIs401(ITestContext ctx)
      throws Exception {
    new DefaultHttpJsonRequest(getUrl(ctx) + "/401/response-code-test").useGetMethod().request();
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void shouldThrowForbiddenExceptionWhenResponseCodeIs403(ITestContext ctx)
      throws Exception {
    new DefaultHttpJsonRequest(getUrl(ctx) + "/403/response-code-test").useGetMethod().request();
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenResponseCodeIs404(ITestContext ctx) throws Exception {
    new DefaultHttpJsonRequest(getUrl(ctx) + "/404/response-code-test").useGetMethod().request();
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionWhenResponseCodeIs409(ITestContext ctx) throws Exception {
    new DefaultHttpJsonRequest(getUrl(ctx) + "/409/response-code-test").useGetMethod().request();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldThrowServerExceptionWhenResponseCodeIs500(ITestContext ctx) throws Exception {
    new DefaultHttpJsonRequest(getUrl(ctx) + "/500/response-code-test").useGetMethod().request();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldThrowServerExceptionWhenResponseWithAnyOtherResponseCode(ITestContext ctx)
      throws Exception {
    new DefaultHttpJsonRequest(getUrl(ctx) + "/501/response-code-test").useGetMethod().request();
  }

  @Test(expectedExceptions = IOException.class)
  public void shouldThrowIOExceptionIfServerReturnsTypeDifferentFromApplicationJson(
      ITestContext ctx) throws Exception {
    new DefaultHttpJsonRequest(getUrl(ctx) + "/text-plain").useGetMethod().request();
  }

  @Test
  public void shouldThrowIOExceptionIfServerDoesNotReturnContentTypeOnNoContentResponse(
      ITestContext ctx) throws Exception {
    new DefaultHttpJsonRequest(getUrl(ctx) + "/no-content").useDeleteMethod().request();
  }

  @Test
  public void shouldReadJsonObjectBodyAsString(ITestContext ctx) throws Exception {
    final DefaultHttpJsonRequest request =
        new DefaultHttpJsonRequest(getUrl(ctx) + "/application-json");
    request.useGetMethod();

    assertEquals(request.request().asString(), TestService.JSON_OBJECT);
  }

  @Test
  public void shouldEncodeRequestUrlInDefaultHttpJsonRequestAndDecodeInService(ITestContext ctx)
      throws Exception {
    final String base = getUrl(ctx) + "/decode";
    final HttpJsonResponse response =
        new DefaultHttpJsonRequest(base)
            .addQueryParam("query", "some white spaces !!")
            .useGetMethod()
            .request();

    final String url = base + "?query=some white spaces !!";

    assertEquals(url, response.asString());
  }

  @Test
  public void shouldSendJsonObjectBody(ITestContext ctx) throws Exception {
    final DefaultHttpJsonRequest request =
        new DefaultHttpJsonRequest(getUrl(ctx) + "/application-json");

    final Link link = LinksHelper.createLink("GET", "localhost:8080/application-json", "rel");
    final List<Link> links =
        request
            .usePostMethod()
            .setBody(Collections.singletonList(link))
            .request()
            .asList(Link.class);

    assertEquals(links, Collections.singletonList(link));
  }

  @Test
  public void shouldSendQueryParameters(ITestContext ctx) throws Exception {
    final DefaultHttpJsonRequest request =
        new DefaultHttpJsonRequest(getUrl(ctx) + "/query-parameters");

    final Map<String, String> map =
        request
            .usePutMethod()
            .addQueryParam("param1", "value1")
            .addQueryParam("param2", "value2")
            .request()
            .asProperties();

    assertEquals(map, ImmutableMap.of("param1", "value1", "param2", "value2"));
  }

  @Test
  public void shouldSendMultipleQueryParameters(ITestContext ctx) throws Exception {
    final DefaultHttpJsonRequest request =
        new DefaultHttpJsonRequest(getUrl(ctx) + "/multi-query-parameters");

    @SuppressWarnings("unchecked")
    final Map<String, List<String>> map =
        request
            .usePutMethod()
            .addQueryParam("param1", "value1")
            .addQueryParam("param1", "value2")
            .request()
            .as(Map.class, new TypeToken<Map<String, List<String>>>() {}.getType());

    assertEquals(map.get("param1"), asList("value1", "value2"));
  }

  @Test
  public void shouldUseTokenFromCurrentContextForAuthorization(ITestContext ctx) throws Exception {
    final EnvironmentContext context = new EnvironmentContext();
    context.setSubject(TEST_SUBJECT);
    EnvironmentContext.setCurrent(context);

    new DefaultHttpJsonRequest(getUrl(ctx) + "/token").usePostMethod().request();
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {

    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(TEST_SUBJECT);
    }
  }

  private String getUrl(ITestContext ctx) {
    return "http://localhost:" + ctx.getAttribute(EverrestJetty.JETTY_PORT) + "/rest/test";
  }

  private void prepareResponse(String response) throws Exception {
    doReturn(new DefaultHttpJsonResponse(response, 200))
        .when(request)
        .doRequest(
            anyInt(),
            anyString(),
            anyString(),
            nullable(Object.class),
            nullable(List.class),
            nullable(String.class),
            nullable(List.class));
  }
}
