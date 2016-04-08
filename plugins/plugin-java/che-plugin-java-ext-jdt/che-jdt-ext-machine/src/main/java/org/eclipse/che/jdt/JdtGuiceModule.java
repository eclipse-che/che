/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.jdt;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.jdt.refactoring.RefactoringManager;
import org.eclipse.che.jdt.rest.CodeAssistService;
import org.eclipse.che.jdt.rest.CompilerSetupService;
import org.eclipse.che.jdt.rest.FormatService;
import org.eclipse.che.jdt.rest.JavaClasspathService;
import org.eclipse.che.jdt.rest.JavaNavigationService;
import org.eclipse.che.jdt.rest.JavaReconcileService;
import org.eclipse.che.jdt.rest.JdtExceptionMapper;
import org.eclipse.che.jdt.rest.RefactoringService;
import org.eclipse.che.jdt.rest.SearchService;
import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.ui.JavaPlugin;

import java.nio.file.Paths;

/**
 * @author Evgen Vidolob
 */
@DynaModule
public class JdtGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JavadocService.class);
        bind(JavaNavigationService.class);
        bind(JavaReconcileService.class);
        bind(JavaClasspathService.class);
        bind(FormatService.class);
        bind(CodeAssistService.class);
        bind(JdtExceptionMapper.class);
        bind(CompilerSetupService.class);
        bind(ResourcesPlugin.class).asEagerSingleton();
        bind(JavaPlugin.class).asEagerSingleton();
        bind(FileBuffersPlugin.class).asEagerSingleton();
        bind(ProjectListeners.class).asEagerSingleton();
        bind(RefactoringManager.class).asEagerSingleton();
        bind(RefactoringService.class);
        bind(SearchService.class);
    }

    @Provides
    @Named("che.jdt.settings.dir")
    @Singleton
    protected String provideSettings(@Named("che.user.workspaces.storage") String wsStorage,
                                     @Named("che.workspace.jdt.metadata") String jdtMetadata) {
        return Paths.get(wsStorage, jdtMetadata, "settings").toString();
    }

    @Provides
    @Named("che.jdt.workspace.index.dir")
    @Singleton
    protected String provideIndex(@Named("che.user.workspaces.storage") String wsStorage,
                                  @Named("che.workspace.jdt.metadata") String jdtMetadata) {
        return Paths.get(wsStorage, jdtMetadata, "index").toString();
    }


}
