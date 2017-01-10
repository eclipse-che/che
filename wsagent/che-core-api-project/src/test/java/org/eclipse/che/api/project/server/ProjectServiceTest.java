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

import com.google.common.io.ByteStreams;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.watcher.FileWatcherManager;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.ApplicationContext;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.Application;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.eclipse.che.commons.lang.ws.rs.ExtMediaType.APPLICATION_ZIP;
import static org.everrest.core.ApplicationContext.anApplicationContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;

/**
 * @author andrew00x
 * @author Eugene Voevodin
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ProjectServiceTest {
    private static final String CONTENT_TYPE = "Content-Type";

    private static final String vfsUser = "dev";

    protected final static String FS_PATH    = "target/fss";
    protected final static String INDEX_PATH = "target/fss_index";

    private static final String URL_ENCODED_QUOTES            = "%22";
    private static final String URL_ENCODED_SPACE             = "%20";
    private static final String URL_ENCODED_BACKSLASH         = "%5C";
    private static final String URL_ENCODED_ASTERISK          = "%2A";

    private static final String AND_OPERATOR = "AND";
    private static final String NOT_OPERATOR = "NOT";

    private static final String EXCLUDE_SEARCH_PATH = ".codenvy";

    private ProjectManager         pm;
    private ResourceLauncher       launcher;
    private ProjectHandlerRegistry phRegistry;

    private org.eclipse.che.commons.env.EnvironmentContext env;

    private List<ProjectConfigDto> projects;

    @Mock
    private UserDao                userDao;
    @Mock
    private WorkspaceDto           usersWorkspaceMock;
    @Mock
    private WorkspaceConfigDto     workspaceConfigMock;
    @Mock
    private HttpJsonRequestFactory httpJsonRequestFactory;
    @Mock
    private HttpJsonResponse       httpJsonResponse;
    @Mock
    private FileWatcherManager     fileWatcherManager;

    protected LocalVirtualFileSystemProvider vfsProvider;

    private ProjectImporterRegistry importerRegistry;

    protected ProjectRegistry projectRegistry;

    protected ProjectTypeRegistry ptRegistry;

    @BeforeMethod
    public void setUp() throws Exception {

        WorkspaceProjectsSyncer workspaceHolder = new WsAgentTestBase.TestWorkspaceHolder();

        File root = new File(FS_PATH);

        if (root.exists()) {
            IoUtil.deleteRecursive(root);
        }
        root.mkdir();


        File indexDir = new File(INDEX_PATH);

        if (indexDir.exists()) {
            IoUtil.deleteRecursive(indexDir);
        }
        indexDir.mkdir();

        Set<PathMatcher> filters = new HashSet<>();
        filters.add(path -> {
            for (java.nio.file.Path pathElement : path) {
                if (pathElement == null || EXCLUDE_SEARCH_PATH.equals(pathElement.toString())) {
                    return true;
                }
            }
            return false;
        });

        FSLuceneSearcherProvider sProvider = new FSLuceneSearcherProvider(indexDir, filters);

        vfsProvider = new LocalVirtualFileSystemProvider(root, sProvider);

        final EventService eventService = new EventService();

        // PTs for test
        ProjectTypeDef chuck = new ProjectTypeDef("chuck_project_type", "chuck_project_type", true, false) {
            {
                addConstantDefinition("x", "attr description", new AttributeValue(Arrays.asList("a", "b")));
            }
        };

        Set<ProjectTypeDef> projectTypes = new HashSet<>();
        final LocalProjectType myProjectType = new LocalProjectType("my_project_type", "my project type");
        projectTypes.add(myProjectType);
        projectTypes.add(new LocalProjectType("module_type", "module type"));
        projectTypes.add(chuck);

        ptRegistry = new ProjectTypeRegistry(projectTypes);

        phRegistry = new ProjectHandlerRegistry(new HashSet<>());

        importerRegistry = new ProjectImporterRegistry(Collections.<ProjectImporter>emptySet());

        projectRegistry = new ProjectRegistry(workspaceHolder, vfsProvider, ptRegistry, phRegistry, eventService);
        projectRegistry.initProjects();

        FileWatcherNotificationHandler fileWatcherNotificationHandler = new DefaultFileWatcherNotificationHandler(vfsProvider);
        FileTreeWatcher fileTreeWatcher = new FileTreeWatcher(root, new HashSet<>(), fileWatcherNotificationHandler);

        pm = new ProjectManager(vfsProvider, new EventService(), ptRegistry, projectRegistry, phRegistry,
                                importerRegistry, fileWatcherNotificationHandler, fileTreeWatcher, workspaceHolder,
                                fileWatcherManager);
        pm.initWatcher();

        HttpJsonRequest httpJsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());

        //List<ProjectConfigDto> modules = new ArrayList<>();

        projects = new ArrayList<>();
        addMockedProjectConfigDto(myProjectType, "my_project");

        when(httpJsonRequestFactory.fromLink(any())).thenReturn(httpJsonRequest);
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
        when(httpJsonResponse.asDto(WorkspaceDto.class)).thenReturn(usersWorkspaceMock);
        when(usersWorkspaceMock.getConfig()).thenReturn(workspaceConfigMock);
        when(workspaceConfigMock.getProjects()).thenReturn(projects);

//        verify(httpJsonRequestFactory).fromLink(eq(DtoFactory.newDto(Link.class)
//                                                             .withHref(apiEndpoint + "/workspace/" + workspace + "/project")
//                                                             .withMethod(PUT)));

        DependencySupplierImpl dependencies = new DependencySupplierImpl();


        dependencies.addInstance(ProjectTypeRegistry.class, ptRegistry);
        dependencies.addInstance(UserDao.class, userDao);
        dependencies.addInstance(ProjectManager.class, pm);
        dependencies.addInstance(ProjectImporterRegistry.class, importerRegistry);
        dependencies.addInstance(ProjectHandlerRegistry.class, phRegistry);
        dependencies.addInstance(EventService.class, eventService);

        ResourceBinder resources = new ResourceBinderImpl();
        ProviderBinder providers = ProviderBinder.getInstance();
        EverrestProcessor processor = new EverrestProcessor(new EverrestConfiguration(),
                                                            dependencies,
                                                            new RequestHandlerImpl(new RequestDispatcher(resources), providers),
                                                            null);
        launcher = new ResourceLauncher(processor);

        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return java.util.Collections.<Class<?>>singleton(ProjectService.class);
            }

            @Override
            public Set<Object> getSingletons() {
                return new HashSet<>(Arrays.asList(new ApiExceptionMapper()));
            }
        });

        ApplicationContext.setCurrent(anApplicationContext().withProviders(providers).build());

        env = org.eclipse.che.commons.env.EnvironmentContext.getCurrent();

    }

    @AfterMethod
    public void tearDown() throws Exception {
        pm.stop();
    }

    private void addMockedProjectConfigDto(org.eclipse.che.api.project.server.type.ProjectTypeDef myProjectType, String projectName)
            throws ForbiddenException, ServerException, NotFoundException, ConflictException {
        final ProjectConfigDto testProjectConfigMock = mock(ProjectConfigDto.class);
        when(testProjectConfigMock.getPath()).thenReturn("/" + projectName);
        when(testProjectConfigMock.getName()).thenReturn(projectName);
        when(testProjectConfigMock.getDescription()).thenReturn("my test project");
        when(testProjectConfigMock.getType()).thenReturn("my_project_type");
        when(testProjectConfigMock.getSource()).thenReturn(DtoFactory.getInstance().createDto(SourceStorageDto.class));
        //        when(testProjectConfigMock.getModules()).thenReturn(modules);
        //        when(testProjectConfigMock.findModule(anyString())).thenReturn(testProjectConfigMock);

        Map<String, List<String>> attr = new HashMap<>();
        for (Attribute attribute : myProjectType.getAttributes()) {
            attr.put(attribute.getName(), attribute.getValue().getList());
        }
        when(testProjectConfigMock.getAttributes()).thenReturn(attr);

        projects.add(testProjectConfigMock);

        pm.createProject(testProjectConfigMock, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetProjects() throws Exception {
        List<RegisteredProject> p = pm.getProjects();

        assertEquals(p.size(), 1);

        vfsProvider.getVirtualFileSystem().getRoot().createFolder("not_project");

        // to refresh
        projectRegistry.initProjects();

        ContainerResponse response =
                launcher.service(GET, "http://localhost:8080/api/project", "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ProjectConfigDto> result = (List<ProjectConfigDto>)response.getEntity();
        assertNotNull(result);
        assertEquals(result.size(), 2);
        int good, bad;

        if (result.get(0).getName().equals("my_project")) {
            good = 0; bad = 1;
        } else {
            good = 1; bad = 0;
        }

        ProjectConfigDto projectDescriptor = result.get(good);

        assertEquals(projectDescriptor.getName(), "my_project");
        assertEquals(projectDescriptor.getDescription(), "my test project");

        assertEquals(projectDescriptor.getType(), "my_project_type");

        ProjectConfigDto badProject = result.get(bad);
        assertEquals(badProject.getName(), "not_project");
        assertNotNull(badProject.getProblems());
    }


    @Test
    public void testGetProject() throws Exception {
        ContainerResponse response =
                launcher.service(GET, "http://localhost:8080/api/project/my_project",
                                 "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        ProjectConfigDto result = (ProjectConfigDto)response.getEntity();
        assertNotNull(result);
        assertEquals(result.getDescription(), "my test project");
        assertEquals(result.getType(), "my_project_type");
        Map<String, List<String>> attributes = result.getAttributes();
        assertNotNull(attributes);
        assertEquals(attributes.size(), 1);
        assertEquals(attributes.get("my_attribute"), singletonList("attribute value 1"));
        validateProjectLinks(result);
    }

    @Test
    public void testGetNotValidProject() throws Exception {
        //MountPoint mountPoint = pm.getProjectsRoot(workspace).getVirtualFile().getMountPoint();
        vfsProvider.getVirtualFileSystem().getRoot().createFolder("not_project");
        // to refresh
        projectRegistry.initProjects();
        ContainerResponse response = launcher.service(GET, "http://localhost:8080/api/project/not_project",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        ProjectConfigDto badProject = (ProjectConfigDto)response.getEntity();
        assertNotNull(badProject);
        assertEquals(badProject.getName(), "not_project");
        assertNotNull(badProject.getProblems());
        assertTrue(badProject.getProblems().size() > 0);
        assertEquals(11, badProject.getProblems().get(0).getCode());
        validateProjectLinks(badProject);
    }

    @Test
    public void testGetProjectCheckUserPermissions() throws Exception {
        // Without roles Collections.<String>emptySet() should get default set of permissions
        env.setSubject(new SubjectImpl(vfsUser, vfsUser, "dummy_token", false));
        ContainerResponse response =
                launcher.service(GET, "http://localhost:8080/api/project/my_project",
                                 "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        ProjectConfigDto result = (ProjectConfigDto)response.getEntity();
        assertNotNull(result);
    }


    @Test
    public void testGetProjectInvalidPath() throws Exception {
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/my_project_invalid",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void testCreateProject() throws Exception {
        final String projectName = "new_project";
        final String projectType = "testCreateProject";
        phRegistry.register(createProjectHandlerFor(projectName, projectType));

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", singletonList(APPLICATION_JSON));

        ProjectTypeDef pt = new ProjectTypeDef("testCreateProject", "my project type", true, false) {
            {
                addConstantDefinition("new_project_attribute", "attr description", "to be or not to be");
            }
        };

        ptRegistry.registerProjectType(pt);

        Map<String, List<String>> attributeValues = new LinkedHashMap<>();
        attributeValues.put("new_project_attribute", singletonList("to be or not to be"));


        final ProjectConfigDto newProjectConfig = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                            .withPath("/new_project")
                                                            .withName(projectName)
                                                            .withDescription("new project")
                                                            .withType(projectType)
                                                            .withAttributes(attributeValues)
                                                            .withSource(DtoFactory.getInstance().createDto(SourceStorageDto.class));
        projects.add(newProjectConfig);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project",
                                                      "http://localhost:8080/api",
                                                      headers,
                                                      DtoFactory.getInstance().toJson(newProjectConfig).getBytes(Charset.defaultCharset()),
                                                      null);

        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        ProjectConfigDto result = (ProjectConfigDto)response.getEntity();
        assertNotNull(result);
        assertEquals(result.getName(), projectName);
        assertEquals(result.getPath(), "/new_project");
        assertEquals(result.getDescription(), newProjectConfig.getDescription());
        assertEquals(result.getType(), newProjectConfig.getType());
        assertEquals(result.getType(), projectType);
        Map<String, List<String>> attributes = result.getAttributes();
        assertNotNull(attributes);
        assertEquals(attributes.size(), 1);
        assertEquals(attributes.get("new_project_attribute"), singletonList("to be or not to be"));
        validateProjectLinks(result);

        RegisteredProject project = pm.getProject("new_project");
        assertNotNull(project);

        //ProjectConfig config = project.getConfig();

        assertEquals(project.getDescription(), newProjectConfig.getDescription());
        assertEquals(project.getProjectType().getId(), newProjectConfig.getType());
        String attributeVal = project.getAttributeEntries().get("new_project_attribute").getString();
        assertNotNull(attributeVal);
        assertEquals(attributeVal, "to be or not to be");

        assertNotNull(project.getBaseFolder().getChild("a"));
        assertNotNull(project.getBaseFolder().getChild("b"));
        assertNotNull(project.getBaseFolder().getChild("test.txt"));
    }

    @Test
    public void testCreateBatchProjects() throws Exception {
        //prepare first project
        final String projectName1 = "testProject1";
        final String projectTypeId1 = "testProjectType1";
        final String projectPath1 = "/testProject1";

        createTestProjectType(projectTypeId1);
        phRegistry.register(createProjectHandlerFor(projectName1, projectTypeId1));

        //prepare inner project
        final String innerProjectName = "innerProject";
        final String innerProjectTypeId = "testProjectType2";
        final String innerProjectPath = "/testProject1/innerProject";

        createTestProjectType(innerProjectTypeId);
        phRegistry.register(createProjectHandlerFor(innerProjectName, innerProjectTypeId));

        //prepare project to import
        final String importProjectName = "testImportProject";
        final String importProjectTypeId = "testImportProjectType";
        final String importProjectPath = "/testImportProject";
        final String importType = "importType";
        final String [] paths = {"a", "b", "test.txt"};

        final List<String> children = new ArrayList<>(Arrays.asList(paths));
        registerImporter(importType, prepareZipArchiveBasedOn(children));
        createTestProjectType(importProjectTypeId);

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", singletonList(APPLICATION_JSON));

        try (InputStream content = getClass().getResourceAsStream("batchNewProjectConfigs.json")) {
            ContainerResponse response = launcher.service(POST,
                                                          "http://localhost:8080/api/project/batch",
                                                          "http://localhost:8080/api",
                                                          headers,
                                                          ByteStreams.toByteArray(content), null);

            assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());

            final List<ProjectConfigDto> result = (List<ProjectConfigDto>)response.getEntity();
            assertNotNull(result);
            assertEquals(result.size(), 3);

            final ProjectConfigDto importProjectConfig = result.get(0);
            checkProjectIsCreated(importProjectName, importProjectPath, importProjectTypeId, importProjectConfig);

            final ProjectConfigDto config1 = result.get(1);
            checkProjectIsCreated(projectName1, projectPath1, projectTypeId1, config1);

            final ProjectConfigDto innerProjectConfig = result.get(2);
            checkProjectIsCreated(innerProjectName, innerProjectPath, innerProjectTypeId, innerProjectConfig);
        }
    }

    @Test
    public void testUpdateProject() throws Exception {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", singletonList(APPLICATION_JSON));

        ProjectTypeDef pt = new ProjectTypeDef("testUpdateProject", "my project type", true, false) {
        };
        ptRegistry.registerProjectType(pt);

        pm.createProject(DtoFactory.getInstance().createDto(ProjectConfigDto.class).withDescription("created project").withType(
                                 "testUpdateProject").withPath("/testUpdateProject"), null);

        Map<String, List<String>> attributeValues = new LinkedHashMap<>();
        attributeValues.put("my_attribute", singletonList("to be or not to be"));

        ProjectConfigDto descriptor = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                .withName("module1")
                                                .withType("testUpdateProject")
                                                .withDescription("updated project")
                                                .withAttributes(attributeValues);


        ContainerResponse response = launcher.service(PUT,
                                                      "http://localhost:8080/api/project/testUpdateProject",
                                                      "http://localhost:8080/api",
                                                      headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()),
                                                      null);

        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());

        RegisteredProject project = pm.getProject("/testUpdateProject");
        assertNotNull(project);
        //ProjectConfig config = project.getConfig();

        assertEquals(project.getDescription(), "updated project");
        assertEquals(project.getProjectType().getId(), "testUpdateProject");
    }

    @Test
    public void testUpdateBadProject() throws Exception {
        //MountPoint mountPoint = pm.getProjectsRoot(workspace).getVirtualFile().getMountPoint();
        //mountPoint.getRoot().createFolder("not_project");
        pm.getProjectsRoot().createFolder("not_project");
        projectRegistry.initProjects();

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", singletonList(APPLICATION_JSON));
        Map<String, List<String>> attributeValues = new LinkedHashMap<>();
        attributeValues.put("my_attribute", singletonList("to be or not to be"));
        ProjectConfigDto descriptor = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                .withType("my_project_type")
                                                .withDescription("updated project")
                                                .withAttributes(attributeValues);

        final ProjectConfigDto newProjectConfig = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                            .withPath("/not_project")
                                                            .withName("not_project")
                                                            .withDescription("updated project")
                                                            .withType("my_project_type")
                                                            .withAttributes(attributeValues)
                                                            .withSource(DtoFactory.getInstance().createDto(SourceStorageDto.class));
        projects.add(newProjectConfig);

        ContainerResponse response = launcher.service(PUT,
                                                      "http://localhost:8080/api/project/not_project",
                                                      "http://localhost:8080/api",
                                                      headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()),
                                                      null);

        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        RegisteredProject project = pm.getProject("not_project");
        assertNotNull(project);
        //ProjectConfig description = project.getConfig();

        assertEquals(project.getDescription(), "updated project");
        assertEquals(project.getProjectType().getId(), "my_project_type");
    }

    @Test
    public void testUpdateProjectInvalidPath() throws Exception {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", singletonList(APPLICATION_JSON));
        Map<String, List<String>> attributeValues = new LinkedHashMap<>();
        attributeValues.put("my_attribute", singletonList("to be or not to be"));
        ProjectConfigDto descriptor = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                .withType("my_project_type")
                                                .withDescription("updated project")
                                                .withAttributes(attributeValues);
        ContainerResponse response = launcher.service(PUT,
                                                      "http://localhost:8080/api/project/my_project_invalid",
                                                      "http://localhost:8080/api",
                                                      headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()),
                                                      null);
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void testEstimateProject() throws Exception {
        VirtualFile root = pm.getProjectsRoot().getVirtualFile();

                                     //getVirtualFileSystemRegistry().getProvider("my_ws").getMountPoint(false).getRoot();
        root.createFolder("testEstimateProjectGood").createFolder("check");
        root.createFolder("testEstimateProjectBad");

        String errMessage = "File /check not found";

        final ValueProviderFactory vpf1 = projectFolder -> new ReadonlyValueProvider() {
            @Override
            public List<String> getValues(String attributeName) throws ValueStorageException {

                VirtualFileEntry file;
                try {
                    file = projectFolder.getChild("check");
                } catch (ServerException e) {
                    throw new ValueStorageException(e.getMessage());
                }

                if (file == null) {
                    throw new ValueStorageException(errMessage);
                }
                return (List <String>)singletonList("checked");
            }

        };

        ProjectTypeDef pt = new ProjectTypeDef("testEstimateProjectPT", "my testEstimateProject type", true, false) {
            {
                addVariableDefinition("calculated_attribute", "attr description", true, vpf1);
                addVariableDefinition("my_property_1", "attr description", true);
                addVariableDefinition("my_property_2", "attr description", false);
            }
        };

        ptRegistry.registerProjectType(pt);

        ContainerResponse response = launcher.service(GET, format("http://localhost:8080/api/project/estimate/%s?type=%s",
                                                                  "testEstimateProjectGood", "testEstimateProjectPT"),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        //noinspection unchecked
        SourceEstimation result = (SourceEstimation)response.getEntity();
        assertTrue(result.isMatched());
        assertEquals(result.getAttributes().size(), 1);
        assertEquals(result.getAttributes().get("calculated_attribute").get(0), "checked");

        // if project not matched
        response = launcher.service(GET, format("http://localhost:8080/api/project/estimate/%s?type=%s",
                                                "testEstimateProjectBad", "testEstimateProjectPT"),
                                    "http://localhost:8080/api", null, null, null);

        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        //noinspection unchecked
        result = (SourceEstimation)response.getEntity();
        assertFalse(result.isMatched());
        assertEquals(result.getAttributes().size(), 0);
    }



    @Test
    public void testResolveSources() throws Exception {

        VirtualFile root = pm.getProjectsRoot().getVirtualFile();
        root.createFolder("testEstimateProjectGood").createFolder("check");
        root.createFolder("testEstimateProjectBad");

        final ValueProviderFactory vpf1 = projectFolder -> new ReadonlyValueProvider() {
            @Override
            public List<String> getValues(String attributeName) throws ValueStorageException {

                VirtualFileEntry file;
                try {
                    file = projectFolder.getChild("check");
                } catch (ServerException e) {
                    throw new ValueStorageException(e.getMessage());
                }

                if (file == null) {
                    throw new ValueStorageException("Check not found");
                }
                return (List<String>)singletonList("checked");
            }

        };

        ProjectTypeDef pt = new ProjectTypeDef("testEstimateProjectPT", "my testEstimateProject type", true, false) {
            {
                addVariableDefinition("calculated_attribute", "attr description", true, vpf1);
                addVariableDefinition("my_property_1", "attr description", true);
                addVariableDefinition("my_property_2", "attr description", false);
            }
        };

        ptRegistry.registerProjectType(pt);

        ContainerResponse response =
                launcher.service(GET, format("http://localhost:8080/api/project/resolve/%s",
                                             "testEstimateProjectGood"),
                                                    "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<SourceEstimation> result = (List<SourceEstimation>) response.getEntity();

        assertTrue(result.size() > 0);
        boolean m = false;
        for(SourceEstimation est : result) {
            if(est.getType().equals("testEstimateProjectPT")) {
                assertTrue(est.isMatched());
                m = true;
            }

        }
        assertTrue(m);

    }


    @Test
    public void testImportProject() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(bout);
        zipOut.putNextEntry(new ZipEntry("folder1/"));
        zipOut.putNextEntry(new ZipEntry("folder1/file1.txt"));
        zipOut.write("to be or not to be".getBytes(Charset.defaultCharset()));
        zipOut.close();
        final InputStream zip = new ByteArrayInputStream(bout.toByteArray());
        final String importType = "_123_";
        registerImporter(importType, zip);

        final String myType = "chuck_project_type";

        final ProjectConfigDto newProjectConfig = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                            .withPath("/new_project")
                                                            .withName("new_project")
                                                            .withDescription("import test")
                                                            .withType(myType);
        projects.add(newProjectConfig);

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", singletonList(APPLICATION_JSON));

        String json = "{\n" +
                      "            \"location\": null,\n" +
                      "            \"type\": \"%s\"\n" +
                      "}";

        byte[] b = format(json, importType).getBytes(Charset.defaultCharset());
        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/import/new_project",
                                                      "http://localhost:8080/api", headers, b, null);
        assertEquals(response.getStatus(), 204);

        RegisteredProject newProject = pm.getProject("new_project");
        assertNotNull(newProject);

        //assertNotNull(newProject.getConfig());
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
                return "Chuck importer";
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
                return ImporterCategory.ARCHIVE;
            }
        });
    }




    @Test
    public void testCreateFile() throws Exception {
        String myContent = "to be or not to be";
        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/file/my_project?name=test.txt",
                                                      "http://localhost:8080/api",
                                                      null,
                                                      myContent.getBytes(Charset.defaultCharset()),
                                                      null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        ItemReference fileItem = (ItemReference)response.getEntity();
        assertEquals(fileItem.getType(), "file");
//        assertEquals(fileItem.getMediaType(), TEXT_PLAIN);
        assertEquals(fileItem.getName(), "test.txt");
        assertEquals(fileItem.getPath(), "/my_project/test.txt");
        validateFileLinks(fileItem);
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/file/my_project/test.txt"));
        VirtualFileEntry file = pm.getProject("my_project").getBaseFolder().getChild("test.txt");
        Assert.assertTrue(file.isFile());
        FileEntry _file = (FileEntry)file;
        //assertEquals(_file.getMediaType(), TEXT_PLAIN);
        assertEquals(new String(_file.contentAsBytes()), myContent);
    }


    @Test
    public void testGetFileContent() throws Exception {
        String myContent = "to be or not to be";
        pm.getProject("my_project").getBaseFolder().createFile("test.txt", myContent.getBytes(Charset.defaultCharset()));
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/file/my_project/test.txt",
                                                      "http://localhost:8080/api", null, null, writer, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        assertEquals(response.getContentType().toString(), TEXT_PLAIN);
        assertEquals(new String(writer.getBody()), myContent);
    }

    @Test
    public void testUpdateFileContent() throws Exception {
        String myContent = "<test>hello</test>";
        pm.getProject("my_project").getBaseFolder().createFile("test.xml", "to be or not to be".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(PUT,
                                                      "http://localhost:8080/api/project/file/my_project/test.xml",
                                                      "http://localhost:8080/api", null, myContent.getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        VirtualFileEntry file = pm.getProject("my_project").getBaseFolder().getChild("test.xml");
        Assert.assertTrue(file.isFile());
        FileEntry _file = (FileEntry)file;
        assertEquals(new String(_file.contentAsBytes()), myContent);
    }

    @Test
    public void testCreateFolder() throws Exception {
        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/folder/my_project/test",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        ItemReference fileItem = (ItemReference)response.getEntity();
        assertEquals(fileItem.getName(), "test");
        assertEquals(fileItem.getPath(), "/my_project/test");
        validateFolderLinks(fileItem);
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/children/my_project/test"));
        VirtualFileEntry folder = pm.getProject("my_project").getBaseFolder().getChild("test");
        Assert.assertTrue(folder.isFolder());
    }

    // any folder created in the root of the workspace automatically becomes project
    @Test
    public void testCreateFolderInRoot() throws Exception {
        String folder = "my_folder";
        ContainerResponse response = launcher.service(POST,
                                                      format("http://localhost:8080/api/project/folder/%s", folder),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        ItemReference fileItem = (ItemReference)response.getEntity();
        assertEquals(fileItem.getType(), "project");
        assertEquals(fileItem.getName(), folder);
        assertEquals(fileItem.getPath(), "/" + folder);
        validateFolderLinks(fileItem);
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(format("http://localhost:8080/api/project/children/%s", folder)));
    }

    @Test
    public void testCreatePath() throws Exception {
        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/folder/my_project/a/b/c",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/children/my_project/a/b/c"));
        VirtualFileEntry folder = pm.getProject("my_project").getBaseFolder().getChild("a/b/c");
        Assert.assertTrue(folder.isFolder());
    }

    @Test
    public void testDeleteFile() throws Exception {
        pm.getProject("my_project").getBaseFolder().createFile("test.txt", "to be or not to be".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(DELETE,
                                                      "http://localhost:8080/api/project/my_project/test.txt",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 204, "Error: " + response.getEntity());
        Assert.assertNull(pm.getProject("my_project").getBaseFolder().getChild("test.txt"));
    }

    @Test
    public void testDeleteFolder() throws Exception {
        pm.getProject("my_project").getBaseFolder().createFolder("test");
        ContainerResponse response = launcher.service(DELETE,
                                                      "http://localhost:8080/api/project/my_project/test",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 204, "Error: " + response.getEntity());
        Assert.assertNull(pm.getProject("my_project").getBaseFolder().getChild("test"));
    }

    @Test
    public void testDeletePath() throws Exception {
        pm.getProject("my_project").getBaseFolder().createFolder("a/b/c");
        ContainerResponse response = launcher.service(DELETE,
                                                      "http://localhost:8080/api/project/my_project/a/b/c",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 204, "Error: " + response.getEntity());
        Assert.assertNull(pm.getProject("my_project").getBaseFolder().getChild("a/b/c"));
    }

    @Test
    public void testDeleteInvalidPath() throws Exception {
        ContainerResponse response = launcher.service(DELETE,
                                                      "http://localhost:8080/api/project/my_project/a/b/c",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 204);
        assertNotNull(pm.getProject("my_project"));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testDeleteProject() throws Exception {


        ContainerResponse response = launcher.service(DELETE,
                                                      "http://localhost:8080/api/project/my_project",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 204, "Error: " + response.getEntity());

        pm.getProject("my_project");
    }

    @Test
    public void testDeleteProjectsConcurrently() throws Exception {
        int threadNumber = 5 * (Runtime.getRuntime().availableProcessors() + 1);
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        CountDownLatch countDownLatch = new CountDownLatch(threadNumber);
        List<Future<ContainerResponse>> futures = new LinkedList<>();


        for (int i = 0; i < threadNumber; i++) {
            addMockedProjectConfigDto(ptRegistry.getProjectType("my_project_type"), "my_project_name" + i);
        }

        IntStream.range(0, threadNumber).forEach(
                i -> {
                    futures.add(executor.submit(() -> {
                        countDownLatch.countDown();
                        countDownLatch.await();

                        try {
                            return launcher.service(DELETE,
                                                    "http://localhost:8080/api/project/my_project_name" + i,
                                                    "http://localhost:8080/api", null, null, null);
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }));
                }
                                                );

        boolean isNotDone;
        do {
            isNotDone = false;
            for (Future<ContainerResponse> future : futures) {
                if (!future.isDone()) {
                    isNotDone = true;
                }
            }
        } while (isNotDone);

        for (Future<ContainerResponse> future : futures) {
            assertEquals(future.get().getStatus(), 204, "Error: " + future.get().getEntity());
        }

        executor.shutdown();
    }

    @Test
    public void testCopyFile() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/copy/my_project/a/b/test.txt?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/file/my_project/a/b/c/test.txt"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/c/test.txt")); // new
        assertNotNull(myProject.getBaseFolder().getChild("a/b/test.txt")); // old
    }

    @Test
    public void testCopyFileWithRename() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes(Charset.defaultCharset()));

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        CopyOptions descriptor = DtoFactory.getInstance().createDto(CopyOptions.class);
        descriptor.setName("copyOfTest.txt");
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/copy/my_project/a/b/test.txt?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/file/my_project/a/b/c/copyOfTest.txt"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/c/copyOfTest.txt")); // new
        assertNotNull(myProject.getBaseFolder().getChild("a/b/test.txt")); // old
    }

    @Test
    public void testCopyFileWithRenameAndOverwrite() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");

        // File names
        String originFileName = "test.txt";
        String destinationFileName = "overwriteMe.txt";

        // File contents
        String originContent = "to be or not no be";
        String overwrittenContent = "that is the question";

        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile(originFileName, originContent.getBytes(Charset.defaultCharset()));
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b/c")).createFile(destinationFileName,
                                                                              overwrittenContent.getBytes(Charset.defaultCharset()));

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        CopyOptions descriptor = DtoFactory.getInstance().createDto(CopyOptions.class);
        descriptor.setName(destinationFileName);
        descriptor.setOverWrite(true);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/copy/my_project/a/b/" + originFileName + "?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/file/my_project/a/b/c/" + destinationFileName));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/c/" + destinationFileName)); // new
        assertNotNull(myProject.getBaseFolder().getChild("a/b/" + originFileName)); // old

        Scanner inputStreamScanner = null;
        String theFirstLineFromDestinationFile;

        try {
            inputStreamScanner = new Scanner(
                    myProject.getBaseFolder().getChild("a/b/c/" + destinationFileName).getVirtualFile().getContent());
            theFirstLineFromDestinationFile = inputStreamScanner.nextLine();
            // destination should contain original file's content
            assertEquals(theFirstLineFromDestinationFile, originContent);
        } catch (ForbiddenException | ServerException e) {
            Assert.fail(e.getMessage());
        } finally {
            if (inputStreamScanner != null) {
                inputStreamScanner.close();
            }
        }
    }

    @Test
    public void testCopyFolder() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/copy/my_project/a/b?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/children/my_project/a/b/c/b"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/test.txt"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/c/b/test.txt"));
    }

    @Test
    public void testCopyFolderWithRename() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes(Charset.defaultCharset()));

        // new name for folder
        final String renamedFolder = "renamedFolder";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        CopyOptions descriptor = DtoFactory.getInstance().createDto(CopyOptions.class);
        descriptor.setName(renamedFolder);
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/copy/my_project/a/b?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             format("http://localhost:8080/api/project/children/my_project/a/b/c/%s", renamedFolder)));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/test.txt"));
        assertNotNull(myProject.getBaseFolder().getChild(format("a/b/c/%s/test.txt", renamedFolder)));
    }

    @Test
    public void testCopyFolderWithRenameAndOverwrite() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");

        // File names
        String originFileName = "test.txt";
        String destinationFileName = "overwriteMe.txt";

        // File contents
        String originContent = "to be or not no be";
        String overwrittenContent = "that is the question";

        // new name for folder
        final String renamedFolder = "renamedFolder";

        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile(originFileName, originContent.getBytes(Charset.defaultCharset()));
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b/c")).createFile(destinationFileName, overwrittenContent.getBytes(Charset.defaultCharset()));

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        CopyOptions descriptor = DtoFactory.getInstance().createDto(CopyOptions.class);
        descriptor.setName(renamedFolder);
        descriptor.setOverWrite(true);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/copy/my_project/a/b?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             format("http://localhost:8080/api/project/children/my_project/a/b/c/%s", renamedFolder)));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/test.txt"));
        assertNotNull(myProject.getBaseFolder().getChild(format("a/b/c/%s/test.txt", renamedFolder)));
        assertEquals(myProject.getBaseFolder().getChild("a/b/test.txt").getName(),
                     myProject.getBaseFolder().getChild(format("a/b/c/%s/%s", renamedFolder, originFileName)).getName());
    }

    @Test
    public void testMoveFile() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/move/my_project/a/b/test.txt?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/file/my_project/a/b/c/test.txt"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/c/test.txt")); // new
        Assert.assertNull(myProject.getBaseFolder().getChild("a/b/test.txt")); // old
    }

    @Test
    public void testMoveFileWithRename() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes(Charset.defaultCharset()));

        // name for file after move
        final String destinationName = "copyOfTestForMove.txt";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(destinationName);
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/move/my_project/a/b/test.txt?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             format("http://localhost:8080/api/project/file/my_project/a/b/c/%s", destinationName)));
        VirtualFileEntry theTargetFile = myProject.getBaseFolder().getChild(format("a/b/c/%s", destinationName));
        assertNotNull(theTargetFile); // new
    }

    @Test
    public void testRenameFile() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes(Charset.defaultCharset()));

        // name for file after move
        final String destinationName = "copyOfTestForMove.txt";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(destinationName);
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/move/my_project/a/b/test.txt",
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             format("http://localhost:8080/api/project/file/my_project/a/b/%s", destinationName)));
        VirtualFileEntry theTargetFile = myProject.getBaseFolder().getChild(format("a/b/%s", destinationName));
        assertNotNull(theTargetFile); // new
    }

    @Test
    public void testMoveFileWithRenameAndOverwrite() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");

        // File names
        String originFileName = "test.txt";
        String destinationFileName = "overwriteMe.txt";

        // File contents
        String originContent = "to be or not no be";
        String overwrittenContent = "that is the question";

        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile(originFileName, originContent.getBytes(Charset.defaultCharset()));
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b/c")).createFile(destinationFileName, overwrittenContent.getBytes(Charset.defaultCharset()));

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(destinationFileName);
        descriptor.setOverWrite(true);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/move/my_project/a/b/" + originFileName +
                                                      "?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/file/my_project/a/b/c/" + destinationFileName));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/c/" + destinationFileName)); // new

        Scanner inputStreamScanner = null;
        String theFirstLineFromDestinationFile;

        try {
            inputStreamScanner = new Scanner(
                    myProject.getBaseFolder().getChild("a/b/c/" + destinationFileName).getVirtualFile().getContent());
            theFirstLineFromDestinationFile = inputStreamScanner.nextLine();
            // destination should contain original file's content
            assertEquals(theFirstLineFromDestinationFile, originContent);
        } catch (ForbiddenException | ServerException e) {
            Assert.fail(e.getMessage());
        } finally {
            if (inputStreamScanner != null) {
                inputStreamScanner.close();
            }
        }
    }

    @Test
    public void testMoveFolder() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b/c")).createFile("test.txt", "to be or not no be".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/move/my_project/a/b/c?to=/my_project/a",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/children/my_project/a/c"));
        assertNotNull(myProject.getBaseFolder().getChild("a/c/test.txt"));
        Assert.assertNull(myProject.getBaseFolder().getChild("a/b/c/test.txt"));
        Assert.assertNull(myProject.getBaseFolder().getChild("a/b/c"));
    }

    @Test
    public void testMoveFolderWithRename() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes(Charset.defaultCharset()));

        // new name for folder
        final String renamedFolder = "renamedFolder";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(renamedFolder);
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/copy/my_project/a/b?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             format("http://localhost:8080/api/project/children/my_project/a/b/c/%s", renamedFolder)));
        assertNotNull(myProject.getBaseFolder().getChild(format("a/b/c/%s/test.txt", renamedFolder)));
    }

    @Test
    public void testRenameFolder() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes(Charset.defaultCharset()));

        // new name for folder
        final String renamedFolder = "renamedFolder";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(renamedFolder);
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/move/my_project/a/b",
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             format("http://localhost:8080/api/project/children/my_project/a/%s", renamedFolder)));
        assertNotNull(myProject.getBaseFolder().getChild(format("a/%s/test.txt", renamedFolder)));
    }

    @Test
    public void testMoveFolderWithRenameAndOverwrite() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");

        // File names
        String originFileName = "test.txt";
        String destinationFileName = "overwriteMe.txt";

        // File contents
        String originContent = "to be or not no be";
        String overwritenContent = "that is the question";

        // new name for folder
        final String renamedFolder = "renamedFolder";

        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile(originFileName, originContent.getBytes(Charset.defaultCharset()));
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b/c")).createFile(destinationFileName, overwritenContent.getBytes(Charset.defaultCharset()));

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(renamedFolder);
        descriptor.setOverWrite(true);

        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/copy/my_project/a/b?to=/my_project/a/b/c",
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(Charset.defaultCharset()), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             format("http://localhost:8080/api/project/children/my_project/a/b/c/%s", renamedFolder)));
        assertNotNull(myProject.getBaseFolder().getChild(format("a/b/c/%s/test.txt", renamedFolder)));
    }

    @Test
    public void testImportZip() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b");

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(bout);
        zipOut.putNextEntry(new ZipEntry("folder1/"));
        zipOut.putNextEntry(new ZipEntry("folder1/file1.txt"));
        zipOut.write("to be or not to be".getBytes(Charset.defaultCharset()));
        zipOut.close();
        byte[] zip = bout.toByteArray();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(ExtMediaType.APPLICATION_ZIP));
        ContainerResponse response = launcher.service(POST,
                                                      format("http://localhost:8080/api/project/import/my_project/a/b"),
                                                      "http://localhost:8080/api", headers, zip, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/children/my_project/a/b"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/folder1/file1.txt"));
    }

    @Test
    public void testImportZipWithoutSkipFirstLevel() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b");

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(bout);
        zipOut.putNextEntry(new ZipEntry("folder1/"));
        zipOut.putNextEntry(new ZipEntry("folder1/folder2/"));
        zipOut.putNextEntry(new ZipEntry("folder1/folder2/file1.txt"));
        zipOut.write("to be or not to be".getBytes(Charset.defaultCharset()));
        zipOut.close();
        byte[] zip = bout.toByteArray();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(ExtMediaType.APPLICATION_ZIP));
        ContainerResponse response = launcher.service(POST,
                                                      "http://localhost:8080/api/project/import/my_project/a/b?skipFirstLevel=false",
                                                      "http://localhost:8080/api", headers, zip, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create("http://localhost:8080/api/project/children/my_project/a/b"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/folder1/"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/folder1/folder2"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/folder1/folder2/file1.txt"));
    }

    @Test
    public void testExportZip() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b").createFile("test.txt", "hello".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/export/my_project",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        assertEquals(response.getContentType().toString(), ExtMediaType.APPLICATION_ZIP);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetChildren() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        FolderEntry a = myProject.getBaseFolder().createFolder("a");
        a.createFolder("b");
        a.createFile("test.txt", "test".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/children/my_project/a",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 2);
        Set<String> names = new LinkedHashSet<>(2);
        names.addAll(result.stream().map(ItemReference::getName).collect(Collectors.toList()));
        Assert.assertTrue(names.contains("b"));
        Assert.assertTrue(names.contains("test.txt"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetItem() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        FolderEntry a = myProject.getBaseFolder().createFolder("a");
        a.createFolder("b");
        a.createFile("test.txt", "test".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/item/my_project/a/b",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());

        ItemReference result = (ItemReference)response.getEntity();
        assertEquals(result.getName(), "b");

        response = launcher.service(GET,
                                    "http://localhost:8080/api/project/item/my_project/a/test.txt",
                                    "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        result = (ItemReference)response.getEntity();
        assertEquals(result.getType(), "file");
        //assertEquals(result.getMediaType(), TEXT_PLAIN);
    }

    @Test
    public void testGetItemWithoutParentProject() throws Exception {
        FolderEntry a = pm.getProjectsRoot().createFolder("a");
        a.createFile("test.txt", "test".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/item/a/test.txt",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        ItemReference result = (ItemReference)response.getEntity();
        assertEquals(result.getType(), "file");
        //assertEquals(result.getMediaType(), TEXT_PLAIN);
    }

    @Test
    public void testGetMissingItem() throws Exception {
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/item/some_missing_project/a/b",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 404, "Error: " + response.getEntity());
    }

    @Test
    public void testGetTree() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        FolderEntry a = myProject.getBaseFolder().createFolder("a");
        a.createFolder("b/c");
        a.createFolder("x/y");
        a.createFile("test.txt", "test".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/tree/my_project/a",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        TreeElement tree = (TreeElement)response.getEntity();
        ItemReference a_node = tree.getNode();
        assertEquals(a_node.getName(), "a");
        validateFolderLinks(a_node);
        List<TreeElement> children = tree.getChildren();
        assertNotNull(children);
        assertEquals(children.size(), 2);
        Set<String> names = new LinkedHashSet<>(2);
        for (TreeElement subTree : children) {
            ItemReference _node = subTree.getNode();
            validateFolderLinks(_node);
            names.add(_node.getName());
            Assert.assertTrue(subTree.getChildren().isEmpty()); // default depth is 1
        }
        Assert.assertTrue(names.contains("b"));
        Assert.assertTrue(names.contains("x"));
    }

    @Test
    public void testGetTreeWithDepth() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        FolderEntry a = myProject.getBaseFolder().createFolder("a");
        a.createFolder("b/c");
        a.createFolder("x/y");
        a.createFile("test.txt", "test".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/tree/my_project/a?depth=2",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        TreeElement tree = (TreeElement)response.getEntity();
        ItemReference a_node = tree.getNode();
        assertEquals(a_node.getName(), "a");
        List<TreeElement> children = tree.getChildren();
        assertNotNull(children);
        Set<String> names = new LinkedHashSet<>(4);
        for (TreeElement subTree : children) {
            ItemReference _node = subTree.getNode();
            validateFolderLinks(_node);
            String name = _node.getName();
            names.add(name);
            for (TreeElement subSubTree : subTree.getChildren()) {
                ItemReference __node = subSubTree.getNode();
                validateFolderLinks(__node);
                names.add(name + "/" + __node.getName());
            }
        }
        Assert.assertTrue(names.contains("b"));
        Assert.assertTrue(names.contains("x"));
        Assert.assertTrue(names.contains("b/c"));
        Assert.assertTrue(names.contains("x/y"));
    }

    @Test
    public void testGetTreeWithDepthAndIncludeFiles() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        FolderEntry a = myProject.getBaseFolder().createFolder("a");
        a.createFolder("b/c");
        a.createFolder("x").createFile("test.txt", "test".getBytes(Charset.defaultCharset()));
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/tree/my_project/a?depth=100&includeFiles=true",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        TreeElement tree = (TreeElement)response.getEntity();
        ItemReference a_node = tree.getNode();
        assertEquals(a_node.getName(), "a");
        List<TreeElement> children = tree.getChildren();
        assertNotNull(children);
        Set<String> names = new LinkedHashSet<>(4);
        for (TreeElement subTree : children) {
            ItemReference _node = subTree.getNode();
            validateFolderLinks(_node);
            String name = _node.getName();
            names.add(name);
            for (TreeElement subSubTree : subTree.getChildren()) {
                ItemReference __node = subSubTree.getNode();
                if (__node.getType().equals("folder")) {
                    validateFolderLinks(__node);
                } else if (__node.getType().equals("file")) {
                    validateFileLinks(__node);
                }
                names.add(name + "/" + __node.getName());
            }
        }
        Assert.assertTrue(names.contains("b"));
        Assert.assertTrue(names.contains("x"));
        Assert.assertTrue(names.contains("b/c"));
        Assert.assertTrue(names.contains("x/test.txt"));
    }

    @Test
    public void testGetTreeWithDepthAndIncludeFilesNoFiles() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        FolderEntry a = myProject.getBaseFolder().createFolder("a");
        a.createFolder("b/c");
        a.createFolder("x");
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/tree/my_project/a?depth=100&includeFiles=true",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        TreeElement tree = (TreeElement)response.getEntity();
        ItemReference a_node = tree.getNode();
        assertEquals(a_node.getName(), "a");
        List<TreeElement> children = tree.getChildren();
        assertNotNull(children);
        Set<String> names = new LinkedHashSet<>(4);
        for (TreeElement subTree : children) {
            ItemReference _node = subTree.getNode();
            validateFolderLinks(_node);
            String name = _node.getName();
            names.add(name);
            for (TreeElement subSubTree : subTree.getChildren()) {
                ItemReference __node = subSubTree.getNode();
                validateFolderLinks(__node);
                names.add(name + "/" + __node.getName());
            }
        }
        Assert.assertTrue(names.contains("b"));
        Assert.assertTrue(names.contains("x"));
        Assert.assertTrue(names.contains("b/c"));
        Assert.assertFalse(names.contains("x/test.txt"));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testSearchByName() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b").createFile("test.txt", "hello".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("x/y").createFile("test.txt", "test".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("c").createFile("exclude", "test".getBytes(Charset.defaultCharset()));

        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/search/my_project?name=test.txt",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 2);
        Set<String> paths = new LinkedHashSet<>(2);
        for (ItemReference itemReference : result) {
            paths.add(itemReference.getPath());
        }
        Assert.assertTrue(paths.contains("/my_project/a/b/test.txt"));
        Assert.assertTrue(paths.contains("/my_project/x/y/test.txt"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchByText() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b").createFile("test.txt", "hello".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("x/y").createFile("__test.txt", "searchhit".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("c").createFile("_test", "searchhit".getBytes(Charset.defaultCharset()));

        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/search/my_project?text=searchhit",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 2);
        Set<String> paths = new LinkedHashSet<>(1);
        paths.addAll(result.stream().map(ItemReference::getPath).collect(Collectors.toList()));
        Assert.assertTrue(paths.contains("/my_project/x/y/__test.txt"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchByTextWhenFileWasNotIndexed() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b").createFile("test.txt", "hello".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("x/y").createFile("__test.txt", "searchhit".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder(EXCLUDE_SEARCH_PATH).createFile("_test", "searchhit".getBytes(Charset.defaultCharset()));

        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/search/my_project?text=searchhit",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 1);
        Set<String> paths = new LinkedHashSet<>(1);
        paths.addAll(result.stream().map(ItemReference::getPath).collect(Collectors.toList()));
        Assert.assertTrue(paths.contains("/my_project/x/y/__test.txt"));
        Assert.assertFalse(paths.contains("/my_project/" + EXCLUDE_SEARCH_PATH + "/_test"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchParticularSequenceWords() throws Exception {
        String queryToSearch = "?text=" + URL_ENCODED_QUOTES +
                               "To" + URL_ENCODED_SPACE +
                               "be" + URL_ENCODED_SPACE +
                               "or" + URL_ENCODED_SPACE +
                               "not" + URL_ENCODED_SPACE +
                               "to" + URL_ENCODED_SPACE +
                               "be" + URL_ENCODED_QUOTES;
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("x/y").createFile("containsSearchText.txt", "To be or not to be that is the question".getBytes(
                Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("a/b").createFile("test.txt", "Pay attention! To be or to be that is the question".getBytes(
                Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("c").createFile("_test", "Pay attention! To be or to not be that is the question".getBytes(Charset.defaultCharset()));

        ContainerResponse response =
                launcher.service(GET, "http://localhost:8080/api/project/search/my_project" + queryToSearch,
                                 "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 1);
        Set<String> paths = new LinkedHashSet<>(1);
        paths.addAll(result.stream().map(ItemReference::getPath).collect(Collectors.toList()));
        Assert.assertTrue(paths.contains("/my_project/x/y/containsSearchText.txt"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchParticularSequenceWordsWithAnyEnding() throws Exception {
        String queryToSearch = "?text=" + URL_ENCODED_QUOTES +
                               "that" + URL_ENCODED_SPACE +
                               "is" + URL_ENCODED_SPACE +
                               "the" + URL_ENCODED_QUOTES + URL_ENCODED_SPACE + AND_OPERATOR + URL_ENCODED_SPACE +
                               "question" + URL_ENCODED_ASTERISK;
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("x/y").createFile("containsSearchText.txt", "To be or not to be that is the question".getBytes(
                Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("a/b")
                 .createFile("containsSearchTextAlso.txt",
                             "Pay attention! To be or not to be that is the questionS".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("c")
                 .createFile("notContainsSearchText",
                             "Pay attention! To be or to not be that is the questEon".getBytes(Charset.defaultCharset()));

        ContainerResponse response =
                launcher.service(GET,"http://localhost:8080/api/project/search/my_project" + queryToSearch,
                                 "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 2);
        Set<String> paths = new LinkedHashSet<>(2);
        paths.addAll(result.stream().map(ItemReference::getPath).collect(Collectors.toList()));
        Assert.assertTrue(paths.contains("/my_project/x/y/containsSearchText.txt"));
        Assert.assertTrue(paths.contains("/my_project/a/b/containsSearchTextAlso.txt"));
        Assert.assertFalse(paths.contains("/my_project/c/notContainsSearchText.txt"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchWordWithAnyEnding() throws Exception {
        String queryToSearch = "?text=" +
                               "question" + URL_ENCODED_ASTERISK;
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("x/y").createFile("containsSearchText.txt", "To be or not to be that is the question".getBytes(
                Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("a/b")
                 .createFile("containsSearchTextAlso.txt", "Pay attention! To be or not to be that is the questionS".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("c")
                 .createFile("notContainsSearchText", "Pay attention! To be or to not be that is the questEon".getBytes(Charset.defaultCharset()));

        ContainerResponse response =
                launcher.service(GET, "http://localhost:8080/api/project/search/my_project" + queryToSearch,
                                 "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 2);
        Set<String> paths = new LinkedHashSet<>(2);
        paths.addAll(result.stream().map(ItemReference::getPath).collect(Collectors.toList()));
        Assert.assertTrue(paths.contains("/my_project/x/y/containsSearchText.txt"));
        Assert.assertTrue(paths.contains("/my_project/a/b/containsSearchTextAlso.txt"));
        Assert.assertFalse(paths.contains("/my_project/c/notContainsSearchText.txt"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchTextWhenExcludeSomeText() throws Exception {
        String queryToSearch = "?text=" +
                               "question" + URL_ENCODED_SPACE + NOT_OPERATOR + URL_ENCODED_SPACE + URL_ENCODED_QUOTES +
                               "attention!" + URL_ENCODED_QUOTES;
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("x/y").createFile("containsSearchText.txt", "To be or not to be that is the question".getBytes(
                Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("b")
                 .createFile("notContainsSearchText", "Pay attention! To be or not to be that is the question".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("c").createFile("alsoNotContainsSearchText",
                                                               "To be or to not be that is the ...".getBytes(Charset.defaultCharset()));

        ContainerResponse response =
                launcher.service(GET, "http://localhost:8080/api/project/search/my_project" + queryToSearch,
                                 "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 1);
        Set<String> paths = new LinkedHashSet<>(1);
        paths.addAll(result.stream().map(ItemReference::getPath).collect(Collectors.toList()));
        Assert.assertTrue(paths.contains("/my_project/x/y/containsSearchText.txt"));
        Assert.assertFalse(paths.contains("/my_project/b/notContainsSearchText.txt"));
        Assert.assertFalse(paths.contains("/my_project/c/alsoContainsSearchText"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchTextWithEscapedCharachters() throws Exception {
        String queryToSearch = "?text=http" +
                               URL_ENCODED_BACKSLASH + ':' +
                               URL_ENCODED_BACKSLASH + '/' +
                               URL_ENCODED_BACKSLASH + '/' + "localhost" +
                               URL_ENCODED_BACKSLASH + ':' + "8080" +
                               URL_ENCODED_BACKSLASH + '/' + "ide" +
                               URL_ENCODED_BACKSLASH + '/' + "dev6" +
                               URL_ENCODED_BACKSLASH + '?' + "action=createProject" +
                               URL_ENCODED_BACKSLASH + ':' + "projectName=test";
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("x/y")
                 .createFile("test.txt",
                             "http://localhost:8080/ide/dev6?action=createProject:projectName=test".getBytes(Charset.defaultCharset()));

        ContainerResponse response = launcher.service(GET, "http://localhost:8080/api/project/search/my_project" + queryToSearch,
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 1);
        Set<String> paths = new LinkedHashSet<>(1);
        paths.addAll(result.stream().map(ItemReference::getPath).collect(Collectors.toList()));
        Assert.assertTrue(paths.contains("/my_project/x/y/test.txt"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchByNameAndText() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b").createFile("test.txt", "test".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("x/y").createFile("test.txt", "test".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("c").createFile("test", "test".getBytes(Charset.defaultCharset()));

        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/search/my_project?text=test&name=test.txt",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 2);
        assertEqualsNoOrder(new Object[]{
                                    result.get(0).getPath(),
                                    result.get(1).getPath()
                            },
                            new Object[]{
                                    "/my_project/a/b/test.txt",
                                    "/my_project/x/y/test.txt"
                            });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchFromWSRoot() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b").createFile("test", "test".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("x/y").createFile("test", "test".getBytes(Charset.defaultCharset()));
        myProject.getBaseFolder().createFolder("c").createFile("test.txt", "test".getBytes(Charset.defaultCharset()));

        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/search/?text=test&name=test.txt",
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 1);
        Assert.assertTrue(result.get(0).getPath().equals("/my_project/c/test.txt"));
    }

    private void validateFileLinks(ItemReference item) {
        Link link = item.getLink("delete");
        assertNotNull(link);
        assertEquals(link.getMethod(), DELETE);
        assertEquals(link.getHref(), "http://localhost:8080/api/project" + item.getPath());
        link = item.getLink("update content");
        assertNotNull(link);
        assertEquals(link.getMethod(), PUT);
        assertEquals(link.getConsumes(), "*/*");
        assertEquals(link.getHref(), "http://localhost:8080/api/project" + "/file" + item.getPath());
    }

    private void validateFolderLinks(ItemReference item) {
        Link link = item.getLink("children");
        assertNotNull(link);
        assertEquals(link.getMethod(), GET);
        assertEquals(link.getHref(), "http://localhost:8080/api/project/children" + item.getPath());
        assertEquals(link.getProduces(), APPLICATION_JSON);

        link = item.getLink("tree");
        assertNotNull(link);
        assertEquals(link.getMethod(), GET);
        assertEquals(link.getHref(), "http://localhost:8080/api/project/tree" + item.getPath());
        assertEquals(link.getProduces(), APPLICATION_JSON);
        link = item.getLink("delete");
        assertNotNull(link);
        assertEquals(link.getMethod(), DELETE);
        assertEquals(link.getHref(), "http://localhost:8080/api/project" + item.getPath());
    }




    private void validateProjectLinks(ProjectConfigDto project) {
        List<Link> links = project.getLinks();

        for (Link link : links) {
            switch (link.getHref()) {
                case "update project":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), PUT);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project" + project.getPath());
                    assertEquals(link.getConsumes(), APPLICATION_JSON);
                    assertEquals(link.getProduces(), APPLICATION_JSON);
                    break;

                case "children":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), GET);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project/children" + project.getPath());
                    assertEquals(link.getProduces(), APPLICATION_JSON);
                    break;

                case "tree":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), GET);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project/tree" + project.getPath());
                    assertEquals(link.getProduces(), APPLICATION_JSON);
                    break;

                case "modules":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), GET);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project/modules" + project.getPath());
                    assertEquals(link.getProduces(), APPLICATION_JSON);
                    break;

                case "zipball sources":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), GET);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project/export" + project.getPath());
                    assertEquals(link.getProduces(), APPLICATION_ZIP);
                    break;

                case "delete":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), DELETE);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project" + project.getPath());
                    break;
            }
        }
    }

    private InputStream prepareZipArchiveBasedOn(List<String> paths) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(bout);

        for (String path : paths) {
            zipOut.putNextEntry(new ZipEntry(path));
        }
        zipOut.close();
        return new ByteArrayInputStream(bout.toByteArray());
    }

    private void checkProjectIsCreated(String expectedName, String expectedPath, String expectedType, ProjectConfigDto actualConfig)
            throws ServerException, NotFoundException {
        final String projectDescription = "someDescription";
        assertEquals(actualConfig.getName(), expectedName);
        assertEquals(actualConfig.getPath(), expectedPath);
        assertEquals(actualConfig.getDescription(), projectDescription);
        assertEquals(actualConfig.getType(), expectedType);

        final String expectedAttribute = "new_test_attribute";
        final String expectedAttributeValue = "some_attribute_value";
        final Map<String, List<String>> attributes = actualConfig.getAttributes();
        assertNotNull(attributes);
        assertEquals(attributes.size(), 1);
        assertEquals(attributes.get(expectedAttribute), singletonList(expectedAttributeValue));

        validateProjectLinks(actualConfig);

        RegisteredProject project = pm.getProject(expectedPath);
        assertNotNull(project);
        assertEquals(project.getDescription(), projectDescription);
        assertEquals(project.getProjectType().getId(), expectedType);
        String attributeVal = project.getAttributeEntries().get(expectedAttribute).getString();
        assertNotNull(attributeVal);
        assertEquals(attributeVal, expectedAttributeValue);

        assertNotNull(project.getBaseFolder().getChild("a"));
        assertNotNull(project.getBaseFolder().getChild("b"));
        assertNotNull(project.getBaseFolder().getChild("test.txt"));
    }

    private void createTestProjectType(final String projectTypeId) throws ProjectTypeConstraintException {
        final ProjectTypeDef pt = new ProjectTypeDef(projectTypeId, "my project type", true, false) {
            {
                addConstantDefinition("new_test_attribute", "attr description", "some_attribute_value");
            }
        };
        ptRegistry.registerProjectType(pt);
    }

    private CreateProjectHandler createProjectHandlerFor(final String projectName, final String projectTypeId) {
        return new CreateProjectHandler() {
            @Override
            public void onCreateProject(Path projectPath, Map<String, AttributeValue> attributes, Map<String, String> options)
                    throws ForbiddenException, ConflictException, ServerException {
                final String pathToProject = projectPath.toString();
                final String pathToParent = pathToProject.substring(0, pathToProject.lastIndexOf("/"));
                final FolderEntry projectFolder = new FolderEntry(
                        vfsProvider.getVirtualFileSystem().getRoot().getChild(Path.of(pathToParent)).createFolder(projectName));
                projectFolder.createFolder("a");
                projectFolder.createFolder("b");
                projectFolder.createFile("test.txt", "test".getBytes(Charset.defaultCharset()));
            }

            @Override
            public String getProjectType() {
                return projectTypeId;
            }
        };
    }

    private class LocalProjectType extends ProjectTypeDef {
        private LocalProjectType(String typeId, String typeName) {
            super(typeId, typeName, true, false);
            addConstantDefinition("my_attribute", "Constant", "attribute value 1");
        }
    }
}
