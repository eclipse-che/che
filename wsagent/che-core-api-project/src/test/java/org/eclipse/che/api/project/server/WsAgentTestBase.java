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
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.SettableValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.impl.file.event.detectors.ProjectTreeChangesDetector;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;
import org.eclipse.che.commons.lang.IoUtil;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;

/**
 * @author gazarenkov
 */
public class WsAgentTestBase {

    protected final static String FS_PATH    = "target/fs";
    protected final static String INDEX_PATH = "target/fs_index";

    protected TestWorkspaceHolder workspaceHolder;

    protected File root;

    protected ProjectManager pm;

    protected LocalVirtualFileSystemProvider vfsProvider;

    protected EventService eventService;

    protected ProjectRegistry projectRegistry;

    protected FileWatcherNotificationHandler fileWatcherNotificationHandler;

    protected FileTreeWatcher fileTreeWatcher;

    protected ProjectTypeRegistry projectTypeRegistry;

    protected ProjectHandlerRegistry projectHandlerRegistry;

    protected ProjectImporterRegistry importerRegistry;

    protected ProjectTreeChangesDetector projectTreeChangesDetector;

    public void setUp() throws Exception {

        if (workspaceHolder == null)
            workspaceHolder = new TestWorkspaceHolder();

        if (root == null)
            root = new File(FS_PATH);

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

        vfsProvider = new LocalVirtualFileSystemProvider(root, sProvider);


        projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());
        projectTypeRegistry.registerProjectType(new PT1());

        projectHandlerRegistry = new ProjectHandlerRegistry(new HashSet<>());

        this.eventService = new EventService();

        projectRegistry = new ProjectRegistry(workspaceHolder, vfsProvider, projectTypeRegistry, projectHandlerRegistry, eventService);
        projectRegistry.initProjects();

        this.importerRegistry = new ProjectImporterRegistry(new HashSet<>());

        fileWatcherNotificationHandler = new DefaultFileWatcherNotificationHandler(vfsProvider);
        fileTreeWatcher = new FileTreeWatcher(root, new HashSet<>(), fileWatcherNotificationHandler);

        TestWorkspaceHolder wsHolder = new  TestWorkspaceHolder();


        pm = new ProjectManager(vfsProvider, eventService, projectTypeRegistry, projectRegistry, projectHandlerRegistry,
                                importerRegistry, fileWatcherNotificationHandler, fileTreeWatcher, wsHolder,
                                mock(ProjectTreeChangesDetector.class));
        pm.initWatcher();
    }


    protected static class TestWorkspaceHolder extends WorkspaceProjectsSyncer {

        private Map<String, ProjectConfig> projects = new HashMap<>();

        protected TestWorkspaceHolder() throws ServerException {

        }


        protected TestWorkspaceHolder(List<ProjectConfig> projects) throws ServerException {
            for (ProjectConfig p : projects) {
                this.projects.put(p.getPath(), p);
            }

        }

        @Override
        public List<? extends ProjectConfig> getProjects() throws ServerException {
            return new ArrayList(projects.values());
        }

        @Override
        public String getWorkspaceId() {
            return "ws";
        }

        @Override
        protected void addProject(ProjectConfig project) throws ServerException {
            projects.put(project.getPath(), project);

        }

        @Override
        protected void updateProject(ProjectConfig project) throws ServerException {

        }

        @Override
        protected void removeProject(ProjectConfig project) throws ServerException {

        }
    }

    protected static class PT1 extends ProjectTypeDef {

        protected PT1() {
            super("primary1", "primary1", true, false);

            addVariableDefinition("var1", "", false);
            addConstantDefinition("const1", "", "my constant");


        }
    }


    protected static class PT2 extends ProjectTypeDef {

        protected PT2() {
            super("pt2", "pt2", true, false);

            addVariableDefinition("pt2-var1", "", false);
            addVariableDefinition("pt2-var2", "", true);
            addConstantDefinition("pt2-const1", "", "my constant");

        }
    }

    protected static class M2 extends ProjectTypeDef {

        protected M2() {
            super("m2", "m2", false, true);
            addConstantDefinition("pt2-const1", "", "my constant");

        }
    }

    protected static class PT3 extends ProjectTypeDef {

        protected PT3() {
            super("pt3", "pt3", true, false);

            addVariableDefinition("pt2-var1", "", false);
            addVariableDefinition("pt2-var2", "", true);
            addConstantDefinition("pt2-const1", "", "my constant");
            addVariableDefinition("pt2-provided1", "", true, new F());

        }

        protected class F implements ValueProviderFactory {

            FolderEntry project;

            @Override
            public ValueProvider newInstance(final FolderEntry projectFolder) {

                return new ReadonlyValueProvider() {

                    @Override
                    public List<String> getValues(String attributeName) throws ValueStorageException {

                        List<String> values = new ArrayList<>();

                        VirtualFileEntry file1;
                        try {
                            file1 = projectFolder.getChild("/file1");
                        } catch (Exception e) {
                            throw new ValueStorageException(e.getMessage());
                        }

                        if(file1 != null)
                            values.add(attributeName);

                        return values;

                    }
                };
            }
        }

        protected static class SrcGenerator implements CreateProjectHandler {

            @Override
            public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
                    throws ForbiddenException, ConflictException, ServerException {

                baseFolder.createFolder("file1");

            }

            @Override
            public String getProjectType() {
                return "pt3";
            }
        }

    }

    protected static class PT4NoGen extends ProjectTypeDef {

        protected PT4NoGen() {
            super("pt4", "pt4", true, false);

            addVariableDefinition("pt4-provided1", "", true, new F4());

        }

        protected class F4 implements ValueProviderFactory {

            @Override
            public ValueProvider newInstance(final FolderEntry projectFolder) {

                return new ReadonlyValueProvider() {

                    @Override
                    public List<String> getValues(String attributeName) throws ValueStorageException {

                        List<String> values = new ArrayList<>();

                        VirtualFileEntry file1;
                        try {
                            file1 = projectFolder.getChild("/file1");
                        } catch (Exception e) {
                            throw new ValueStorageException(e.getMessage());
                        }

                        if(file1 != null)
                            values.add(attributeName);

                        return values;

                    }
                };
            }
        }

    }


    protected static class PTsettableVP extends ProjectTypeDef {

        public PTsettableVP() {
            super("settableVPPT", "settableVPPT", true, false);
            addVariableDefinition("my", "my", false, new MySettableVPFactory());
        }


        private static class MySettableVPFactory implements ValueProviderFactory {

            public static String value = "notset";


            @Override
            public ValueProvider newInstance(FolderEntry projectFolder) {
                return new MySettableValueProvider();
            }

            public static class MySettableValueProvider extends SettableValueProvider {

                @Override
                public List<String> getValues(String attributeName) throws ValueStorageException {
                    return Arrays.asList(value);
                }

                @Override
                public void setValues(String attributeName, List<String> values) throws ValueStorageException {
                    value = values.get(0);
                }
            }
        }
    }




}
