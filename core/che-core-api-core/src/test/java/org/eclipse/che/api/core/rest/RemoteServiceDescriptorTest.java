/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.annotations.Description;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.core.rest.annotations.Valid;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.ServiceDescriptor;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.everrest.assured.EverrestJetty;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners({MockitoTestNGListener.class, EverrestJetty.class})
public class RemoteServiceDescriptorTest {
  @SuppressWarnings("unused") // used by EverrestJetty
  private static final EchoService service = new EchoService();

  private static final String SERVICE_PATH = "/test";
  private static final String SERVICE_DESCRIPTION = "test service";

  @Spy private DefaultHttpJsonRequestFactory requestFactory;
  private HttpJsonRequest request;
  @Mock private HttpJsonResponse response;

  private String serverUrl;
  private RemoteServiceDescriptor remoteServiceDescriptor;

  @BeforeClass
  public void setUp(ITestContext ctx) throws Exception {
    serverUrl = getServerUrl(ctx);
    request = mock(HttpJsonRequest.class, new SelfReturningAnswer());
    when(request.request()).thenReturn(response);
  }

  @Test
  public void shouldBeAbleToReturnServiceDescriptors() throws Exception {
    remoteServiceDescriptor = new RemoteServiceDescriptor(serverUrl + SERVICE_PATH, requestFactory);

    final ServiceDescriptor serviceDescriptor = remoteServiceDescriptor.getServiceDescriptor();

    assertNotNull(serviceDescriptor);
    assertEquals(serviceDescriptor.getHref(), serverUrl + SERVICE_PATH);
    assertEquals(serviceDescriptor.getDescription(), SERVICE_DESCRIPTION);
    assertEquals(serviceDescriptor.getLinks().size(), 1);
  }

  @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "test error")
  public void shouldThrowServerExceptionIfServiceNotFound() throws Exception {
    doReturn(request).when(requestFactory).fromUrl(anyString());
    when(request.request()).thenThrow(new NotFoundException("test error"));

    remoteServiceDescriptor =
        new RemoteServiceDescriptor(serverUrl + "/non/existing/path", requestFactory);

    remoteServiceDescriptor.getServiceDescriptor();
  }

  @Test
  public void shouldBeAbleToConfirmAvailability() throws Exception {
    remoteServiceDescriptor = new RemoteServiceDescriptor(serverUrl + SERVICE_PATH, requestFactory);

    assertTrue(remoteServiceDescriptor.isAvailable());
  }

  @Test
  public void shouldReturnFalseOnNonAvailableServiceAvailabilityCheck() {
    remoteServiceDescriptor =
        new RemoteServiceDescriptor(serverUrl + "/non/existing/path", requestFactory);

    assertFalse(remoteServiceDescriptor.isAvailable());
  }

  @Test
  public void shouldReturnNullIfLinkWithRequiredRelDoesNotExist() throws Exception {
    remoteServiceDescriptor = new RemoteServiceDescriptor(serverUrl + SERVICE_PATH, requestFactory);

    assertNull(remoteServiceDescriptor.getLink("echo2"));
  }

  @Test
  public void shouldBeAbleToReturnLinks() throws Exception {
    remoteServiceDescriptor = new RemoteServiceDescriptor(serverUrl + SERVICE_PATH, requestFactory);

    final List<Link> links = remoteServiceDescriptor.getLinks();

    assertEquals(links.size(), 1);
    assertEquals(links.get(0).getMethod(), HttpMethod.GET);
    assertEquals(links.get(0).getHref(), serverUrl + SERVICE_PATH + "/my_method");
    assertEquals(links.get(0).getProduces(), MediaType.TEXT_PLAIN);
  }

  @Description(SERVICE_DESCRIPTION)
  @Path(SERVICE_PATH)
  public static class EchoService extends Service {
    @GET
    @Path("my_method")
    @GenerateLink(rel = "echo")
    @Produces(MediaType.TEXT_PLAIN)
    public String echo(
        @Description("some text")
            @Required
            @Valid({"a", "b"})
            @DefaultValue("a")
            @QueryParam("text")
            String test) {
      return test;
    }
  }

  private String getServerUrl(ITestContext ctx) {
    return "http://localhost:" + ctx.getAttribute(EverrestJetty.JETTY_PORT) + "/rest";
  }
}
