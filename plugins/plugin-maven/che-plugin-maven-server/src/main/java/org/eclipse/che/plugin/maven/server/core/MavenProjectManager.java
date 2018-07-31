/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.core;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenWorkspaceCache;
import org.eclipse.che.maven.server.MavenTerminal;
import org.eclipse.che.plugin.maven.server.MavenServerManager;
import org.eclipse.che.plugin.maven.server.MavenServerWrapper;
import org.eclipse.che.plugin.maven.server.MavenWrapperManager;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.che.plugin.maven.server.core.project.MavenProjectModifications;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;

/**
 * Holds all maven projects in workspace
 *
 * @author Evgen Vidolob
 */
@Singleton
public class MavenProjectManager {

  private final MavenWorkspaceCache mavenWorkspaceCache;
  private final Map<MavenKey, MavenProject> keyToProjectMap;
  private final Map<IProject, MavenProject> projectToMavenProjectMap;
  private final Map<MavenProject, List<MavenProject>> parentToModulesMap;
  private final Map<MavenProject, MavenProject> moduleToParentMap;

  private final List<MavenProjectListener> listeners = new CopyOnWriteArrayList<>();

  // project that does not have parent project in our workspace
  private final List<MavenProject> rootProjects;

  private final MavenWrapperManager wrapperManager;
  private final MavenServerManager serverManager;
  private final MavenTerminal terminal;
  private final MavenProgressNotifier mavenNotifier;
  private final Provider<IWorkspace> workspaceProvider;

  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock readLock = readWriteLock.readLock();
  private final Lock writeLock = readWriteLock.writeLock();

  private final MavenProjectListener dispatcher;

  @Inject
  public MavenProjectManager(
      MavenWrapperManager wrapperManager,
      MavenServerManager serverManager,
      MavenTerminal terminal,
      MavenProgressNotifier mavenNotifier,
      EclipseWorkspaceProvider workspaceProvider) {
    this.wrapperManager = wrapperManager;
    this.serverManager = serverManager;
    this.terminal = terminal;
    this.mavenNotifier = mavenNotifier;
    this.workspaceProvider = workspaceProvider;
    mavenWorkspaceCache = new MavenWorkspaceCache();
    keyToProjectMap = new HashMap<>();
    projectToMavenProjectMap = new HashMap<>();
    parentToModulesMap = new HashMap<>();
    dispatcher = createListenersDispatcher();
    moduleToParentMap = new HashMap<>();
    rootProjects = new ArrayList<>();
  }

  private MavenProjectListener createListenersDispatcher() {
    return (MavenProjectListener)
        Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] {MavenProjectListener.class},
            (proxy, method, args) -> {
              for (MavenProjectListener listener : listeners) {
                method.invoke(listener, args);
              }
              return null;
            });
  }

  public void addListener(MavenProjectListener listener) {
    listeners.add(listener);
  }

  public void removeListener(MavenProjectListener listener) {
    listeners.remove(listener);
  }

  public void resolveMavenProject(IProject project, MavenProject mavenProject) {
    MavenServerWrapper mavenServer =
        wrapperManager.getMavenServer(MavenWrapperManager.ServerType.RESOLVE);
    try {

      mavenNotifier.setText("Resolving project: " + mavenProject.getName());
      mavenServer.customize(copyWorkspaceCache(), terminal, mavenNotifier, false, true);
      MavenProjectModifications modifications =
          mavenProject.resolve(project, mavenServer, serverManager);
      dispatcher.projectResolved(mavenProject, modifications);

    } finally {
      wrapperManager.release(mavenServer);
    }
  }

  public void update(List<IProject> projects, boolean recursive) {
    if (projects.isEmpty()) {
      return;
    }
    mavenNotifier.start();
    UpdateState state = new UpdateState();
    Deque<MavenProject> stack = new LinkedList<>();
    for (IProject project : projects) {
      MavenProject mavenProject = findMavenProject(project);
      if (mavenProject != null) {
        internalUpdate(
            mavenProject, findParentProject(mavenProject), false, recursive, state, stack);
      } else {
        internalAddMavenProject(project, recursive, state, stack);
      }
    }
    mavenNotifier.stop();
    state.fireUpdate();
  }

  public MavenProject findParentProject(MavenProject mavenProject) {
    readLock.lock();
    try {
      return moduleToParentMap.get(mavenProject);
    } finally {
      readLock.unlock();
    }
  }

  private void internalAddMavenProject(
      IProject project, boolean recursive, UpdateState state, Deque<MavenProject> stack) {
    MavenProject mavenProject = new MavenProject(project, workspaceProvider.get());
    MavenProject potentialParent = null;
    for (MavenProject parent : getAllProjects()) {
      if (parent.containsAsModule(project.getFullPath())) {
        potentialParent = parent;
        break;
      }
    }
    internalUpdate(mavenProject, potentialParent, true, recursive, state, stack);
  }

  private void internalUpdate(
      MavenProject mavenProject,
      MavenProject parentProject,
      boolean isNew,
      boolean recursive,
      UpdateState state,
      Deque<MavenProject> stack) {
    if (stack.contains(mavenProject)) {
      return; // recursion
    }

    stack.addFirst(mavenProject);

    mavenNotifier.setText("Reading pom: " + mavenProject.getPomPath());

    List<MavenProject> oldModules = findModules(mavenProject);
    Set<MavenProject> oldChilds = new HashSet<>();
    if (!isNew) {
      oldChilds.addAll(findChildProjects(mavenProject));
    }

    writeLock.lock();
    try {
      if (!isNew) {
        clearMavenKeyMap(mavenProject);
      }
    } finally {
      writeLock.unlock();
    }
    MavenProjectModifications modifications = new MavenProjectModifications();
    // re read maven project meta info from pom.xml
    modifications = modifications.addChanges(mavenProject.read(serverManager));

    writeLock.lock();
    try {
      fillMavenKeyMap(mavenProject);
      projectToMavenProjectMap.put(mavenProject.getProject(), mavenProject);
    } finally {
      writeLock.unlock();
    }

    if (isNew) {
      addToChild(parentProject, mavenProject);
    } else {
      updateChild(parentProject, mavenProject);
    }

    //        if (hasparent) {
    state.addUpdate(mavenProject, modifications);
    //        }

    List<IProject> modules = mavenProject.getModulesProjects();
    List<MavenProject> removedModules =
        oldModules
            .stream()
            .filter(oldModule -> !modules.contains(oldModule.getProject()))
            .collect(Collectors.toList());

    for (MavenProject removedModule : removedModules) {
      removeModule(mavenProject, removedModule);
      internalDelete(mavenProject, removedModule, state);
      oldChilds.removeAll(state.removedProjects);
    }

    for (IProject module : modules) {
      MavenProject project = findMavenProject(module);
      boolean isNewProject = project == null;
      if (isNewProject) {
        project = new MavenProject(module, workspaceProvider.get());
      } else {
        MavenProject parent = findParentProject(project);
        if (parent != null && parent != mavenProject) {
          // TODO add log
          continue;
        }
      }

      if (isNewProject || recursive) {
        internalUpdate(project, mavenProject, isNewProject, recursive, state, stack);
      } else {
        if (updateChild(mavenProject, project)) {
          state.addUpdate(project, new MavenProjectModifications());
        }
      }
    }
    oldChilds.addAll(findChildProjects(mavenProject));

    for (MavenProject oldModule : oldChilds) {
      internalUpdate(oldModule, findParentProject(oldModule), false, false, state, stack);
    }

    stack.pop();
  }

  private void internalDelete(
      MavenProject parentProject, MavenProject removedModule, UpdateState state) {
    for (MavenProject project : findModules(removedModule)) {
      internalDelete(removedModule, project, state);
    }

    writeLock.lock();
    try {
      if (parentProject == null) {
        rootProjects.remove(removedModule);
      } else {
        removeModule(parentProject, removedModule);
      }

      projectToMavenProjectMap.remove(removedModule.getProject());
      clearMavenKeyMap(removedModule);
      moduleToParentMap.remove(removedModule);
      parentToModulesMap.remove(removedModule);
    } finally {
      writeLock.unlock();
    }

    state.remove(removedModule);
  }

  private boolean updateChild(MavenProject parentProject, MavenProject module) {
    MavenProject oldParent = findParentProject(module);
    if (oldParent == parentProject) {
      return false;
    }
    writeLock.lock();
    try {
      if (oldParent == null) {
        rootProjects.remove(module);
      } else {
        removeModule(oldParent, module);
      }

      if (parentProject == null) {
        rootProjects.add(module);
      } else {
        addModule(parentProject, module);
      }
    } finally {
      writeLock.unlock();
    }

    return false;
  }

  private void removeModule(MavenProject oldParent, MavenProject module) {
    writeLock.lock();
    try {
      List<MavenProject> modules = parentToModulesMap.get(oldParent);
      if (modules != null) {
        modules.remove(module);
        moduleToParentMap.remove(module);
      }
    } finally {
      writeLock.unlock();
    }
  }

  private void addToChild(MavenProject parentProject, MavenProject module) {
    writeLock.lock();
    try {
      if (parentProject == null) {
        rootProjects.add(module);
      } else {
        addModule(parentProject, module);
      }
    } finally {
      writeLock.unlock();
    }
  }

  private void addModule(MavenProject parentProject, MavenProject module) {
    writeLock.lock();
    try {
      List<MavenProject> modules = parentToModulesMap.get(parentProject);
      if (modules == null) {
        modules = new ArrayList<>();
        parentToModulesMap.put(parentProject, modules);
      }

      modules.add(module);
      moduleToParentMap.put(module, parentProject);
    } finally {
      writeLock.unlock();
    }
  }

  public List<MavenProject> getAllProjects() {
    readLock.lock();
    try {
      return new ArrayList<>(projectToMavenProjectMap.values());
    } finally {
      readLock.unlock();
    }
  }

  public MavenProject getMavenProject(IProject iProject) {
    readLock.lock();
    try {
      return projectToMavenProjectMap.get(iProject);
    } finally {
      readLock.unlock();
    }
  }

  public MavenProject getMavenProject(String projectPath) {
    final IProject project = workspaceProvider.get().getRoot().getProject(projectPath);
    return getMavenProject(project);
  }

  private void fillMavenKeyMap(MavenProject mavenProject) {
    MavenKey mavenKey = mavenProject.getMavenKey();
    mavenWorkspaceCache.put(mavenKey, mavenProject.getPomFile());
    keyToProjectMap.put(mavenKey, mavenProject);
  }

  private void clearMavenKeyMap(MavenProject mavenProject) {
    MavenKey mavenKey = mavenProject.getMavenKey();
    mavenWorkspaceCache.invalidate(mavenKey);
    keyToProjectMap.remove(mavenKey);
  }

  private List<MavenProject> findChildProjects(MavenProject mavenProject) {
    readLock.lock();
    try {
      MavenKey parentKey = mavenProject.getMavenKey();
      return projectToMavenProjectMap
          .values()
          .stream()
          .filter(project -> mavenProject != project)
          .filter(project -> project.getParentKey().equals(parentKey))
          .collect(Collectors.toList());
    } finally {
      readLock.unlock();
    }
  }

  public List<MavenProject> findModules(MavenProject parent) {
    readLock.lock();
    try {
      List<MavenProject> modules = parentToModulesMap.get(parent);
      if (modules == null) {
        modules = Collections.emptyList();
      }
      return new ArrayList<>(modules);
    } finally {
      readLock.unlock();
    }
  }

  public MavenProject findMavenProject(IProject project) {
    readLock.lock();
    try {
      return projectToMavenProjectMap.get(project);
    } finally {
      readLock.unlock();
    }
  }

  public MavenWorkspaceCache copyWorkspaceCache() {
    readLock.lock();
    try {
      return mavenWorkspaceCache.copy();
    } finally {
      readLock.unlock();
    }
  }

  public List<MavenProject> findDependentProjects(List<MavenProject> projects) {
    readLock.lock();
    try {
      List<MavenProject> result = new ArrayList<>();

      Set<MavenKey> mavenKeys =
          projects.stream().map(MavenProject::getMavenKey).collect(Collectors.toSet());
      Set<String> paths =
          projects
              .stream()
              .map(project -> project.getProject().getFullPath().toOSString())
              .collect(Collectors.toSet());

      for (MavenProject project : projectToMavenProjectMap.values()) {
        boolean isAdd = false;
        for (String path : project.getModulesPath()) {
          if (paths.contains(path)) {
            isAdd = true;
            break;
          }
        }

        if (!isAdd) {
          for (MavenArtifact artifact : project.getDependencies()) {
            if (contains(
                mavenKeys,
                artifact.getArtifactId(),
                artifact.getGroupId(),
                artifact.getVersion())) {
              isAdd = true;
              break;
            }
          }
        }

        if (isAdd) {
          result.add(project);
        }
      }

      return result;

    } finally {
      readLock.unlock();
    }
  }

  private boolean contains(
      Set<MavenKey> mavenKeys, String artifactId, String groupId, String version) {
    return mavenKeys
        .stream()
        .filter(
            key ->
                Objects.equals(key.getArtifactId(), artifactId)
                    && Objects.equals(key.getGroupId(), groupId)
                    && Objects.equals(key.getVersion(), version))
        .findFirst()
        .isPresent();
  }

  public void delete(List<IProject> projects) {
    if (projects.isEmpty()) {
      return;
    }
    UpdateState state = new UpdateState();

    Deque<MavenProject> stack = new LinkedList<>();

    Set<MavenProject> childToUpdate = new HashSet<>();

    for (IProject project : projects) {
      MavenProject mavenProject = findMavenProject(project);
      if (mavenProject == null) {
        return;
      }

      childToUpdate.addAll(findChildProjects(mavenProject));
      internalDelete(findParentProject(mavenProject), mavenProject, state);
    }

    childToUpdate.removeAll(state.removedProjects);

    for (MavenProject mavenProject : childToUpdate) {
      internalUpdate(mavenProject, null, false, false, state, stack);
    }

    state.fireUpdate();
  }

  private class UpdateState {
    Map<MavenProject, MavenProjectModifications> projectWithModification = new HashMap<>();

    Set<MavenProject> removedProjects = new HashSet<>();

    public void addUpdate(MavenProject mavenProject, MavenProjectModifications modifications) {
      removedProjects.remove(mavenProject);
      projectWithModification.put(mavenProject, modifications);
    }

    public void remove(MavenProject mavenProject) {
      projectWithModification.remove(mavenProject);
      removedProjects.add(mavenProject);
    }

    public void fireUpdate() {
      if (projectWithModification.isEmpty() && removedProjects.isEmpty()) {
        return;
      }

      Map<MavenProject, MavenProjectModifications> modified =
          new HashMap<>(projectWithModification);
      List<MavenProject> removed = new ArrayList<>(removedProjects);
      dispatcher.projectUpdated(modified, removed);
    }
  }
}
