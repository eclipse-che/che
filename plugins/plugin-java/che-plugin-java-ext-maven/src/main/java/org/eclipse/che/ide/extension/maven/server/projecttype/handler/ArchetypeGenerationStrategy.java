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
package org.eclipse.che.ide.extension.maven.server.projecttype.handler;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.util.DownloadPlugin;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.HttpDownloadPlugin;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.vfs.server.VirtualFileSystem;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.generator.archetype.ArchetypeGenerator;
import org.eclipse.che.generator.archetype.dto.GenerationTaskDescriptor;
import org.eclipse.che.generator.archetype.dto.MavenArchetype;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.name.Named;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.eclipse.che.generator.archetype.dto.GenerationTaskDescriptor.Status.FAILED;
import static org.eclipse.che.generator.archetype.dto.GenerationTaskDescriptor.Status.IN_PROGRESS;
import static org.eclipse.che.generator.archetype.dto.GenerationTaskDescriptor.Status.SUCCESSFUL;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_GENERATION_STRATEGY;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * Generates Maven project using maven-archetype-plugin.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ArchetypeGenerationStrategy implements GeneratorStrategy {
    private static final long CHECK_GENERATION_STATUS_DELAY = 1000;
    private final VirtualFileSystemRegistry vfsRegistry;
    private ArchetypeGenerator archetypeGenerator;
    private final DownloadPlugin downloadPlugin = new HttpDownloadPlugin();
    private ExecutorService executor;

    @Inject
    public ArchetypeGenerationStrategy(VirtualFileSystemRegistry vfsRegistry,
                                       ArchetypeGenerator archetypeGenerator) {
        // As a temporary solution we're using first slave builder URL
        // in order to get archetype-generator service URL.
        this.vfsRegistry = vfsRegistry;
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

            archetypeGenerator.generateFromArchetype(archetype, groupId.getList().get(0), artifactId.getList().get(0), version.getList().get(0));
//            Callable<GenerationTaskDescriptor> callable =
//                    createGenerationTask(archetype, groupId.getString(), artifactId.getString(), version.getString());
//            final GenerationTaskDescriptor task = executor.submit(callable).get();
//            if (task.getStatus() == SUCCESSFUL) {
//                final File downloadFolder = Files.createTempDirectory("generated-project-").toFile();
//                final File generatedProject = new File(downloadFolder, "project.zip");
//                downloadGeneratedProject(task, generatedProject);
//                importZipToFolder(generatedProject, baseFolder);
//                FileCleaner.addFile(downloadFolder);
//            } else if (task.getStatus() == FAILED) {
//                throw new ServerException(task.getReport().isEmpty() ? "Failed to generate project: " : task.getReport());
//            }
    }

//    private Callable<GenerationTaskDescriptor> createGenerationTask(final MavenArchetype archetype,
//                                                                    final String groupId, final String artifactId, final String version) {
//        return new Callable<GenerationTaskDescriptor>() {
//            @Override
//            public GenerationTaskDescriptor call() throws Exception {
//                final ValueHolder<String> statusUrlHolder = new ValueHolder<>();
//                try {
//                    GenerationTaskDescriptor task =
//                            HttpJsonHelper.post(GenerationTaskDescriptor.class, generatorServiceUrl + "/generate", archetype,
//                                                Pair.of("groupId", groupId),
//                                                Pair.of("artifactId", artifactId),
//                                                Pair.of("version", version));
//                    statusUrlHolder.set(task.getStatusUrl());
//                } catch (IOException | UnauthorizedException | NotFoundException e) {
//                    throw new ServerException(e);
//                }
//
//                final String statusUrl = statusUrlHolder.get();
//                try {
//                    for (; ; ) {
//                        if (Thread.currentThread().isInterrupted()) {
//                            return null;
//                        }
//                        try {
//                            Thread.sleep(CHECK_GENERATION_STATUS_DELAY);
//                        } catch (InterruptedException e) {
//                            return null;
//                        }
//                        GenerationTaskDescriptor generateTask = HttpJsonHelper.get(GenerationTaskDescriptor.class, statusUrl);
//                        if (IN_PROGRESS != generateTask.getStatus()) {
//                            return generateTask;
//                        }
//                    }
//                } catch (IOException | ServerException | NotFoundException | UnauthorizedException | ForbiddenException |
//                        ConflictException e) {
//                    throw new ServerException(e);
//                }
//            }
//        };
//    }
//
//    private void downloadGeneratedProject(GenerationTaskDescriptor task, File file) throws IOException {
//        downloadPlugin.download(task.getDownloadUrl(), file.getParentFile(), file.getName(), true);
//    }
//
//    private void importZipToFolder(File file, FolderEntry baseFolder)
//            throws ForbiddenException, ServerException, NotFoundException, ConflictException, IOException {
//        final VirtualFileSystem vfs = vfsRegistry.getProvider(baseFolder.getWorkspace()).newInstance(null);
//        vfs.importZip(baseFolder.getVirtualFile().getId(), Files.newInputStream(file.toPath()), true, false);
//    }
}
