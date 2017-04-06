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


import org.eclipse.che.api.promises.client.Promise;

/**
 * Access to the server configuration related to the Pull Request workflow and panel in Che.
 */
public interface PullRequestWorkflowServiceClient {
    /**
     * Ask the server master pullrequest workflow service if it should generate a review factory url after the creation of a PR.
     */
    Promise<Boolean> shouldGenerateReviewUrl();
}
