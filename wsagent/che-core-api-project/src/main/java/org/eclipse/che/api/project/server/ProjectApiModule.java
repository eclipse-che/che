/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.project.server.handlers.CreateBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.importer.ProjectImportersService;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.InitBaseProjectTypeHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.vfs.VirtualFileFilter;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.event.GitCheckoutHiEventDetector;
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;
import org.eclipse.che.api.vfs.impl.file.event.HiEventService;
import org.eclipse.che.api.vfs.impl.file.event.LoEventListener;
import org.eclipse.che.api.vfs.impl.file.event.LoEventService;
import org.eclipse.che.api.vfs.impl.file.event.PomModifiedHiEventDetector;
import org.eclipse.che.api.vfs.search.MediaTypeFilter;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;

import java.nio.file.PathMatcher;

/**
 * Guice module contains configuration of Project API components.
 *
 * @author gazarenkov
 * @author Artem Zatsarynnyi
 */
public class ProjectApiModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<ProjectImporter> projectImportersMultibinder = Multibinder.newSetBinder(binder(), ProjectImporter.class);
        projectImportersMultibinder.addBinding().to(ZipProjectImporter.class);

        Multibinder.newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(BaseProjectType.class);

        Multibinder<ProjectHandler> projectHandlersMultibinder = Multibinder.newSetBinder(binder(), ProjectHandler.class);
        projectHandlersMultibinder.addBinding().to(CreateBaseProjectTypeHandler.class);
        projectHandlersMultibinder.addBinding().to(InitBaseProjectTypeHandler.class);

        bind(ProjectRegistry.class).asEagerSingleton();
        bind(ProjectService.class);
        bind(ProjectTypeService.class);
        bind(ProjectImportersService.class);

        bind(WorkspaceProjectsSyncer.class).to(WorkspaceHolder.class);

        // configure VFS
        Multibinder<VirtualFileFilter> filtersMultibinder = Multibinder.newSetBinder(binder(),
                                                                                     VirtualFileFilter.class,
                                                                                     Names.named("vfs.index_filter"));
        filtersMultibinder.addBinding().to(MediaTypeFilter.class);

        Multibinder<PathMatcher> pathMatcherMultibinder = Multibinder.newSetBinder(binder(),
                                                                                   PathMatcher.class,
                                                                                   Names.named("vfs.index_filter_matcher"));

        bind(SearcherProvider.class).to(FSLuceneSearcherProvider.class);
        bind(VirtualFileSystemProvider.class).to(LocalVirtualFileSystemProvider.class);

        bind(FileWatcherNotificationHandler.class).to(DefaultFileWatcherNotificationHandler.class);

        bind(LoEventListener.class);
        bind(LoEventService.class);
        bind(HiEventService.class);

        Multibinder<HiEventDetector<?>> highLevelVfsEventDetectorMultibinder =
                Multibinder.newSetBinder(binder(), new TypeLiteral<HiEventDetector<?>>() {
                });
        highLevelVfsEventDetectorMultibinder.addBinding().to(PomModifiedHiEventDetector.class);
        highLevelVfsEventDetectorMultibinder.addBinding().to(GitCheckoutHiEventDetector.class);
    }
}
