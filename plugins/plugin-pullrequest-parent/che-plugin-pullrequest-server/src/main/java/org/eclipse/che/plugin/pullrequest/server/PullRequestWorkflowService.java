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


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.che.plugin.pullrequest.shared.dto.ShouldGenerateReviewUrl;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.plugin.pullrequest.shared.Constants.CHE_PR_GENERATE_REVIEW_FACTORY_PROP;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Pull Request Workflow REST API. It provides server defined configuration of the Pull Request Workflow feature.
 */
@Path("/pullrequest")
@Api(value = "/pullrequest", //
     description = "Pull Request Workflow REST API. It provides server defined configuration of the Pull Request Workflow feature")
public class PullRequestWorkflowService {

    protected boolean generateReviewFactory;

    @Inject
    public PullRequestWorkflowService(@Named(CHE_PR_GENERATE_REVIEW_FACTORY_PROP) boolean generateReviewFactory) {
        this.generateReviewFactory = generateReviewFactory;
    }

    @GET
    @Path("reviewurl")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Enable generation of a review factory url of a Pull Request created with the Che PR panel")
    @ApiResponses({@ApiResponse(code = 200, message = "Server contains configuration about generation of review factory URL")})
    public ShouldGenerateReviewUrl shouldGenerateReviewFactory() {
        return newDto(ShouldGenerateReviewUrl.class).withActive(generateReviewFactory);
    }
}
