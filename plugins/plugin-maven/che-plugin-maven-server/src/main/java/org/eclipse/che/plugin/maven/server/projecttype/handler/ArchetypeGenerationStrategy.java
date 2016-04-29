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
package org.eclipse.che.plugin.maven.server.projecttype.handler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.maven.generator.archetype.ArchetypeGenerator;
import org.eclipse.che.plugin.maven.generator.archetype.dto.MavenArchetype;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARCHETYPE_GENERATION_STRATEGY;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.VERSION;

/**
 * Generates Maven project using maven-archetype-plugin.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ArchetypeGenerationStrategy implements GeneratorStrategy {

    private ArchetypeGenerator archetypeGenerator;
    private ExecutorService    executor;

    @Inject
    public ArchetypeGenerationStrategy(ArchetypeGenerator archetypeGenerator) {
        this.archetypeGenerator = archetypeGenerator;
    }

    public String getId() {
        return ARCHETYPE_GENERATION_STRATEGY;
    }

    @PostConstruct
    void start() {
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("-ProjectGenerator-maven-archetype-%d")
                                                                           .setDaemon(true).build());
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }

    @Override
    public void generateProject(final FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
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
        options.remove("type"); //TODO: remove prop 'type' now it use only for detecting generation strategy
        for (Map.Entry<String, String> entry : options.entrySet()) {
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

        if (archetypeGroupId == null || archetypeGroupId.isEmpty() ||
            archetypeArtifactId == null || archetypeArtifactId.isEmpty() ||
            archetypeVersion == null || archetypeVersion.isEmpty()) {
            throw new ServerException("Missed some required option (archetypeGroupId, archetypeArtifactId or archetypeVersion)");
        }

        final MavenArchetype archetype = DtoFactory.getInstance().createDto(MavenArchetype.class)
                                                   .withGroupId(archetypeGroupId)
                                                   .withArtifactId(archetypeArtifactId)
                                                   .withVersion(archetypeVersion)
                                                   .withRepository(archetypeRepository)
                                                   .withProperties(archetypeProperties);

        archetypeGenerator.generateFromArchetype(archetype,
                                                 groupId.getList().get(0),
                                                 artifactId.getList().get(0),
                                                 version.getList().get(0));
    }
}
