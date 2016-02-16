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
package org.eclipse.che.ide.extension.maven.server.projecttype;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.ValueStorageException;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.commons.test.SelfReturningAnswer;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.server.projecttype.JavaProjectType;
import org.eclipse.che.ide.ext.java.server.projecttype.JavaPropertiesValueProviderFactory;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.GeneratorStrategy;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.MavenProjectGenerator;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
import org.eclipse.che.ide.maven.tools.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** @author gazarenkov */
// TODO need to rework in according to the new Project API
public class MavenProjectTypeTest {

    private ProjectManager  pm;
    private HttpJsonRequest httpJsonRequest;

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
        final String vfsUser = "dev";
        final Set<String> vfsUserGroups = new LinkedHashSet<>(Collections.singletonList("workspace/developer"));

        final EventService eventService = new EventService();

//        VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
//        final MemoryFileSystemProvider memoryFileSystemProvider =
//                new MemoryFileSystemProvider(workspace, eventService, new VirtualFileSystemUserContext() {
//                    @Override
//                    public VirtualFileSystemUser getVirtualFileSystemUser() {
//                        return new VirtualFileSystemUser(vfsUser, vfsUserGroups);
//                    }
//                }, vfsRegistry, SystemPathsFilter.ANY);
//        vfsRegistry.registerProvider(workspace, memoryFileSystemProvider);

        Set<ProjectTypeDef> projTypes = new HashSet<>();
        projTypes.add(new JavaProjectType(new JavaPropertiesValueProviderFactory()));
        projTypes.add(new MavenProjectType(new MavenValueProviderFactory()));

        ProjectTypeRegistry ptRegistry = new ProjectTypeRegistry(projTypes);

        Set<ProjectHandler> handlers = new HashSet<>();
        handlers.add(new MavenProjectGenerator(Collections.<GeneratorStrategy>emptySet()));

        ProjectHandlerRegistry handlerRegistry = new ProjectHandlerRegistry(handlers);

//        pm = new ProjectManager(vfsRegistry,
//                                       eventService,
//                                       ptRegistry,
//                                       handlerRegistry,
//                                       filterProvider,
//                                       API_ENDPOINT,
//                                       httpJsonRequestFactory);

        httpJsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
    }

    @Test
    public void testGetProjectType() throws Exception {
        ProjectTypeDef pt = pm.getProjectTypeRegistry().getProjectType("maven");

        //Assert.assertNotNull(pt);
        Assert.assertTrue(pt.getAttributes().size() > 0);
        Assert.assertTrue(pt.isTypeOf("java"));
    }

    @Test
    public void testMavenProject() throws Exception {
        UsersWorkspaceDto usersWorkspaceMock = mock(UsersWorkspaceDto.class);
//        when(httpJsonRequestFactory.fromLink(eq(DtoFactory.newDto(Link.class)
//                                                          .withMethod("GET")
//                                                          .withHref(API_ENDPOINT + "/workspace/" + workspace))))
//                .thenReturn(httpJsonRequest);
//        when(httpJsonRequestFactory.fromLink(eq(DtoFactory.newDto(Link.class)
//                                                          .withMethod("PUT")
//                                                          .withHref(API_ENDPOINT + "/workspace/" + workspace + "/project"))))
//                .thenReturn(httpJsonRequest);
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
        when(httpJsonResponse.asDto(UsersWorkspaceDto.class)).thenReturn(usersWorkspaceMock);
        final ProjectConfigDto projectConfig = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                         .withName("project")
                                                         .withPath("/myProject")
                                                         .withType(MavenAttributes.MAVEN_ID);
        when(usersWorkspaceMock.getProjects()).thenReturn(Collections.singletonList(projectConfig));

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(MavenAttributes.ARTIFACT_ID, Collections.singletonList("myartifact"));
        attributes.put(MavenAttributes.GROUP_ID, Collections.singletonList("mygroup"));
        attributes.put(MavenAttributes.VERSION, Collections.singletonList("1.0"));
        attributes.put(MavenAttributes.PACKAGING, Collections.singletonList("jar"));

        RegisteredProject project = pm.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                               .withType("maven")
                                                               .withAttributes(attributes)
                                                               .withPath("/myProject")
                                                               .withName("myProject"),
                                                     new HashMap<>(0));

        for (VirtualFileEntry file : project.getBaseFolder().getChildren()) {
            if (file.getName().equals("pom.xml")) {
                Model pom = Model.readFrom(file.getVirtualFile().getContent());
                Assert.assertEquals(pom.getVersion(), "1.0");
            }
        }
    }

    @Test
    public void testEstimation() throws Exception {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(MavenAttributes.ARTIFACT_ID, Collections.singletonList("myartifact"));
        attributes.put(MavenAttributes.GROUP_ID, Collections.singletonList("mygroup"));
        attributes.put(MavenAttributes.VERSION, Collections.singletonList("1.0"));
        attributes.put(MavenAttributes.PACKAGING, Collections.singletonList("jar"));

//        when(httpJsonRequestFactory.fromLink(eq(DtoFactory.newDto(Link.class)
//                                                          .withMethod("PUT")
//                                                          .withHref(API_ENDPOINT + "/workspace/" + workspace + "/project"))))
//                .thenReturn(httpJsonRequest);

        pm.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                   .withType("maven")
                                   .withAttributes(attributes)
                                   .withName("testEstimate")
                                   .withPath("/testEstimate"),
                         new HashMap<>(0));

        pm.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                   .withType("blank")
                                   .withName("testEstimateBad")
                                   .withPath("/testEstimateBad"),
                         new HashMap<>(0));

        Map<String, AttributeValue> out = pm.estimateProject("testEstimate", "maven");

        Assert.assertEquals(out.get(MavenAttributes.ARTIFACT_ID).getString(), "myartifact");
        Assert.assertEquals(out.get(MavenAttributes.VERSION).getString(), "1.0");

        try {
            pm.estimateProject("testEstimateBad", "maven");
            Assert.fail("ValueStorageException expected");
        } catch (ValueStorageException ignored) {
        }
    }
}
