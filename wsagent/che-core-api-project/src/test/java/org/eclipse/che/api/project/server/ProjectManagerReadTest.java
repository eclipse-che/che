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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.testng.Assert.assertTrue;


/**
 * @author gazarenkov
 */
public class ProjectManagerReadTest extends WsAgentTestBase {



    @Before
    public void setUp() throws Exception {

        super.setUp();


        new File(root, "/fromFolder").mkdir();
        new File(root, "/normal").mkdir();
        new File(root, "/normal/module").mkdir();


        List<ProjectConfig> projects = new ArrayList<>();
        projects.add(DtoFactory.newDto(ProjectConfigDto.class)
                               .withPath("/normal")
                               .withName("project1Name")
                               .withType("primary1"));

        projects.add(DtoFactory.newDto(ProjectConfigDto.class)
                               .withPath("/fromConfig")
                               .withName("")
                               .withType("primary1"));


        projects.add(DtoFactory.newDto(ProjectConfigDto.class)
                              .withPath("/normal/module")
                              .withName("project1Name")
                              .withType("primary1"));


        workspaceHolder = new TestWorkspaceHolder(projects);
        ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());
        projectTypeRegistry.registerProjectType(new PT1());
        projectTypeRegistry.registerProjectType(new PT3());

        ProjectHandlerRegistry projectHandlerRegistry = new ProjectHandlerRegistry(new HashSet<>());

        projectRegistry = new ProjectRegistry(workspaceHolder, vfsProvider, projectTypeRegistry, projectHandlerRegistry, eventService);
        projectRegistry.initProjects();

        pm = new ProjectManager(vfsProvider, null, projectTypeRegistry, projectRegistry, projectHandlerRegistry,
                                null, fileWatcherNotificationHandler, fileTreeWatcher, workspaceHolder, projectTreeChangesDetector);
        pm.initWatcher();
    }


    @Test
    public void testInit() throws Exception {

        assertEquals(4, projectRegistry.getProjects().size());
        assertEquals(0, projectRegistry.getProject("/normal").getProblems().size());
        assertEquals(1, projectRegistry.getProject("/fromConfig").getProblems().size());
        assertEquals(1, projectRegistry.getProject("/fromFolder").getProblems().size());

    }


    @Test
    public void testInitWithBadProject() throws Exception {

        new File(root, "/foo").mkdir();
        new File(root, "/bar").mkdir();

        workspaceHolder.addProject(DtoFactory.newDto(ProjectConfigDto.class)
                                             .withPath("/foo")
                                             .withName("project1Name")
                                             .withType("notFoundProjectType"));
        workspaceHolder.addProject(DtoFactory.newDto(ProjectConfigDto.class)
                                             .withPath("/bar")
                                             .withName("project1Name")
                                             .withType("pt3"));

        ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());
        projectTypeRegistry.registerProjectType(new PT1());
        projectTypeRegistry.registerProjectType(new PT3());

        projectRegistry = new ProjectRegistry(workspaceHolder, vfsProvider, projectTypeRegistry, projectHandlerRegistry, eventService);
        projectRegistry.initProjects();

        assertEquals(6, projectRegistry.getProjects().size());
        assertEquals(1, projectRegistry.getProject("/foo").getProblems().size());
        assertEquals(13, projectRegistry.getProject("/foo").getProblems().get(0).code);
        assertEquals(1, projectRegistry.getProject("/bar").getProblems().size());
        assertEquals(12, projectRegistry.getProject("/bar").getProblems().get(0).code);
    }


    @Test
    public void testNormalProject() throws Exception {

        assertEquals(4, pm.getProjects().size());
        assertEquals("/normal", pm.getProject("/normal").getPath());
        assertEquals("project1Name", pm.getProject("/normal").getName());
        assertEquals(0, pm.getProject("/normal").getProblems().size());

        for(VirtualFileEntry entry : pm.getProjectsRoot().getChildren()) {
            System.out.println(">>>> "+entry.getPath()+" "+entry.getProject());
        }

        VirtualFileEntry entry = pm.getProjectsRoot().getChild("normal");
        assertTrue(entry.isProject());

    }

    @Test
    public void testProjectFromFolder() throws Exception {

        assertEquals("/fromFolder", pm.getProject("/fromFolder").getPath());
        assertEquals("fromFolder", pm.getProject("/fromFolder").getName());
        assertEquals(1, pm.getProject("/fromFolder").getProblems().size());
        assertEquals(BaseProjectType.ID, pm.getProject("/fromFolder").getProjectType().getId());
        assertEquals(11, pm.getProject("/fromFolder").getProblems().get(0).code);
    }

    @Test
    public void testProjectFromConfig() throws Exception {

        assertEquals("/fromConfig", pm.getProject("/fromConfig").getPath());
        assertEquals(1, pm.getProject("/fromConfig").getProblems().size());
        assertEquals("primary1", pm.getProject("/fromConfig").getProjectType().getId());
        assertEquals(10, pm.getProject("/fromConfig").getProblems().get(0).code);
    }

    @Test
    public void testInnerProject() throws Exception {

        String path = "/normal/module";
        assertEquals(0, pm.getProject(path).getProblems().size());
        assertEquals("primary1", pm.getProject(path).getProjectType().getId());


    }

    @Test
    public void testParentProject() throws Exception {

//        try {
        assertEquals("/normal", projectRegistry.getParentProject("/normal").getPath());
//            fail("NotFoundException expected");
//        } catch (NotFoundException e) {}

        assertEquals("/normal", projectRegistry.getParentProject("/normal/some/path").getPath());
        assertEquals("/normal/module", projectRegistry.getParentProject("/normal/module/some/path").getPath());

//        try {
        assertNull(projectRegistry.getParentProject("/some/path"));
//            fail("NotFoundException expected");
//        } catch (NotFoundException e) {}


    }

    @Test
    public void testSerializeProject() throws Exception {
        ProjectConfig config = DtoConverter.asDto(pm.getProject("/fromConfig"));

        assertEquals("/fromConfig", config.getPath());
        assertEquals("primary1", config.getType());

    }


    @Test
    public void testDoNotReturnNotInitializedAttribute() throws Exception {

        // SPEC:
        // Not initialized attributes should not be returned

        assertEquals(1, projectRegistry.getProject("/normal").getAttributes().size());

    }

//    @Test
//    public void testEstimateProject() throws Exception {
//
//        //pm.getProject("/normal").getBaseFolder().createFolder("file1");
//
//        System.out.println (">>>> "+pm.estimateProject("/normal", "pt3").get("pt2-provided1").getString());
//
//    }

    @Test
    public void testResolveSources() throws Exception {

    }


    @Test
    public void testIfConstantAttrIsAccessible() throws Exception {

        assertEquals("my constant", pm.getProject("/normal").getAttributeEntries().get("const1").getString());

    }



}
