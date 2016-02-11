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
package org.eclipse.che.api.analytics;


import org.eclipse.che.api.analytics.logger.EventLogger;
import org.eclipse.che.api.analytics.shared.dto.EventParameters;
import org.eclipse.che.api.analytics.shared.dto.MetricInfoDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricInfoListDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricValueDTO;
import org.eclipse.che.api.analytics.shared.dto.MetricValueListDTO;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.dto.server.JsonArrayImpl;
import org.eclipse.che.dto.server.JsonStringMapImpl;
import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Service is responsible for processing REST requests for analytics data.
 *
 * @author Anatoliy Bazko
 */
@Api(value = "/analytics",
     description = "Analytics manager")
@Path("/analytics")
public class AnalyticsService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsService.class);

    private final MetricHandler metricHandler;
    private final EventLogger   eventLogger;

    @Inject
    public AnalyticsService(MetricHandler metricHandler, EventLogger eventLogger) {
        this.metricHandler = metricHandler;
        this.eventLogger = eventLogger;
    }

    @ApiOperation(value = "Get metric by name",
                  notes = "Get metric by name. Additional display filters can be used as query parameters.",
                  response = MetricValueDTO.class,
                  position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Unexpected error occurred. Can't get value for metric")})
    @GenerateLink(rel = "metric value")
    @GET
    @Path("/metric/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getValue(@ApiParam(value = "Metric name", required = true)
                             @PathParam("name") String metricName,
                             @ApiParam(value = "Page number. Relevant only for LONG data type")
                             @QueryParam("page") String page,
                             @ApiParam(value = "Number of results per page.")
                             @QueryParam("per_page") String perPage,
                             @Context UriInfo uriInfo) throws ServerException {
        try {
            Map<String, String> metricContext = extractContext(uriInfo,
                                                               page,
                                                               perPage);
            MetricValueDTO value = metricHandler.getValue(metricName, metricContext, uriInfo);
            return Response.status(Response.Status.OK).entity(value).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("Unexpected error occurred. Can't get value for metric " + metricName);
        }
    }

    @ApiOperation(value = "Get list of metric values",
                  notes = "Get list of metric values",
                  response = MetricInfoListDTO.class,
                  position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Unexpected error occurred. Can't get value for metric")})

    @GenerateLink(rel = "list of metric values")
    @POST
    @Path("/metric/{name}/list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getListValues(@ApiParam(value = "Metric name", required = true)
                                  @PathParam("name") String metricName,
                                  @Context UriInfo uriInfo,
                                  @ApiParam(value = "Search filter", required = true)
                                  List<Map<String, String>> parameters) throws ServerException {
        try {
            Map<String, String> metricContext = extractContext(uriInfo);
            MetricValueListDTO list = metricHandler.getListValues(metricName, parameters, metricContext, uriInfo);
            return Response.status(Response.Status.OK).entity(list).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("Unexpected error occurred. Can't get list of metrics");
        }
    }

    @GenerateLink(rel = "metric value")
    @POST
    @Path("/metric/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getValueByJson(Map<String, String> parameters,
                                   @PathParam("name") String metricName,
                                   @QueryParam("page") String page,
                                   @QueryParam("per_page") String perPage,
                                   @Context UriInfo uriInfo) throws ServerException {
        try {
            Map<String, String> metricContext = extractContext(uriInfo,
                                                               page,
                                                               perPage);
            MetricValueDTO value = metricHandler.getValueByJson(metricName,
                                                                new JsonStringMapImpl<>(parameters),
                                                                metricContext,
                                                                uriInfo);
            return Response.status(Response.Status.OK).entity(value).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("Unexpected error occurred. Can't get value for metric " + metricName);
        }
    }

    @ApiOperation(value = "Get public metric",
                  notes = "Get public metric (Factory)",
                  response = MetricValueDTO.class,
                  position = 4)
    @ApiResponses(value = {
                  @ApiResponse(code = 200, message = "OK"),
                  @ApiResponse(code = 404, message  ="Not Found"),
                  @ApiResponse(code = 500, message = "Unexpected error occurred. Can't get value for metric")})
    @GenerateLink(rel = "metric value")
    @GET
    @Path("/public-metric/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicValue(@ApiParam(value = "Metric name", required = true, allowableValues = "factory_used")
                                   @PathParam("name") String metricName,
                                   @ApiParam(value = "Page number")
                                   @QueryParam("page") String page,
                                   @ApiParam(value = "Resylts per page")
                                   @QueryParam("per_page") String perPage,
                                   @Context UriInfo uriInfo) throws ServerException {
        try {
            Map<String, String> metricContext = extractContext(uriInfo,
                                                               page,
                                                               perPage);
            MetricValueDTO value = metricHandler.getPublicValue(metricName, metricContext, uriInfo);
            return Response.status(Response.Status.OK).entity(value).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("Unexpected error occurred. Can't get value for metric " + metricName);
        }
    }

    @ApiOperation(value = "Get metric value for current user",
                  notes = "Get metric value for current user",
                  response = MetricValueListDTO.class,
                  position = 5)
    @ApiResponses(value = {
                  @ApiResponse(code = 200, message = "OK"),
                  @ApiResponse(code = 404, message  ="Not Found"),
                  @ApiResponse(code = 500, message = "Unexpected error occurred. Can't get value for metric")})
    @GenerateLink(rel = "list of metric values")
    @POST
    @Path("/metric/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getUserValues(@ApiParam(value = "Metric names", required = true)
                                      List<String> metricNames, @Context UriInfo uriInfo) throws ServerException {
        try {
            Map<String, String> metricContext = extractContext(uriInfo);
            MetricValueListDTO list = metricHandler.getUserValues(new JsonArrayImpl<>(metricNames),
                                                                  metricContext,
                                                                  uriInfo);
            return Response.status(Response.Status.OK).entity(list).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("Unexpected error occurred. Can't get values of metrics");
        }
    }

    @ApiOperation(value = "Get metric info",
                  notes = "Get information about specified metric",
                  response = MetricInfoDTO.class,
                  position = 6)
    @ApiResponses(value = {
                  @ApiResponse(code = 200, message = "OK"),
                  @ApiResponse(code = 404, message  ="Not Found"),
                  @ApiResponse(code = 500, message = "Unexpected error occurred. Can't get info for metric")})
    @GenerateLink(rel = "metric info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/metricinfo/{name}")
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getInfo(@ApiParam(value = "Metric name", required = true)
                                @PathParam("name") String metricName, @Context UriInfo uriInfo) throws ServerException {
        try {
            MetricInfoDTO metricInfoDTO = metricHandler.getInfo(metricName, uriInfo);
            return Response.status(Response.Status.OK).entity(metricInfoDTO).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("Unexpected error occurred. Can't get info for metric " + metricName);
        }
    }

    @ApiOperation(value = "Get info on all available metric",
                  notes = "Get info on all available metric",
                  response = MetricInfoListDTO.class,
                  position = 7)
    @ApiResponses(value = {
                  @ApiResponse(code = 200, message = "OK"),
                  @ApiResponse(code = 404, message  ="Not Found"),
                  @ApiResponse(code = 500, message = "Unexpected error occurred. Can't get info for metric")})
    @GenerateLink(rel = "all metric info")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/metricinfo")
    @RolesAllowed({"user", "system/admin", "system/manager"})
    public Response getAllInfo(@Context UriInfo uriInfo) throws ServerException {
        try {
            MetricInfoListDTO metricInfoListDTO = metricHandler.getAllInfo(uriInfo);
            return Response.status(Response.Status.OK).entity(metricInfoListDTO).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("Unexpected error occurred. Can't get metric info");
        }
    }

    @GenerateLink(rel = "log analytics event")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("log/{event}")
    @RolesAllowed({"user", "temp_user", "system/admin", "system/manager"})
    public Response logEvent(@PathParam("event") String event,
                             @Context SecurityContext securityContext,
                             EventParameters parameters) throws ServerException {
        try {
            Map<String, String> params;
            if (parameters == null) {
                params = new HashMap<>();
            } else {
                params = parameters.getParams();
            }

            if (!params.containsKey(EventLogger.USER_PARAM) && securityContext != null && securityContext.getUserPrincipal() != null) {
                params.put(EventLogger.USER_PARAM, securityContext.getUserPrincipal().getName());
            }

            eventLogger.log(event, params);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            throw new ServerException("Unexpected error occurred. Can't log event " + event);
        }
    }

    private Map<String, String> extractContext(UriInfo info,
                                               String page,
                                               String perPage) {

        MultivaluedMap<String, String> parameters = info.getQueryParameters();
        Map<String, String> context = new HashMap<>(parameters.size());

        for (String key : parameters.keySet()) {
            context.put(key.toUpperCase(), parameters.getFirst(key));
        }

        if (page != null && perPage != null) {
            context.put("PAGE", page);
            context.put("PER_PAGE", perPage);
        }

        return context;
    }

    private Map<String, String> extractContext(UriInfo info) {
        return extractContext(info, null, null);
    }
}
