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
package org.eclipse.che.multiuser.resource.api.license;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.multiuser.resource.api.DtoConverter.asDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.shared.dto.AccountLicenseDto;

/**
 * Defines Account License REST API.
 *
 * @author Sergii Leschenko
 */
@Api(value = "license-account", description = "Account License REST API")
@Path("/license/account")
public class AccountLicenseService {
  private AccountLicenseManager accountAccountLicenseManager;

  @Inject
  public AccountLicenseService(AccountLicenseManager accountAccountLicenseManager) {
    this.accountAccountLicenseManager = accountAccountLicenseManager;
  }

  @GET
  @Path("/{accountId}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Get license for given account", response = AccountLicenseDto.class)
  @ApiResponses({
    @ApiResponse(code = 200, message = "The license successfully fetched"),
    @ApiResponse(code = 404, message = "Account with specified id was not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public AccountLicenseDto getLicense(
      @ApiParam("Account id") @PathParam("accountId") String accountId)
      throws NotFoundException, ServerException {
    return asDto(accountAccountLicenseManager.getByAccount(accountId));
  }
}
