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
package org.eclipse.che.plugin.maven.generator.archetype;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.maven.generator.archetype.dto.GenerationTaskDescriptor;
import org.eclipse.che.plugin.maven.generator.archetype.dto.MavenArchetype;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class ArchetypeGeneratorServiceTest {
    private final static long taskId = 1;
    @Mock
    private ArchetypeGenerator archetypeGenerator;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private ArchetypeGenerator.GenerationTask taskMock;

    @InjectMocks
    private ArchetypeGeneratorService service;

    @Before
    public void setUp() throws Exception {
        doReturn(new UriBuilderImpl().uri(URI.create("http://localhost:8080"))).when(uriInfo).getBaseUriBuilder();
        when(taskMock.getId()).thenReturn(taskId);
    }

    @Test
    public void testGenerate() throws Exception {
        MavenArchetype archetype = DtoFactory.getInstance().createDto(MavenArchetype.class)
                                             .withGroupId("archetypeGroupId")
                                             .withArtifactId("archetypeArtifactId")
                                             .withVersion("archetypeVersion");

        when(archetypeGenerator.generateFromArchetype(anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(taskMock);

        GenerationTaskDescriptor task = service.generate(uriInfo, "groupId", "artifactId", "version", archetype);

        verify(archetypeGenerator).generateFromArchetype(eq(archetype), eq("groupId"), eq("artifactId"), eq("version"));
        Assert.assertEquals("http://localhost:8080/generator-archetype/status/" + taskId, task.getStatusUrl());
    }

    @Test
    public void testGetStatusWhenTaskIsNotDone() throws Exception {
        doReturn(false).when(taskMock).isDone();
        doReturn(taskMock).when(archetypeGenerator).getTaskById(anyLong());

        GenerationTaskDescriptor task = service.getStatus(uriInfo, String.valueOf(taskId));

        Assert.assertEquals(GenerationTaskDescriptor.Status.IN_PROGRESS, task.getStatus());
    }

    @Test
    public void testGetStatusWhenTaskIsSuccessful() throws Exception {
        doReturn(true).when(taskMock).isDone();

        GenerationResult generationResult = mock(GenerationResult.class);
        doReturn(true).when(generationResult).isSuccessful();

        doReturn(generationResult).when(taskMock).getResult();
        doReturn(taskMock).when(archetypeGenerator).getTaskById(anyLong());

        GenerationTaskDescriptor task = service.getStatus(uriInfo, String.valueOf(taskId));

        Assert.assertEquals(GenerationTaskDescriptor.Status.SUCCESSFUL, task.getStatus());
        Assert.assertEquals("http://localhost:8080/generator-archetype/download/1", task.getDownloadUrl());
    }

    @Test
    public void testGetStatusWhenTaskIsFailed() throws Exception {
        final Path testLogFile = Paths.get(Thread.currentThread().getContextClassLoader().getResource("test.log").toURI());
        doReturn(true).when(taskMock).isDone();

        GenerationResult generationResult = mock(GenerationResult.class);
        doReturn(false).when(generationResult).isSuccessful();
        doReturn(new File(testLogFile.toString())).when(generationResult).getGenerationReport();

        doReturn(generationResult).when(taskMock).getResult();
        doReturn(taskMock).when(archetypeGenerator).getTaskById(anyLong());

        GenerationTaskDescriptor task = service.getStatus(uriInfo, String.valueOf(taskId));

        Assert.assertEquals(GenerationTaskDescriptor.Status.FAILED, task.getStatus());
        Assert.assertEquals(new String(Files.readAllBytes(testLogFile)), task.getReport());
    }

    @Test(expected = ServerException.class)
    public void testGetStatusWithInvalidTaskId() throws Exception {
        doThrow(ServerException.class).when(archetypeGenerator).getTaskById(anyLong());

        service.getStatus(uriInfo, String.valueOf(taskId));
    }
}
