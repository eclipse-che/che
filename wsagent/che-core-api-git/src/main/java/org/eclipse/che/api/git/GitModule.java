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
package org.eclipse.che.api.git;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.project.server.ProjectImporter;
import org.eclipse.che.api.project.server.VcsStatusProvider;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;

/**
 * The module that contains configuration of the server side part of the Git extension.
 *
 * @author andrew00x
 */
public class GitModule extends AbstractModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    Multibinder<ProjectImporter> projectImporterMultibinder =
        Multibinder.newSetBinder(binder(), ProjectImporter.class);
    projectImporterMultibinder.addBinding().to(GitProjectImporter.class);
    Multibinder.newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(GitProjectType.class);
    bind(GitConfigurationChecker.class).asEagerSingleton();

    Multibinder<VcsStatusProvider> vcsStatusProviderMultibinder =
        newSetBinder(binder(), VcsStatusProvider.class);
    vcsStatusProviderMultibinder.addBinding().to(GitStatusProvider.class);

    Multibinder<ValueProviderFactory> multiBinder =
        Multibinder.newSetBinder(binder(), ValueProviderFactory.class);
    multiBinder.addBinding().to(GitValueProviderFactory.class);

    bind(GitUserResolver.class).to(LocalGitUserResolver.class);

    bind(GitService.class);
    bind(GitExceptionMapper.class);
    bind(BranchListWriter.class);
    bind(CommitMessageWriter.class);
    bind(MergeResultWriter.class);
    bind(RemoteListWriter.class);
    bind(StatusPageWriter.class);
    bind(TagListWriter.class);
    bind(GitJsonRpcMessenger.class);

    Multibinder.newSetBinder(binder(), CredentialsProvider.class)
        .addBinding()
        .to(GitBasicAuthenticationCredentialsProvider.class);

    bind(GitCheckoutDetector.class).asEagerSingleton();
    bind(GitChangesDetector.class).asEagerSingleton();
    bind(GitStatusChangedDetector.class).asEagerSingleton();
  }
}
