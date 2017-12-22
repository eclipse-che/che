/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.github.server.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.project.server.ProjectImporter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.github.server.GitHubDTOFactory;
import org.eclipse.che.plugin.github.server.GitHubKeyUploader;
import org.eclipse.che.plugin.github.server.GitHubProjectImporter;
import org.eclipse.che.plugin.github.server.rest.GitHubService;
import org.eclipse.che.plugin.ssh.key.script.SshKeyUploader;

/**
 * The module that contains configuration of the server side part of the GitHub extension.
 *
 * @author Andrey Plotnikov
 */
@DynaModule
public class GitHubModule extends AbstractModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    bind(GitHubDTOFactory.class);

    Multibinder<ProjectImporter> projectImporterMultibinder =
        Multibinder.newSetBinder(binder(), ProjectImporter.class);
    projectImporterMultibinder.addBinding().to(GitHubProjectImporter.class);

    Multibinder.newSetBinder(binder(), SshKeyUploader.class)
        .addBinding()
        .to(GitHubKeyUploader.class);

    bind(GitHubService.class);
  }
}
