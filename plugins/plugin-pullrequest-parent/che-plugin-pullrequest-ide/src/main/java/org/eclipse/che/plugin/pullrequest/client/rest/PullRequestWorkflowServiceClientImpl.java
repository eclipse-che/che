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
package org.eclipse.che.plugin.pullrequest.client.rest;

import javax.inject.Inject;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.plugin.pullrequest.shared.dto.ShouldGenerateReviewUrl;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

public class PullRequestWorkflowServiceClientImpl implements PullRequestWorkflowServiceClient {
    private final AsyncRequestFactory    asyncRequestFactory;
    private final String                 baseHttpUrl;
    private final DtoUnmarshallerFactory unmarshallerFactory;

    @Inject
    public PullRequestWorkflowServiceClientImpl(AppContext appContext,//
                                                AsyncRequestFactory asyncRequestFactory,//
                                                DtoUnmarshallerFactory unmarshallerFactory) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.baseHttpUrl = appContext.getMasterEndpoint() + "/pullrequest";
        this.unmarshallerFactory = unmarshallerFactory;
    }

    @Override
    public Promise<Boolean> shouldGenerateReviewUrl() {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + "/reviewurl") //
                                  .header(ACCEPT, APPLICATION_JSON) //
                                  .send(unmarshallerFactory.newUnmarshaller(ShouldGenerateReviewUrl.class)) //
                                  .then((ShouldGenerateReviewUrl generateReviewUrl) -> generateReviewUrl.isActive());
    }
}
