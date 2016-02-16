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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystem;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.dto.server.DtoFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * @author gazarenkov
 */
public class ProjectManagerReadTest {

    protected final static String FS_PATH = "target/fs";

    private WorkspaceHolder workspaceHolder;

    private File root;

    private ProjectManager pm;

//    @BeforeClass
//    public static void beforeClass() {
//
//        File root = new File(FS_PATH);
//        if (root.exists()) {
//            IoUtil.deleteRecursive(root);
//        }
//        root.mkdir();
//    }

    @Before
    public void setUp() throws Exception {

        root = new File(FS_PATH);

        if (root.exists()) {
            IoUtil.deleteRecursive(root);
        }
        root.mkdir();

        LocalVirtualFileSystem vfs = new LocalVirtualFileSystem(root, null, null, null);


        new File(root, "/fromFolder").mkdir();
        new File(root, "/normal").mkdir();
        new File(root, "/normal/module").mkdir();


        List<ProjectConfigDto> modules = new ArrayList<>();
        modules.add(DtoFactory.newDto(ProjectConfigDto.class)
                              .withPath("/normal/module")
                              .withName("project1Name")
                              .withType("primary1"));


        List<ProjectConfigDto> projects = new ArrayList<>();
        projects.add(DtoFactory.newDto(ProjectConfigDto.class)
                               .withPath("/normal")
                               .withName("project1Name")
                               .withType("primary1")
                               .withModules(modules));

        projects.add(DtoFactory.newDto(ProjectConfigDto.class)
                               .withPath("/fromConfig")
                               .withName("")
                               .withType("primary1"));

        workspaceHolder = new TestWorkspaceHolder(projects);
        ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());
        projectTypeRegistry.registerProjectType(new PT1());

        ProjectHandlerRegistry projectHandlerRegistry = new ProjectHandlerRegistry(new HashSet<>());

        pm = new ProjectManager(vfs, null, projectTypeRegistry, projectHandlerRegistry,
                                   null, workspaceHolder);
    }

//    @AfterClass
//    public static void remove() throws Exception {
//        File root = new File(FS_PATH);
//        FileUtils.deleteDirectory(root);
//    }

    @Test
    public void testInitManager() throws Exception {

        //pm.getSearcher().addIndexFilter()



    }


    @Test
    public void testNormalProject() throws Exception {

        assertEquals(4, pm.getProjects().size());
        assertNotNull(pm.getProject("/normal"));
        assertEquals("/normal", pm.getProject("/normal").getPath());
        assertEquals("project1Name", pm.getProject("/normal").getName());
        assertEquals(0, pm.getProject("/normal").getProblems().size());
    }

    @Test
    public void testProjectFromFolder() throws Exception {

        assertNotNull(pm.getProject("/fromFolder"));
        assertEquals("/fromFolder", pm.getProject("/fromFolder").getPath());
        assertEquals("fromFolder", pm.getProject("/fromFolder").getName());
        assertEquals(1, pm.getProject("/fromFolder").getProblems().size());
        assertEquals(BaseProjectType.ID, pm.getProject("/fromFolder").getProjectType().getId());
        assertEquals(11, pm.getProject("/fromFolder").getProblems().get(0).code);
    }

    @Test
    public void testProjectFromConfig() throws Exception {

        assertNotNull(pm.getProject("/fromConfig"));
        assertEquals("/fromConfig", pm.getProject("/fromConfig").getPath());
        assertEquals(1, pm.getProject("/fromConfig").getProblems().size());
        assertEquals("primary1", pm.getProject("/fromConfig").getProjectType().getId());
        assertEquals(10, pm.getProject("/fromConfig").getProblems().get(0).code);
    }

    @Test
    public void testModule() throws Exception {

        String path = "/normal/module";
        assertNotNull(pm.getProject(path));
        assertEquals(0, pm.getProject(path).getProblems().size());
        assertEquals("primary1", pm.getProject(path).getProjectType().getId());

        ProjectImpl parent = pm.getProject("/normal");
        assertEquals(1, parent.getModulePaths().size());
        assertEquals(path, parent.getModulePaths().iterator().next());

        List<String> projects = pm.getProjects("/normal");
        assertEquals(1, projects.size());
        assertEquals(path, projects.get(0));

    }

    @Test
    public void testOwnerProject() throws Exception {

        assertEquals("/normal", pm.getOwnerProject("/normal").getPath());
        assertEquals("/normal", pm.getOwnerProject("/normal/some/path").getPath());
        assertEquals("/normal/module", pm.getOwnerProject("/normal/module/some/path").getPath());

        try {
            pm.getOwnerProject("/some/path");
            fail("NotFoundException expected");
        } catch (NotFoundException e) {}


    }

    @Test
    public void testSerializeProject() throws Exception {

        ProjectConfig config =
                           DtoConverter.toProjectConfig(pm.getProject("/fromConfig"), workspaceHolder.getWorkspace().getId(), null);

        assertEquals("/fromConfig", config.getPath());
        assertEquals("primary1", config.getType());

    }

    @Test
    public void testProvidedAttributesNotSerialized() throws Exception {

    }

    @Test
    public void testEstimateProject() throws Exception {

    }

    @Test
    public void testResolveSources() throws Exception {

    }

    @Test
    public void testIfConstantAttrIsAccessible() throws Exception {

        assertEquals("my constant", pm.getProject("/normal").getAttributeEntries().get("const1").getString());

    }


    private static class TestWorkspaceHolder extends WorkspaceHolder {

        private TestWorkspaceHolder(List <ProjectConfigDto> projects) throws ServerException {
            super(DtoFactory.newDto(UsersWorkspaceDto.class).
                    withId("id").withName("name")
                            .withProjects(projects));
        }

    }

    private static class PT1 extends ProjectTypeDef {
        private PT1() {
            super("primary1", "primary1", true, false);

            addVariableDefinition("var1", "", false);
            addConstantDefinition("const1", "", "my constant");
        }
    }


}
