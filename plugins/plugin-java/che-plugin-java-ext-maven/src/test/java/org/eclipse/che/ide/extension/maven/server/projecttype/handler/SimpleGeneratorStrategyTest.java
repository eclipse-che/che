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

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.project.server.FileEntry;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
// TODO need to rework in according to the new Project API
public class SimpleGeneratorStrategyTest {

    private ProjectManager    pm;
    private GeneratorStrategy simple;

//    @Mock
//    private Provider<AttributeFilter> filterProvider;
//    @Mock
//    private AttributeFilter           filter;
    @Mock
    private HttpJsonRequestFactory    httpJsonRequestFactory;
    @Mock
    private HttpJsonResponse          httpJsonResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
//        when(filterProvider.get()).thenReturn(filter);
        simple = new SimpleGeneratorStrategy();
    }

    @Test
    public void testGetId() throws Exception {
        Assert.assertEquals(MavenAttributes.SIMPLE_GENERATION_STRATEGY, simple.getId());
    }

    @Test
    public void testGeneratingProject() throws Exception {
        prepareProject();
        final Path pomXml = Paths.get(Thread.currentThread().getContextClassLoader().getResource("test-pom.xml").toURI());

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(MavenAttributes.ARTIFACT_ID, new AttributeValue("my_artifact"));
        attributeValues.put(MavenAttributes.GROUP_ID, new AttributeValue("my_group"));
        attributeValues.put(MavenAttributes.PACKAGING, new AttributeValue("jar"));
        attributeValues.put(MavenAttributes.VERSION, new AttributeValue("1.0-SNAPSHOT"));
        attributeValues.put(MavenAttributes.SOURCE_FOLDER, new AttributeValue("src/main/java"));
        attributeValues.put(MavenAttributes.TEST_SOURCE_FOLDER, new AttributeValue("src/test/java"));

        FolderEntry folder = pm.getProject("my_project").getBaseFolder();

        simple.generateProject(folder, attributeValues, null);

        VirtualFileEntry pomFile = pm.getProject("my_project").getBaseFolder().getChild("pom.xml");
        Assert.assertTrue(pomFile.isFile());
        Assert.assertEquals(new String(((FileEntry)pomFile).contentAsBytes()), new String(Files.readAllBytes(pomXml)));

        VirtualFileEntry srcFolder = pm.getProject("my_project").getBaseFolder().getChild("src/main/java");
        Assert.assertTrue(srcFolder.isFolder());
        VirtualFileEntry testFolder = pm.getProject("my_project").getBaseFolder().getChild("src/test/java");
        Assert.assertTrue(testFolder.isFolder());
    }

    private void prepareProject() throws Exception {
        final String vfsUser = "dev";
        final Set<String> vfsUserGroups = new LinkedHashSet<>(Collections.singletonList("workspace/developer"));

        Set<ProjectTypeDef> pts = new HashSet<>();
        final ProjectTypeDef pt = new ProjectTypeDef("mytype", "mytype type", true, false) {
        };
        pts.add(pt);

        final ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(pts);

        final EventService eventService = new EventService();
//        final VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
//        final MemoryFileSystemProvider memoryFileSystemProvider =
//                new MemoryFileSystemProvider(workspace,
//                                             eventService,
//                                             new VirtualFileSystemUserContext() {
//                    @Override
//                    public VirtualFileSystemUser getVirtualFileSystemUser() {
//                        return new VirtualFileSystemUser(vfsUser, vfsUserGroups);
//                    }
//                },
//                                             vfsRegistry,
//                                             SystemPathsFilter.ANY);
//        vfsRegistry.registerProvider(workspace, memoryFileSystemProvider);

        UsersWorkspaceDto usersWorkspaceMock = mock(UsersWorkspaceDto.class);
        final ProjectConfigDto projectConfigDto = DtoFactory.getInstance().createDto(ProjectConfigDto.class).withPath("/my_project");
        when(usersWorkspaceMock.getProjects()).thenReturn(Collections.singletonList(projectConfigDto));

        ProjectHandlerRegistry handlerRegistry = new ProjectHandlerRegistry(new HashSet<>());

//        pm = new ProjectManager(vfsRegistry,
//                                       eventService,
//                                       projectTypeRegistry,
//                                       handlerRegistry,
//                                       filterProvider,
//                                       API_ENDPOINT,
//                                       httpJsonRequestFactory);

//        HttpJsonRequest httpJsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
//        when(httpJsonRequestFactory.fromLink(eq(DtoFactory.newDto(Link.class)
//                                                          .withMethod("PUT")
//                                                          .withHref(API_ENDPOINT + "/workspace/" + workspace + "/project"))))
//                .thenReturn(httpJsonRequest);
//        when(httpJsonRequestFactory.fromLink(eq(DtoFactory.newDto(Link.class)
//                                                          .withMethod("GET")
//                                                          .withHref(API_ENDPOINT + "/workspace/" + workspace))))
//                .thenReturn(httpJsonRequest);
//        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
        when(httpJsonResponse.asDto(UsersWorkspaceDto.class)).thenReturn(usersWorkspaceMock);

        pm.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                   .withType(pt.getId())
                                   .withName("my_project")
                                   .withPath("/my_project"), null);
    }
}