/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.pullrequest.server;

import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.common.collect.ImmutableMap;

import static org.eclipse.che.plugin.pullrequest.shared.Constants.CHE_PULLREQUEST_GENERATE__REVIEW__FACTORY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/pullrequestwf")
@Api(value = "/pullrequestwf", //
     description = "Pull Request Workflow REST API")
public class PullRequestWorkflowService {

    protected boolean generateReviewFactory;

    @Inject
    public PullRequestWorkflowService(@Named(CHE_PULLREQUEST_GENERATE__REVIEW__FACTORY) boolean generateReviewFactory) {
        this.generateReviewFactory = generateReviewFactory;
    }

    @GET
    @Path("settings")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get pullrequest workflow server configuration values")
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains server settings")})
    public Map<String, String> getSettings() {
        return ImmutableMap.of(CHE_PULLREQUEST_GENERATE__REVIEW__FACTORY, Boolean.toString(generateReviewFactory));
    }
}
