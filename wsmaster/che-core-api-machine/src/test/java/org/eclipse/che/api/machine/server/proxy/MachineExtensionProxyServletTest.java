/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.server.proxy;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.everrest.test.mock.MockHttpServletRequest;
import org.everrest.test.mock.MockHttpServletResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javax.servlet.http.HttpServletResponse.SC_BAD_GATEWAY;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class MachineExtensionProxyServletTest {
    private static final String WORKSPACE_ID            = "workspace123";
    private static final String EXTENSIONS_API_PORT     = "4301/tcp";
    private static final String PROXY_ENDPOINT          = "http://localhost:8080";
    private static final String BASE_PATH               = "/che/api/ext/api/";
    private static final String DEFAULT_PATH            = BASE_PATH + WORKSPACE_ID + "/java/";
    private static final String DEFAULT_URL             = PROXY_ENDPOINT + DEFAULT_PATH;
    private static final String DEFAULT_RESPONSE_ENTITY = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

    private static final Map<String, List<String>> defaultHeaders;

    static {
        defaultHeaders = Collections.singletonMap("Host", Collections.singletonList("localhost"));
    }

    private ContextHandler contextHandler;

    private MachineManager machineManager;

    private MachineImpl machine;

    private MachineRuntimeInfoImpl machineRuntimeInfo;

    private MachineExtensionProxyServlet proxyServlet;

    private org.eclipse.jetty.server.Server jettyServer;

    private ExtensionApiResponse extensionApiResponse;

    private ExtensionApiRequest extensionApiRequest;

    // Todo
    // send entity to destination
    // check that proxy doesn't copy hop-by-hop headers
    // check headers send to destination
    // check used destination url
    // machine does not exist
    // request url does not contain machine id
    // no server on destination side
    // all type of response codes from destination side
    // https to http proxy
    // json object in response
    // json object in request
    // html in response
    // including cookies and http-only cookies
    // secure cookies for https
    // read entity from error stream of destination response
    // check that cookies are not saved in proxy between requests
    // responses on exceptions
    // responses on missing machine id in utl

    @BeforeClass
    public void setUpClass() throws Exception {
        jettyServer = new org.eclipse.jetty.server.Server(0);
        contextHandler = new ContextHandler(DEFAULT_PATH);
        contextHandler.setHandler(new ExtensionApiHandler());
        jettyServer.setHandler(contextHandler);

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                jettyServer.start();
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
            }
        });

        while (!org.eclipse.jetty.server.Server.STARTED.equals(jettyServer.getState())) {
            Thread.sleep(500);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        String serverAddress = "localhost:" + jettyServer.getURI().getPort();
        Map<String, ServerImpl> machineServers = Collections.singletonMap(String.valueOf(EXTENSIONS_API_PORT),
                                                                          new ServerImpl(null,
                                                                                         "http",
                                                                                         serverAddress,
                                                                                         null,
                                                                                         "http://" + serverAddress));

        machineManager = mock(MachineManager.class);

        machine = mock(MachineImpl.class);

        machineRuntimeInfo = mock(MachineRuntimeInfoImpl.class);

        extensionApiResponse = spy(new ExtensionApiResponse());

        extensionApiRequest = new ExtensionApiRequest();

        proxyServlet = new MachineExtensionProxyServlet(EXTENSIONS_API_PORT, machineManager);

        when(machineManager.getDevMachine(WORKSPACE_ID)).thenReturn(machine);
        when(machine.getRuntime()).thenReturn(machineRuntimeInfo);
        when(machineRuntimeInfo.getServers()).thenReturn(machineServers);
    }

    @AfterClass
    public void tearDown() throws Exception {
        jettyServer.stop();
    }

    @Test(dataProvider = "methodProvider")
    public void shouldBeAbleToProxyRequestWithDifferentMethod(String method) throws Exception {
        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest(DEFAULT_URL,
                                           new ByteArrayInputStream(new byte[0]),
                                           0,
                                           method,
                                           defaultHeaders);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), 200, mockResponse.getOutputContent());
    }

    @DataProvider(name = "methodProvider")
    public Object[][] methodProvider() {
        return new String[][]{{"GET"}, {"PUT"}, {"POST"}, {"DELETE"}, {"OPTIONS"}};
    }

    @Test
    public void shouldCopyEntityFromResponse() throws Exception {
        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest(DEFAULT_URL,
                                           new ByteArrayInputStream(new byte[0]),
                                           0,
                                           "GET",
                                           defaultHeaders);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), 200);
        assertEquals(mockResponse.getOutputContent(), DEFAULT_RESPONSE_ENTITY);
    }

    @Test
    public void shouldBeAbleToProxyWithDifferentContext() throws Exception {
        final String path = "/api/ext/service/" + WORKSPACE_ID + "/java/codeassistant/index";

        final String defaultContextPath = contextHandler.getContextPath();
        try {
            contextHandler.setContextPath("/api/ext/service/" + WORKSPACE_ID + "/java/");

            MockHttpServletRequest mockRequest =
                    new MockHttpServletRequest(PROXY_ENDPOINT + path,
                                               new ByteArrayInputStream(new byte[0]),
                                               0,
                                               "GET",
                                               defaultHeaders);

            MockHttpServletResponse mockResponse = new MockHttpServletResponse();

            proxyServlet.service(mockRequest, mockResponse);

            assertEquals(mockResponse.getStatus(), 200);
            assertEquals(extensionApiRequest.uri, path);
        } finally {
            contextHandler.setContextPath(defaultContextPath);
        }
    }

    @Test
    public void shouldBeAbleToProxyWithout() throws Exception {
        final String path = "/api/ext/" + WORKSPACE_ID + "/";

        final String defaultContextPath = contextHandler.getContextPath();
        try {
            contextHandler.setContextPath("/api/ext/");

            MockHttpServletRequest mockRequest =
                    new MockHttpServletRequest(PROXY_ENDPOINT + path,
                                               new ByteArrayInputStream(new byte[0]),
                                               0,
                                               "GET",
                                               defaultHeaders);

            MockHttpServletResponse mockResponse = new MockHttpServletResponse();

            proxyServlet.service(mockRequest, mockResponse);

            assertEquals(mockResponse.getStatus(), 200);
            assertEquals(extensionApiRequest.uri, "/api/ext/");
        } finally {
            contextHandler.setContextPath(defaultContextPath);
        }
    }

    @Test
    public void shouldProxyWithQueryString() throws Exception {
        final String query = "key1=value1&key2=value2&key2=value3";

        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest(DEFAULT_URL + "?" + query,
                                           new ByteArrayInputStream(new byte[0]),
                                           0,
                                           "GET",
                                           defaultHeaders);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), 200);
        assertEquals(extensionApiRequest.query, query);
    }

    @Test
    public void shouldProxyResponseWithError() throws Exception {
        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest(PROXY_ENDPOINT + BASE_PATH + WORKSPACE_ID + "/not/existing/path",
                                           new ByteArrayInputStream(new byte[0]),
                                           0,
                                           "GET",
                                           defaultHeaders);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), 404);
    }

    @Test
    public void shouldRespondInternalServerErrorIfExtServerIsNotFoundInMachine() throws Exception {
        when(machineRuntimeInfo.getServers()).thenReturn(Collections.emptyMap());

        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest(DEFAULT_URL,
                                           new ByteArrayInputStream(new byte[0]),
                                           0,
                                           "GET",
                                           defaultHeaders);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), SC_BAD_GATEWAY);
        // TODO look like bug in everrest test library, if {@code HttpServletResponse#sendError(int code, String message)}
        // is used no response message in output stream can be found
//        assertEquals(mockResponse.getOutputContent(), "Request can't be forwarded to machine. No extension server found in machine");
    }

    @Test
    public void shouldCopyHeadersFromResponse() throws Exception {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Accept-Ranges", Collections.singletonList("bytes"));
        headers.put("Allow", Collections.singletonList("GET, HEAD, PUT"));
        headers.put("ETag", Collections.singletonList("xyzzy"));
        headers.put("Expires", Collections.singletonList("Thu, 01 Dec 2020 16:00:00 GMT"));
        headers.put("Last-Modified", Collections.singletonList("Tue, 15 Nov 1994 12:45:26 GMT"));
        headers.put("Retry-After", Collections.singletonList("120"));

        when(extensionApiResponse.getHeaders()).thenReturn(headers);

        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest(DEFAULT_URL,
                                           new ByteArrayInputStream(new byte[0]),
                                           0,
                                           "POST",
                                           defaultHeaders);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), 200);

        final Map<String, List<String>> actualHeaders = getHeaders(mockResponse);
        actualHeaders.remove("content-length");
        actualHeaders.remove("server");
        actualHeaders.remove("date");

        assertEqualsHeaders(actualHeaders, headers);
    }

    @Test
    public void shouldNotCopyHeadersFromResponseIfTheyAreIgnored() throws Exception {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Connection", Collections.singletonList("close"));
        headers.put("Keep-Alive", Collections.singletonList("timeout=600"));
        headers.put("Proxy-Authentication", Collections.singletonList("value"));
        headers.put("Proxy-Authorization", Collections.singletonList("username:password"));
        headers.put("TE", Collections.singletonList("value"));
        headers.put("Trailers", Collections.singletonList("Expires"));
        headers.put("Transfer-Encoding", Collections.singletonList("chunked"));
        headers.put("Upgrade", Collections.singletonList("websocket"));

        when(extensionApiResponse.getHeaders()).thenReturn(headers);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(DEFAULT_URL,
                                                                        new ByteArrayInputStream(new byte[0]),
                                                                        0,
                                                                        "POST",
                                                                        defaultHeaders);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), 200);

        final Map<String, List<String>> actualHeaders = getHeaders(mockResponse);
        actualHeaders.remove("server");
        actualHeaders.remove("date");

        assertTrue(actualHeaders.isEmpty());
    }

    @Test
    public void shouldCopyHeadersFromRequest() throws Exception {
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Encoding", Collections.singletonList("gzip"));
        headers.put("Content-Language", Collections.singletonList("mi, en"));
        headers.put("Content-Type", Collections.singletonList("text/html; charset=ISO-8859-4"));
        headers.put("Date", Collections.singletonList("Tue, 15 Nov 1994 08:12:31 GMT"));
        headers.put("From", Collections.singletonList("webmaster@w3.org"));
        headers.put("Accept", Collections.singletonList("*/*"));
        headers.put("Accept-Charset", Collections.singletonList("iso-8859-5, unicode-1-1;q=0.8"));
        headers.put("Accept-Encoding", Collections.singletonList("compress, gzip"));
        headers.put("Accept-Language", Collections.singletonList("da, en-gb;q=0.8, en;q=0.7"));
        headers.put("Referer", Collections.singletonList("http://www.w3.org/hypertext/DataSources/Overview.html"));
        headers.put("Max-Forwards", Collections.singletonList("5"));
        headers.put("If-Modified-Since", Collections.singletonList("Sat, 29 Oct 2016 19:43:31 GMT"));
        headers.put("If-Match", Collections.singletonList("xyzzy"));
        headers.put("Host", Collections.singletonList("www.w3.org"));
        headers.put("User-Agent", Collections
                .singletonList("curl/7.22.0 (x86_64-pc-linux-gnu) libcurl/7.22.0 OpenSSL/1.0.1 zlib/1.2.3.4 libidn/1.23 librtmp/2.3"));
        headers.put("Connection", Collections.singletonList("close"));
        headers.put("Content-Length", Collections.singletonList("0"));
        headers.put("X-Requested-With", Collections.singletonList("XMLHttpRequest"));
        headers.put("Cookie", Collections.singletonList("JSESSIONID=D06F9296FE0D3A48519836666E668893; logged_in=true"));

        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest(DEFAULT_URL,
                                           new ByteArrayInputStream(new byte[0]),
                                           0,
                                           "POST",
                                           headers);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), 200);

        // should not be copied to headers of request to extension API
        headers.remove("Connection");
        // proxy will set it separately
        extensionApiRequest.headers.remove("Connection");

        // fixme jetty return 127.0.0.1:jettyPort
        // I suppose we need add X-Forwarded-* header support to jetty conf to fix it
        headers.remove("Host");
        extensionApiRequest.headers.remove("Host");

        extensionApiRequest.headers.remove("X-Forwarded-Host");

        assertEqualsHeaders(extensionApiRequest.headers, headers);
    }

    @Test
    public void shouldNotCopyHeadersFromRequestIfTheyAreIgnored() throws Exception {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Host", Collections.singletonList("www.w3.org"));
        //ignored headers
        headers.put(null, Collections.singletonList("HTTP/1.1 200 OK"));
        headers.put("Connection", Collections.singletonList("close"));
        headers.put("Keep-Alive", Collections.singletonList("timeout=600"));
        headers.put("Proxy-Authentication", Collections.singletonList("value"));
        headers.put("Proxy-Authorization", Collections.singletonList("username:password"));
        headers.put("TE", Collections.singletonList("value"));
        headers.put("Trailers", Collections.singletonList("Expires"));
        headers.put("Transfer-Encoding", Collections.singletonList("chunked"));
        headers.put("Upgrade", Collections.singletonList("websocket"));

        Map<String, List<String>> requiredHeaders = new HashMap<>();
        requiredHeaders.put("Accept", Collections.singletonList("application/json"));
        requiredHeaders.put("X-Forwarded-Host", Collections.singletonList("www.w3.org"));
        requiredHeaders.put("User-Agent", Collections.singletonList("Java"));
        requiredHeaders.put("Connection", Collections.singletonList("keep-alive"));

        headers.putAll(requiredHeaders);

        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest(DEFAULT_URL,
                                           new ByteArrayInputStream(new byte[0]),
                                           0,
                                           "GET",
                                           headers);

        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), 200);
        // should not be copied to headers of request to extension API
        extensionApiRequest.headers.remove("Host");

        assertEqualsHeaders(extensionApiRequest.headers, requiredHeaders);
    }

    @Test
    public void shouldRespondInternalServerErrorIfMachineExceptionOccursOnMachineRetrieval() throws Exception {
        when(machineManager.getDevMachine(WORKSPACE_ID))
                .thenThrow(new MachineException("Machine retrieval failed."));

        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest(DEFAULT_URL,
                                           new ByteArrayInputStream(new byte[0]),
                                           0,
                                           "GET",
                                           defaultHeaders);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), SC_BAD_GATEWAY, mockResponse.getOutputContent());
    }

    @Test
    public void shouldRespondInternalServerErrorIfNoDevMachineFoundForWorkspace() throws Exception {
        when(machineManager.getDevMachine(WORKSPACE_ID))
                .thenThrow(new NotFoundException("No running dev machine found in workspace " + WORKSPACE_ID));

        MockHttpServletRequest mockRequest =
                new MockHttpServletRequest(DEFAULT_URL,
                                           new ByteArrayInputStream(new byte[0]),
                                           0,
                                           "GET",
                                           defaultHeaders);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        proxyServlet.service(mockRequest, mockResponse);

        assertEquals(mockResponse.getStatus(), SC_SERVICE_UNAVAILABLE, mockResponse.getOutputContent());
    }

    /**
     * Header name is case insensitive in accordance to spec. So we can't compare arrays via equals method.
     *
     * @param actual
     *         map of actual headers
     * @param expected
     *         map of expected headers
     */
    private void assertEqualsHeaders(Map<String, List<String>> actual, Map<String, List<String>> expected) {
        assertEquals(actual.size(), expected.size());

        final Set<String> expectedHeadersKeys = expected.keySet();

        for (Map.Entry<String, List<String>> actualHeader : actual.entrySet()) {
            List<String> expectedHeaderValues = null;
            if (expectedHeadersKeys.contains(actualHeader.getKey())) {
                expectedHeaderValues = expected.get(actualHeader.getKey());
            } else {
                for (String expectedHeaderKey : expectedHeadersKeys) {
                    if (expectedHeaderKey.equalsIgnoreCase(actualHeader.getKey())) {
                        expectedHeaderValues = expected.get(expectedHeaderKey);
                        break;
                    }
                }
            }
            if (expectedHeaderValues != null) {
                assertEquals(actualHeader.getValue(), expectedHeaderValues);
            } else {
                fail("Expected headers don't contain header:" + actualHeader.getKey());
            }
        }
    }

    private Map<String, List<String>> getHeaders(HttpServletRequest req) {
        final Map<String, List<String>> result = new HashMap<>();

        final Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            result.put(headerName, new ArrayList<>());

            final Enumeration<String> headerValues = req.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                result.get(headerName).add(headerValue);
            }
        }

        return result;
    }

    private Map<String, List<String>> getHeaders(HttpServletResponse resp) {
        final Map<String, List<String>> result = new HashMap<>();

        for (String headerName : resp.getHeaderNames()) {
            result.put(headerName, new ArrayList<>(resp.getHeaders(headerName)));
        }

        return result;
    }

    private class ExtensionApiHandler extends AbstractHandler {
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            // to be able validate request received by extension API server
            extensionApiRequest.headers = getHeaders(request);
            extensionApiRequest.method = request.getMethod();
            extensionApiRequest.uri = request.getRequestURI();
            extensionApiRequest.query = request.getQueryString();
            try (InputStream is = request.getInputStream()) {
                extensionApiRequest.entity = IoUtil.readStream(is);
            }

            response.setStatus(extensionApiResponse.getStatus());
            for (Map.Entry<String, List<String>> header : extensionApiResponse.getHeaders().entrySet()) {
                for (String headerValue : header.getValue()) {
                    response.addHeader(header.getKey(), headerValue);
                }
            }
            response.getWriter().print(extensionApiResponse.getEntity());

            baseRequest.setHandled(true);
        }
    }

    private static class ExtensionApiResponse {
        int getStatus() {
            return 200;
        }

        String getEntity() {
            return DEFAULT_RESPONSE_ENTITY;
        }

        Map<String, List<String>> getHeaders() {
            return Collections.singletonMap("Content-type", Collections.singletonList("application/json"));
        }
    }

    private static class ExtensionApiRequest {
        Map<String, List<String>> headers;
        String                    entity;
        String                    method;
        String                    uri;
        String                    query;
    }
}
