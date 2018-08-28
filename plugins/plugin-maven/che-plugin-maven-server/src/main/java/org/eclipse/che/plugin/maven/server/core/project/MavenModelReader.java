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
package org.eclipse.che.plugin.maven.server.core.project;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_OUTPUT_DIRECTORY;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_RESOURCES_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_OUTPUT_DIRECTORY;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_RESOURCES_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.UNKNOWN_VALUE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.xml.XMLTreeException;
import org.eclipse.che.ide.maven.tools.Activation;
import org.eclipse.che.ide.maven.tools.ActivationFile;
import org.eclipse.che.ide.maven.tools.ActivationOS;
import org.eclipse.che.ide.maven.tools.ActivationProperty;
import org.eclipse.che.ide.maven.tools.Build;
import org.eclipse.che.ide.maven.tools.Dependency;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.ide.maven.tools.Parent;
import org.eclipse.che.ide.maven.tools.Plugin;
import org.eclipse.che.ide.maven.tools.Profile;
import org.eclipse.che.ide.maven.tools.Resource;
import org.eclipse.che.maven.data.MavenActivation;
import org.eclipse.che.maven.data.MavenActivationFile;
import org.eclipse.che.maven.data.MavenActivationOS;
import org.eclipse.che.maven.data.MavenActivationProperty;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenBuild;
import org.eclipse.che.maven.data.MavenExplicitProfiles;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.data.MavenParent;
import org.eclipse.che.maven.data.MavenPlugin;
import org.eclipse.che.maven.data.MavenProblemType;
import org.eclipse.che.maven.data.MavenProfile;
import org.eclipse.che.maven.data.MavenProjectProblem;
import org.eclipse.che.maven.data.MavenResource;
import org.eclipse.che.maven.server.MavenProjectInfo;
import org.eclipse.che.maven.server.MavenServerResult;
import org.eclipse.che.maven.server.ProfileApplicationResult;
import org.eclipse.che.plugin.maven.server.MavenServerManager;
import org.eclipse.che.plugin.maven.server.MavenServerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
public class MavenModelReader {
  private static final Logger LOG = LoggerFactory.getLogger(MavenModelReader.class);

  public MavenModelReaderResult resolveMavenProject(
      File pom,
      MavenServerWrapper mavenServer,
      List<String> activeProfiles,
      List<String> inactiveProfiles,
      MavenServerManager serverManager) {
    try {
      MavenServerResult resolveProject =
          mavenServer.resolveProject(pom, activeProfiles, inactiveProfiles);
      MavenProjectInfo projectInfo = resolveProject.getProjectInfo();
      if (projectInfo != null) {
        return new MavenModelReaderResult(
            projectInfo.getMavenModel(),
            projectInfo.getActiveProfiles(),
            emptyList(),
            resolveProject.getProblems(),
            resolveProject.getUnresolvedArtifacts());
      } else {
        MavenModelReaderResult readMavenProject = readMavenProject(pom, serverManager);
        readMavenProject.getProblems().addAll(resolveProject.getProblems());
        readMavenProject.getUnresolvedArtifacts().addAll(resolveProject.getUnresolvedArtifacts());
        return readMavenProject;
      }

    } catch (Throwable t) {
      String message = t.getMessage();
      LOG.info(message, t);
      MavenModelReaderResult readMavenProject = readMavenProject(pom, serverManager);
      if (message != null) {
        readMavenProject
            .getProblems()
            .add(MavenProjectProblem.newStructureProblem(pom.getPath(), message));
      } else {
        readMavenProject
            .getProblems()
            .add(MavenProjectProblem.newSyntaxProblem(pom.getPath(), MavenProblemType.SYNTAX));
      }
      return readMavenProject;
    }
  }

  public MavenModelReaderResult readMavenProject(File pom, MavenServerManager serverManager) {
    Pair<ModelReadingResult, Pair<List<String>, List<String>>> readResult =
        readModel(pom, serverManager);
    MavenModel model = readResult.first.model;
    model = serverManager.interpolateModel(model, pom.getParentFile());

    Pair<List<String>, List<String>> profilesPair = readResult.second;
    return new MavenModelReaderResult(
        model,
        profilesPair.first,
        profilesPair.second,
        readResult.first.problems,
        Collections.emptySet());
  }

  private Pair<ModelReadingResult, Pair<List<String>, List<String>>> readModel(
      File pom, MavenServerManager serverManager) {
    ModelReadingResult readingResult = doRead(pom);
    // TODO resolve parent pom

    ProfileApplicationResult applied =
        serverManager.applyProfiles(
            readingResult.model, pom.getParentFile(), MavenExplicitProfiles.NONE, emptyList());
    final MavenModel model = applied.getModel();
    updateModelBody(model);

    final MavenExplicitProfiles activatedProfiles = applied.getActivatedProfiles();

    final ArrayList<String> disabledProfiles =
        new ArrayList<>(activatedProfiles.getDisabledProfiles().size());
    disabledProfiles.addAll(activatedProfiles.getDisabledProfiles());

    final ArrayList<String> enableProfiles =
        new ArrayList<>(activatedProfiles.getEnabledProfiles().size());
    enableProfiles.addAll(activatedProfiles.getEnabledProfiles());

    return Pair.of(
        new ModelReadingResult(model, readingResult.problems, readingResult.enabledProfiles),
        Pair.of(enableProfiles, disabledProfiles));
  }

  private ModelReadingResult doRead(File pom) {
    List<MavenProjectProblem> problems = new ArrayList<>();
    Set<String> enabledProfiles = new HashSet<>();
    MavenModel result = new MavenModel();

    fillModelByDefaults(result);

    Model model = null;
    try {
      model = Model.readFrom(pom);
    } catch (IOException e) {
      problems.add(
          MavenProjectProblem.newProblem(pom.getPath(), e.getMessage(), MavenProblemType.SYNTAX));
    } catch (XMLTreeException xmlExc) {
      problems.add(
          MavenProjectProblem.newProblem(
              pom.getPath(), xmlExc.getMessage(), MavenProblemType.STRUCTURE));
    } catch (Exception exc) {
      problems.add(
          MavenProjectProblem.newProblem(
              pom.getPath(), exc.getMessage(), MavenProblemType.STRUCTURE));
    }

    if (model == null) {
      return new ModelReadingResult(result, problems, enabledProfiles);
    }

    final MavenKey parentKey;

    if (model.getParent() == null) {
      parentKey = result.getParent().getMavenKey();
    } else {
      Parent modelParent = model.getParent();
      parentKey =
          new MavenKey(
              modelParent.getGroupId(), modelParent.getArtifactId(), modelParent.getVersion());
      MavenParent parent = new MavenParent(parentKey, modelParent.getRelativePath());
      result.setParent(parent);
    }

    final MavenKey mavenKey =
        new MavenKey(
            getNotNull(model.getGroupId(), parentKey.getGroupId()),
            model.getArtifactId(),
            getNotNull(model.getVersion(), parentKey.getVersion()));
    result.setMavenKey(mavenKey);

    if (model.getPackaging() != null) {
      result.setPackaging(model.getPackaging());
    }
    result.setName(model.getName());

    final List<String> modules = model.getModules();
    if (modules != null) {
      result.setModules(new ArrayList<>(model.getModules()));
    }

    Map<String, String> properties = model.getProperties();
    Properties prop = new Properties();
    if (properties != null) {
      prop.putAll(properties);
    }
    result.setProperties(prop);

    final Build build = model.getBuild();
    if (build != null) {
      final String sourceDirectory = build.getSourceDirectory();
      if (sourceDirectory != null) {
        result.getBuild().setSources(singletonList(sourceDirectory));
      }
      final String testSourceDirectory = build.getTestSourceDirectory();
      if (testSourceDirectory != null) {
        result.getBuild().setTestSources(singletonList(testSourceDirectory));
      }
      result.getBuild().setResources(convertResources(build.getResources()));
      String testOutputDirectory = build.getTestOutputDirectory();
      if (testOutputDirectory != null) {
        result.getBuild().setTestOutputDirectory(testOutputDirectory);
      }

      String outputDirectory = build.getOutputDirectory();
      if (outputDirectory != null) {
        result.getBuild().setOutputDirectory(outputDirectory);
      }
    }

    List<Profile> profiles = model.getProfiles();
    if (profiles != null && !profiles.isEmpty()) {
      List<MavenProfile> resultProfiles = new ArrayList<>(profiles.size());
      for (Profile profile : profiles) {
        enabledProfiles.add(profile.getId());

        MavenProfile resultProfile = new MavenProfile(profile.getId(), Profile.SOURCE_POM);

        setProfileActivation(resultProfile, profile);
        setProfileProperties(resultProfile, profile);
        setProfileModules(resultProfile, profile);
        setProfileBuild(resultProfile, profile);
        setProfileDependencies(resultProfile, profile);

        resultProfiles.add(resultProfile);
      }
      result.setProfiles(resultProfiles);
    }

    return new ModelReadingResult(result, problems, enabledProfiles);
  }

  private void setProfileProperties(MavenProfile resultProfile, Profile profile) {
    Properties resultProperties = new Properties();
    resultProperties.putAll(profile.getProperties());
    resultProfile.setProperties(resultProperties);
  }

  private void updateModelBody(MavenModel model) {
    MavenBuild build = model.getBuild();

    if (build == null) {
      return;
    }

    if (isNullOrEmpty(build.getFinalName())) {
      build.setFinalName("${project.artifactId}-${project.version}");
    }

    if (build.getSources().isEmpty()) {
      build.setSources(singletonList("src/main/java"));
    }
    if (build.getTestSources().isEmpty()) {
      build.setTestSources(singletonList("src/test/java"));
    }

    build.setResources(updateResources(build.getResources(), "src/main/resources"));
    build.setTestResources(updateResources(build.getTestResources(), "src/test/resources"));
    build.setDirectory(isNullOrEmpty(build.getDirectory()) ? "target" : build.getDirectory());
    build.setOutputDirectory(
        isNullOrEmpty(build.getOutputDirectory())
            ? "${project.build.directory}/classes"
            : build.getOutputDirectory());
    build.setTestOutputDirectory(
        isNullOrEmpty(build.getTestOutputDirectory())
            ? "${project.build.directory}/test-classes"
            : build.getTestOutputDirectory());
  }

  private List<MavenResource> updateResources(List<MavenResource> resources, String defaultDir) {
    List<MavenResource> result = new ArrayList<>();
    if (resources.isEmpty()) {
      result.add(createResource(defaultDir));
      return result;
    }

    for (MavenResource each : resources) {
      if (isNullOrEmpty(each.getDirectory())) continue;
      result.add(each);
    }
    return result;
  }

  private MavenResource createResource(String directory) {
    return new MavenResource(directory, false, null, emptyList(), emptyList());
  }

  private void setProfileDependencies(MavenProfile resultProfile, Profile profile) {
    final List<Dependency> dependencies = profile.getDependencies();
    final List<MavenArtifact> mavenArtifacts =
        dependencies
            .stream()
            .map(
                dependency ->
                    new MavenArtifact(
                        dependency.getGroupId(),
                        dependency.getArtifactId(),
                        dependency.getVersion(),
                        dependency.getVersion(),
                        dependency.getType(),
                        dependency.getClassifier(),
                        dependency.getScope(),
                        dependency.isOptional(),
                        dependency.getType(),
                        null,
                        null,
                        false,
                        false))
            .collect(Collectors.toList());

    resultProfile.setDependencies(mavenArtifacts);
  }

  private void setProfileActivation(MavenProfile resultProfile, Profile profile) {
    final Activation activation = profile.getActivation();
    if (activation != null) {
      resultProfile.setActivation(convertActivation(activation));
    }
  }

  private void setProfileBuild(MavenProfile resultProfile, Profile profile) {
    final Build build = profile.getBuild();
    if (build == null) {
      return;
    }
    final List<Plugin> plugins = build.getPlugins();
    if (plugins != null && !plugins.isEmpty()) {
      List<MavenPlugin> mavenPlugins = new ArrayList<>(plugins.size());
      for (Plugin plugin : plugins) {
        mavenPlugins.add(
            new MavenPlugin(
                plugin.getGroupId(),
                plugin.getArtifactId(),
                plugin.getVersion(),
                false,
                null,
                emptyList(),
                emptyList()));
      }
      resultProfile.setPlugins(mavenPlugins);
      resultProfile.getBuild().setFinalName(build.getFinalName());
      resultProfile.getBuild().setDefaultGoal(build.getDefaultGoal());
      resultProfile.getBuild().setOutputDirectory(build.getOutputDirectory());
      resultProfile.getBuild().setTestOutputDirectory(build.getTestOutputDirectory());
      resultProfile.getBuild().setTestOutputDirectory(build.getTestOutputDirectory());
    }
  }

  private void setProfileModules(MavenProfile resultProfile, Profile profile) {
    final List<String> profileModules = profile.getModules();
    if (profileModules != null) {
      resultProfile.setModules(profileModules);
    }
  }

  private void fillModelByDefaults(MavenModel model) {
    model.setMavenKey(new MavenKey(UNKNOWN_VALUE, UNKNOWN_VALUE, UNKNOWN_VALUE));

    final MavenKey parentKey = new MavenKey(UNKNOWN_VALUE, UNKNOWN_VALUE, UNKNOWN_VALUE);
    model.setParent(new MavenParent(parentKey, "../pom.xml"));

    model.setPackaging("jar");

    model.setModules(emptyList());

    final MavenBuild build = model.getBuild();
    build.setSources(singletonList(DEFAULT_SOURCE_FOLDER));
    build.setTestSources(singletonList(DEFAULT_TEST_SOURCE_FOLDER));
    build.setResources(
        singletonList(
            new MavenResource(DEFAULT_RESOURCES_FOLDER, false, null, emptyList(), emptyList())));
    build.setTestResources(
        singletonList(
            new MavenResource(
                DEFAULT_TEST_RESOURCES_FOLDER, false, null, emptyList(), emptyList())));
    build.setOutputDirectory(DEFAULT_OUTPUT_DIRECTORY);
    build.setTestOutputDirectory(DEFAULT_TEST_OUTPUT_DIRECTORY);
  }

  private String getNotNull(String value, String defaultValue) {
    return value == null ? defaultValue : value;
  }

  private List<MavenResource> convertResources(List<Resource> resources) {
    return resources
        .stream()
        .map(
            resource ->
                new MavenResource(
                    resource.getDirectory(),
                    resource.isFiltering(),
                    resource.getTargetPath(),
                    resource.getIncludes(),
                    resource.getExcludes()))
        .collect(Collectors.toList());
  }

  private MavenActivation convertActivation(Activation activation) {
    MavenActivation mavenActivation = new MavenActivation();

    final ActivationOS activationOs = activation.getOs();
    final ActivationFile activationFile = activation.getFile();
    final ActivationProperty activationProperty = activation.getProperty();
    if (activationOs != null) {
      mavenActivation.setOs(
          new MavenActivationOS(
              activationOs.getName(),
              activationOs.getFamily(),
              activationOs.getArch(),
              activationOs.getVersion()));
    }
    if (activationFile != null) {
      mavenActivation.setFile(
          new MavenActivationFile(activationFile.getExist(), activationFile.getMissing()));
    }
    if (activationProperty != null) {
      mavenActivation.setProperty(
          new MavenActivationProperty(activationProperty.getName(), activationProperty.getValue()));
    }

    mavenActivation.setJdk(activation.getJdk());
    mavenActivation.setActiveByDefault(parseBoolean(activation.isActiveByDefault()));

    return mavenActivation;
  }

  private static class ModelReadingResult {
    MavenModel model;
    List<MavenProjectProblem> problems;
    Set<String> enabledProfiles;

    public ModelReadingResult(
        MavenModel model, List<MavenProjectProblem> problems, Set<String> enabledProfiles) {
      this.model = model;
      this.problems = problems;
      this.enabledProfiles = enabledProfiles;
    }
  }
}
