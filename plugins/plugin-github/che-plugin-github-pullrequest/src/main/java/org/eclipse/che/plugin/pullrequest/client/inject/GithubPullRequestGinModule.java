/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.pullrequest.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.pullrequest.client.GitHubContributionWorkflow;
import org.eclipse.che.plugin.pullrequest.client.GitHubHostingService;
import org.eclipse.che.plugin.pullrequest.client.GithubStagesProvider;
import org.eclipse.che.plugin.pullrequest.client.parts.contribute.StagesProvider;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import org.eclipse.che.plugin.pullrequest.client.workflow.ContributionWorkflow;

/**
 * Gin module definition for GitHub pull request plugin.
 *
 * @author Mihail Kuznyetsov
 */
@ExtensionGinModule
public class GithubPullRequestGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    final GinMapBinder<String, ContributionWorkflow> workflowBinder =
        GinMapBinder.newMapBinder(binder(), String.class, ContributionWorkflow.class);
    workflowBinder
        .addBinding(GitHubHostingService.SERVICE_NAME)
        .to(GitHubContributionWorkflow.class);

    final GinMapBinder<String, StagesProvider> stagesProvider =
        GinMapBinder.newMapBinder(binder(), String.class, StagesProvider.class);
    stagesProvider.addBinding(GitHubHostingService.SERVICE_NAME).to(GithubStagesProvider.class);

    final GinMultibinder<VcsHostingService> vcsHostingService =
        GinMultibinder.newSetBinder(binder(), VcsHostingService.class);
    vcsHostingService.addBinding().to(GitHubHostingService.class);
  }
}
