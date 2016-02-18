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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.commons.test.SelfReturningAnswer;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Vitaly Parfonov
 */
@Ignore
// TODO: rework after new Project API
public class MavenProjectImportedTest {

    private String pomJar =
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>org.eclipse.che.sdk</groupId>\n" +
            "    <artifactId>codenvy-sdk-parent</artifactId>\n" +
            "    <version>3.1.0-SNAPSHOT</version>\n" +
            "</project>";

    private String pom =
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>org.eclipse.che.sdk</groupId>\n" +
            "    <artifactId>codenvy-sdk-parent</artifactId>\n" +
            "    <version>3.1.0-SNAPSHOT</version>\n" +
            "    <packaging>pom</packaging>\n" +
            "    <modules>" +
            "      <module>module1</module>" +
            "      <module>module2</module>" +
            "   </modules>" +
            "</project>";

    private String pomWithNestingModule =
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>org.eclipse.che.sdk</groupId>\n" +
            "    <artifactId>codenvy-sdk-parent</artifactId>\n" +
            "    <version>3.1.0-SNAPSHOT</version>\n" +
            "    <packaging>pom</packaging>\n" +
            "    <modules>" +
            "      <module>../module2</module>" +
            "      <module>../module3</module>" +
            "   </modules>" +
            "</project>";

    private MavenProjectImportedHandler mavenProjectImportedHandler;
    private HttpJsonRequest             httpJsonRequest;

//    @Mock
//    private Provider<AttributeFilter> filterProvider;
//    @Mock
//    private AttributeFilter           filter;
    @Mock
    private HttpJsonRequestFactory    httpJsonRequestFactory;
    @Mock
    private HttpJsonResponse          httpJsonResponse;

    private ProjectManager projectManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
//        when(filterProvider.get()).thenReturn(filter);
        Set<ProjectTypeDef> pts = new HashSet<>();
        final ProjectTypeDef pt = new ProjectTypeDef("maven", "Maven type", true, false) {
        };

        pts.add(pt);
        final ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(pts);

        EventService eventService = new EventService();
        ProjectHandlerRegistry handlerRegistry = new ProjectHandlerRegistry(new HashSet<>());
//        projectManager = new ProjectManager(virtualFileSystemRegistry,
//                                            eventService,
//                                            projectTypeRegistry,
//                                            handlerRegistry,
//                                            filterProvider,
//                                            API_ENDPOINT,
//                                            httpJsonRequestFactory);
        // Bind components
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder<ProjectHandler> projectTypeResolverMultibinder = Multibinder.newSetBinder(binder(), ProjectHandler.class);
                projectTypeResolverMultibinder.addBinding().to(MavenProjectImportedHandler.class);
                bind(ProjectManager.class).toInstance(projectManager);
            }
        });

        mavenProjectImportedHandler = injector.getInstance(MavenProjectImportedHandler.class);
        projectManager = injector.getInstance(ProjectManager.class);

        httpJsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
        UsersWorkspaceDto usersWorkspaceMock = mock(UsersWorkspaceDto.class);
//        when(httpJsonRequestFactory.fromLink(eq(DtoFactory.newDto(Link.class)
//                                                          .withMethod("PUT")
//                                                          .withHref(API_ENDPOINT + "/workspace/" + workspace + "/project"))))
//                .thenReturn(httpJsonRequest);
//        when(httpJsonRequestFactory.fromLink(eq(DtoFactory.newDto(Link.class)
//                                                          .withMethod("GET")
//                                                          .withHref(API_ENDPOINT + "/workspace/" + workspace))))
//                .thenReturn(httpJsonRequest);
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
        when(httpJsonResponse.asDto(UsersWorkspaceDto.class)).thenReturn(usersWorkspaceMock);
        final ProjectConfigDto projectConfig = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                         .withPath("/test")
                                                         .withType(MavenAttributes.MAVEN_ID);
        final ProjectConfigDto module1 = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                   .withPath("/test/module1")
                                                   .withType(MavenAttributes.MAVEN_ID);
        final ProjectConfigDto module2 = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                   .withPath("/test/module2")
                                                   .withType(MavenAttributes.MAVEN_ID);
        final ProjectConfigDto module3 = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                   .withPath("/test/module3")
                                                   .withType(MavenAttributes.MAVEN_ID);
        List<ProjectConfigDto> projectsList = new ArrayList<>();
        projectsList.add(projectConfig);
        projectsList.add(module1);
        projectsList.add(module2);
        projectsList.add(module3);
        when(usersWorkspaceMock.getProjects()).thenReturn(projectsList);
    }

    @Test
    public void shouldNotChangeParentProjectType() throws Exception {
        RegisteredProject test = projectManager.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                                        .withPath("/test/module1")
                                                                        .withName("module1")
                                                                        .withType("maven"), null);
        test.getBaseFolder().createFile("pom.xml", pomJar.getBytes());
        test.getBaseFolder().createFolder("module1");
        mavenProjectImportedHandler.onProjectImported(test.getBaseFolder());
        assertNotNull(projectManager.getProject("test"));
    }

    @Test
    public void withPomXmlWithFolders() throws Exception {
        RegisteredProject test = projectManager.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                                  .withType("maven")
                                                                  .withType("maven")
                                                                  .withName("test"),
                                                        new HashMap<>(0));
        test.getBaseFolder().createFile("pom.xml", pomJar.getBytes());
        FolderEntry folder = test.getBaseFolder().createFolder("folder1");
        folder.createFile("pom.xml", pomJar.getBytes());

        FolderEntry folder1 = test.getBaseFolder().createFolder("folder2");
        folder1.createFile("pom.xml", pomJar.getBytes());

        mavenProjectImportedHandler.onProjectImported(test.getBaseFolder());
        assertNotNull(projectManager.getProject("test"));
        assertNull(projectManager.getProject("test/folder1"));
        assertNull(projectManager.getProject("test/folder2"));
    }

    @Test
    public void withPomXmlMultiModule() throws Exception {
        RegisteredProject test = projectManager.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                                  .withType("maven")
                                                                  .withName("test")
                                                                  .withPath("/test"),
                                                        new HashMap<>(0));
        test.getBaseFolder().createFile("pom.xml", pom.getBytes());

        FolderEntry module1 = test.getBaseFolder().createFolder("module1");
        module1.createFile("pom.xml", pom.getBytes());

        FolderEntry module2 = test.getBaseFolder().createFolder("module2");
        module2.createFile("pom.xml", pom.getBytes());

        FolderEntry moduleNotDescribedInParentPom = test.getBaseFolder().createFolder("moduleNotDescribedInParentPom");
        moduleNotDescribedInParentPom.createFile("pom.xml", pom.getBytes());


        mavenProjectImportedHandler.onProjectImported(test.getBaseFolder());
        assertNotNull(projectManager.getProject("test"));
        assertNotNull(projectManager.getProject("test/module1"));
        assertNotNull(projectManager.getProject("test/module2"));
        assertNull(projectManager.getProject("test/moduleNotDescribedInParentPom"));
    }

    @Test
    public void withPomXmlMultiModuleWithNesting() throws Exception {
        //test for multi module project in which the modules are specified in format: <module>../module</module>
        FolderEntry rootProject =
                projectManager.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                       .withType("maven")
                                                       .withName("test")
                                                       .withType("maven"),
                                             new HashMap<>(0)).getBaseFolder();
        rootProject.createFile("pom.xml", pom.getBytes());

        FolderEntry module1 = rootProject.createFolder("module1");
        module1.createFile("pom.xml", pomWithNestingModule.getBytes());

        FolderEntry module2 = rootProject.createFolder("module2");
        module2.createFile("pom.xml", pom.getBytes());

        FolderEntry module3 = rootProject.createFolder("module3");
        module3.createFile("pom.xml", pom.getBytes());

        FolderEntry moduleNotDescribedInParentPom = rootProject.createFolder("moduleNotDescribedInParentPom");
        moduleNotDescribedInParentPom.createFile("pom.xml", pom.getBytes());

        mavenProjectImportedHandler.onProjectImported(rootProject);
        assertNotNull(projectManager.getProject("test"));
        assertNotNull(projectManager.getProject("test/module1"));
        assertNotNull(projectManager.getProject("test/module2"));
        assertNotNull(projectManager.getProject("test/module3"));
        assertNull(projectManager.getProject("test/test/moduleNotDescribedInParentPom"));
    }
}
