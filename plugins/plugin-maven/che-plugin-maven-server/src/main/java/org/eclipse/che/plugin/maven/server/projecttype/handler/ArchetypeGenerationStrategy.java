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
package org.eclipse.che.plugin.maven.server.projecttype.handler;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.getFirst;
import static java.io.File.separator;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARCHETYPE_GENERATION_STRATEGY;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_VERSION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.VERSION;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.ide.maven.tools.MavenArtifact;
import org.eclipse.che.plugin.maven.generator.archetype.ArchetypeGenerator;
import org.eclipse.che.plugin.maven.generator.archetype.MavenArchetypeImpl;
import org.eclipse.che.plugin.maven.shared.MavenArchetype;

/**
 * Generates Maven project using maven-archetype-plugin.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ArchetypeGenerationStrategy implements GeneratorStrategy {

  private final ArchetypeGenerator archetypeGenerator;
  private final RootDirPathProvider rootDirPathProvider;

  @Inject
  public ArchetypeGenerationStrategy(
      ArchetypeGenerator archetypeGenerator, RootDirPathProvider rootDirPathProvider) {
    this.archetypeGenerator = archetypeGenerator;
    this.rootDirPathProvider = rootDirPathProvider;
  }

  public String getId() {
    return ARCHETYPE_GENERATION_STRATEGY;
  }

  @Override
  public void generateProject(
      String projectPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ServerException {

    AttributeValue artifactId = attributes.get(ARTIFACT_ID);
    AttributeValue groupId = attributes.get(GROUP_ID);
    AttributeValue version = attributes.get(VERSION);
    if (groupId == null || artifactId == null || version == null) {
      throw new ServerException("Missed some required attribute (groupId, artifactId or version)");
    }

    String archetypeGroupId = null;
    String archetypeArtifactId = null;
    String archetypeVersion = null;
    String archetypeRepository = null;
    Map<String, String> archetypeProperties = new HashMap<>();
    options.remove(
        "type"); // TODO: remove prop 'type' now it use only for detecting generation strategy
    for (Entry<String, String> entry : options.entrySet()) {
      switch (entry.getKey()) {
        case "archetypeGroupId":
          archetypeGroupId = entry.getValue();
          break;
        case "archetypeArtifactId":
          archetypeArtifactId = entry.getValue();
          break;
        case "archetypeVersion":
          archetypeVersion = entry.getValue();
          break;
        case "archetypeRepository":
          archetypeRepository = entry.getValue();
          break;
        default:
          archetypeProperties.put(entry.getKey(), entry.getValue());
      }
    }

    if (isNullOrEmpty(archetypeGroupId)
        || isNullOrEmpty(archetypeArtifactId)
        || isNullOrEmpty(archetypeVersion)) {
      throw new ServerException(
          "Missed some required option (archetypeGroupId, archetypeArtifactId or archetypeVersion)");
    }

    Path projectsParentPath = Paths.get(rootDirPathProvider.get(), projectPath).getParent();
    if (Files.exists(projectsParentPath.resolve("pom.xml"))) {
      throw new ServerException("Parent path witch contains 'pom.xml' file is not allowed");
    }

    MavenArchetype mavenArchetype =
        new MavenArchetypeImpl(
            archetypeGroupId,
            archetypeArtifactId,
            archetypeVersion,
            archetypeRepository,
            archetypeProperties);

    String projectName = projectPath.substring(projectPath.lastIndexOf(separator) + 1);
    final MavenArtifact mavenArtifact = new MavenArtifact();
    mavenArtifact.setGroupId(getFirst(groupId.getList(), projectName));
    mavenArtifact.setArtifactId(getFirst(artifactId.getList(), projectName));
    mavenArtifact.setVersion(getFirst(version.getList(), DEFAULT_VERSION));
    archetypeGenerator.generateFromArchetype(
        projectName, projectsParentPath.toFile(), mavenArchetype, mavenArtifact);
  }
}
