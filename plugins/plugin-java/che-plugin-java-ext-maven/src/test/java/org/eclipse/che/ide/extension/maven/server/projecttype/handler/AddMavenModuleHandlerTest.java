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
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.server.ProjectImpl;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.SelfReturningAnswer;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Vitaly Parfonov
 * @author Dmitry Shnurenko
 */
// TODO need to rework in according to the new Project API
@RunWith(MockitoJUnitRunner.class)
public class AddMavenModuleHandlerTest {

    private static final String WORKSPACE    = "my_ws";
    private static final String API_ENDPOINT = "http://localhost:8080/che/api";

    private static final String POM_XML_TEMPL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><packaging>%s</packaging></project>";

    private AddMavenModuleHandler addMavenModuleHandler;
    private ProjectManager        projectManager;
    private ProjectTypeRegistry   projectTypeRegistry;

//    @Mock
//    private Provider<AttributeFilter> filterProvider;
//    @Mock
//    private AttributeFilter           filter;
    @Mock
    private HttpJsonRequestFactory    httpJsonRequestFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
//        when(filterProvider.get()).thenReturn(filter);
        addMavenModuleHandler = new AddMavenModuleHandler();
        ProjectTypeDef mavenProjectType = Mockito.mock(ProjectTypeDef.class);
        Mockito.when(mavenProjectType.getId()).thenReturn(MavenAttributes.MAVEN_ID);
        Mockito.when(mavenProjectType.getDisplayName()).thenReturn(MavenAttributes.MAVEN_ID);
        Mockito.when(mavenProjectType.isPrimaryable()).thenReturn(true);
        final EventService eventService = new EventService();

        Set<ProjectTypeDef> projTypes = new HashSet<>();
        projTypes.add(mavenProjectType);

        projectTypeRegistry = new ProjectTypeRegistry(projTypes);

        Set<ProjectHandler> handlers = new HashSet<>();
        ProjectHandlerRegistry handlerRegistry = new ProjectHandlerRegistry(handlers);

//        projectManager = new ProjectManager(vfsRegistry,
//                                            eventService,
//                                            projectTypeRegistry,
//                                            handlerRegistry,
//                                            filterProvider,
//                                            API_ENDPOINT,
//                                            httpJsonRequestFactory);

        HttpJsonRequest httpJsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
        when(httpJsonRequestFactory.fromLink(eq(DtoFactory.newDto(Link.class)
                                                          .withMethod("PUT")
                                                          .withHref(API_ENDPOINT + "/workspace/" + WORKSPACE + "/project"))))
                .thenReturn(httpJsonRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAddModuleIfNotPomPackage() throws Exception {
        String parent = NameGenerator.generate("parent", 5);
        String module = NameGenerator.generate("module", 5);
        ProjectImpl project =
                projectManager.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                       .withPath('/' + parent + '/' + module)
                                                       .withName(module)
                                                       .withType(MavenAttributes.MAVEN_ID),
                                             new HashMap<>(0));
        project.getBaseFolder().createFile("pom.xml", String.format(POM_XML_TEMPL, "jar").getBytes());
        addMavenModuleHandler
                .onCreateModule(project.getBaseFolder(), project.getPath() + "/" + module, "maven",
                                Collections.<String, String>emptyMap());
    }

    @Test
    public void methodShouldReturnedTheControlWhenPomNotFound() throws Exception {
        String parent = NameGenerator.generate("parent", 5);
        String module = NameGenerator.generate("module", 5);
        ProjectImpl project =
                projectManager.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                       .withPath('/' + parent + '/' + module)
                                                       .withName(module)
                                                       .withType(MavenAttributes.MAVEN_ID), null);
        addMavenModuleHandler
                .onCreateModule(project.getBaseFolder(), project.getPath() + "/" + module, "maven",
                                Collections.<String, String>emptyMap());

        VirtualFileEntry pom = project.getBaseFolder().getChild("pom.xml");
        Assert.assertNull(pom);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAddModuleIfModuleNotMaven() throws Exception {
        ProjectTypeDef notMaven = Mockito.mock(ProjectTypeDef.class);
        Mockito.when(notMaven.getId()).thenReturn("notMaven");
        Mockito.when(notMaven.getDisplayName()).thenReturn("notMaven");
        Mockito.when(notMaven.isPrimaryable()).thenReturn(true);
        projectTypeRegistry.registerProjectType(notMaven);

        String parent = NameGenerator.generate("parent", 5);
        String module = NameGenerator.generate("module", 5);

        ProjectImpl project = projectManager.createProject(DtoFactory.getInstance()
                                                                     .createDto(ProjectConfigDto.class)
                                                                     .withPath('/' + parent + '/' + module)
                                                                     .withName(module)
                                                                     .withType(MavenAttributes.MAVEN_ID), new HashMap<>(0));

        project.getBaseFolder().createFile("pom.xml", String.format(POM_XML_TEMPL, "pom").getBytes());

        addMavenModuleHandler.onCreateModule(project.getBaseFolder(),
                                             project.getPath() + "/" + module,
                                             "notMaven",
                                             Collections.<String, String>emptyMap());
    }

    @Test
    public void addModuleOk() throws Exception {
        String parent = NameGenerator.generate("parent", 5);
        String module = NameGenerator.generate("module", 5);
        ProjectImpl project =
                projectManager.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                       .withPath('/' + parent + '/' + module)
                                                       .withName(module)
                                                       .withType(MavenAttributes.MAVEN_ID), new HashMap<>(0));
        project.getBaseFolder().createFile("pom.xml", String.format(POM_XML_TEMPL, "pom").getBytes());
        addMavenModuleHandler.onCreateModule(project.getBaseFolder(), project.getPath() + "/" + module, "maven",
                                             Collections.<String, String>emptyMap());

        VirtualFileEntry pom = project.getBaseFolder().getChild("pom.xml");
        Assert.assertNotNull(pom);
        InputStream content = pom.getVirtualFile().getContent();
        Assert.assertNotNull(content);
        String pomContent = IoUtil.readStream(content);
        Assert.assertNotNull(pomContent);
        Assert.assertFalse(pomContent.isEmpty());
        String mavenModule = String.format("<module>%s</module>", module);
        Assert.assertTrue(pomContent.contains(mavenModule));
    }
}
