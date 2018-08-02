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
package org.eclipse.che.plugin.pullrequest.client.parts.contribute;

import java.util.List;
import java.util.Set;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;

/**
 * Provider should be implemented one per {@link VcsHostingService}.
 *
 * <p>Binding example:
 *
 * <pre>{@code
 * final GinMapBinder<String, StagesProvider> stagesProvider
 *         = GinMapBinder.newMapBinder(binder(),
 *                                     String.class,
 *                                     StagesProvider.class);
 * stagesProvider.addBinding(GitHubHostingService.SERVICE_NAME).to(GithubStagesProvider.class);
 * }</pre>
 *
 * @author Yevhenii Voevodin
 */
public interface StagesProvider {

  /**
   * Returns the list of stages which should be displayed when pull request update or creation
   * starts.
   *
   * @param context current execution context
   * @return the list of stages
   */
  List<String> getStages(final Context context);

  /**
   * When step is done and its class is result of this method then current stage is considered as
   * successfully done.
   *
   * @param context current execution context
   * @return react classes
   */
  Set<Class<? extends Step>> getStepDoneTypes(final Context context);

  /**
   * When step is done with an error and its class is result of this method then current stage is
   * considered as successfully done.
   *
   * @param context current execution context
   * @return error react classes
   */
  Set<Class<? extends Step>> getStepErrorTypes(final Context context);

  /**
   * Stages are shown only once and the time to show stages is defined by return type of this
   * method. If that step(which type is returned) is successfully executed then {@link
   * #getStages(Context)} method will be used to show the stages. It is needed for dynamic stages
   * list detection (e.g. when workflow configures context in create/update chains).
   *
   * @param context current execution context
   * @return returns step class after which successful execution stages should be shown
   */
  Class<? extends Step> getDisplayStagesType(final Context context);
}
