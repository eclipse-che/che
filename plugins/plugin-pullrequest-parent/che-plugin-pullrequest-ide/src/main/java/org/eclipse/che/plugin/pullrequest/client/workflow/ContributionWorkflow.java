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
package org.eclipse.che.plugin.pullrequest.client.workflow;

/**
 * Defines contribution workflow.
 *
 * <p>The contribution workflow consists of 3 main steps: initialization, pull request creation,
 * pull request update. According to these 3 steps implementation should provide steps chains based
 * on specific VCS Hosting service.
 *
 * <p>Binding example:
 *
 * <pre>{@code
 * final GinMapBinder<String, ContributionWorkflow> strategyBinder
 *             = GinMapBinder.newMapBinder(binder(), String.class, ContributionWorkflow.class);
 * strategyBinder.addBinding("my-vcs-service").to(MyVcsServiceContributionWorkflow.class);
 * }</pre>
 *
 * @author Yevhenii Voevodin
 */
public interface ContributionWorkflow {

  /** Returns the steps chain which should be executed when plugin initializes. */
  StepsChain initChain(Context context);

  /** Returns the steps chain which should be executed when pull request should be created. */
  StepsChain creationChain(Context context);

  /** Returns the steps chain which should be executed for the pull request updatePullRequest. */
  StepsChain updateChain(Context context);
}
