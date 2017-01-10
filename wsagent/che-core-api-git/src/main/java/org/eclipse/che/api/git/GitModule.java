/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.git;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.inject.DynaModule;

/**
 * The module that contains configuration of the server side part of the Git extension.
 *
 * @author andrew00x
 */
@DynaModule
public class GitModule extends AbstractModule {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        Multibinder<ProjectImporter> projectImporterMultibinder = Multibinder.newSetBinder(binder(), ProjectImporter.class);
        projectImporterMultibinder.addBinding().to(GitProjectImporter.class);
        Multibinder.newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(GitProjectType.class);
        bind(GitConfigurationChecker.class).asEagerSingleton();

        Multibinder<ValueProviderFactory> multiBinder = Multibinder.newSetBinder(binder(), ValueProviderFactory.class);
        multiBinder.addBinding().to(GitValueProviderFactory.class);

        bind(GitService.class);
        bind(GitExceptionMapper.class);
        bind(BranchListWriter.class);
        bind(CommitMessageWriter.class);
        bind(MergeResultWriter.class);
        bind(RemoteListWriter.class);
        bind(StatusPageWriter.class);
        bind(TagListWriter.class);
        bind(GitWebSocketMessenger.class);

        //bind(GitConnectionFactory.class).to(NativeGitConnectionFactory.class);

        bind(GitCheckoutDetector.class).asEagerSingleton();
    }
}
