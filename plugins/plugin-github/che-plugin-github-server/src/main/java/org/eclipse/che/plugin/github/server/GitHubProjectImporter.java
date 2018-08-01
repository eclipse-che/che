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
package org.eclipse.che.plugin.github.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitProjectImporter;

/** @author Roman Nikitenko */
@Singleton
public class GitHubProjectImporter extends GitProjectImporter {

  @Inject
  public GitHubProjectImporter(
      GitConnectionFactory gitConnectionFactory,
      EventService eventService,
      FsManager fsManager,
      PathTransformer pathTransformer) {
    super(gitConnectionFactory, eventService, fsManager, pathTransformer);
  }

  @Override
  public String getId() {
    return "github";
  }

  @Override
  public String getDescription() {
    return "Import project from github.";
  }
}
