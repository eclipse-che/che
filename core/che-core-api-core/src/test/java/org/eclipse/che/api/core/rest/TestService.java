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
package org.eclipse.che.api.core.rest;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.server.JsonArrayImpl;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * Test service class, used in {@link DefaultHttpJsonRequestTest}.
 *
 * @author Yevhenii Voevodin
 */
@Path("/test")
public class TestService extends Service {

    public static final String JSON_OBJECT = new JsonArrayImpl<>(singletonList("element")).toJson();

    @GET
    @Path("/{response-code}/response-code-test")
    public Response getRequestedResponseCode(@PathParam("response-code") int responseCode) {
        return Response.status(responseCode)
                       .entity(DtoFactory.newDto(ServiceError.class).withMessage("response code test method"))
                       .build();
    }

    @GET
    @Path("/text-plain")
    @Produces(TEXT_PLAIN)
    public String getTextPlain() {
        return "this is text/plain message";
    }

    @GET
    @Path("/application-json")
    @Produces(APPLICATION_JSON)
    public String getJsonObject() {
        return JSON_OBJECT;
    }

    @POST
    @Path("/application-json")
    @Produces(APPLICATION_JSON)
    public List<Link> receiveJsonObject(List<Link> elements) {
        return elements;
    }

    @PUT
    @Path("/query-parameters")
    @Produces(APPLICATION_JSON)
    public Map<String, String> queryParamsTest(@QueryParam("param1") String qp1, @QueryParam("param2") String qp2) {
        final Map<String, String> map = new HashMap<>();
        map.put("param1", qp1);
        map.put("param2", qp2);
        return map;
    }

    @PUT
    @Path("/multi-query-parameters")
    @Produces(APPLICATION_JSON)
    public Map<String, List<String>> queryParamsTest(@QueryParam("param1") List<String> values) {
        final Map<String, List<String>> map = new HashMap<>();
        map.put("param1", values);
        return map;
    }

    @POST
    @Path("/token")
    public void checkAuthorization(@HeaderParam(HttpHeaders.AUTHORIZATION) String token) throws UnauthorizedException {
        if (!EnvironmentContext.getCurrent().getSubject().getToken().equals(token)) {
            throw new UnauthorizedException("Token '" + token + "' it is different from token in EnvironmentContext");
        }
    }

    @GET
    @Path("/decode")
    @Produces(APPLICATION_JSON)
    public String getUriInfo(@QueryParam("query") String query,
                             @Context UriInfo uriInfo) {
        return URLDecoder.decode(uriInfo.getRequestUri().toString());
    }

    @GET
    @Path("/paging/{value}")
    @Produces(APPLICATION_JSON)
    public Response getStringList(@PathParam("value") String value, @QueryParam("query-param") String param) {
        final Page<String> page = new Page<>(asList("item3", "item4", "item5"), 3, 3, 7);

        return Response.ok()
                       .entity(page.getItems())
                       .header("Link", createLinkHeader(page, "getStringList", singletonMap("query-param", param), value))
                       .build();
    }
}
