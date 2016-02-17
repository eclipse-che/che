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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
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
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.vfs.ArchiverFactory;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystem;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.commons.test.SelfReturningAnswer;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.Application;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.eclipse.che.commons.lang.ws.rs.ExtMediaType.APPLICATION_ZIP;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
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

    private static final String      vfsUser       = "dev";
    private static final Set<String> vfsUserGroups = singleton("workspace/developer");
    private static final String      workspace     = "my_ws";
    private static final String      apiEndpoint   = "http://localhost:8080/che/api";

    protected final static String FS_PATH    = "target/fss";
    protected final static String INDEX_PATH = "target/fss_index";

    private ProjectManager         pm;
    private ResourceLauncher       launcher;
    private ProjectHandlerRegistry phRegistry;

    private org.eclipse.che.commons.env.EnvironmentContext env;

    private List<ProjectConfigDto> projects;

    @Mock
    private UserDao                userDao;
    @Mock
    private UsersWorkspaceDto      usersWorkspaceMock;
    @Mock
    private HttpJsonRequestFactory httpJsonRequestFactory;
    @Mock
    private HttpJsonResponse       httpJsonResponse;

    private LocalVirtualFileSystem vfs;

    private ProjectImporterRegistry importerRegistry;

    protected ProjectRegistry projectRegistry;


    @BeforeMethod
    public void setUp() throws Exception {

        WorkspaceHolder workspaceHolder = new TestWorkspaceHolder();

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
        filters.add(path -> true);
        FSLuceneSearcherProvider sProvider = new FSLuceneSearcherProvider(indexDir, filters);

        vfs = new LocalVirtualFileSystem(root, new ArchiverFactory(), sProvider, null);


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

        ProjectTypeRegistry ptRegistry = new ProjectTypeRegistry(projectTypes);

        phRegistry = new ProjectHandlerRegistry(new HashSet<>());

        importerRegistry = new ProjectImporterRegistry(Collections.<ProjectImporter>emptySet());

        this.projectRegistry = new ProjectRegistry(workspaceHolder, vfs, ptRegistry);

        pm = new ProjectManager(vfs, null, ptRegistry, phRegistry,
                                importerRegistry, projectRegistry);

        HttpJsonRequest httpJsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());

        List<ProjectConfigDto> modules = new ArrayList<>();

        final ProjectConfigDto testProjectConfigMock = mock(ProjectConfigDto.class);
        when(testProjectConfigMock.getPath()).thenReturn("/my_project");
        when(testProjectConfigMock.getName()).thenReturn("my_project");
        when(testProjectConfigMock.getDescription()).thenReturn("my test project");
        when(testProjectConfigMock.getType()).thenReturn("my_project_type");
//        when(testProjectConfigMock.getModules()).thenReturn(modules);
        when(testProjectConfigMock.getSource()).thenReturn(DtoFactory.getInstance().createDto(SourceStorageDto.class));
//        when(testProjectConfigMock.findModule(anyString())).thenReturn(testProjectConfigMock);

        Map<String, List<String>> attr = new HashMap<>();
        for (Attribute attribute : myProjectType.getAttributes()) {
            attr.put(attribute.getName(), attribute.getValue().getList());
        }
        when(testProjectConfigMock.getAttributes()).thenReturn(attr);

        projects = new ArrayList<>();
        projects.add(testProjectConfigMock);
        when(httpJsonRequestFactory.fromLink(any())).thenReturn(httpJsonRequest);
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
        when(httpJsonResponse.asDto(UsersWorkspaceDto.class)).thenReturn(usersWorkspaceMock);
        when(usersWorkspaceMock.getProjects()).thenReturn(projects);

        pm.createProject(testProjectConfigMock, null);

//        verify(httpJsonRequestFactory).fromLink(eq(DtoFactory.newDto(Link.class)
//                                                             .withHref(apiEndpoint + "/workspace/" + workspace + "/project")
//                                                             .withMethod(PUT)));

        DependencySupplierImpl dependencies = new DependencySupplierImpl();


        dependencies.addComponent(ProjectTypeRegistry.class, ptRegistry);
        dependencies.addComponent(UserDao.class, userDao);
        dependencies.addComponent(ProjectManager.class, pm);
        dependencies.addComponent(ProjectImporterRegistry.class, importerRegistry);
        dependencies.addComponent(ProjectHandlerRegistry.class, phRegistry);
//        dependencies.addComponent(SearcherProvider.class, new TestSercherProvider());
        dependencies.addComponent(EventService.class, eventService);

        ResourceBinder resources = new ResourceBinderImpl();
        ProviderBinder providers = new ApplicationProviderBinder();
        EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencies, new EverrestConfiguration(), null);
        launcher = new ResourceLauncher(processor);

        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return java.util.Collections.<Class<?>>singleton(ProjectService.class);
            }

            @Override
            public Set<Object> getSingletons() {
                return new HashSet<>(Arrays.asList(/*new CodenvyJsonProvider(singleton(ContentStream.class)),*/
                                                   /*new ContentStreamWriter(),*/
                                                   new ApiExceptionMapper()));
            }
        });

        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, ProviderBinder.getInstance()));

        env = org.eclipse.che.commons.env.EnvironmentContext.getCurrent();
//        env.setUser(new UserImpl(vfsUser, vfsUser, "dummy_token", vfsUserGroups, false));
//        env.setWorkspaceName(workspace);
//        env.setWorkspaceId(workspace);
    }


    private static class TestWorkspaceHolder extends WorkspaceHolder {

        private TestWorkspaceHolder() throws ServerException {
            super(DtoFactory.newDto(UsersWorkspaceDto.class).
                    withId("id").withName("name").withProjects(new ArrayList<>()));


        }



    }

    private static class TestSearcherProvider implements SearcherProvider {


        @Override
        public Searcher getSearcher(VirtualFileSystem virtualFileSystem, boolean create) throws ServerException {
            return null;
        }

        @Override
        public Searcher getSearcher(VirtualFileSystem virtualFileSystem) throws ServerException {
            return null;
        }

        @Override
        public void close() throws ServerException {

        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetProjects() throws Exception {
        List<RegisteredProject> p = pm.getProjects();

        assertEquals(p.size(), 1);

       vfs.getRoot().createFolder("not_project");

        // to refresh
        projectRegistry.initProjects();

        ContainerResponse response =
                launcher.service(GET, "http://localhost:8080/api/project/my_ws", "http://localhost:8080/api", null, null, null);
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
    public void shouldReturnNotFoundStatusWhenGettingModulesFromProjectWhichDoesNotExist() throws Exception {
        ContainerResponse response = launcher.service(GET,
                                                      "http://localhost:8080/api/project/" + workspace + "/modules/fake",
                                                      "http://localhost:8080/api",
                                                      null,
                                                      null,
                                                      null);

        assertEquals(response.getStatus(), 404);
    }


    @Test
    public void testGetProject() throws Exception {
        ContainerResponse response =
                launcher.service(GET, String.format("http://localhost:8080/api/project/%s/my_project", workspace),
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
        vfs.getRoot().createFolder("not_project");
        // to refresh
        projectRegistry.initProjects();
        ContainerResponse response = launcher.service(GET, String.format("http://localhost:8080/api/project/%s/not_project", workspace),
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
        env.setUser(new UserImpl(vfsUser, vfsUser, "dummy_token", Collections.<String>emptySet(), false));
        ContainerResponse response =
                launcher.service(GET, String.format("http://localhost:8080/api/project/%s/my_project", workspace),
                                 "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        ProjectConfigDto result = (ProjectConfigDto)response.getEntity();
        assertNotNull(result);
    }


    @Test
    public void testGetProjectInvalidPath() throws Exception {
        ContainerResponse response = launcher.service(GET,
                                                      String.format("http://localhost:8080/api/project/%s/my_project_invalid", workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void testCreateProject() throws Exception {



        phRegistry.register(new CreateProjectHandler() {
            @Override
            public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
                    throws ForbiddenException, ConflictException, ServerException {
                baseFolder.createFolder("a");
                baseFolder.createFolder("b");
                baseFolder.createFile("test.txt", "test".getBytes());
            }

            @Override
            public String getProjectType() {
                return "testCreateProject";
            }
        });

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", singletonList(APPLICATION_JSON));

        ProjectTypeDef pt = new ProjectTypeDef("testCreateProject", "my project type", true, false) {
            {
                addConstantDefinition("new_project_attribute", "attr description", "to be or not to be");
            }
        };

        pm.getProjectTypeRegistry().registerProjectType(pt);

        Map<String, List<String>> attributeValues = new LinkedHashMap<>();
        attributeValues.put("new_project_attribute", singletonList("to be or not to be"));


        final ProjectConfigDto newProjectConfig = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                            .withPath("/new_project")
                                                            .withName("new_project")
                                                            .withDescription("new project")
                                                            .withType("testCreateProject")
                                                            .withAttributes(attributeValues)
                                                            .withSource(DtoFactory.getInstance().createDto(SourceStorageDto.class));
        projects.add(newProjectConfig);

        ContainerResponse response = launcher.service(POST,
                                                      String.format("http://localhost:8080/api/project/%s", workspace),
                                                      "http://localhost:8080/api",
                                                      headers,
                                                      DtoFactory.getInstance().toJson(newProjectConfig).getBytes(),
                                                      null);

        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        ProjectConfigDto result = (ProjectConfigDto)response.getEntity();
        assertNotNull(result);
        assertEquals(result.getName(), "new_project");
        assertEquals(result.getPath(), "/new_project");
        assertEquals(result.getDescription(), newProjectConfig.getDescription());
        assertEquals(result.getType(), newProjectConfig.getType());
        assertEquals(result.getType(), "testCreateProject");
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
    public void testUpdateProject() throws Exception {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", singletonList(APPLICATION_JSON));

        ProjectTypeDef pt = new ProjectTypeDef("testUpdateProject", "my project type", true, false) {
        };
        pm.getProjectTypeRegistry().registerProjectType(pt);

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
                                                      String.format("http://localhost:8080/api/project/%s/testUpdateProject", workspace),
                                                      "http://localhost:8080/api",
                                                      headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(),
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
                                                      String.format("http://localhost:8080/api/project/%s/not_project", workspace),
                                                      "http://localhost:8080/api",
                                                      headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(),
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
                                                      String.format("http://localhost:8080/api/project/%s/my_project_invalid",
                                                                    workspace),
                                                      "http://localhost:8080/api",
                                                      headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(),
                                                      null);
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void testEstimateProject() throws Exception {
        VirtualFile root = pm.getProjectsRoot().getVirtualFile();

                                     //getVirtualFileSystemRegistry().getProvider("my_ws").getMountPoint(false).getRoot();
        root.createFolder("testEstimateProjectGood").createFolder("check");
        root.createFolder("testEstimateProjectBad");

        final ValueProviderFactory vpf1 = projectFolder -> new ValueProvider() {
            @Override
            public List<String> getValues(String attributeName) throws ValueStorageException {

                VirtualFileEntry file;
                try {
                    file = projectFolder.getChild("check");
                } catch (ForbiddenException | ServerException e) {
                    throw new ValueStorageException(e.getMessage());
                }

                if (file == null) {
                    throw new ValueStorageException("Check not found");
                }
                return (List <String>)singletonList("checked");
            }

//            @Override
//            public void setValues(String attributeName, List<String> value) {
//            }
        };

        ProjectTypeDef pt = new ProjectTypeDef("testEstimateProjectPT", "my testEstimateProject type", true, false) {
            {
                addVariableDefinition("calculated_attribute", "attr description", true, vpf1);
                addVariableDefinition("my_property_1", "attr description", true);
                addVariableDefinition("my_property_2", "attr description", false);
            }
        };

        pm.getProjectTypeRegistry().registerProjectType(pt);

        ContainerResponse response =
                launcher.service(GET, String.format("http://localhost:8080/api/project/%s/estimate/%s?type=%s",
                                                    workspace, "testEstimateProjectGood", "testEstimateProjectPT"),
                                 "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        //noinspection unchecked
        Map<String, List<String>> result = (Map<String, List<String>>)response.getEntity();

        assertEquals(result.size(), 1);
        assertEquals(result.get("calculated_attribute").get(0), "checked");

        response = launcher.service(GET, String.format("http://localhost:8080/api/project/%s/estimate/%s?type=%s",
                                                       workspace, "testEstimateProjectBad", "testEstimateProjectPT"),
                                    "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 409, "Error: " + response.getEntity());
    }

    @Test
    public void testResolveSources() throws Exception {

        VirtualFile root = pm.getProjectsRoot().getVirtualFile();
        root.createFolder("testEstimateProjectGood").createFolder("check");
        root.createFolder("testEstimateProjectBad");

        final ValueProviderFactory vpf1 = projectFolder -> new ValueProvider() {
            @Override
            public List<String> getValues(String attributeName) throws ValueStorageException {

                VirtualFileEntry file;
                try {
                    file = projectFolder.getChild("check");
                } catch (ForbiddenException | ServerException e) {
                    throw new ValueStorageException(e.getMessage());
                }

                if (file == null) {
                    throw new ValueStorageException("Check not found");
                }
                return (List<String>)singletonList("checked");
            }

//            @Override
//            public void setValues(String attributeName, List<String> value) {
//            }
        };

        ProjectTypeDef pt = new ProjectTypeDef("testEstimateProjectPT", "my testEstimateProject type", true, false) {
            {
                addVariableDefinition("calculated_attribute", "attr description", true, vpf1);
                addVariableDefinition("my_property_1", "attr description", true);
                addVariableDefinition("my_property_2", "attr description", false);
            }
        };

        pm.getProjectTypeRegistry().registerProjectType(pt);

        ContainerResponse response =
                launcher.service(GET, String.format("http://localhost:8080/api/project/%s/estimate/%s?type=%s",
                                                    workspace, "testEstimateProjectGood", "testEstimateProjectPT"),
                                 "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        //noinspection unchecked
        Map<String, List<String>> result = (Map<String, List<String>>)response.getEntity();

        assertEquals(result.size(), 1);
        assertEquals(result.get("calculated_attribute").get(0), "checked");

        response = launcher.service(GET, String.format("http://localhost:8080/api/project/%s/estimate/%s?type=%s",
                                                       workspace, "testEstimateProjectBad", "testEstimateProjectPT"),
                                    "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 409, "Error: " + response.getEntity());
    }


    @Test
    public void testImportProject() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(bout);
        zipOut.putNextEntry(new ZipEntry("folder1/"));
        zipOut.putNextEntry(new ZipEntry("folder1/file1.txt"));
        zipOut.write("to be or not to be".getBytes());
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

        byte[] b = String.format(json, importType).getBytes();
        ContainerResponse response = launcher.service(POST,
                                                      String.format("http://localhost:8080/api/project/%s/import/new_project", workspace),
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
                                                      String.format("http://localhost:8080/api/project/%s/file/my_project?name=test.txt",
                                                                    workspace),
                                                      "http://localhost:8080/api",
                                                      null,
                                                      myContent.getBytes(),
                                                      null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        ItemReference fileItem = (ItemReference)response.getEntity();
        assertEquals(fileItem.getType(), "file");
//        assertEquals(fileItem.getMediaType(), TEXT_PLAIN);
        assertEquals(fileItem.getName(), "test.txt");
        assertEquals(fileItem.getPath(), "/my_project/test.txt");
        validateFileLinks(fileItem);
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/file/my_project/test.txt", workspace)));
        VirtualFileEntry file = pm.getProject("my_project").getBaseFolder().getChild("test.txt");
        Assert.assertTrue(file.isFile());
        FileEntry _file = (FileEntry)file;
        //assertEquals(_file.getMediaType(), TEXT_PLAIN);
        assertEquals(new String(_file.contentAsBytes()), myContent);
    }

//
//    @Test
//    public void testUploadFile() throws Exception {
//        String fileContent = "to be or not to be";
//        String fileName = "test.txt";
//        String fileMediaType = TEXT_PLAIN;
//        Map<String, List<String>> headers = new HashMap<>();
//        headers.putProject(CONTENT_TYPE, singletonList("multipart/form-data; boundary=abcdef"));
//        String uploadBodyPattern =
//                "--abcdef\r\nContent-Disposition: form-data; name=\"file\"; filename=\"%1$s\"\r\nContent-Type: %2$s\r\n\r\n%3$s"
//                + "\r\n--abcdef\r\nContent-Disposition: form-data; name=\"mimeType\"\r\n\r\n%4$s"
//                + "\r\n--abcdef\r\nContent-Disposition: form-data; name=\"name\"\r\n\r\n%5$s"
//                + "\r\n--abcdef\r\nContent-Disposition: form-data; name=\"overwrite\"\r\n\r\n%6$b"
//                + "\r\n--abcdef--\r\n";
//        byte[] formData = String.format(uploadBodyPattern, fileName, fileMediaType, fileContent, fileMediaType, fileName, false).getBytes();
//        EnvironmentContext env = new EnvironmentContext();
//        env.putProject(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(formData),
//                                                                     formData.length, POST, headers));
//        ContainerResponse response = launcher.service(POST,
//                                                      String.format("http://localhost:8080/api/project/%s/uploadfile/my_project",
//                                                                    workspace),
//                                                      "http://localhost:8080/api",
//                                                      headers,
//                                                      formData,
//                                                      env);
//        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
//        VirtualFileEntry file = pm.getProject(workspace, "my_project").getBaseFolder().getChild(fileName);
//        Assert.assertTrue(file.isFile());
//        FileEntry _file = (FileEntry)file;
//        assertEquals(_file.getMediaType(), fileMediaType);
//        assertEquals(new String(_file.contentAsBytes()), fileContent);
//    }
//
//    @Test
//    public void testUploadFileWhenFileAlreadyExistAndOverwriteIsTrue() throws Exception {
//        String oldFileContent = "to be or not to be";
//        String newFileContent = "To be, or not to be, that is the question!";
//        String fileName = "test.txt";
//        String fileMediaType = TEXT_PLAIN;
//        Map<String, List<String>> headers = new HashMap<>();
//        headers.putProject(CONTENT_TYPE, singletonList("multipart/form-data; boundary=abcdef"));
//        String uploadBodyPattern =
//                "--abcdef\r\nContent-Disposition: form-data; name=\"file\"; filename=\"%1$s\"\r\nContent-Type: %2$s\r\n\r\n%3$s"
//                + "\r\n--abcdef\r\nContent-Disposition: form-data; name=\"mimeType\"\r\n\r\n%4$s"
//                + "\r\n--abcdef\r\nContent-Disposition: form-data; name=\"name\"\r\n\r\n%5$s"
//                + "\r\n--abcdef\r\nContent-Disposition: form-data; name=\"overwrite\"\r\n\r\n%6$b"
//                + "\r\n--abcdef--\r\n";
//        pm.getProject(workspace, "my_project").getBaseFolder().createFile(fileName, oldFileContent.getBytes());
//        byte[] newFileData =
//                String.format(uploadBodyPattern, fileName, fileMediaType, newFileContent, fileMediaType, fileName, true).getBytes();
//
//        EnvironmentContext env = new EnvironmentContext();
//        env.putProject(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(newFileData),
//                                                                     newFileData.length, POST, headers));
//        ContainerResponse response = launcher.service(POST,
//                                                      String.format("http://localhost:8080/api/project/%s/uploadfile/my_project",
//                                                                    workspace),
//                                                      "http://localhost:8080/api",
//                                                      headers,
//                                                      newFileData,
//                                                      env);
//        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
//        VirtualFileEntry file = pm.getProject(workspace, "my_project").getBaseFolder().getChild(fileName);
//        Assert.assertTrue(file.isFile());
//        FileEntry _file = (FileEntry)file;
//        assertEquals(_file.getMediaType(), fileMediaType);
//        assertEquals(new String(_file.contentAsBytes()), newFileContent);
//    }
//
//    @Test
//    public void testUploadFileWhenFileAlreadyExistAndOverwriteIsFalse() throws Exception {
//        String oldFileContent = "to be or not to be";
//        String newFileContent = "To be, or not to be, that is the question!";
//        String fileName = "test.txt";
//        String fileMediaType = TEXT_PLAIN;
//        Map<String, List<String>> headers = new HashMap<>();
//        headers.putProject(CONTENT_TYPE, singletonList("multipart/form-data; boundary=abcdef"));
//        String uploadBodyPattern =
//                "--abcdef\r\nContent-Disposition: form-data; name=\"file\"; filename=\"%1$s\"\r\nContent-Type: %2$s\r\n\r\n%3$s"
//                + "\r\n--abcdef\r\nContent-Disposition: form-data; name=\"mimeType\"\r\n\r\n%4$s"
//                + "\r\n--abcdef\r\nContent-Disposition: form-data; name=\"name\"\r\n\r\n%5$s"
//                + "\r\n--abcdef\r\nContent-Disposition: form-data; name=\"overwrite\"\r\n\r\n%6$b"
//                + "\r\n--abcdef--\r\n";
//        pm.getProject(workspace, "my_project").getBaseFolder().createFile(fileName, oldFileContent.getBytes());
//        byte[] newFileData =
//                String.format(uploadBodyPattern, fileName, fileMediaType, newFileContent, fileMediaType, fileName, false).getBytes();
//
//        EnvironmentContext env = new EnvironmentContext();
//        env.putProject(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(newFileData),
//                                                                     newFileData.length, POST, headers));
//        ContainerResponse response = launcher.service(POST,
//                                                      String.format("http://localhost:8080/api/project/%s/uploadfile/my_project",
//                                                                    workspace),
//                                                      "http://localhost:8080/api",
//                                                      headers,
//                                                      newFileData,
//                                                      env);
//        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
//        VirtualFileEntry file = pm.getProject(workspace, "my_project").getBaseFolder().getChild(fileName);
//        Assert.assertTrue(file.isFile());
//        FileEntry _file = (FileEntry)file;
//        assertEquals(_file.getMediaType(), fileMediaType);
//        assertEquals(new String(_file.contentAsBytes()), oldFileContent);
//    }
//
    @Test
    public void testGetFileContent() throws Exception {
        String myContent = "to be or not to be";
        pm.getProject("my_project").getBaseFolder().createFile("test.txt", myContent.getBytes());
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service(GET,
                                                      String.format("http://localhost:8080/api/project/%s/file/my_project/test.txt",
                                                                    workspace),
                                                      "http://localhost:8080/api", null, null, writer, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        assertEquals(response.getContentType().toString(), TEXT_PLAIN);
        assertEquals(new String(writer.getBody()), myContent);
    }

    @Test
    public void testUpdateFileContent() throws Exception {
        String myContent = "<test>hello</test>";
        pm.getProject("my_project").getBaseFolder().createFile("test.xml", "to be or not to be".getBytes());
        ContainerResponse response = launcher.service(PUT,
                                                      String.format("http://localhost:8080/api/project/%s/file/my_project/test.xml", workspace),
                                                      "http://localhost:8080/api", null, myContent.getBytes(), null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        VirtualFileEntry file = pm.getProject("my_project").getBaseFolder().getChild("test.xml");
        Assert.assertTrue(file.isFile());
        FileEntry _file = (FileEntry)file;
        //assertEquals(_file.getMediaType(), "text/xml");
        assertEquals(new String(_file.contentAsBytes()), myContent);
    }

    @Test
    public void testCreateFolder() throws Exception {
        ContainerResponse response = launcher.service(POST,
                                                      String.format("http://localhost:8080/api/project/%s/folder/my_project/test",
                                                                    workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        ItemReference fileItem = (ItemReference)response.getEntity();
        //assertEquals(fileItem.getMediaType(), "text/directory");
        assertEquals(fileItem.getName(), "test");
        assertEquals(fileItem.getPath(), "/my_project/test");
        validateFolderLinks(fileItem);
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/children/my_project/test", workspace)));
        VirtualFileEntry folder = pm.getProject("my_project").getBaseFolder().getChild("test");
        Assert.assertTrue(folder.isFolder());
    }

    @Test
    public void testCreatePath() throws Exception {
        ContainerResponse response = launcher.service(POST,
                                                      String.format("http://localhost:8080/api/project/%s/folder/my_project/a/b/c",
                                                                    workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/children/my_project/a/b/c", workspace)));
        VirtualFileEntry folder = pm.getProject("my_project").getBaseFolder().getChild("a/b/c");
        Assert.assertTrue(folder.isFolder());
    }

    @Test
    public void testDeleteFile() throws Exception {
        pm.getProject("my_project").getBaseFolder().createFile("test.txt", "to be or not to be".getBytes());
        ContainerResponse response = launcher.service(DELETE,
                                                      String.format("http://localhost:8080/api/project/%s/my_project/test.txt", workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 204, "Error: " + response.getEntity());
        Assert.assertNull(pm.getProject("my_project").getBaseFolder().getChild("test.txt"));
    }

    @Test
    public void testDeleteFolder() throws Exception {
        pm.getProject("my_project").getBaseFolder().createFolder("test");
        ContainerResponse response = launcher.service(DELETE,
                                                      String.format("http://localhost:8080/api/project/%s/my_project/test", workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 204, "Error: " + response.getEntity());
        Assert.assertNull(pm.getProject("my_project").getBaseFolder().getChild("test"));
    }

    @Test
    public void testDeletePath() throws Exception {
        pm.getProject("my_project").getBaseFolder().createFolder("a/b/c");
        ContainerResponse response = launcher.service(DELETE,
                                                      String.format("http://localhost:8080/api/project/%s/my_project/a/b/c", workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 204, "Error: " + response.getEntity());
        Assert.assertNull(pm.getProject("my_project").getBaseFolder().getChild("a/b/c"));
    }

    @Test
    public void testDeleteInvalidPath() throws Exception {
        ContainerResponse response = launcher.service(DELETE,
                                                      String.format("http://localhost:8080/api/project/%s/my_project/a/b/c", workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 204);
        assertNotNull(pm.getProject("my_project"));
    }

    @Test
    public void testDeleteProject() throws Exception {


        ContainerResponse response = launcher.service(DELETE,
                                                      String.format("http://localhost:8080/api/project/%s/my_project", workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 204, "Error: " + response.getEntity());
        Assert.assertNull(pm.getProject("my_project"));

//        verify(httpJsonRequestFactory).fromLink(eq(DtoFactory.newDto(Link.class)
//                                                             .withHref(apiEndpoint + "/workspace/" + workspace + "/project/my_project")
//                                                             .withMethod(DELETE)));
    }

    @Test
    public void testCopyFile() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes());
        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/copy/my_project/a/b/test.txt?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/file/my_project/a/b/c/test.txt", workspace)));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/c/test.txt")); // new
        assertNotNull(myProject.getBaseFolder().getChild("a/b/test.txt")); // old
    }

    @Test
    public void testCopyFileWithRename() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes());

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        CopyOptions descriptor = DtoFactory.getInstance().createDto(CopyOptions.class);
        descriptor.setName("copyOfTest.txt");
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/copy/my_project/a/b/test.txt?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/file/my_project/a/b/c/copyOfTest.txt", workspace)));
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

        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile(originFileName, originContent.getBytes());
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b/c")).createFile(destinationFileName, overwrittenContent.getBytes());

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        CopyOptions descriptor = DtoFactory.getInstance().createDto(CopyOptions.class);
        descriptor.setName(destinationFileName);
        descriptor.setOverWrite(true);

        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/copy/my_project/a/b/" + originFileName +
                                                              "?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/file/my_project/a/b/c/" + destinationFileName,
                                              workspace)));
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
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes());
        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/copy/my_project/a/b?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/children/my_project/a/b/c/b", workspace)));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/test.txt"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/c/b/test.txt"));
    }

    @Test
    public void testCopyFolderWithRename() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes());

        // new name for folder
        final String renamedFolder = "renamedFolder";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        CopyOptions descriptor = DtoFactory.getInstance().createDto(CopyOptions.class);
        descriptor.setName(renamedFolder);
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/copy/my_project/a/b?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             String.format("http://localhost:8080/api/project/%s/children/my_project/a/b/c/%s", workspace, renamedFolder)));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/test.txt"));
        assertNotNull(myProject.getBaseFolder().getChild(String.format("a/b/c/%s/test.txt", renamedFolder)));
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

        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile(originFileName, originContent.getBytes());
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b/c")).createFile(destinationFileName, overwrittenContent.getBytes());

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        CopyOptions descriptor = DtoFactory.getInstance().createDto(CopyOptions.class);
        descriptor.setName(renamedFolder);
        descriptor.setOverWrite(true);

        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/copy/my_project/a/b?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             String.format("http://localhost:8080/api/project/%s/children/my_project/a/b/c/%s", workspace, renamedFolder)));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/test.txt"));
        assertNotNull(myProject.getBaseFolder().getChild(String.format("a/b/c/%s/test.txt", renamedFolder)));
        assertEquals(myProject.getBaseFolder().getChild("a/b/test.txt").getName(),
                     myProject.getBaseFolder().getChild(String.format("a/b/c/%s/%s", renamedFolder, originFileName)).getName());
    }

    @Test
    public void testMoveFile() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes());
        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/move/my_project/a/b/test.txt?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/file/my_project/a/b/c/test.txt", workspace)));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/c/test.txt")); // new
        Assert.assertNull(myProject.getBaseFolder().getChild("a/b/test.txt")); // old
    }

    @Test
    public void testMoveFileWithRename() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes());

        // name for file after move
        final String destinationName = "copyOfTestForMove.txt";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(destinationName);
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/move/my_project/a/b/test.txt?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             String.format("http://localhost:8080/api/project/%s/file/my_project/a/b/c/%s", workspace, destinationName)));
        VirtualFileEntry theTargetFile = myProject.getBaseFolder().getChild(String.format("a/b/c/%s", destinationName));
        assertNotNull(theTargetFile); // new
    }

    @Test
    public void testRenameFile() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes());

        // name for file after move
        final String destinationName = "copyOfTestForMove.txt";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(destinationName);
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/move/my_project/a/b/test.txt",
                                                              workspace),
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             String.format("http://localhost:8080/api/project/%s/file/my_project/a/b/%s", workspace, destinationName)));
        VirtualFileEntry theTargetFile = myProject.getBaseFolder().getChild(String.format("a/b/%s", destinationName));
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

        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile(originFileName, originContent.getBytes());
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b/c")).createFile(destinationFileName, overwrittenContent.getBytes());

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(destinationFileName);
        descriptor.setOverWrite(true);

        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/move/my_project/a/b/" + originFileName +
                                                              "?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/file/my_project/a/b/c/" + destinationFileName,
                                              workspace)));
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
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b/c")).createFile("test.txt", "to be or not no be".getBytes());
        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/move/my_project/a/b/c?to=/my_project/a",
                                                              workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/children/my_project/a/c", workspace)));
        assertNotNull(myProject.getBaseFolder().getChild("a/c/test.txt"));
        Assert.assertNull(myProject.getBaseFolder().getChild("a/b/c/test.txt"));
        Assert.assertNull(myProject.getBaseFolder().getChild("a/b/c"));
    }

    @Test
    public void testMoveFolderWithRename() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b/c");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes());

        // new name for folder
        final String renamedFolder = "renamedFolder";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(renamedFolder);
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/copy/my_project/a/b?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             String.format("http://localhost:8080/api/project/%s/children/my_project/a/b/c/%s", workspace, renamedFolder)));
        assertNotNull(myProject.getBaseFolder().getChild(String.format("a/b/c/%s/test.txt", renamedFolder)));
    }

    @Test
    public void testRenameFolder() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b");
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile("test.txt", "to be or not no be".getBytes());

        // new name for folder
        final String renamedFolder = "renamedFolder";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(renamedFolder);
        descriptor.setOverWrite(false);

        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/move/my_project/a/b",
                                                              workspace),
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             String.format("http://localhost:8080/api/project/%s/children/my_project/a/%s", workspace, renamedFolder)));
        assertNotNull(myProject.getBaseFolder().getChild(String.format("a/%s/test.txt", renamedFolder)));
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

        ((FolderEntry)myProject.getBaseFolder().getChild("a/b")).createFile(originFileName, originContent.getBytes());
        ((FolderEntry)myProject.getBaseFolder().getChild("a/b/c")).createFile(destinationFileName, overwritenContent.getBytes());

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_JSON));

        MoveOptions descriptor = DtoFactory.getInstance().createDto(MoveOptions.class);
        descriptor.setName(renamedFolder);
        descriptor.setOverWrite(true);

        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/copy/my_project/a/b?to=/my_project/a/b/c",
                                                              workspace),
                                                      "http://localhost:8080/api", headers,
                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(), null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(
                             String.format("http://localhost:8080/api/project/%s/children/my_project/a/b/c/%s", workspace, renamedFolder)));
        assertNotNull(myProject.getBaseFolder().getChild(String.format("a/b/c/%s/test.txt", renamedFolder)));
    }

    @Test
    public void testImportZip() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b");

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(bout);
        zipOut.putNextEntry(new ZipEntry("folder1/"));
        zipOut.putNextEntry(new ZipEntry("folder1/file1.txt"));
        zipOut.write("to be or not to be".getBytes());
        zipOut.close();
        byte[] zip = bout.toByteArray();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(ExtMediaType.APPLICATION_ZIP));
        ContainerResponse response = launcher.service(POST,
                                                      String.format("http://localhost:8080/api/project/%s/import/my_project/a/b",
                                                                    workspace),
                                                      "http://localhost:8080/api", headers, zip, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/children/my_project/a/b", workspace)));
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
        zipOut.write("to be or not to be".getBytes());
        zipOut.close();
        byte[] zip = bout.toByteArray();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, singletonList(ExtMediaType.APPLICATION_ZIP));
        ContainerResponse response = launcher.service(POST,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/import/my_project/a/b?skipFirstLevel=false",
                                                              workspace),
                                                      "http://localhost:8080/api", headers, zip, null);
        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
        assertEquals(response.getHttpHeaders().getFirst("Location"),
                     URI.create(String.format("http://localhost:8080/api/project/%s/children/my_project/a/b", workspace)));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/folder1/"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/folder1/folder2"));
        assertNotNull(myProject.getBaseFolder().getChild("a/b/folder1/folder2/file1.txt"));
    }

//    @Test
//    public void testExportZip() throws Exception {
//        ProjectImpl myProject = pm.getProject("my_project");
//        myProject.getBaseFolder().createFolder("a/b").createFile("test.txt", "hello".getBytes());
//        ContainerResponse response = launcher.service(GET,
//                                                      String.format("http://localhost:8080/api/project/%s/export/my_project", workspace),
//                                                      "http://localhost:8080/api", null, null, null);
//        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
//        assertEquals(response.getContentType().toString(), ExtMediaType.APPLICATION_ZIP);
//    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetChildren() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        FolderEntry a = myProject.getBaseFolder().createFolder("a");
        a.createFolder("b");
        a.createFile("test.txt", "test".getBytes());
        ContainerResponse response = launcher.service(GET,
                                                      String.format("http://localhost:8080/api/project/%s/children/my_project/a",
                                                                    workspace),
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
        a.createFile("test.txt", "test".getBytes());
        ContainerResponse response = launcher.service(GET,
                                                      String.format("http://localhost:8080/api/project/%s/item/my_project/a/b", workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());

        ItemReference result = (ItemReference)response.getEntity();
        assertEquals(result.getName(), "b");

        response = launcher.service(GET,
                                    String.format("http://localhost:8080/api/project/%s/item/my_project/a/test.txt", workspace),
                                    "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        result = (ItemReference)response.getEntity();
        assertEquals(result.getType(), "file");
        //assertEquals(result.getMediaType(), TEXT_PLAIN);
    }

    @Test
    public void testGetItemWithoutParentProject() throws Exception {
        FolderEntry a = pm.getProjectsRoot().createFolder("a");
        a.createFile("test.txt", "test".getBytes());
        ContainerResponse response = launcher.service(GET,
                String.format("http://localhost:8080/api/project/%s/item/a/test.txt",
                        workspace),
                "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        ItemReference result = (ItemReference)response.getEntity();
        assertEquals(result.getType(), "file");
        //assertEquals(result.getMediaType(), TEXT_PLAIN);
    }

    @Test
    public void testGetMissingItem() throws Exception {
        ContainerResponse response = launcher.service(GET,
                String.format("http://localhost:8080/api/project/%s/item/some_missing_project/a/b",
                        workspace),
                "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 404, "Error: " + response.getEntity());
    }

//    @Test
//    @SuppressWarnings("unchecked")
//    public void testGetItemWithHandler() throws Exception {
//        final ProjectImpl myProject = pm.getProject("my_project");
//        GetItemHandler myHandler = new GetItemHandler() {
//            @Override
//            public void onGetItem(VirtualFileEntry virtualFile) {
//
//                virtualFile.getAttributeEntries().putProject("my", "myValue");
//                if (virtualFile.isFile())
//                    virtualFile.getAttributeEntries().putProject("file", "a");
//            }
//
//            @Override
//            public String getProjectType() {
//                return "my_project_type";
//            }
//        };
//        pm.getHandlers().register(myHandler);
//
//        FolderEntry a = myProject.getBaseFolder().createFolder("a");
//        a.createFolder("b");
//        a.createFile("test.txt", "test".getBytes());
//        ContainerResponse response = launcher.service(GET,
//                                                      String.format("http://localhost:8080/api/project/%s/item/my_project/a/b", workspace),
//                                                      "http://localhost:8080/api", null, null, null);
//        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
//
//        ItemReference result = (ItemReference)response.getEntity();
//        assertEquals(result.getName(), "b");
//        assertNotNull(result.getCreated());
//        assertNotNull(result.getModified());
//        assertEquals(result.getAttributeEntries().size(), 1);
//
//        response = launcher.service(GET,
//                                    String.format("http://localhost:8080/api/project/%s/item/my_project/a/test.txt", workspace),
//                                    "http://localhost:8080/api", null, null, null);
//        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
//        result = (ItemReference)response.getEntity();
//        assertEquals(result.getProjectType(), "file");
//        //assertEquals(result.getMediaType(), TEXT_PLAIN);
//        assertNotNull(result.getContentLength());
//        assertEquals(result.getAttributeEntries().size(), 2);
//    }

    @Test
    public void testGetTree() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        FolderEntry a = myProject.getBaseFolder().createFolder("a");
        a.createFolder("b/c");
        a.createFolder("x/y");
        a.createFile("test.txt", "test".getBytes());
        ContainerResponse response = launcher.service(GET,
                                                      String.format("http://localhost:8080/api/project/%s/tree/my_project/a", workspace),
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
        a.createFile("test.txt", "test".getBytes());
        ContainerResponse response = launcher.service(GET,
                                                      String.format("http://localhost:8080/api/project/%s/tree/my_project/a?depth=2",
                                                                    workspace),
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
        a.createFolder("x").createFile("test.txt", "test".getBytes());
        ContainerResponse response = launcher.service(GET,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/tree/my_project/a?depth=100&includeFiles=true",
                                                              workspace),
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
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/tree/my_project/a?depth=100&includeFiles=true",
                                                              workspace),
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
        myProject.getBaseFolder().createFolder("a/b").createFile("test.txt", "hello".getBytes());
        myProject.getBaseFolder().createFolder("x/y").createFile("test.txt", "test".getBytes());
        myProject.getBaseFolder().createFolder("c").createFile("exclude", "test".getBytes());

        ContainerResponse response = launcher.service(GET,
                                                      String.format("http://localhost:8080/api/project/%s/search/my_project?name=test.txt",
                                                                    workspace),
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
        myProject.getBaseFolder().createFolder("a/b").createFile("test.txt", "hello".getBytes());
        myProject.getBaseFolder().createFolder("x/y").createFile("__test.txt", "searchhit".getBytes());
        myProject.getBaseFolder().createFolder("c").createFile("_test", "searchhit".getBytes());

        ContainerResponse response = launcher.service(GET,
                                                      String.format("http://localhost:8080/api/project/%s/search/my_project?text=searchhit",
                                                                    workspace),
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
    public void testSearchByNameAndText() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b").createFile("test.txt", "test".getBytes());
        myProject.getBaseFolder().createFolder("x/y").createFile("test.txt", "test".getBytes());
        myProject.getBaseFolder().createFolder("c").createFile("test", "test".getBytes());

        ContainerResponse response = launcher.service(GET,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/search/my_project?text=test&name=test.txt",
                                                              workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 2);
        Assert.assertTrue(result.get(0).getPath().equals("/my_project/a/b/test.txt"));
        Assert.assertTrue(result.get(1).getPath().equals("/my_project/x/y/test.txt"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchFromWSRoot() throws Exception {
        RegisteredProject myProject = pm.getProject("my_project");
        myProject.getBaseFolder().createFolder("a/b").createFile("test", "test".getBytes());
        myProject.getBaseFolder().createFolder("x/y").createFile("test", "test".getBytes());
        myProject.getBaseFolder().createFolder("c").createFile("test.txt", "test".getBytes());

        ContainerResponse response = launcher.service(GET,
                                                      String.format(
                                                              "http://localhost:8080/api/project/%s/search/?text=test&name=test.txt",
                                                              workspace),
                                                      "http://localhost:8080/api", null, null, null);
        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
        List<ItemReference> result = (List<ItemReference>)response.getEntity();
        assertEquals(result.size(), 1);
        Assert.assertTrue(result.get(0).getPath().equals("/my_project/c/test.txt"));
    }



/* ---- To think --- */


//    @Test
//    public void testImportProjectWithModules() throws Exception {
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        ZipOutputStream zipOut = new ZipOutputStream(bout);
//        zipOut.putNextEntry(new ZipEntry("module1/"));
//        zipOut.putNextEntry(new ZipEntry("module1/marker"));
//        zipOut.write("to be or not to be".getBytes());
//        zipOut.close();
//        final InputStream zip = new ByteArrayInputStream(bout.toByteArray());
//        final String importType = "_123_";
//
//        registerImporter(importType, zip);
//
//        phRegistry.register(new PostImportProjectHandler() {
//            @Override
//            public void onProjectImported(FolderEntry projectFolder)
//                    throws ForbiddenException, ConflictException, ServerException, IOException, NotFoundException {
//            }
//
//            @Override
//            public String getProjectType() {
//                return "chuck_project_type";
//            }
//        });
//
//        final ProjectConfigDto newProjectConfig = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
//                                                            .withPath("/new_project")
//                                                            .withName("new_project")
//                                                            .withDescription("import test")
//                                                            .withType("chuck_project_type");
//        final ProjectConfigDto newModuleConfig = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
//                                                           .withPath("/new_project/module1")
//                                                           .withName("module1")
//                                                           .withDescription("module description")
//                                                           .withType("module_type");
//        //projects.add(newProjectConfig);
//        //modules.add(newModuleConfig);
//
//        Map<String, List<String>> headers = new HashMap<>();
//        headers.putProject(CONTENT_TYPE, singletonList(APPLICATION_JSON));
//
//        SourceStorageDto source = DtoFactory.newDto(SourceStorageDto.class)
//                                            .withParameters(Collections.emptyMap())
//                                            .withLocation("location/new_project.ext")
//                                            .withType(importType);
//
//        ProjectConfigDto pModule = DtoFactory.newDto(ProjectConfigDto.class)
//                                             .withName("module1")
//                                             .withPath("/module1")
//                                             .withType("module_type")
//                                             .withDescription("module description");
//
//        ProjectConfigDto project = DtoFactory.newDto(ProjectConfigDto.class)
//                                             .withDescription("import test")
//                                             .withType("chuck_project_type")
//                                             .withModules(singletonList(pModule));
//
//        ContainerResponse response1 = launcher.service(POST,
//                                                       String.format("http://localhost:8080/api/project/%s/import/new_project", workspace),
//                                                       "http://localhost:8080/api", headers, JsonHelper.toJson(source).getBytes(),
//                                                       null);
//        assertEquals(response1.getStatus(), 204, "Error: " + response1.getEntity());
//
//        ContainerResponse response = launcher.service(PUT,
//                                                      String.format("http://localhost:8080/api/project/%s/new_project", workspace),
//                                                      "http://localhost:8080/api", headers, JsonHelper.toJson(project).getBytes(),
//                                                      null);
//        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
//
//        ProjectConfigDto descriptor = (ProjectConfigDto)response.getEntity();
//        assertEquals(descriptor.getDescription(), "import test");
//        assertEquals(descriptor.getType(), "chuck_project_type");
//        ProjectImpl newProject = pm.getProject("new_project");
//        assertNotNull(newProject);
//    }
//

//    @Test
//    public void testRenameModule() throws Exception {
//        //create new module
//        phRegistry.register(new CreateProjectHandler() {
//
//            @Override
//            public String getProjectType() {
//                return "my_project_type";
//            }
//
//            @Override
//            public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
//                    throws ConflictException, ForbiddenException, ServerException {
//                baseFolder.createFolder("a");
//                baseFolder.createFolder("b");
//                baseFolder.createFile("test.txt", "test".getBytes());
//            }
//        });
//
//        Map<String, List<String>> headers = new HashMap<>();
//        headers.putProject("Content-Type", Arrays.asList(APPLICATION_JSON));
//
//        Map<String, List<String>> attributeValues = new LinkedHashMap<>();
//        attributeValues.putProject("new module attribute", Arrays.asList("to be or not to be"));
//
//        ProjectConfigDto descriptor = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
//                                                .withType("my_project_type")
//                                                .withDescription("new module")
//                                                .withAttributes(attributeValues);
//
//        ContainerResponse response = launcher.service(POST,
//                                                      String.format("http://localhost:8080/api/project/%s?path=%s",
//                                                                    workspace, "new_module"),
//                                                      "http://localhost:8080/api",
//                                                      headers,
//                                                      DtoFactory.getInstance().toJson(descriptor).getBytes(),
//                                                      null);
//        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
//
//        //rename module
//        ProjectImpl myProject = pm.getProject("my_project");
//
//        assertTrue(myProject.getModulePaths().contains("new_module"));
//
//        final String newName = "moduleRenamed";
//
//        response = launcher.service(POST,
//                                    String.format("http://localhost:8080/api/project/%s/rename/my_project/new_module?name=%s",
//                                                  workspace, newName),
//                                    "http://localhost:8080/api", null, null, null);
//        assertEquals(response.getStatus(), 201, "Error: " + response.getEntity());
//        assertEquals(response.getHttpHeaders().getFirst("Location"),
//                     URI.create(String.format("http://localhost:8080/api/project/%s/children/my_project/%s",
//                                              workspace, newName)));
//        assertNotNull(myProject.getBaseFolder().getChild(newName + "/a"));
//        assertNotNull(myProject.getBaseFolder().getChild(newName + "/b"));
//        assertNotNull(myProject.getBaseFolder().getChild(newName + "/test.txt"));
//
//        assertTrue(pm.getProject("my_project").getModulePaths().contains(newName));
//        assertFalse(pm.getProject("my_project").getModulePaths().contains("new_module"));
//    }


//    @Test
//    @SuppressWarnings("unchecked")
//    public void testGetModulesWithHandler() throws Exception {
//        ProjectTypeDef pt = new ProjectTypeDef("testGetModules", "my module type", true, false) {
//            {
//                addConstantDefinition("my_module_attribute", "attr description", "attribute value 1");
//            }
//        };
//        pm.getProjectTypeRegistry().registerProjectType(pt);
//
//        ProjectImpl myProject = pm.getProject("/my_project");
//        //create other module but not add to modules should be added to response by handler
//        //myProject.getBaseFolder().createFolder("my_module2");
//        //pm.initProjects();
//
//        phRegistry.register(new GetModulesHandler() {
//            @Override
//            public void onGetModules(FolderEntry parentProjectFolder, final List<String> modulesPath)
//                    throws ForbiddenException, ServerException, NotFoundException, IOException {
//                FolderEntry child = (FolderEntry)parentProjectFolder.getChild("my_module2");
//                if (pm.isProject(child.getPath().toString())) {
//                    modulesPath.add(child.getPath().toString());
//                }
//            }
//
//            @Override
//            public String getProjectType() {
//                return "my_project_type";
//            }
//        });
//
//        final ProjectConfigDto moduleConfig1 = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
//                                                         .withPath("/my_project/my_module")
//                                                         .withName("my_module")
//                                                         .withDescription("my test module")
//                                                         .withType("testGetModules");
//        final ProjectConfigDto moduleConfig2 = DtoFactory.getInstance().createDto(ProjectConfigDto.class)
//                                                         .withPath("/my_project/my_module2")
//                                                         .withName("my_module2")
//                                                         .withDescription("my test module")
//                                                         .withType("testGetModules");
//
//        pm.createProject(moduleConfig1, null, "/my_project");
//        //create other module but not add to modules should be added to response by handler
//        pm.createProject(moduleConfig2, null);
//        myProject.addModule("/my_project/my_module2");
////        pm.createProject(moduleConfig2, null, "/my_project");
////        modules.add(moduleConfig1);
////        modules.add(moduleConfig2);
//
//        ContainerResponse response = launcher.service(GET,
//                                                      String.format("http://localhost:8080/api/project/%s/modules/my_project", workspace),
//                                                      "http://localhost:8080/api", null, null, null);
//        assertEquals(response.getStatus(), 200, "Error: " + response.getEntity());
//        List<ProjectConfigDto> result = (List<ProjectConfigDto>)response.getEntity();
//        assertNotNull(result);
//
//        assertEquals(result.size(), 2);
//        ProjectConfigDto moduleDescriptor = result.get(0);
//        assertEquals(moduleDescriptor.getName(), "my_module");
//
//        ProjectConfigDto moduleDescriptor2 = result.get(1);
//        assertEquals(moduleDescriptor2.getName(), "my_module2");
//    }





    private void validateFileLinks(ItemReference item) {
        Link link = item.getLink("delete");
        assertNotNull(link);
        assertEquals(link.getMethod(), DELETE);
        assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + item.getPath());

//        link = item.getLink("get content");
//        assertNotNull(link);
//        assertEquals(link.getMethod(), GET);
//        assertEquals(link.getProduces(), item.getMediaType());
//        assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + "/file" + item.getPath());

        link = item.getLink("update content");
        assertNotNull(link);
        assertEquals(link.getMethod(), PUT);
        assertEquals(link.getConsumes(), "*/*");
        assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + "/file" + item.getPath());
    }

    private void validateFolderLinks(ItemReference item) {
        Link link = item.getLink("children");
        assertNotNull(link);
        assertEquals(link.getMethod(), GET);
        assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + "/children" + item.getPath());
        assertEquals(link.getProduces(), APPLICATION_JSON);

        link = item.getLink("tree");
        assertNotNull(link);
        assertEquals(link.getMethod(), GET);
        assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + "/tree" + item.getPath());
        assertEquals(link.getProduces(), APPLICATION_JSON);

//        link = item.getLink("modules");
//        assertNotNull(link);
//        assertEquals(link.getMethod(), GET);
//        assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + "/modules" + item.getPath());
//        assertEquals(link.getProduces(), APPLICATION_JSON);

//        link = item.getLink("zipball");
//        assertNotNull(link);
//        assertEquals(link.getMethod(), GET);
//        assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + "/export" + item.getPath());
//        assertEquals(link.getProduces(), ExtMediaType.APPLICATION_ZIP);

        link = item.getLink("delete");
        assertNotNull(link);
        assertEquals(link.getMethod(), DELETE);
        assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + item.getPath());
    }




    private void validateProjectLinks(ProjectConfigDto project) {
        List<Link> links = project.getLinks();

        for (Link link : links) {
            switch (link.getHref()) {
                case "update project":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), PUT);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + project.getPath());
                    assertEquals(link.getConsumes(), APPLICATION_JSON);
                    assertEquals(link.getProduces(), APPLICATION_JSON);
                    break;

                case "children":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), GET);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + "/children" + project.getPath());
                    assertEquals(link.getProduces(), APPLICATION_JSON);
                    break;

                case "tree":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), GET);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + "/tree" + project.getPath());
                    assertEquals(link.getProduces(), APPLICATION_JSON);
                    break;

                case "modules":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), GET);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + "/modules" + project.getPath());
                    assertEquals(link.getProduces(), APPLICATION_JSON);
                    break;

                case "zipball sources":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), GET);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + "/export" + project.getPath());
                    assertEquals(link.getProduces(), APPLICATION_ZIP);
                    break;

                case "delete":
                    assertNotNull(link);
                    assertEquals(link.getMethod(), DELETE);
                    assertEquals(link.getHref(), "http://localhost:8080/api/project/" + workspace + project.getPath());
                    break;
            }
        }
    }

    private class LocalProjectType extends ProjectTypeDef {
        private LocalProjectType(String typeId, String typeName) {
            super(typeId, typeName, true, false);
            addConstantDefinition("my_attribute", "Constant", "attribute value 1");
        }
    }
}
