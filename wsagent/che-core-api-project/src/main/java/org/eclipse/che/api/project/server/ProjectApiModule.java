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
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.core.jsonrpc.RequestHandler;
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
import org.eclipse.che.api.vfs.impl.file.event.HiEventDetector;
import org.eclipse.che.api.vfs.impl.file.event.HiEventService;
import org.eclipse.che.api.vfs.impl.file.event.LoEventListener;
import org.eclipse.che.api.vfs.impl.file.event.LoEventService;
import org.eclipse.che.api.vfs.impl.file.event.detectors.FileStatusDetector;
import org.eclipse.che.api.vfs.impl.file.event.detectors.FileTrackingOperationReceiver;
import org.eclipse.che.api.vfs.impl.file.event.detectors.FileTrackingOperationTransmitter;
import org.eclipse.che.api.vfs.impl.file.event.detectors.ProjectTreeChangesDetector;
import org.eclipse.che.api.vfs.search.MediaTypeFilter;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;

import java.nio.file.Path;
import java.nio.file.PathMatcher;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/**
 * Guice module contains configuration of Project API components.
 *
 * @author gazarenkov
 * @author Artem Zatsarynnyi
 * @author Dmitry Kuleshov
 */
public class ProjectApiModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<ProjectImporter> projectImportersMultibinder = newSetBinder(binder(), ProjectImporter.class);
        projectImportersMultibinder.addBinding().to(ZipProjectImporter.class);

        newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(BaseProjectType.class);

        Multibinder<ProjectHandler> projectHandlersMultibinder = newSetBinder(binder(), ProjectHandler.class);
        projectHandlersMultibinder.addBinding().to(CreateBaseProjectTypeHandler.class);
        projectHandlersMultibinder.addBinding().to(InitBaseProjectTypeHandler.class);

        bind(ProjectRegistry.class).asEagerSingleton();
        bind(ProjectService.class);
        bind(ProjectTypeService.class);
        bind(ProjectImportersService.class);

        bind(WorkspaceProjectsSyncer.class).to(WorkspaceHolder.class);

        // configure VFS
        Multibinder<VirtualFileFilter> filtersMultibinder =
                newSetBinder(binder(), VirtualFileFilter.class, Names.named("vfs.index_filter"));

        filtersMultibinder.addBinding().to(MediaTypeFilter.class);

        Multibinder<PathMatcher> excludeMatcher = newSetBinder(binder(), PathMatcher.class, Names.named("vfs.index_filter_matcher"));

        bind(SearcherProvider.class).to(FSLuceneSearcherProvider.class);
        bind(VirtualFileSystemProvider.class).to(LocalVirtualFileSystemProvider.class);

        bind(FileWatcherNotificationHandler.class).to(DefaultFileWatcherNotificationHandler.class);

        configureVfsFilters(excludeMatcher);
        configureVfsEvent();
    }

    private void configureVfsFilters(Multibinder<PathMatcher> excludeMatcher) {
        addVfsFilter(excludeMatcher, ".che");
        addVfsFilter(excludeMatcher, ".codenvy");
        addVfsFilter(excludeMatcher, ".#");
    }

    private void addVfsFilter(Multibinder<PathMatcher> excludeMatcher, String filter) {
        excludeMatcher.addBinding().toInstance(path -> {
            for (Path pathElement : path) {
                if (pathElement == null || filter.equals(pathElement.toString())) {
                    return true;
                }
            }
            return false;
        });
    }

    private void configureVfsEvent() {
        bind(LoEventListener.class);
        bind(LoEventService.class);
        bind(HiEventService.class);

        Multibinder<HiEventDetector<?>> highLevelVfsEventDetectorMultibinder =
                newSetBinder(binder(), new TypeLiteral<HiEventDetector<?>>() {
                });

        highLevelVfsEventDetectorMultibinder.addBinding().to(FileStatusDetector.class);
        highLevelVfsEventDetectorMultibinder.addBinding().to(ProjectTreeChangesDetector.class);

        bind(FileTrackingOperationTransmitter.class).asEagerSingleton();


        MapBinder.newMapBinder(binder(), String.class, RequestHandler.class)
                 .addBinding("track:editor-file")
                 .to(FileTrackingOperationReceiver.class);
    }
}
