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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.NewProjectConfig;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.project.server.RegisteredProject.Problem;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.importer.ProjectImportOutputWSLineConsumer;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.Variable;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.workspace.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author gazarenkov
 */
public class ProjectManagerWriteTest extends WsAgentTestBase {
    private static final String FILE_CONTENT = "to be or not to be";

    @Before
    public void setUp() throws Exception {

        super.setUp();

        projectTypeRegistry.registerProjectType(new PT2());
        projectTypeRegistry.registerProjectType(new PT3());
        projectTypeRegistry.registerProjectType(new PT4NoGen());
        projectTypeRegistry.registerProjectType(new M2());
        projectTypeRegistry.registerProjectType(new PTsettableVP());

        projectHandlerRegistry.register(new SrcGenerator());
    }

    @Test
    public void testCreateBatchProjectsByConfigs() throws Exception {
        final String projectPath1 = "/testProject1";
        final String projectPath2 = "/testProject2";

        final NewProjectConfig config1 = createProjectConfigObject("testProject1", projectPath1, BaseProjectType.ID, null);
        final NewProjectConfig config2 = createProjectConfigObject("testProject2", projectPath2, BaseProjectType.ID, null);

        final List<NewProjectConfig> configs = new ArrayList<>(2);
        configs.add(config1);
        configs.add(config2);

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));

        checkProjectExist(projectPath1);
        checkProjectExist(projectPath2);
        assertEquals(2, projectRegistry.getProjects().size());
    }

    @Test
    public void testCreateBatchProjectsByImportingSourceCode() throws Exception {
        final String projectPath1 = "/testProject1";
        final String projectPath2 = "/testProject2";
        final String importType1 = "importType1";
        final String importType2 = "importType2";

        final String [] paths1 = {"folder1/", "folder1/file1.txt"};
        final List<String> children1 = new ArrayList<>(Arrays.asList(paths1));
        registerImporter(importType1, prepareZipArchiveBasedOn(children1));

        final String [] paths2 = {"folder2/", "folder2/file2.txt"};
        final List<String> children2 = new ArrayList<>(Arrays.asList(paths2));
        registerImporter(importType2, prepareZipArchiveBasedOn(children2));

        final SourceStorageDto source1 = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType(importType1);
        final NewProjectConfigDto config1 = createProjectConfigObject("testProject1", projectPath1, BaseProjectType.ID, source1);

        final SourceStorageDto source2 = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType(importType2);
        final NewProjectConfigDto config2 = createProjectConfigObject("testProject2", projectPath2, BaseProjectType.ID, source2);

        final List<NewProjectConfig> configs = new ArrayList<>(2);
        configs.add(config1);
        configs.add(config2);

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));

        final RegisteredProject project1 = projectRegistry.getProject(projectPath1);
        final FolderEntry projectFolder1 = project1.getBaseFolder();
        checkProjectExist(projectPath1);
        checkChildrenFor(projectFolder1, children1);

        final RegisteredProject project2 = projectRegistry.getProject(projectPath2);
        final FolderEntry projectFolder2 = project2.getBaseFolder();
        checkProjectExist(projectPath2);
        checkChildrenFor(projectFolder2, children2);
    }

    @Test
    public void testCreateProjectWhenSourceCodeIsNotReachable() throws Exception {
        final String projectPath = "/testProject";
        final SourceStorageDto source = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType("importType");
        final NewProjectConfigDto config = createProjectConfigObject("testProject1", projectPath, BaseProjectType.ID, source);

        final List<NewProjectConfig> configs = new ArrayList<>(1);
        configs.add(config);

        try {
            pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));
            fail("Exception should be thrown when source code is not reachable");
        } catch (Exception e) {
            assertEquals(0, projectRegistry.getProjects().size());
            assertNull(projectRegistry.getProject(projectPath));
            assertNull(pm.getProjectsRoot().getChild(projectPath));
        }
    }

    @Test
    public void shouldRollbackCreatingBatchProjects() throws Exception {
        // we should rollback operation of creating batch projects when we have not source code for at least one project
        // For example: two projects were success created, but we could not get source code for third configuration
        // At this use case we should rollback the operation and clean up all created projects

        final String projectPath1 = "/testProject1";
        final String projectPath2 = "/testProject2";
        final String projectPath3 = "/testProject3";
        final String importType1 = "importType1";
        final String importType2 = "importType2";

        final String [] paths1 = {"folder1/", "folder1/file1.txt"};
        final List<String> children1 = new ArrayList<>(Arrays.asList(paths1));
        registerImporter(importType1, prepareZipArchiveBasedOn(children1));

        final String [] paths2 = {"folder2/", "folder2/file2.txt"};
        final List<String> children2 = new ArrayList<>(Arrays.asList(paths2));
        registerImporter(importType2, prepareZipArchiveBasedOn(children2));

        final SourceStorageDto source1 = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType(importType1);
        final NewProjectConfigDto config1 = createProjectConfigObject("testProject1", projectPath1, BaseProjectType.ID, source1);

        final SourceStorageDto source2 = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType(importType2);
        final NewProjectConfigDto config2 = createProjectConfigObject("testProject2", projectPath2, BaseProjectType.ID, source2);

        final SourceStorageDto source = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType("importType");
        final NewProjectConfigDto config3 = createProjectConfigObject("testProject3", projectPath3, BaseProjectType.ID, source);

        final List<NewProjectConfig> configs = new ArrayList<>(2);
        configs.add(config1); //will be success created
        configs.add(config2); //will be success created
        configs.add(config3); //we be failed - we have not registered importer - source code will not be imported

        try {
            pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));
            fail("We should rollback operation of creating batch projects when we could not get source code for at least one project");
        } catch (Exception e) {
            assertEquals(0, projectRegistry.getProjects().size());

            assertNull(projectRegistry.getProject(projectPath1));
            assertNull(pm.getProjectsRoot().getChild(projectPath1));

            assertNull(projectRegistry.getProject(projectPath2));
            assertNull(pm.getProjectsRoot().getChild(projectPath2));

            assertNull(projectRegistry.getProject(projectPath3));
            assertNull(pm.getProjectsRoot().getChild(projectPath3));
        }
    }

    @Test
    public void testCreateBatchProjectsWithInnerProject() throws Exception {
        final String rootProjectPath = "/testProject1";
        final String innerProjectPath = "/testProject1/innerProject";
        final String importType = "importType1";
        final String innerProjectType = "pt2";

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("pt2-var2", new AttributeValue("test").getList());

        final String [] paths1 = {"folder1/", "folder1/file1.txt"};
        final String [] paths2 = {"innerProject/", "innerProject/folder2/", "innerProject/folder2/file2.txt"};
        final List<String> children1 = Arrays.asList(paths1);
        final List<String> children2 = Arrays.asList(paths2);
        final List<String> children = new ArrayList<>(children1);
        children.addAll(children2);
        registerImporter(importType, prepareZipArchiveBasedOn(children));

        SourceStorageDto source = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType(importType);
        NewProjectConfigDto config1 = createProjectConfigObject("testProject1", rootProjectPath, BaseProjectType.ID, source);
        NewProjectConfigDto config2 = createProjectConfigObject("innerProject", innerProjectPath, innerProjectType, null);
        config2.setAttributes(attributes);

        List<NewProjectConfig> configs = new ArrayList<>(2);
        configs.add(config1);
        configs.add(config2);

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));

        RegisteredProject rootProject = projectRegistry.getProject(rootProjectPath);
        FolderEntry rootProjectFolder = rootProject.getBaseFolder();
        RegisteredProject innerProject = projectRegistry.getProject(innerProjectPath);
        FolderEntry innerProjectFolder = innerProject.getBaseFolder();


        assertNotNull(rootProject);
        assertTrue(rootProjectFolder.getVirtualFile().exists());
        assertEquals(rootProjectPath, rootProject.getPath());
        checkChildrenFor(rootProjectFolder, children1);

        assertNotNull(innerProject);
        assertTrue(innerProjectFolder.getVirtualFile().exists());
        assertEquals(innerProjectPath, innerProject.getPath());
        assertEquals(innerProjectType, innerProject.getType());
        checkChildrenFor(rootProjectFolder, children2);
    }

    @Test
    public void testCreateBatchProjectsWithInnerProjectWhenInnerProjectContainsSource() throws Exception {
        final String rootProjectPath = "/rootProject";
        final String innerProjectPath = "/rootProject/innerProject";
        final String rootImportType = "rootImportType";
        final String innerImportType = "innerImportType";

        final String innerProjectType = "pt2";

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("pt2-var2", new AttributeValue("test").getList());

        final String [] paths1 = {"folder1/", "folder1/file1.txt"};
        final List<String> children1 = new ArrayList<>(Arrays.asList(paths1));
        registerImporter(rootImportType, prepareZipArchiveBasedOn(children1));

        final String [] paths2 = {"folder2/", "folder2/file2.txt"};
        final List<String> children2 = new ArrayList<>(Arrays.asList(paths2));
        registerImporter(innerImportType, prepareZipArchiveBasedOn(children2));

        final SourceStorageDto source1 = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType(rootImportType);
        final NewProjectConfigDto config1 = createProjectConfigObject("testProject1", rootProjectPath, BaseProjectType.ID, source1);

        final SourceStorageDto source2 = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType(innerImportType);
        final NewProjectConfigDto config2 = createProjectConfigObject("testProject2", innerProjectPath, innerProjectType, source2);
        config2.setAttributes(attributes);

        final List<NewProjectConfig> configs = new ArrayList<>(2);
        configs.add(config2);
        configs.add(config1);

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));

        RegisteredProject rootProject = projectRegistry.getProject(rootProjectPath);
        FolderEntry rootProjectFolder = rootProject.getBaseFolder();
        checkProjectExist(rootProjectPath);
        checkChildrenFor(rootProjectFolder, children1);

        RegisteredProject innerProject = projectRegistry.getProject(innerProjectPath);
        FolderEntry innerProjectFolder = innerProject.getBaseFolder();
        assertNotNull(innerProject);
        assertTrue(innerProjectFolder.getVirtualFile().exists());
        assertEquals(innerProjectPath, innerProject.getPath());
        assertEquals(innerProjectType, innerProject.getType());
        checkChildrenFor(innerProjectFolder, children2);
    }

    @Test
    public void testCreateBatchProjectsWithMixInnerProjects() throws Exception { // Projects should be sorted by path before creating
        final String [] paths = {"/1/z", "/2/z", "/1/d", "/2", "/1", "/1/a"};
        final List<String> projectsPaths = new ArrayList<>(Arrays.asList(paths));

        final List<NewProjectConfig> configs = new ArrayList<>(projectsPaths.size());
        for (String path : projectsPaths) {
            configs.add(createProjectConfigObject(path.substring(path.length() - 1, path.length()), path, BaseProjectType.ID, null));
        }

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));

        for (String path : projectsPaths) {
            checkProjectExist(path);
        }
    }

    @Test
    public void testCreateBatchProjectsWhenConfigContainsOnlyPath()
            throws Exception { // NewProjectConfig object contains only one mandatory Project Path field

        final String projectPath1 = "/testProject1";
        final String projectPath2 = "/testProject2";

        final NewProjectConfig config1 = createProjectConfigObject(null, projectPath1, null, null);
        final NewProjectConfig config2 = createProjectConfigObject(null, projectPath2, null, null);

        final List<NewProjectConfig> configs = new ArrayList<>(2);
        configs.add(config1);
        configs.add(config2);

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));

        checkProjectExist(projectPath1);
        checkProjectExist(projectPath2);
        assertEquals(2, projectRegistry.getProjects().size());
    }

    @Test
    public void shouldThrowBadRequestExceptionAtCreatingBatchProjectsWhenConfigNotContainsPath()
            throws Exception { //Path is mandatory field for NewProjectConfig
        final SourceStorageDto source = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType("importType");
        final NewProjectConfig config = createProjectConfigObject("project", null, BaseProjectType.ID, source);

        final List<NewProjectConfig> configs = new ArrayList<>(1);
        configs.add(config);

        try {
            pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));
            fail("BadRequestException should be thrown : path field is mandatory");
        } catch (BadRequestException e) {
            assertEquals(0, projectRegistry.getProjects().size());
        }
    }

    @Test
    public void shouldThrowConflictExceptionAtCreatingBatchProjectsWhenProjectWithPathAlreadyExist() throws Exception {
        final String path = "/somePath";
        final NewProjectConfig config = createProjectConfigObject("project", path, BaseProjectType.ID, null);

        final List<NewProjectConfig> configs = new ArrayList<>(1);
        configs.add(config);

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));
        checkProjectExist(path);
        assertEquals(1, projectRegistry.getProjects().size());

        try {
            pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));
            fail("ConflictException should be thrown : Project config with the same path is already exists");
        } catch (ConflictException e) {
            assertEquals(1, projectRegistry.getProjects().size());
        }
    }

    @Test
    public void shouldCreateParentFolderAtCreatingProjectWhenParentDoesNotExist() throws Exception {
        final String nonExistentParentPath = "/rootProject";
        final String innerProjectPath = "/rootProject/innerProject";

        final NewProjectConfig config = createProjectConfigObject(null, innerProjectPath, null, null);

        final List<NewProjectConfig> configs = new ArrayList<>(2);
        configs.add(config);

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));

        checkProjectExist(nonExistentParentPath);
        checkProjectExist(innerProjectPath);
        assertEquals(2, projectRegistry.getProjects().size());
    }

    @Test
    public void shouldRewriteProjectAtCreatingBatchProjectsWhenProjectAlreadyExist() throws Exception {
        final String projectPath = "/testProject";
        final String importType1 = "importType1";
        final String importType2 = "importType2";

        final String [] paths1 = {"folder1/", "folder1/file1.txt"};
        final List<String> children1 = new ArrayList<>(Arrays.asList(paths1));
        registerImporter(importType1, prepareZipArchiveBasedOn(children1));

        final String [] paths2 = {"folder2/", "folder2/file2.txt"};
        final List<String> children2 = new ArrayList<>(Arrays.asList(paths2));
        registerImporter(importType2, prepareZipArchiveBasedOn(children2));

        final SourceStorageDto source1 = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType(importType1);
        final NewProjectConfigDto config1 = createProjectConfigObject("testProject1", projectPath, "blank", source1);

        final SourceStorageDto source2 = DtoFactory.newDto(SourceStorageDto.class).withLocation("someLocation").withType(importType2);
        final NewProjectConfigDto config2 = createProjectConfigObject("testProject2", projectPath, "blank", source2);

        final List<NewProjectConfig> configs = new ArrayList<>(1);
        configs.add(config1);

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));

        final FolderEntry projectFolder1 = projectRegistry.getProject(projectPath).getBaseFolder();
        checkProjectExist(projectPath);
        checkChildrenFor(projectFolder1, children1);
        assertEquals(1, projectRegistry.getProjects().size());

        configs.clear();
        configs.add(config2);
        pm.createBatchProjects(configs, true, new ProjectOutputLineConsumerFactory("ws", 300));

        final FolderEntry projectFolder2 = projectRegistry.getProject(projectPath).getBaseFolder();
        checkProjectExist(projectPath);
        checkChildrenFor(projectFolder2, children2);
        assertEquals(1, projectRegistry.getProjects().size());
        assertNull(projectFolder2.getChild("folder1/"));
        assertNull(projectFolder2.getChild("folder1/file1.txt"));
    }

    @Test
    public void shouldSetBlankTypeAtCreatingBatchProjectsWhenConfigContainsUnregisteredProjectType()
            throws Exception {// If declared primary PT is not registered, project is created as Blank, with Problem 12

        final String projectPath = "/testProject";
        final String projectType = "unregisteredProjectType";

        final NewProjectConfig config = createProjectConfigObject("projectName", projectPath, projectType, null);

        final List<NewProjectConfig> configs = new ArrayList<>(1);
        configs.add(config);

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));

        final RegisteredProject project = projectRegistry.getProject(projectPath);
        final List<Problem> problems = project.getProblems();
        checkProjectExist(projectPath);
        assertNotEquals(projectType, project.getType());
        assertEquals(1, problems.size());
        assertEquals(12, problems.get(0).code);
        assertEquals(1, projectRegistry.getProjects().size());
    }

    @Test
    public void shouldCreateBatchProjectsWithoutMixinPTWhenThisOneIsUnregistered()
            throws Exception {// If declared mixin PT is not registered, project is created w/o it, with Problem 12

        final String projectPath = "/testProject";
        final String mixinPType = "unregistered";

        final NewProjectConfig config = createProjectConfigObject("projectName", projectPath, BaseProjectType.ID, null);
        config.getMixins().add(mixinPType);

        final List<NewProjectConfig> configs = new ArrayList<>(1);
        configs.add(config);

        pm.createBatchProjects(configs, false, new ProjectOutputLineConsumerFactory("ws", 300));

        final RegisteredProject project = projectRegistry.getProject(projectPath);
        final List<Problem> problems = project.getProblems();
        checkProjectExist(projectPath);
        assertEquals(1, problems.size());
        assertEquals(12, problems.get(0).code);
        assertTrue(project.getMixins().isEmpty());
        assertEquals(1, projectRegistry.getProjects().size());
    }

    @Test
    public void testCreateProject() throws Exception {
        Map<String, List<String>> attrs = new HashMap<>();
        List<String> v = new ArrayList<>();
        v.add("meV");
        attrs.put("var1", v);


        ProjectConfig config = DtoFactory.newDto(ProjectConfigDto.class)
                                         .withPath("createProject")
                                         .withName("create")
                                         .withType("primary1")
                                         .withDescription("description")
                                         .withAttributes(attrs);
        pm.createProject(config, new HashMap<>());


        RegisteredProject project = pm.getProject("/createProject");

        assertTrue(project.getBaseFolder().getVirtualFile().exists());
        assertEquals("/createProject", project.getPath());
        assertEquals(2, project.getAttributeEntries().size());
        assertEquals("meV", project.getAttributeEntries().get("var1").getString());
    }

    @Test
    public void testCreateProjectWithInvalidAttribute() throws Exception {
        // SPECS:
        // Project will be created with problem code = 13(Value for required attribute is not initialized)
        // when required attribute is not initialized
        final String path = "/testCreateProjectInvalidAttributes";
        final String projectType = "pt2";
        final NewProjectConfig config = new NewProjectConfigImpl(path, projectType, null, "name", "descr", null, null, null);

        pm.createProject(config, null);

        RegisteredProject project = projectRegistry.getProject(path);
        assertNotNull(project);
        assertNotNull(pm.getProjectsRoot().getChild(path));
        assertEquals(projectType, project.getType());

        List<Problem> problems = project.getProblems();
        assertNotNull(problems);
        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals(13, problems.get(0).code);
    }


    @Test
    public void testCreateProjectWithRequiredProvidedAttributeWhenGivenProjectTypeHasGenerator() throws Exception {
        final String path = "/testCreateProjectWithRequiredProvidedAttribute";
        final String projectTypeId = "pt3";
        final Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("pt2-var2", new AttributeValue("test").getList());
        final ProjectConfig pc = new NewProjectConfigImpl(path, projectTypeId, null, "name", "descr", attributes, null, null);

        pm.createProject(pc, null);

        final RegisteredProject project = projectRegistry.getProject(path);
        assertEquals(projectTypeId, project.getType());
        assertNotNull(project.getBaseFolder().getChild("file1"));
        assertEquals("pt2-provided1", project.getAttributes().get("pt2-provided1").get(0));
    }

    @Test
    public void testCreateProjectWithRequiredProvidedAttributeWhenGivenProjectTypeHasNotGenerator() throws Exception {
        // SPECS:
        // Project will be created with problem code = 13 (Value for required attribute is not initialized)
        // when project type has provided required attributes
        // but have not respective generator(CreateProjectHandler)

        final String path = "/testCreateProjectWithRequiredProvidedAttribute";
        final String projectTypeId = "pt4";
        final ProjectConfig pc = new NewProjectConfigImpl(path, projectTypeId, null, "name", "descr", null, null, null);

        pm.createProject(pc, null);

        final RegisteredProject project = projectRegistry.getProject(path);
        final List<VirtualFileEntry> children = project.getBaseFolder().getChildren();
        final List<Problem> problems = project.getProblems();
        assertNotNull(project);
        assertNotNull(pm.getProjectsRoot().getChild(path));
        assertEquals(projectTypeId, project.getType());
        assertTrue(children.isEmpty());
        assertTrue(project.getAttributes().isEmpty());
        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals(13, problems.get(0).code);
    }


    @Test
    public void testSamePathProjectCreateFailed() throws Exception {
        // SPECS:
        // If there is a project with the same path ConflictException will be thrown on create project

        ProjectConfig pc = new NewProjectConfigImpl("/testSamePathProjectCreateFailed", "blank", null, "name", "descr", null, null, null);

        pm.createProject(pc, null);

        pc = new NewProjectConfigImpl("/testSamePathProjectCreateFailed", "blank", null, "name", "descr", null, null, null);

        try {
            pm.createProject(pc, null);
            fail("ConflictException: Project config already exists /testSamePathProjectCreateFailed");
        } catch (ConflictException e) {
        }

        assertNotNull(projectRegistry.getProject("/testSamePathProjectCreateFailed"));
    }

    @Test
    public void testInvalidPTProjectCreateFailed() throws Exception {
        // SPECS:
        // project will be created as project of "blank" type
        // with problem code 12(Primary type "someType" is not registered. Base Project Type assigned.)
        // when primary project type is not registered in PT registry
        final String path = "/testInvalidPTProjectCreateFailed";
        ProjectConfig pc = new NewProjectConfigImpl(path, "invalid", null, "name", "descr", null, null, null);

        pm.createProject(pc, null);

        RegisteredProject project = projectRegistry.getProject(path);
        assertNotNull(project);
        assertNotNull(pm.getProjectsRoot().getChild(path));
        assertEquals(BaseProjectType.ID, project.getType());

        List<Problem> problems = project.getProblems();
        assertNotNull(problems);
        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals(12, problems.get(0).code);

        //clean up
        project.getBaseFolder().getVirtualFile().delete();
        projectRegistry.removeProjects(path);
        assertNull(projectRegistry.getProject(path));


        // SPECS:
        // project will be created without mixin project type and
        // with problem code 12(Project type "someType" is not registered. Skipped.)
        // when mixin project type is not registered in PT registry
        List<String> ms = new ArrayList<>();
        ms.add("invalid");

        pc = new NewProjectConfigImpl(path, "blank", ms, "name", "descr", null, null, null);
        pm.createProject(pc, null);

        project = projectRegistry.getProject(path);
        assertNotNull(project);
        assertNotNull(pm.getProjectsRoot().getChild(path));
        assertTrue(project.getMixins().isEmpty());

        problems = project.getProblems();
        assertNotNull(problems);
        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals(12, problems.get(0).code);
    }

    @Test
    public void testConflictAttributesProjectCreateFailed() throws Exception {
        // SPECS:
        // project will be created with problem code 13(Attribute name conflict)
        // when there are attributes with the same name in primary and mixin PT or between mixins

        final String path = "/testConflictAttributesProjectCreateFailed";
        final String projectTypeId = "pt2";
        final String mixin = "m2";
        final List<String> ms = new ArrayList<>(1);
        ms.add(mixin);

        ProjectConfig pc = new NewProjectConfigImpl(path, projectTypeId, ms, "name", "descr", null, null, null);
        pm.createProject(pc, null);

        final RegisteredProject project = projectRegistry.getProject(path);
        assertNotNull(project);
        assertNotNull(pm.getProjectsRoot().getChild(path));

        final List<String> mixins = project.getMixins();
        assertFalse(mixins.isEmpty());
        assertEquals(mixin, mixins.get(0));

        final List<Problem> problems = project.getProblems();
        assertNotNull(problems);
        assertFalse(problems.isEmpty());
        assertEquals(13, problems.get(0).code);
    }

    @Test
    public void testInvalidConfigProjectCreateFailed() throws Exception {
        // SPECS:
        // If project path is not defined
        // Project creation failed with ConflictException

        ProjectConfig pc = new NewProjectConfigImpl(null, "pt2", null, "name", "descr", null, null, null);

        try {
            pm.createProject(pc, null);
            fail("ConflictException: Path for new project should be defined ");
        } catch (ConflictException e) {
        }
    }


    @Test
    public void testCreateInnerProject() throws Exception {
        ProjectConfig pc = new NewProjectConfigImpl("/testCreateInnerProject", BaseProjectType.ID, null, "name", "descr", null, null, null);
        pm.createProject(pc, null);

        pc = new NewProjectConfigImpl("/testCreateInnerProject/inner", BaseProjectType.ID, null, "name", "descr", null, null, null);
        pm.createProject(pc, null);

        assertNotNull(projectRegistry.getProject("/testCreateInnerProject/inner"));
        assertEquals(2, projectRegistry.getProjects().size());
        assertEquals(1, projectRegistry.getProjects("/testCreateInnerProject").size());

        // If there are no parent folder it will be created

        pc = new NewProjectConfigImpl("/nothing/inner", BaseProjectType.ID, null, "name", "descr", null, null, null);

        pm.createProject(pc, null);
        assertNotNull(projectRegistry.getProject("/nothing/inner"));
        assertNotNull(projectRegistry.getProject("/nothing"));
        assertNotNull(pm.getProjectsRoot().getChildFolder("/nothing"));
    }


    @Test
    public void testUpdateProjectWithPersistedAttributes() throws Exception {
        Map<String, List<String>> attributes = new HashMap<>();

        ProjectConfig pc = new NewProjectConfigImpl("/testUpdateProject", BaseProjectType.ID, null, "name", "descr", null, null, null);
        RegisteredProject p = pm.createProject(pc, null);

        assertEquals(BaseProjectType.ID, p.getType());
        assertEquals("name", p.getName());

        attributes.put("pt2-var2", new AttributeValue("updated").getList());
        ProjectConfig pc1 = new NewProjectConfigImpl("/testUpdateProject", "pt2", null, "updatedName", "descr", attributes, null, null);

        p = pm.updateProject(pc1);

        assertEquals("pt2", p.getType());
        assertEquals("updated", p.getAttributes().get("pt2-var2").get(0));
        assertEquals("updatedName", p.getName());


    }

    @Test
    public void testUpdateProjectWithProvidedAttributes() throws Exception {
        // SPECS: Project should be updated with problem code = 13 when value for required attribute is not initialized

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("pt2-var2", new AttributeValue("test").getList());

        ProjectConfig pc = new NewProjectConfigImpl("/testUpdateProject", "pt2", null, "name", "descr", attributes, null, null);
        pm.createProject(pc, null);

        pc = new NewProjectConfigImpl("/testUpdateProject", "pt3", null, "updatedName", "descr", attributes, null, null);


        RegisteredProject project = pm.updateProject(pc);

        final List<Problem> problems = project.getProblems();
        assertNotNull(problems);
        assertFalse(problems.isEmpty());
        assertEquals(1, problems.size());
        assertEquals(13, problems.get(0).code);
    }


    @Test
    public void testUpdateProjectOnRawFolder() throws Exception {
        ProjectConfig pc = new NewProjectConfigImpl("/testUpdateProjectOnRawFolder", BaseProjectType.ID, null, "name", "descr", null, null, null);
        pm.createProject(pc, null);
        String folderPath = "/testUpdateProjectOnRawFolder/folder";
        pm.getProjectsRoot().createFolder(folderPath);

        // SPECS:
        // If update is called on raw folder new project should be created

        pc = new NewProjectConfigImpl(folderPath, BaseProjectType.ID, null, "raw", "descr", null, null, null);
        pm.updateProject(pc);

        assertEquals(BaseProjectType.ID, pm.getProject(folderPath).getType());
    }

    @Test
    public void testInvalidUpdateConfig() throws Exception {
        ProjectConfig pc = new NewProjectConfigImpl(null, BaseProjectType.ID, null, "name", "descr", null, null,  null);

        try {
            pm.updateProject(pc);
            fail("ConflictException: Project path is not defined");
        } catch (ConflictException e) {
        }

        pc = new NewProjectConfigImpl("/nothing", BaseProjectType.ID, null, "name", "descr", null, null, null);
        try {
            pm.updateProject(pc);
            fail("NotFoundException: Project '/nothing' doesn't exist.");
        } catch (NotFoundException e) {
        }
    }


    @Test
    public void testDeleteProject() throws Exception {

        ProjectConfig pc = new NewProjectConfigImpl("/testDeleteProject", BaseProjectType.ID, null, "name", "descr", null, null, null);
        pm.createProject(pc, null);
        pc = new NewProjectConfigImpl("/testDeleteProject/inner", BaseProjectType.ID, null, "name", "descr", null, null, null);
        pm.createProject(pc, null);

        assertNotNull(projectRegistry.getProject("/testDeleteProject/inner"));

        pm.delete("/testDeleteProject");

        assertNull(projectRegistry.getProject("/testDeleteProject/inner"));
        assertNull(projectRegistry.getProject("/testDeleteProject"));
        assertNull(pm.getProjectsRoot().getChild("/testDeleteProject/inner"));
        //assertNull(projectRegistry.folder("/testDeleteProject/inner"));
    }

    @Test
    public void testDeleteProjectEvent() throws Exception {
        ProjectConfig pc = new NewProjectConfigImpl("/testDeleteProject", BaseProjectType.ID, null, "name", "descr", null, null, null);
        pm.createProject(pc, null);

        String[] deletedPath = new String[1];
        eventService.subscribe(new EventSubscriber<ProjectDeletedEvent>() {
            @Override
            public void onEvent(ProjectDeletedEvent event) {deletedPath[0] = event.getProjectPath();}
        });
        pm.delete("/testDeleteProject");

        assertEquals("/testDeleteProject", deletedPath[0]);
    }


    @Test
    public void testImportProject() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        String fileContent = "to be or not to be";
        ZipOutputStream zipOut = new ZipOutputStream(bout);
        zipOut.putNextEntry(new ZipEntry("folder1/"));
        zipOut.putNextEntry(new ZipEntry("folder1/file1.txt"));
        zipOut.putNextEntry(new ZipEntry("file1"));
        zipOut.write(fileContent.getBytes());
        zipOut.close();
        final InputStream zip = new ByteArrayInputStream(bout.toByteArray());
        final String importType = "_123_";
        registerImporter(importType, zip);

        SourceStorage sourceConfig = DtoFactory.newDto(SourceStorageDto.class).withType(importType);

        pm.importProject("/testImportProject", sourceConfig, false, () -> new ProjectImportOutputWSLineConsumer("BATCH", "ws", 300));

        RegisteredProject project = projectRegistry.getProject("/testImportProject");

        assertNotNull(project);

        // BASE
        //System.out.println(">>> "+project.getProjectType());

        assertNotNull(project.getBaseFolder().getChild("file1"));
        assertEquals(fileContent, project.getBaseFolder().getChild("file1").getVirtualFile().getContentAsString());
    }

    @Test
    public void testRemoveFolderForSourcesWhenImportingProjectIsFailed() throws Exception {
        final String projectPath = "/testImportProject";
        final String importType = "_123_";

        registerImporter(importType, null);

        SourceStorage sourceConfig = DtoFactory.newDto(SourceStorageDto.class).withType(importType);
        try {
            pm.importProject(projectPath, sourceConfig, false, () -> new ProjectImportOutputWSLineConsumer("testImportProject", "ws", 300));
        } catch (Exception e) {
        }

        boolean projectFolderExist = vfsProvider.getVirtualFileSystem().getRoot().hasChild(Path.of(projectPath));
        assertFalse(projectFolderExist);
    }

    @Test
    public void testImportProjectWithoutImporterFailed() throws Exception {
        SourceStorage sourceConfig = DtoFactory.newDto(SourceStorageDto.class).withType("nothing");

        try {
            pm.importProject("/testImportProject", sourceConfig, false, () -> new ProjectImportOutputWSLineConsumer("testImportProject", "ws", 300));
            fail("NotFoundException: Unable import sources project from 'null'. Sources type 'nothing' is not supported.");
        } catch (NotFoundException e) {
        }
    }


    @Test
    public void testProvidedAttributesNotSerialized() throws Exception {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("pt2-var2", new AttributeValue("test2").getList());
        attributes.put("pt2-var1", new AttributeValue("test1").getList());
        ProjectConfig pc =
                new NewProjectConfigImpl("/testProvidedAttributesNotSerialized", "pt3", null, "name", "descr", attributes, null, null);

        pm.createProject(pc, null);

        // SPECS:
        // Only persisted variables should be persisted (no constants, no provided variables)

        for (ProjectConfig project : workspaceHolder.getProjects()) {

            if (project.getPath().equals("/testProvidedAttributesNotSerialized")) {

                assertNotNull(project.getAttributes().get("pt2-var1"));
                assertNotNull(project.getAttributes().get("pt2-var2"));
                assertNull(project.getAttributes().get("pt2-const1"));
                assertNull(project.getAttributes().get("pt2-provided1"));
            }
        }
    }


    @Test
    public void testSettableValueProvider() throws Exception {
        assertTrue(((Variable)projectTypeRegistry.getProjectType("settableVPPT").getAttribute("my")).isValueProvided());

        ProjectConfig pc = new NewProjectConfigImpl("/testSettableValueProvider", "settableVPPT", null, "", "", new HashMap<>(), null, null);

        pm.createProject(pc, null);

        RegisteredProject project = pm.getProject("/testSettableValueProvider");

        assertEquals(1, project.getAttributes().size());
        assertEquals("notset", project.getAttributes().get("my").get(0));

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("my", new AttributeValue("set").getList());
        pc = new NewProjectConfigImpl("/testSettableValueProvider", "settableVPPT", null, "", "", attributes, null, null);

        pm.updateProject(pc);
        project = pm.getProject("/testSettableValueProvider");
        assertEquals("set", project.getAttributes().get("my").get(0));
    }

     /* ---------------------------------- */
    /* private */
    /* ---------------------------------- */

    private void checkProjectExist(String projectPath) {
        RegisteredProject project = projectRegistry.getProject(projectPath);
        FolderEntry projectFolder = project.getBaseFolder();
        assertNotNull(project);
        assertTrue(projectFolder.getVirtualFile().exists());
        assertEquals(projectPath, project.getPath());
        assertEquals(BaseProjectType.ID, project.getType());
    }

    private void checkChildrenFor(FolderEntry projectFolder, List<String> children) throws ServerException, ForbiddenException {
        for (String path : children) {
            assertNotNull(projectFolder.getChild(path));
            if (path.contains("file")) {
                String fileContent = projectFolder.getChild(path).getVirtualFile().getContentAsString();
                assertEquals(FILE_CONTENT, fileContent);
            }
        }
    }

    private InputStream prepareZipArchiveBasedOn(List<String> paths) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(bout);

        for (String path : paths) {
            zipOut.putNextEntry(new ZipEntry(path));

            if (path.contains("file")) {
                zipOut.write(FILE_CONTENT.getBytes());
            }
        }
        zipOut.close();
        return new ByteArrayInputStream(bout.toByteArray());
    }

    private NewProjectConfigDto createProjectConfigObject(String projectName,
                                                          String projectPath,
                                                          String projectType,
                                                          SourceStorageDto sourceStorage) {
        return DtoFactory.newDto(NewProjectConfigDto.class)
                         .withPath(projectPath)
                         .withName(projectName)
                         .withType(projectType)
                         .withDescription("description")
                         .withSource(sourceStorage)
                         .withAttributes(new HashMap<>());
    }

    private void registerImporter(String importType, InputStream zip) throws Exception {
        final ValueHolder<FolderEntry> folderHolder = new ValueHolder<>();
        importerRegistry.register(new ProjectImporter() {
            @Override
            public String getId() {
                return importType;
            }

            @Override
            public boolean isInternal() {
                return false;
            }

            @Override
            public String getDescription() {
                return "importer";
            }

            @Override
            public void importSources(FolderEntry baseFolder, SourceStorage storage) throws ConflictException,
                                                                                            ServerException,
                                                                                            ForbiddenException {
                importSources(baseFolder, storage, LineConsumerFactory.NULL);
            }

            @Override
            public void importSources(FolderEntry baseFolder,
                                      SourceStorage storage,
                                      LineConsumerFactory importOutputConsumerFactory) throws ConflictException,
                                                                                              ServerException,
                                                                                              ForbiddenException {
                // Don't really use location in this test.
                baseFolder.getVirtualFile().unzip(zip, true, 0);
                folderHolder.set(baseFolder);
            }

            @Override
            public ImporterCategory getCategory() {
                return ProjectImporter.ImporterCategory.ARCHIVE;
            }
        });
    }


    class SrcGenerator implements CreateProjectHandler {

        @Override
        public void onCreateProject(Path projectPath, Map<String, AttributeValue> attributes, Map<String, String> options)
                throws ForbiddenException, ConflictException, ServerException {
            FolderEntry baseFolder = new FolderEntry(vfsProvider.getVirtualFileSystem().getRoot().createFolder(projectPath.toString()));
            baseFolder.createFolder("file1");
        }

        @Override
        public String getProjectType() {
            return "pt3";
        }
    }
}
