/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
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

  @Inject
  public ArchetypeGenerationStrategy(ArchetypeGenerator archetypeGenerator) throws ServerException {
    this.archetypeGenerator = archetypeGenerator;
  }

  public String getId() {
    return ARCHETYPE_GENERATION_STRATEGY;
  }

  @Override
  public void generateProject(
      String projectPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException {

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

    MavenArchetype mavenArchetype =
        new MavenArchetypeImpl(
            archetypeGroupId,
            archetypeArtifactId,
            archetypeVersion,
            archetypeRepository,
            archetypeProperties);

    String projectName = projectPath.substring(projectPath.lastIndexOf(separator));
    final MavenArtifact mavenArtifact = new MavenArtifact();
    mavenArtifact.setGroupId(getFirst(groupId.getList(), projectName));
    mavenArtifact.setArtifactId(getFirst(artifactId.getList(), projectName));
    mavenArtifact.setVersion(getFirst(version.getList(), DEFAULT_VERSION));
    archetypeGenerator.generateFromArchetype(new File("/projects"), mavenArchetype, mavenArtifact);
  }
}
