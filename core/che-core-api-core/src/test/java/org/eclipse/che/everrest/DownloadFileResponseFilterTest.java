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
package org.eclipse.che.everrest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.everrest.core.ApplicationContext.anApplicationContext;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.DownloadFileResponseFilter;
import org.everrest.core.ApplicationContext;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test the DownloadFileResponseFilter filter
 *
 * @author Florent Benoit
 */
public class DownloadFileResponseFilterTest {

  /** Base URI */
  private static final String BASE_URI = "http://localhost/service";

  /** Base Service */
  private static final String SERVICE_PATH = BASE_URI + "/myservice";

  /** Dummy JAX-RS POJO */
  @Path("/myservice")
  public static class MyJaxRSService {

    @GET
    @Path("/list")
    @Produces(APPLICATION_JSON)
    public List<String> getMembers() {
      return Arrays.asList("a", "b", "c");
    }

    @GET
    @Path("/single")
    @Produces(APPLICATION_JSON)
    public String getMember() {
      return "hello";
    }

    @POST
    @Path("/modify")
    @Produces(APPLICATION_JSON)
    public Response myPostMethod() {
      return Response.ok("helloContent").build();
    }
  }

  /** Resource Launcher */
  private ResourceLauncher resourceLauncher;

  /**
   * Setup env for launching requests
   *
   * @throws Exception
   */
  @BeforeMethod
  public void before() throws Exception {
    // set up launcher
    final ResourceBinderImpl resources = new ResourceBinderImpl();
    resources.addResource(MyJaxRSService.class, null);
    final DependencySupplierImpl dependencies = new DependencySupplierImpl();
    final ApplicationProviderBinder providers = new ApplicationProviderBinder();
    providers.addExceptionMapper(ApiExceptionMapper.class);
    providers.addResponseFilter(EverrestDownloadFileResponseFilter.class);
    final URI uri = new URI(BASE_URI);
    final ContainerRequest req = new ContainerRequest(null, uri, uri, null, null, null);
    final ApplicationContext context =
        anApplicationContext()
            .withRequest(req)
            .withProviders(providers)
            .withDependencySupplier(dependencies)
            .build();
    ApplicationContext.setCurrent(context);
    final EverrestProcessor processor =
        new EverrestProcessor(
            new EverrestConfiguration(),
            dependencies,
            new RequestHandlerImpl(new RequestDispatcher(resources), providers),
            null);
    resourceLauncher = new ResourceLauncher(processor);
  }

  /** Check if header is absent when parameter is empty */
  @Test
  public void checkNoParameterTest() throws Exception {

    final ContainerResponse response =
        resourceLauncher.service(
            HttpMethod.GET, SERVICE_PATH + "/list", BASE_URI, null, null, null);
    assertEquals(response.getStatus(), OK.getStatusCode());
    // check entity
    Assert.assertEquals(response.getEntity(), Arrays.asList("a", "b", "c"));
    // Check headers
    MultivaluedMap<String, Object> headerTags = response.getHttpHeaders();
    Assert.assertNotNull(headerTags);
    Assert.assertEquals(headerTags.size(), 1);
  }

  /** Check if header for downloading is added in response if we're also using a custom header */
  @Test
  public void checkDownloadFileWithParameter() throws Exception {

    final ContainerResponse response =
        resourceLauncher.service(
            HttpMethod.GET,
            SERVICE_PATH
                + "/list?"
                + DownloadFileResponseFilter.QUERY_DOWNLOAD_PARAMETER
                + "=hello.json",
            BASE_URI,
            null,
            null,
            null);
    assertEquals(response.getStatus(), OK.getStatusCode());
    // check entity
    Assert.assertEquals(response.getEntity(), Arrays.asList("a", "b", "c"));

    // headers = 2
    Assert.assertEquals(response.getHttpHeaders().size(), 2);

    // Check custom header
    List<Object> headers = response.getHttpHeaders().get(HttpHeaders.CONTENT_DISPOSITION);
    Assert.assertNotNull(headers);
    Assert.assertEquals(headers.size(), 1);
    Assert.assertEquals(headers.get(0), "attachment; filename=hello.json");
  }

  /** Check that parameter is only handled if there is a GET method, not POST */
  @Test
  public void checkOnlyOnGetMethodTest() throws Exception {
    final ContainerResponse response =
        resourceLauncher.service(
            HttpMethod.POST,
            SERVICE_PATH
                + "/modify?"
                + DownloadFileResponseFilter.QUERY_DOWNLOAD_PARAMETER
                + "=hello.json",
            BASE_URI,
            null,
            null,
            null);
    assertEquals(response.getStatus(), OK.getStatusCode());
    // check entity
    Assert.assertEquals(response.getEntity(), "helloContent");

    // headers = 2
    Assert.assertEquals(response.getHttpHeaders().size(), 1);

    // Check custom header
    List<Object> headers = response.getHttpHeaders().get(HttpHeaders.CONTENT_DISPOSITION);
    Assert.assertNull(headers);
  }
}
