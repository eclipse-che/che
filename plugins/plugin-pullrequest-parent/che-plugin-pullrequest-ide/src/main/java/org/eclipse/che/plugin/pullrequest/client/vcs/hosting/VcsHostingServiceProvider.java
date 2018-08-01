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
package org.eclipse.che.plugin.pullrequest.client.vcs.hosting;

import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.plugin.pullrequest.client.vcs.VcsService;
import org.eclipse.che.plugin.pullrequest.client.vcs.VcsServiceProvider;

/**
 * Provider for the {@link VcsHostingService}.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
public class VcsHostingServiceProvider {
  private static final String ORIGIN_REMOTE_NAME = "origin";

  private final VcsServiceProvider vcsServiceProvider;
  private final Set<VcsHostingService> vcsHostingServices;

  @Inject
  public VcsHostingServiceProvider(
      final VcsServiceProvider vcsServiceProvider,
      final Set<VcsHostingService> vcsHostingServices) {
    this.vcsServiceProvider = vcsServiceProvider;
    this.vcsHostingServices = vcsHostingServices;
  }

  /**
   * Returns the dedicated {@link VcsHostingService} implementation for the {@link
   * #ORIGIN_REMOTE_NAME origin} remote.
   *
   * @param project project used to find origin remote and extract VCS hosting service
   */
  public Promise<VcsHostingService> getVcsHostingService(final ProjectConfig project) {
    if (project == null) {
      return Promises.reject(
          JsPromiseError.create(new NoVcsHostingServiceImplementationException()));
    }
    final VcsService vcsService = vcsServiceProvider.getVcsService(project);
    if (vcsService == null) {
      return Promises.reject(
          JsPromiseError.create(new NoVcsHostingServiceImplementationException()));
    }
    return vcsService
        .listRemotes(project)
        .then(
            new Function<List<Remote>, VcsHostingService>() {
              @Override
              public VcsHostingService apply(List<Remote> remotes) throws FunctionException {
                for (Remote remote : remotes) {
                  if (ORIGIN_REMOTE_NAME.equals(remote.getName())) {
                    for (final VcsHostingService hostingService : vcsHostingServices) {
                      if (hostingService.isHostRemoteUrl(remote.getUrl())) {
                        return hostingService.init(remote.getUrl());
                      }
                    }
                  }
                }
                throw new FunctionException(new NoVcsHostingServiceImplementationException());
              }
            });
  }
}
