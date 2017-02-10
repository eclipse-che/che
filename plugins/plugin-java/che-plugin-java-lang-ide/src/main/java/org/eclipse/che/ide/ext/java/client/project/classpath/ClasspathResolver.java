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
package org.eclipse.che.ide.ext.java.client.project.classpath;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.plugin.java.plain.client.service.ClasspathUpdaterServiceClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind.LIBRARY;
import static org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind.PROJECT;
import static org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind.SOURCE;

/**
 * Class supports project classpath. It reads classpath content, parses its and writes.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathResolver {
    private final static String WORKSPACE_PATH = "/projects";

    private final ClasspathUpdaterServiceClient classpathUpdater;
    private final NotificationManager           notificationManager;
    private final EventBus                      eventBus;
    private final AppContext                    appContext;
    private final DtoFactory                    dtoFactory;

    private Set<String>            libs;
    private Set<String>            sources;
    private Set<String>            projects;
    private Set<ClasspathEntryDto> containers;

    @Inject
    public ClasspathResolver(ClasspathUpdaterServiceClient classpathUpdater,
                             NotificationManager notificationManager,
                             EventBus eventBus,
                             AppContext appContext,
                             DtoFactory dtoFactory) {
        this.classpathUpdater = classpathUpdater;
        this.notificationManager = notificationManager;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.dtoFactory = dtoFactory;
    }

    /** Reads and parses classpath entries. */
    public void resolveClasspathEntries(List<ClasspathEntryDto> entries) {
        libs = new HashSet<>();
        containers = new HashSet<>();
        sources = new HashSet<>();
        projects = new HashSet<>();
        for (ClasspathEntryDto entry : entries) {
            switch (entry.getEntryKind()) {
                case ClasspathEntryKind.LIBRARY:
                    libs.add(entry.getPath());
                    break;
                case ClasspathEntryKind.CONTAINER:
                    containers.add(entry);
                    break;
                case ClasspathEntryKind.SOURCE:
                    sources.add(entry.getPath());
                    break;
                case ClasspathEntryKind.PROJECT:
                    projects.add(WORKSPACE_PATH + entry.getPath());
                    break;
                default:
                    // do nothing
            }
        }
    }

    /** Concatenates classpath entries and update classpath file. */
    public Promise<Void> updateClasspath() {

        final Resource resource = appContext.getResource();

        checkState(resource != null);

        final Optional<Project> optProject = resource.getRelatedProject();

        checkState(optProject.isPresent());

        final List<ClasspathEntryDto> entries = new ArrayList<>();
        for (String path : libs) {
            entries.add(dtoFactory.createDto(ClasspathEntryDto.class).withPath(path).withEntryKind(LIBRARY));
        }
        for (ClasspathEntryDto container : containers) {
            entries.add(container);
        }
        for (String path : sources) {
            entries.add(dtoFactory.createDto(ClasspathEntryDto.class).withPath(path).withEntryKind(SOURCE));
        }
        for (String path : projects) {
            entries.add(dtoFactory.createDto(ClasspathEntryDto.class).withPath(path).withEntryKind(PROJECT));
        }

        final Project project = optProject.get();

        Promise<Void> promise = classpathUpdater.setRawClasspath(project.getLocation().toString(), entries);

        promise.then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                project.synchronize().then(new Operation<Resource[]>() {
                    @Override
                    public void apply(Resource[] arg) throws OperationException {
                        eventBus.fireEvent(new ClasspathChangedEvent(project.getLocation().toString(), entries));
                    }
                });
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify("Problems with updating classpath", arg.getMessage(), FAIL, EMERGE_MODE);
            }
        });

        return promise;
    }

    /** Returns list of libraries from classpath. */
    public Set<String> getLibs() {
        return libs;
    }

    /** Returns list of containers from classpath. */
    public Set<ClasspathEntryDto> getContainers() {
        return containers;
    }

    /** Returns list of sources from classpath. */
    public Set<String> getSources() {
        return sources;
    }

    /** Returns list of projects from classpath. */
    public Set<String> getProjects() {
        return projects;
    }
}
