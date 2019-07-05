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
package org.eclipse.che.ide.ext.java.client.project.classpath;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind.LIBRARY;
import static org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind.PROJECT;
import static org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind.SOURCE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.ClasspathEntryKind;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.eclipse.che.plugin.java.plain.client.service.ClasspathUpdaterServiceClient;

/**
 * Class supports project classpath. It reads classpath content, parses its and writes.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathResolver {
  private static final String WORKSPACE_PATH = "/projects";

  private final ClasspathUpdaterServiceClient classpathUpdater;
  private final NotificationManager notificationManager;
  private final AppContext appContext;
  private final DtoFactory dtoFactory;

  private Set<String> libs;
  private Set<String> sources;
  private Set<String> projects;
  private Set<ClasspathEntry> containers;

  @Inject
  public ClasspathResolver(
      ClasspathUpdaterServiceClient classpathUpdater,
      NotificationManager notificationManager,
      AppContext appContext,
      DtoFactory dtoFactory) {
    this.classpathUpdater = classpathUpdater;
    this.notificationManager = notificationManager;
    this.appContext = appContext;
    this.dtoFactory = dtoFactory;
  }

  /** Reads and parses classpath entries. */
  public void resolveClasspathEntries(List<ClasspathEntry> entries) {
    libs = new HashSet<>();
    containers = new HashSet<>();
    sources = new HashSet<>();
    projects = new HashSet<>();
    for (ClasspathEntry entry : entries) {
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

    Project optProject = resource.getProject();

    final List<ClasspathEntry> entries = new ArrayList<>();
    for (String path : libs) {
      entries.add(dtoFactory.createDto(ClasspathEntry.class).withPath(path).withEntryKind(LIBRARY));
    }

    entries.addAll(containers);

    for (String path : sources) {
      entries.add(dtoFactory.createDto(ClasspathEntry.class).withPath(path).withEntryKind(SOURCE));
    }

    for (String path : projects) {
      entries.add(dtoFactory.createDto(ClasspathEntry.class).withPath(path).withEntryKind(PROJECT));
    }

    Promise<Void> promise =
        classpathUpdater.setRawClasspath(optProject.getLocation().toString(), entries);
    promise.catchError(
        error -> {
          notificationManager.notify(
              "Problems with updating classpath", error.getMessage(), FAIL, EMERGE_MODE);
        });
    return promise;
  }

  /** Returns list of libraries from classpath. */
  public Set<String> getLibs() {
    return libs;
  }

  /** Returns list of containers from classpath. */
  public Set<ClasspathEntry> getContainers() {
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
