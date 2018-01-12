/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.core.project;

import static java.util.Collections.emptyList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.commons.lang.PathUtil;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenConstants;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.data.MavenPlugin;
import org.eclipse.che.maven.data.MavenProblemType;
import org.eclipse.che.maven.data.MavenProfile;
import org.eclipse.che.maven.data.MavenProjectProblem;
import org.eclipse.che.maven.data.MavenRemoteRepository;
import org.eclipse.che.maven.data.MavenResource;
import org.eclipse.che.plugin.maven.server.MavenServerManager;
import org.eclipse.che.plugin.maven.server.MavenServerWrapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jdom.Element;

/** @author Evgen Vidolob */
public class MavenProject {

  private final IProject project;
  private final IWorkspace workspace;
  private volatile Info info = new Info();

  public MavenProject(IProject project, IWorkspace workspace) {
    this.project = project;
    this.workspace = workspace;
  }

  public MavenKey getParentKey() {
    return info.parentKey;
  }

  public MavenKey getMavenKey() {
    return info.mavenKey;
  }

  public String getPackaging() {
    return info.packaging;
  }

  public Properties getProperties() {
    return info.properties;
  }

  public List<String> getSources() {
    return info.sources;
  }

  public Collection<String> getProfilesIds() {
    return info.profilesIds;
  }

  public List<MavenResource> getResources() {
    return info.resources;
  }

  public List<String> getTestSources() {
    return info.testSources;
  }

  public List<MavenResource> getTestResources() {
    return info.testResources;
  }

  public String getOutputDirectory() {
    return info.outputDirectory;
  }

  public String getTestOutputDirectory() {
    return info.testOutputDirectory;
  }

  public String getName() {
    String name = info.name;
    if (name == null) {
      name = info.mavenKey.getArtifactId();
    }
    return name;
  }

  public String getSourceLevel() {
    // todo
    throw new UnsupportedOperationException();
  }

  public String getTargetLevel() {
    // todo
    throw new UnsupportedOperationException();
  }

  public List<String> getModules() {
    return new ArrayList<>(info.modulesNameToPath.keySet());
  }

  public List<MavenPlugin> getPlugins() {
    return info.plugins;
  }

  public List<MavenProjectProblem> getProblems() {
    synchronized (info) {
      if (info.problemsCache == null) {
        info.problemsCache = generateProblems();
      }
      return info.problemsCache;
    }
  }

  private List<MavenProjectProblem> generateProblems() {
    List<MavenProjectProblem> result = new ArrayList<>();

    result.addAll(info.problems);
    result.addAll(
        info.modulesNameToPath
            .entrySet()
            .stream()
            .filter(entry -> !project.getFolder(entry.getKey()).exists())
            .map(
                entry ->
                    new MavenProjectProblem(
                        getPomPath(),
                        "Can't find module: " + entry.getKey(),
                        MavenProblemType.DEPENDENCY))
            .collect(Collectors.toList()));

    result.addAll(
        getDependencies()
            .stream()
            .filter(artifact -> !artifact.isResolved())
            .map(
                artifact ->
                    new MavenProjectProblem(
                        getPomPath(),
                        "Can't find dependency: " + artifact.getDisplayString(),
                        MavenProblemType.DEPENDENCY))
            .collect(Collectors.toList()));

    // TODO add unresolved plugins and extensions
    return result;
  }

  private boolean isParentResolved() {
    return info.unresolvedArtifacts.contains(info.parentKey);
  }

  public List<MavenArtifact> getDependencies() {
    return info.dependencies;
  }

  /**
   * Invoke maven to build project model.
   *
   * @param project to resolve
   * @param mavenServer the maven server
   * @return the modification types that applied to this project
   */
  public MavenProjectModifications resolve(
      IProject project, MavenServerWrapper mavenServer, MavenServerManager serverManager) {
    MavenModelReader reader = new MavenModelReader();

    MavenModelReaderResult modelReaderResult =
        reader.resolveMavenProject(
            getPom(project),
            mavenServer,
            info.activeProfiles,
            info.inactiveProfiles,
            serverManager);

    return setModel(modelReaderResult, modelReaderResult.getProblems().isEmpty(), false);
  }

  public MavenProjectModifications read(MavenServerManager manager) {
    return read(project, manager);
  }

  public MavenProjectModifications read(IProject project, MavenServerManager serverManager) {
    MavenModelReader reader = new MavenModelReader();
    return setModel(reader.readMavenProject(getPom(project), serverManager), false, true);
  }

  private MavenProjectModifications setModel(
      MavenModelReaderResult readerResult, boolean clearArtifacts, boolean clearProfiles) {
    Info newInfo = info.clone();
    newInfo.problems = readerResult.getProblems();
    newInfo.activeProfiles = readerResult.getActiveProfiles();
    MavenModel model = readerResult.getMavenModel();
    newInfo.mavenKey = model.getMavenKey();
    if (model.getParent() != null) {
      newInfo.parentKey = model.getParent().getMavenKey();
    }

    newInfo.packaging = model.getPackaging();
    newInfo.name = model.getName();

    newInfo.sources = model.getBuild().getSources();
    newInfo.testSources = model.getBuild().getTestSources();
    newInfo.resources = model.getBuild().getResources();
    newInfo.testResources = model.getBuild().getTestResources();
    newInfo.properties = model.getProperties();
    newInfo.filters = model.getBuild().getFilters();

    newInfo.outputDirectory = model.getBuild().getOutputDirectory();
    newInfo.testOutputDirectory = model.getBuild().getTestOutputDirectory();

    Set<MavenRemoteRepository> remoteRepositories = new HashSet<>();
    Set<MavenArtifact> extensions = new HashSet<>();
    Set<MavenArtifact> dependencies = new HashSet<>();
    Set<MavenPlugin> plugins = new HashSet<>();
    Set<MavenKey> unresolvedArtifacts = new HashSet<>();

    if (!clearArtifacts) {
      if (info.remoteRepositories != null) {
        remoteRepositories.addAll(info.remoteRepositories);
      }
      if (info.extensions != null) {
        extensions.addAll(info.extensions);
      }
      if (info.dependencies != null) {
        dependencies.addAll(info.dependencies);
      }
      if (info.plugins != null) {
        plugins.addAll(info.plugins);
      }
      if (info.unresolvedArtifacts != null) {
        unresolvedArtifacts.addAll(info.unresolvedArtifacts);
      }
    }

    remoteRepositories.addAll(model.getRemoteRepositories());
    extensions.addAll(model.getExtensions());
    dependencies.addAll(model.getDependencies());
    plugins.addAll(model.getPlugins());
    unresolvedArtifacts.addAll(readerResult.getUnresolvedArtifacts());

    newInfo.remoteRepositories = new ArrayList<>(remoteRepositories);
    newInfo.extensions = new ArrayList<>(extensions);
    newInfo.dependencies = new ArrayList<>(dependencies);
    newInfo.plugins = new ArrayList<>(plugins);
    newInfo.unresolvedArtifacts = unresolvedArtifacts;

    newInfo.modulesNameToPath = collectModulesNameAndPath(model.getModules());

    Collection<String> newProfiles = collectProfilesIds(model.getProfiles());
    if (clearProfiles || newInfo.profilesIds == null) {
      newInfo.profilesIds = new ArrayList<>(newProfiles);
    } else {
      Set<String> mergedProfiles = new HashSet<>(newInfo.profilesIds);
      mergedProfiles.addAll(newProfiles);
      newInfo.profilesIds = new ArrayList<>(mergedProfiles);
    }

    return setInfo(newInfo);
  }

  private static Collection<String> collectProfilesIds(Collection<MavenProfile> profiles) {
    if (profiles == null) {
      return emptyList();
    }

    Set<String> result = new HashSet<>(profiles.size());
    for (MavenProfile each : profiles) {
      result.add(each.getId());
    }
    return result;
  }

  private Map<String, String> collectModulesNameAndPath(List<String> modules) {
    Map<String, String> result = new HashMap<>();
    String projectPath = project.getFullPath().toOSString();
    if (!projectPath.endsWith("/")) {
      projectPath += "/";
    }

    for (String name : modules) {
      result.put(name, PathUtil.toCanonicalPath(projectPath + name, false));
    }
    return result;
  }

  private MavenProjectModifications setInfo(Info newInfo) {
    MavenProjectModifications modifications = info.generateChanges(newInfo);
    info = newInfo;
    info.problemsCache = null; // info has been changed, so we must to clean cache
    return modifications;
  }

  private File getPom(IProject project) {
    IFile file = project.getFile(MavenConstants.POM_FILE_NAME);
    if (file == null) {
      return null;
    }

    return file.getLocation().toFile();
  }

  /** @return workspace relative pom.xml path or null if pom.xml does not exist */
  public String getPomPath() {
    IFile file = project.getFile(MavenConstants.POM_FILE_NAME);
    if (file == null) {
      return null;
    }

    return file.getFullPath().toOSString();
  }

  public IProject getProject() {
    return project;
  }

  public File getPomFile() {
    return getPom(project);
  }

  public boolean containsAsModule(IPath modulePath) {
    if (!project.getFullPath().equals(modulePath)) {
      return false;
    }
    String moduleName = modulePath.lastSegment();
    List<String> modules = getModules();
    return modules.contains(moduleName);
  }

  public List<IProject> getModulesProjects() {
    Collection<String> modulesPath = info.modulesNameToPath.values();
    return modulesPath
        .stream()
        .map(
            path -> {
              if (path.endsWith(MavenConstants.POM_FILE_NAME)) {
                return workspace
                    .getRoot()
                    .getProject(new Path(path).removeLastSegments(1).toOSString());
              }
              return workspace.getRoot().getProject(path);
            })
        .collect(Collectors.toList());
  }

  public Collection<String> getModulesPath() {
    return info.modulesNameToPath.values();
  }

  public Element getPluginConfiguration(String groupId, String artifactId, String goal) {
    MavenPlugin plugin = findPlugin(groupId, artifactId);
    if (plugin == null) {
      return null;
    }

    if (goal == null) {
      return plugin.getConfiguration();
    } else {
      return plugin.getGoalConfiguration(goal);
    }
  }

  public MavenPlugin findPlugin(String groupId, String artifactId) {
    return findPlugin(groupId, artifactId, false);
  }

  private MavenPlugin findPlugin(String groupId, String artifactId, boolean declaredOnly) {
    List<MavenPlugin> plugins;
    if (declaredOnly) {
      plugins = getDeclaredPlugins();
    } else {
      plugins = getPlugins();
    }

    for (MavenPlugin plugin : plugins) {
      if (plugin.getGroupId().equals(groupId) && plugin.getArtifactId().equals(artifactId)) {
        return plugin;
      }
    }
    return null;
  }

  private List<MavenPlugin> getDeclaredPlugins() {
    return info.plugins.stream().filter(plugin -> !plugin.isDefault()).collect(Collectors.toList());
  }

  private static class Info implements Cloneable {
    public MavenKey mavenKey;
    public MavenKey parentKey;

    public String packaging;
    public String name;

    public Properties properties;

    public List<String> sources;
    public List<String> testSources;
    public List<MavenResource> resources;
    public List<MavenResource> testResources;

    public List<String> profilesIds;
    public List<String> activeProfiles;
    public List<String> inactiveProfiles;
    public List<String> filters;

    public List<MavenArtifact> dependencies;
    public List<MavenArtifact> extensions;
    public List<MavenPlugin> plugins;
    public List<MavenProjectProblem> problems;
    public List<MavenRemoteRepository> remoteRepositories;

    public Map<String, String> modulesNameToPath;

    public Set<MavenKey> unresolvedArtifacts;
    public List<MavenProjectProblem> problemsCache;

    public String testOutputDirectory;
    public String outputDirectory;

    public Info clone() {
      try {
        Info newInfo = (Info) super.clone();
        return newInfo;
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }

    public MavenProjectModifications generateChanges(Info newInfo) {
      MavenProjectModifications result = new MavenProjectModifications();
      result.setPackaging(!Objects.equals(packaging, newInfo.packaging));
      result.setSources(
          !Objects.equals(sources, newInfo.sources)
              || !Objects.equals(resources, newInfo.resources)
              || !Objects.equals(testSources, newInfo.testSources)
              || !Objects.equals(testResources, newInfo.testResources));

      result.setDependencies(!Objects.equals(dependencies, newInfo.dependencies));
      result.setPlugins(!Objects.equals(plugins, newInfo.plugins));

      return result;
    }
  }
}
