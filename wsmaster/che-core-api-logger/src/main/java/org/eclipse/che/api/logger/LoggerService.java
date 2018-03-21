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
package org.eclipse.che.api.logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.logger.shared.dto.LoggerDto;
import org.slf4j.LoggerFactory;

/**
 * Defines Logger REST API. It allows to manage the loggers (with log level) dynamically.
 *
 * @author Florent Benoit
 */
@Api(value = "/logger", description = "Logger REST API")
@Path("/logger")
public class LoggerService extends Service {

  @GET
  @Path("/{name}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get the logger level for the given logger")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The response contains requested logger entity"),
    @ApiResponse(code = 404, message = "The logger with specified name does not exist")
  })
  public LoggerDto getLoggerByName(@ApiParam(value = "logger name") @PathParam("name") String name)
      throws NotFoundException {
    return asDto(getLogger(name));
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Get loggers which are configured",
    notes = "This operation can be performed only by authorized user",
    response = LoggerDto.class,
    responseContainer = "List"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "The loggers successfully fetched"),
  })
  public List<LoggerDto> getLoggers(
      @ApiParam("The number of the items to skip") @DefaultValue("0") @QueryParam("skipCount")
          Integer skipCount,
      @ApiParam("The limit of the items in the response, default is 30")
          @DefaultValue("30")
          @QueryParam("maxItems")
          Integer maxItems) {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    return loggerContext
        .getLoggerList()
        .stream()
        .filter(log -> log.getLevel() != null || log.iteratorForAppenders().hasNext())
        .skip(skipCount)
        .limit(maxItems)
        .map(this::asDto)
        .collect(Collectors.toList());
  }

  protected LoggerDto asDto(final Logger log) {
    return newDto(LoggerDto.class).withName(log.getName()).withLevel(log.getLevel().levelStr);
  }

  @PUT
  @Path("/{name}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Update the logger level")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The logger successfully updated"),
  })
  public LoggerDto updateLogger(
      @ApiParam(value = "logger name") @PathParam("name") String name, LoggerDto update)
      throws NotFoundException {
    Logger logger = getLogger(name);
    logger.setLevel(Level.toLevel(update.getLevel()));
    return asDto(logger);
  }

  @POST
  @Path("/{name}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Create a new logger level")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The logger successfully created"),
  })
  public LoggerDto createLogger(
      @ApiParam(value = "logger name") @PathParam("name") String name, LoggerDto createdLogger)
      throws NotFoundException {
    Logger logger = getLogger(name, false);
    logger.setLevel(Level.toLevel(createdLogger.getLevel()));
    return asDto(logger);
  }

  /** Check if given logger exists */
  protected Logger getLogger(String name) throws NotFoundException {
    return getLogger(name, true);
  }

  /**
   * Gets a logger, if checkLevel is true and if logger has no level defined it will return a
   * NameNotFound exception
   */
  protected Logger getLogger(String name, boolean checkLevel) throws NotFoundException {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    Logger log = loggerContext.getLogger(name);
    if (checkLevel && log.getLevel() == null) {
      throw new NotFoundException("The logger with name " + name + " is not existing.");
    }
    return log;
  }
}
