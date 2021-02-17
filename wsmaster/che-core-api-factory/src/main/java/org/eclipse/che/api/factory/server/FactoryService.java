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
package org.eclipse.che.api.factory.server;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.factory.server.FactoryLinksHelper.createLinks;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.factory.shared.dto.FactoryMetaDto;
import org.eclipse.che.api.user.server.UserManager;

/**
 * Defines Factory REST API.
 *
 * @author Anton Korneta
 * @author Florent Benoit
 */
@Api(value = "/factory", description = "Factory manager")
@Path("/factory")
public class FactoryService extends Service {

  /** Error message if there is no plugged resolver. */
  public static final String FACTORY_NOT_RESOLVABLE =
      "Cannot build factory with any of the provided parameters. Please check parameters correctness, and resend query.";

  /** Validate query parameter. If true, factory will be validated */
  public static final String VALIDATE_QUERY_PARAMETER = "validate";

  private final UserManager userManager;
  private final FactoryAcceptValidator acceptValidator;
  private final FactoryParametersResolverHolder factoryParametersResolverHolder;

  @Inject
  public FactoryService(
      UserManager userManager,
      FactoryAcceptValidator acceptValidator,
      FactoryParametersResolverHolder factoryParametersResolverHolder) {
    this.userManager = userManager;
    this.acceptValidator = acceptValidator;
    this.factoryParametersResolverHolder = factoryParametersResolverHolder;
  }

  @POST
  @Path("/resolver")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @ApiOperation(
      value = "Create factory by providing map of parameters",
      notes = "Get JSON with factory information")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Factory successfully built from parameters"),
    @ApiResponse(code = 400, message = "Missed required parameters, failed to validate factory"),
    @ApiResponse(code = 500, message = "Internal server error")
  })
  public FactoryMetaDto resolveFactory(
      @ApiParam(value = "Parameters provided to create factories") Map<String, String> parameters,
      @ApiParam(
              value = "Whether or not to validate values like it is done when accepting a Factory",
              allowableValues = "true,false",
              defaultValue = "false")
          @DefaultValue("false")
          @QueryParam(VALIDATE_QUERY_PARAMETER)
          Boolean validate)
      throws ApiException {

    // check parameter
    requiredNotNull(parameters, "Factory build parameters");

    // search matching resolver and create factory from matching resolver
    FactoryMetaDto resolvedFactory =
        factoryParametersResolverHolder
            .getFactoryParametersResolver(parameters)
            .createFactory(parameters);
    if (resolvedFactory == null) {
      throw new BadRequestException(FACTORY_NOT_RESOLVABLE);
    }
    if (validate) {
      acceptValidator.validateOnAccept(resolvedFactory);
    }

    resolvedFactory = injectLinks(resolvedFactory);

    return resolvedFactory;
  }

  /** Injects factory links. If factory is named then accept named link will be injected. */
  private FactoryMetaDto injectLinks(FactoryMetaDto factory) {
    String username = null;
    if (factory.getCreator() != null && factory.getCreator().getUserId() != null) {
      try {
        username = userManager.getById(factory.getCreator().getUserId()).getName();
      } catch (ApiException ignored) {
        // when impossible to get username then named factory link won't be injected
      }
    }
    return factory.withLinks(createLinks(factory, getServiceContext(), username));
  }

  /** Usage of a dedicated class to manage the optional service-specific resolvers */
  protected static class FactoryParametersResolverHolder {

    /** Optional inject for the resolvers. */
    @com.google.inject.Inject(optional = true)
    @SuppressWarnings("unused")
    private Set<FactoryParametersResolver> specificFactoryParametersResolvers;

    @Inject private DefaultFactoryParameterResolver defaultFactoryResolver;

    /**
     * Provides a suitable resolver for the given parameters. If there is no at least one resolver
     * able to process parameters,then {@link BadRequestException} will be thrown
     *
     * @return suitable service-specific resolver or default one
     */
    public FactoryParametersResolver getFactoryParametersResolver(Map<String, String> parameters)
        throws BadRequestException {
      if (specificFactoryParametersResolvers != null) {
        for (FactoryParametersResolver factoryParametersResolver :
            specificFactoryParametersResolvers) {
          if (factoryParametersResolver.accept(parameters)) {
            return factoryParametersResolver;
          }
        }
      }
      if (defaultFactoryResolver.accept(parameters)) {
        return defaultFactoryResolver;
      } else {
        throw new BadRequestException(FACTORY_NOT_RESOLVABLE);
      }
    }
  }

  /**
   * Checks object reference is not {@code null}
   *
   * @param object object reference to check
   * @param subject used as subject of exception message "{subject} required"
   * @throws BadRequestException when object reference is {@code null}
   */
  private static void requiredNotNull(Object object, String subject) throws BadRequestException {
    if (object == null) {
      throw new BadRequestException(subject + " required");
    }
  }

  /**
   * Checks that expression is true, throws {@link BadRequestException} otherwise.
   *
   * <p>Exception uses error message built from error message template and error message parameters.
   */
  private static void checkArgument(boolean expression, String errorMessage)
      throws BadRequestException {
    if (!expression) {
      throw new BadRequestException(errorMessage);
    }
  }
}
