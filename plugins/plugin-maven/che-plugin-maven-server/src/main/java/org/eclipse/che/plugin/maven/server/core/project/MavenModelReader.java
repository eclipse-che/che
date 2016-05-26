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
package org.eclipse.che.plugin.maven.server.core.project;

import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.plugin.maven.server.MavenServerManager;
import org.eclipse.che.plugin.maven.server.MavenServerWrapper;
import org.eclipse.che.ide.maven.tools.Build;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.ide.maven.tools.Parent;
import org.eclipse.che.ide.maven.tools.Resource;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.data.MavenParent;
import org.eclipse.che.maven.data.MavenProblemType;
import org.eclipse.che.maven.data.MavenProjectProblem;
import org.eclipse.che.maven.data.MavenResource;
import org.eclipse.che.maven.server.MavenProjectInfo;
import org.eclipse.che.maven.server.MavenServerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * @author Evgen Vidolob
 */
public class MavenModelReader {
    private static final Logger LOG = LoggerFactory.getLogger(MavenModelReader.class);

    public MavenModelReaderResult resolveMavenProject(File pom, MavenServerWrapper mavenServer, List<String> activeProfiles,
                                                      List<String> inactiveProfiles, MavenServerManager serverManager) {
        try {
            MavenServerResult resolveProject = mavenServer.resolveProject(pom, activeProfiles, inactiveProfiles);
            MavenProjectInfo projectInfo = resolveProject.getProjectInfo();
            if (projectInfo != null) {
                return new MavenModelReaderResult(projectInfo.getMavenModel(),
                                                  projectInfo.getActiveProfiles(),
                                                  Collections.emptyList(),
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
                readMavenProject.getProblems().add(MavenProjectProblem.newStructureProblem(pom.getPath(), message));
            } else {
                readMavenProject.getProblems().add(MavenProjectProblem.newSyntaxProblem(pom.getPath(), MavenProblemType.SYNTAX));
            }
            return readMavenProject;
        }
    }

    public MavenModelReaderResult readMavenProject(File pom, MavenServerManager serverManager) {
        Pair<ModelReadingResult, Pair<List<String>, List<String>>> readResult = readModel(pom);
        MavenModel model = readResult.first.model;
        model = serverManager.interpolateModel(model, pom.getParentFile());

        Pair<List<String>, List<String>> profilesPair = readResult.second;
        return new MavenModelReaderResult(model,
                                          profilesPair.first,
                                          profilesPair.first,
                                          readResult.first.problems,
                                          Collections.emptySet());
    }

    private Pair<ModelReadingResult, Pair<List<String>, List<String>>> readModel(File pom) {
        ModelReadingResult readingResult = doRead(pom);
        //TODO resolve parent pom and profiles

        return Pair.of(readingResult, Pair.of(Collections.emptyList(), Collections.emptyList()));
    }

    private ModelReadingResult doRead(File pom) {
        List<MavenProjectProblem> problems = new ArrayList<>();
        Set<String> enabledProfiles = new HashSet<>();
        MavenModel result = new MavenModel();

        Model model = null;
        try {
            model = Model.readFrom(pom);
        } catch (IOException e) {
            problems.add(MavenProjectProblem.newProblem(pom.getPath(), e.getMessage(), MavenProblemType.SYNTAX));
        }

        if (model == null) {
            result.setMavenKey(new MavenKey("unknown", "unknown", "unknown"));
            result.setPackaging("jar");
            return new ModelReadingResult(result, problems, enabledProfiles);
        }

        MavenKey parentKey;

        if (model.getParent() == null) {
            parentKey = new MavenKey("unknown", "unknown", "unknown");
            result.setParent(new MavenParent(parentKey, "../pom.xml"));
        } else {
            Parent modelParent = model.getParent();
            parentKey = new MavenKey(modelParent.getGroupId(), modelParent.getArtifactId(), modelParent.getVersion());
            MavenParent parent =
                    new MavenParent(parentKey, modelParent.getRelativePath());
            result.setParent(parent);
        }

        MavenKey mavenKey =
                new MavenKey(getNotNull(model.getGroupId(), parentKey.getGroupId()), model.getArtifactId(),
                             getNotNull(model.getVersion(), parentKey.getVersion()));
        result.setMavenKey(mavenKey);

        result.setPackaging(model.getPackaging() == null ? "jar" : model.getPackaging());
        result.setName(model.getName());
        result.setModules(model.getModules() == null ? Collections.emptyList() : new ArrayList<>(model.getModules()));

        Map<String, String> properties = model.getProperties();
        Properties prop = new Properties();
        if (properties != null) {
            prop.putAll(properties);
        }
        result.setProperties(prop);

        Build build = model.getBuild();
        if (build == null) {
            result.getBuild().setSources(Collections.singletonList("src/main/java"));
            result.getBuild().setTestSources(Collections.singletonList("src/test/java"));
            result.getBuild().setResources(Collections.singletonList(
                    new MavenResource("src/main/resources", false, null, Collections.emptyList(), Collections.emptyList())));
            result.getBuild().setTestResources(Collections.singletonList(
                    new MavenResource("src/test/resources", false, null, Collections.emptyList(), Collections.emptyList())));
        } else {
            String sourceDirectory = build.getSourceDirectory();
            if (sourceDirectory == null) {
                sourceDirectory = "src/main/java";
            }
            String testSourceDirectory = build.getTestSourceDirectory();
            if (testSourceDirectory == null) {
                testSourceDirectory = "src/test/java";
            }
            result.getBuild().setSources(Collections.singletonList(sourceDirectory));
            result.getBuild().setTestSources(Collections.singletonList(testSourceDirectory));
            result.getBuild().setResources(convertResources(build.getResources()));
        }
        //TODO add profiles
        return new ModelReadingResult(result, problems, enabledProfiles);
    }

    private String getNotNull(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private List<MavenResource> convertResources(List<Resource> resources) {
        return resources.stream()
                        .map(resource -> new MavenResource(resource.getDirectory(),
                                                           resource.isFiltering(),
                                                           resource.getTargetPath(),
                                                           resource.getIncludes(),
                                                           resource.getExcludes()))
                        .collect(Collectors.toList());
    }

    private static class ModelReadingResult {
        MavenModel                model;
        List<MavenProjectProblem> problems;
        Set<String>               enabledProfiles;

        public ModelReadingResult(MavenModel model, List<MavenProjectProblem> problems, Set<String> enabledProfiles) {
            this.model = model;
            this.problems = problems;
            this.enabledProfiles = enabledProfiles;
        }
    }
}
