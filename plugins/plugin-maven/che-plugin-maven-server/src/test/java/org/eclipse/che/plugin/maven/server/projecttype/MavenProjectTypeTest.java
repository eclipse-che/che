/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.server.projecttype;

import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.java.server.projecttype.JavaProjectType;
import org.eclipse.che.plugin.java.server.projecttype.JavaValueProviderFactory;
import org.eclipse.che.plugin.maven.server.projecttype.handler.GeneratorStrategy;
import org.eclipse.che.plugin.maven.server.projecttype.handler.MavenProjectGenerator;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.eclipse.che.ide.maven.tools.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

/** @author gazarenkov */
// TODO: rework after new Project API
@Ignore
public class MavenProjectTypeTest {

    private ProjectTypeRegistry ptRegistry;
    private ProjectManager      pm;
    private HttpJsonRequest     httpJsonRequest;

    @Mock
    private HttpJsonRequestFactory    httpJsonRequestFactory;
    @Mock
    private HttpJsonResponse          httpJsonResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Set<ProjectTypeDef> projTypes = new HashSet<>();
        projTypes.add(new JavaProjectType(new JavaValueProviderFactory()));
        projTypes.add(new MavenProjectType(new MavenValueProviderFactory()));

        ptRegistry = new ProjectTypeRegistry(projTypes);

        Set<ProjectHandler> handlers = new HashSet<>();
        handlers.add(new MavenProjectGenerator(Collections.<GeneratorStrategy>emptySet()));

        httpJsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
    }

    @Test
    public void testGetProjectType() throws Exception {
        ProjectTypeDef pt = ptRegistry.getProjectType("maven");
        Assert.assertTrue(pt.getAttributes().size() > 0);
        Assert.assertTrue(pt.isTypeOf("java"));
    }

    @Test
    public void testMavenProject() throws Exception {
        WorkspaceDto usersWorkspaceMock = mock(WorkspaceDto.class);
        WorkspaceConfigDto workspaceConfigMock = mock(WorkspaceConfigDto.class);
        when(httpJsonRequestFactory.fromLink(eq(DtoFactory.newDto(Link.class)
                                                          .withMethod("GET")
                                                          .withHref("/workspace/"))))
                .thenReturn(httpJsonRequest);
        when(httpJsonRequestFactory.fromLink(eq(DtoFactory.newDto(Link.class)
                                                          .withMethod("PUT")
                                                          .withHref("/workspace/" + "/project"))))
                .thenReturn(httpJsonRequest);
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
        when(httpJsonResponse.asDto(WorkspaceDto.class)).thenReturn(usersWorkspaceMock);
        final ProjectConfigDto projectConfig = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                         .withName("project")
                                                         .withPath("/myProject")
                                                         .withType(MavenAttributes.MAVEN_ID);
        when(usersWorkspaceMock.getConfig()).thenReturn(workspaceConfigMock);
        when(workspaceConfigMock.getProjects()).thenReturn(Collections.singletonList(projectConfig));

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
    }
}
