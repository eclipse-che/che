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
package org.eclipse.che.maven.server;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.model.ActivationOS;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.che.maven.data.MavenActivation;
import org.eclipse.che.maven.data.MavenActivationFile;
import org.eclipse.che.maven.data.MavenActivationOS;
import org.eclipse.che.maven.data.MavenActivationProperty;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenBuild;
import org.eclipse.che.maven.data.MavenBuildBase;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.data.MavenParent;
import org.eclipse.che.maven.data.MavenPlugin;
import org.eclipse.che.maven.data.MavenPluginExecution;
import org.eclipse.che.maven.data.MavenProfile;
import org.eclipse.che.maven.data.MavenRemoteRepository;
import org.eclipse.che.maven.data.MavenRepositoryPolicy;
import org.eclipse.che.maven.data.MavenResource;
import org.jdom.Element;
import org.jdom.IllegalNameException;

/**
 * Util methods for converting maven model objects into maven-server objects
 *
 * @author Evgen Vidolob
 */
public class MavenModelUtil {

  public static MavenKey keyFor(Artifact artifact) {
    return new MavenKey(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

  public static MavenModel convertModel(Model model, File projectDir) {
    Build build = model.getBuild();
    List<String> sources = new ArrayList<>();
    List<String> testSources = new ArrayList<>();
    if (build != null) {
      String sourceDirectory = build.getSourceDirectory();
      if (sourceDirectory != null) {
        sources.add(sourceDirectory);
      }
      String testSourceDirectory = build.getTestSourceDirectory();
      if (testSourceDirectory != null) {
        testSources.add(testSourceDirectory);
      }
    }

    return convertModel(
        model,
        projectDir,
        sources,
        testSources,
        Collections.emptyList(),
        Collections.emptyList(),
        null);
  }

  public static MavenModel convertModel(
      Model model,
      File projectDir,
      List<String> sources,
      List<String> testSources,
      Collection<Artifact> dependencies,
      Collection<Artifact> extensions,
      File localRepo) {

    MavenModel result = new MavenModel();
    result.setMavenKey(new MavenKey(model.getGroupId(), model.getArtifactId(), model.getVersion()));

    Parent parent = model.getParent();
    if (parent != null) {
      result.setParent(
          new MavenParent(
              new MavenKey(parent.getGroupId(), parent.getArtifactId(), parent.getVersion()),
              parent.getRelativePath()));
    }

    result.setName(model.getName());
    result.setPackaging(model.getPackaging());
    result.setProperties(model.getProperties() != null ? model.getProperties() : new Properties());
    result.setModules(model.getModules());
    result.setPlugins(convertPlugins(model));

    Map<Artifact, MavenArtifact> convertedArtifacts = new HashMap<>();

    result.setExtensions(convertArtifacts(extensions, convertedArtifacts, localRepo));
    result.setDependencies(convertArtifacts(dependencies, convertedArtifacts, localRepo));

    result.setRemoteRepositories(convertRepositories(model.getRepositories()));
    result.setProfiles(convertProfiles(model.getProfiles(), projectDir));
    convertBuild(result.getBuild(), model.getBuild(), projectDir, sources, testSources);

    return result;
  }

  public static MavenModel convertProjectToModel(
      MavenProject project, List<DependencyNode> dependencyNodes, File localRepository) {
    Model model = project.getModel();
    return convertModel(
        model,
        project.getBasedir(),
        project.getCompileSourceRoots(),
        project.getTestCompileSourceRoots(),
        project.getArtifacts(),
        project.getExtensionArtifacts(),
        localRepository);
  }

  private static void convertBuild(
      MavenBuild mavenBuild,
      Build build,
      File projectDir,
      List<String> compileSourceRoots,
      List<String> testCompileSourceRoots) {
    convertBaseBuild(build, mavenBuild, projectDir);
    mavenBuild.setOutputDirectory(relativize(projectDir, build.getOutputDirectory()));
    mavenBuild.setTestOutputDirectory(relativize(projectDir, build.getTestOutputDirectory()));
    mavenBuild.setSources(
        compileSourceRoots.stream().map(path -> relativize(projectDir, path)).collect(toList()));
    mavenBuild.setTestSources(
        testCompileSourceRoots
            .stream()
            .map(path -> relativize(projectDir, path))
            .collect(toList()));
  }

  private static List<MavenProfile> convertProfiles(List<Profile> profiles, File projectDir) {
    List<MavenProfile> result = new ArrayList<>();

    if (profiles != null) {
      for (Profile profile : profiles) {
        if (profile.getId() == null) {
          continue;
        }

        MavenProfile mavenProfile = new MavenProfile(profile.getId(), profile.getSource());
        List<String> modules = profile.getModules();
        if (modules == null) {
          mavenProfile.setModules(Collections.emptyList());
        } else {
          mavenProfile.setModules(modules);
        }

        mavenProfile.setActivation(convertActivation(profile.getActivation()));
        if (profile.getBuild() != null) {
          convertBaseBuild(profile.getBuild(), mavenProfile.getBuild(), projectDir);
        }
        result.add(mavenProfile);
      }
    }

    return result;
  }

  private static void convertBaseBuild(
      BuildBase build, MavenBuildBase mavenBuild, File projectDir) {
    mavenBuild.setDefaultGoal(build.getDefaultGoal());
    mavenBuild.setDirectory(relativize(projectDir, build.getDirectory()));
    mavenBuild.setFinalName(build.getFinalName());
    mavenBuild.setResources(convenrtResources(build.getResources(), projectDir));
    mavenBuild.setTestResources(convenrtResources(build.getTestResources(), projectDir));
    List<String> filters = build.getFilters();
    if (filters == null) {
      mavenBuild.setFilters(Collections.emptyList());
    } else {
      mavenBuild.setFilters(filters);
    }
  }

  private static List<MavenResource> convenrtResources(List<Resource> resources, File projectDir) {
    List<MavenResource> result = new ArrayList<>();
    if (resources != null) {
      for (Resource res : resources) {
        result.add(
            new MavenResource(
                relativize(projectDir, res.getDirectory()),
                res.isFiltering(),
                res.getTargetPath(),
                patternsOrEmptyList(res.getIncludes()),
                patternsOrEmptyList(res.getExcludes())));
      }
    }

    return result;
  }

  private static List<String> patternsOrEmptyList(List<String> patterns) {
    return patterns == null ? Collections.emptyList() : patterns;
  }

  private static MavenActivation convertActivation(Activation activation) {
    if (activation == null) {
      return null;
    }

    MavenActivation result = new MavenActivation();
    result.setActiveByDefault(activation.isActiveByDefault());
    result.setFile(convertFileActivation(activation.getFile()));
    result.setJdk(activation.getJdk());
    result.setOs(convertOsActivation(activation.getOs()));
    result.setProperty(convertPropertyActivation(activation.getProperty()));

    return result;
  }

  private static MavenActivationProperty convertPropertyActivation(ActivationProperty property) {
    if (property == null) {
      return null;
    }
    return new MavenActivationProperty(property.getName(), property.getValue());
  }

  private static MavenActivationOS convertOsActivation(ActivationOS os) {
    if (os == null) {
      return null;
    }

    return new MavenActivationOS(os.getName(), os.getFamily(), os.getArch(), os.getVersion());
  }

  private static MavenActivationFile convertFileActivation(ActivationFile file) {
    if (file == null) {
      return null;
    }
    return new MavenActivationFile(file.getExists(), file.getMissing());
  }

  private static List<MavenRemoteRepository> convertRepositories(List<Repository> repositories) {
    List<MavenRemoteRepository> result = new ArrayList<>();
    if (repositories != null) {
      for (Repository repo : repositories) {
        result.add(
            new MavenRemoteRepository(
                repo.getId(),
                repo.getName(),
                repo.getUrl(),
                repo.getLayout(),
                convertPolicy(repo.getReleases()),
                convertPolicy(repo.getSnapshots())));
      }
    }

    return result;
  }

  private static MavenRepositoryPolicy convertPolicy(RepositoryPolicy policy) {
    if (policy != null) {
      return new MavenRepositoryPolicy(
          policy.isEnabled(), policy.getUpdatePolicy(), policy.getChecksumPolicy());
    }
    return null;
  }

  private static List<MavenArtifact> convertArtifacts(
      Collection<Artifact> artifacts,
      Map<Artifact, MavenArtifact> convertedArtifacts,
      File localRepository) {
    ArrayList<MavenArtifact> result = new ArrayList<>();
    if (artifacts != null) {
      result.addAll(
          artifacts
              .stream()
              .map(artifact -> convertArtifact(artifact, convertedArtifacts, localRepository))
              .collect(toList()));
    }

    return result;
  }

  private static MavenArtifact convertArtifact(
      Artifact artifact, Map<Artifact, MavenArtifact> convertedArtifacts, File localRepository) {
    MavenArtifact mavenArtifact = convertedArtifacts.get(artifact);
    if (mavenArtifact == null) {
      mavenArtifact = convertArtifact(artifact, localRepository);
      convertedArtifacts.put(artifact, mavenArtifact);
    }
    return mavenArtifact;
  }

  public static MavenArtifact convertArtifact(Artifact artifact, File localRepository) {
    return new MavenArtifact(
        artifact.getGroupId(),
        artifact.getArtifactId(),
        artifact.getVersion(),
        artifact.getBaseVersion(),
        artifact.getType(),
        artifact.getClassifier(),
        artifact.getScope(),
        artifact.isOptional(),
        convertExtension(artifact),
        artifact.getFile(),
        localRepository,
        artifact.isResolved(),
        false);
  }

  private static String convertExtension(Artifact artifact) {
    ArtifactHandler artifactHandler = artifact.getArtifactHandler();
    String result = null;

    if (artifactHandler != null) {
      result = artifactHandler.getExtension();
    }

    if (result == null) {
      result = artifact.getType();
    }
    return result;
  }

  private static List<MavenPlugin> convertPlugins(Model model) {
    List<MavenPlugin> result = new ArrayList<>();

    Build build = model.getBuild();
    if (build != null) {
      List<Plugin> plugins = build.getPlugins();
      if (plugins != null) {
        result.addAll(plugins.stream().map(MavenModelUtil::convertPlugin).collect(toList()));
      }
    }

    return result;
  }

  private static MavenPlugin convertPlugin(Plugin plugin) {
    List<MavenPluginExecution> executions =
        plugin.getExecutions().stream().map(MavenModelUtil::convertExecution).collect(toList());

    List<MavenKey> dependecies =
        plugin
            .getDependencies()
            .stream()
            .map(
                dependency ->
                    new MavenKey(
                        dependency.getGroupId(),
                        dependency.getArtifactId(),
                        dependency.getVersion()))
            .collect(toList());

    return new MavenPlugin(
        plugin.getGroupId(),
        plugin.getArtifactId(),
        plugin.getVersion(),
        false,
        convertConfiguration(plugin.getConfiguration()),
        executions,
        dependecies);
  }

  private static Element convertConfiguration(Object configuration) {
    return configuration == null ? null : convertXpp((Xpp3Dom) configuration);
  }

  private static Element convertXpp(Xpp3Dom xpp3Dom) {
    Element result;
    try {
      result = new Element(xpp3Dom.getName());
    } catch (IllegalNameException e) {
      return null;
    }

    Xpp3Dom[] children = xpp3Dom.getChildren();
    if (children == null || children.length == 0) {
      result.setText(xpp3Dom.getValue());
    } else {
      for (Xpp3Dom child : children) {
        Element element = convertXpp(child);
        if (element != null) {
          result.addContent(element);
        }
      }
    }

    return result;
  }

  private static MavenPluginExecution convertExecution(PluginExecution execution) {
    return new MavenPluginExecution(
        execution.getId(),
        convertConfiguration(execution.getConfiguration()),
        execution.getGoals());
  }

  public static Model convertToMavenModel(MavenModel model, File projectDir) {
    Model result = new Model();
    result.setArtifactId(model.getMavenKey().getArtifactId());
    result.setGroupId(model.getMavenKey().getGroupId());
    result.setVersion(model.getMavenKey().getVersion());
    result.setPackaging(model.getPackaging());
    result.setName(model.getName());

    if (model.getParent() != null) {
      Parent parent = new Parent();
      MavenKey parentKey = model.getParent().getMavenKey();
      parent.setArtifactId(parentKey.getArtifactId());
      parent.setGroupId(parentKey.getGroupId());
      parent.setVersion(parentKey.getVersion());
      parent.setRelativePath(model.getParent().getRelativePath());
      result.setParent(parent);
    }

    result.setProperties(model.getProperties());
    result.setModules(model.getModules());
    result.setBuild(new Build());
    MavenBuild modelBuild = model.getBuild();
    convertToMavenBuildBase(modelBuild, result.getBuild(), projectDir);
    result.getBuild().setSourceDirectory(relativize(projectDir, modelBuild.getSources().get(0)));
    result
        .getBuild()
        .setTestSourceDirectory(relativize(projectDir, modelBuild.getTestSources().get(0)));

    result.getBuild().setOutputDirectory(modelBuild.getOutputDirectory());
    result.getBuild().setTestOutputDirectory(modelBuild.getTestOutputDirectory());

    result.setProfiles(convertToMavenProfiles(model.getProfiles(), projectDir));
    return result;
  }

  private static List<Profile> convertToMavenProfiles(
      List<MavenProfile> profiles, File projectDir) {
    return profiles
        .stream()
        .map(profile -> convertToMavenProfile(profile, projectDir))
        .collect(toList());
  }

  private static Profile convertToMavenProfile(MavenProfile mavenProfile, File projectDir) {
    Profile result = new Profile();
    result.setId(mavenProfile.getId());
    result.setSource(mavenProfile.getSource());
    result.setModules(mavenProfile.getModules());
    result.setProperties(mavenProfile.getProperties());
    result.setBuild(new Build());
    result.setActivation(convertToMavenActivation(mavenProfile.getActivation()));
    convertToMavenBuildBase(mavenProfile.getBuild(), result.getBuild(), projectDir);
    return result;
  }

  private static Activation convertToMavenActivation(MavenActivation activation) {
    if (activation != null) {
      Activation result = new Activation();
      result.setActiveByDefault(activation.isActiveByDefault());
      result.setFile(convertToMavenActivationFile(activation.getFile()));
      result.setJdk(activation.getJdk());
      result.setOs(convertToMavenActivationOs(activation.getOs()));
      result.setProperty(convertToMavenActivationProperty(activation.getProperty()));
      return result;
    }
    return null;
  }

  private static ActivationProperty convertToMavenActivationProperty(
      MavenActivationProperty property) {
    if (property != null) {
      ActivationProperty result = new ActivationProperty();
      result.setName(property.getName());
      result.setValue(property.getValue());
      return result;
    }
    return null;
  }

  private static ActivationOS convertToMavenActivationOs(MavenActivationOS os) {
    if (os != null) {
      ActivationOS result = new ActivationOS();
      result.setArch(os.getArch());
      result.setFamily(os.getFamily());
      result.setName(os.getName());
      result.setVersion(os.getVersion());
      return result;
    }
    return null;
  }

  private static ActivationFile convertToMavenActivationFile(MavenActivationFile file) {
    if (file != null) {
      ActivationFile result = new ActivationFile();
      result.setExists(file.getExist());
      result.setMissing(file.getMissing());
      return result;
    }
    return null;
  }

  private static void convertToMavenBuildBase(
      MavenBuildBase modelBuild, BuildBase build, File projectDir) {
    build.setFinalName(modelBuild.getFinalName());
    build.setDefaultGoal(modelBuild.getDefaultGoal());
    build.setDirectory(relativize(projectDir, modelBuild.getDirectory()));
    build.setFilters(modelBuild.getFilters());
    build.setResources(convertToMavenResources(modelBuild.getResources(), projectDir));
    build.setTestResources(convertToMavenResources(modelBuild.getTestResources(), projectDir));
  }

  private static List<Resource> convertToMavenResources(
      List<MavenResource> resources, File projectDir) {
    return resources
        .stream()
        .map(resource -> convertToMavenResource(resource, projectDir))
        .collect(toList());
  }

  private static Resource convertToMavenResource(MavenResource mavenResource, File projectDir) {
    Resource resource = new Resource();
    resource.setDirectory(relativize(projectDir, mavenResource.getDirectory()));
    resource.setFiltering(mavenResource.isFiltered());
    resource.setTargetPath(mavenResource.getTargetPath());
    resource.setIncludes(mavenResource.getIncludes());
    resource.setExcludes(mavenResource.getExcludes());
    return resource;
  }

  public static Repository convertToMavenRepository(MavenRemoteRepository repository) {
    Repository result = new Repository();
    result.setId(repository.getId());
    result.setName(repository.getName());
    result.setUrl(repository.getUrl());
    if (repository.getLayout() == null) {
      result.setLayout("default");
    } else {
      result.setLayout(repository.getLayout());
    }
    if (repository.getSnapshotsPolicy() != null) {
      result.setSnapshots(convertToMavenPolicy(repository.getSnapshotsPolicy()));
    }

    if (repository.getReleasesPolicy() != null) {
      result.setReleases(convertToMavenPolicy(repository.getReleasesPolicy()));
    }
    return result;
  }

  private static RepositoryPolicy convertToMavenPolicy(MavenRepositoryPolicy policy) {
    RepositoryPolicy result = new RepositoryPolicy();
    result.setChecksumPolicy(policy.getChecksumPolicy());
    result.setEnabled(policy.isEnabled());
    result.setUpdatePolicy(policy.getUpdatePolicy());
    return result;
  }

  private static String relativize(File basePath, String rawPath) {
    if (rawPath == null) {
      return null;
    }

    if (rawPath.isEmpty()) {
      return rawPath;
    }

    return rawPath.startsWith(File.separator)
        ? basePath.toURI().relativize(new File(rawPath).toURI()).getPath()
        : rawPath;
  }
}
