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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.ide.extension.maven.server.core.MavenWorkspace;
import org.eclipse.che.ide.extension.maven.server.core.EclipseWorkspaceProvider;
import org.eclipse.core.resources.IProject;

import java.io.IOException;
import java.util.Collections;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
@Singleton
public class MavenProjectResolver {


    private final EclipseWorkspaceProvider provider;
    private final MavenWorkspace           mavenWorkspace;

    @Inject
    public MavenProjectResolver(EclipseWorkspaceProvider provider, MavenWorkspace mavenWorkspace) {
        this.provider = provider;
        this.mavenWorkspace = mavenWorkspace;
    }

    /**
     * The method allows define project structure as it is in project tree. Project can has got some modules and each module can has got
     * own modules.
     *
     * @param projectFolder
     *         base folder which represents project
     * @param projectManager
     *         special manager which is necessary for updating project after resolve
     * @throws ConflictException
     * @throws ForbiddenException
     * @throws ServerException
     * @throws NotFoundException
     * @throws IOException
     */
    public void resolve(FolderEntry projectFolder, ProjectManager projectManager) throws ConflictException,
                                                                                         ForbiddenException,
                                                                                         ServerException,
                                                                                         NotFoundException,
                                                                                         IOException {
        VirtualFileEntry pom = projectFolder.getChild("pom.xml");

        if (pom == null) {
            return;
        }
        IProject project = provider.get().getRoot().getProject(projectFolder.getPath());
        mavenWorkspace.update(Collections.singletonList(project));

        Model model = Model.readFrom(pom.getVirtualFile());
        MavenClassPathConfigurator.configure(projectFolder);

        String packaging = model.getPackaging();
        if (packaging != null && packaging.equals("pom")) {
            RegisteredProject project = projectManager.getProject(projectFolder.getPath().toString());

            ProjectConfigImpl projectConfig = createConfig(projectFolder);

            List<ProjectConfig> modules = new ArrayList<>();

            for (FolderEntry folderEntry : project.getBaseFolder().getChildFolders()) {
                MavenClassPathConfigurator.configure(folderEntry);

                defineModules(folderEntry, modules);
            }
//        Model model = Model.readFrom(pom.getVirtualFile());
//        MavenClassPathConfigurator.configure(projectFolder);
//
//        String packaging = model.getPackaging();
//        if (packaging != null && packaging.equals("pom")) {
//            Project project = new Project(projectFolder, projectManager);
//
//            ProjectConfigImpl projectConfig = createConfig(projectFolder);
//
//            List<ProjectConfig> modules = new ArrayList<>();
//
//            for (FolderEntry folderEntry : project.getBaseFolder().getChildFolders()) {
//                MavenClassPathConfigurator.configure(folderEntry);
//
//                defineModules(folderEntry, modules);
//            }
//
//            projectConfig.setModules(modules);
//            projectConfig.setSource(getSourceStorage(project.getConfig()));
//
//            project.updateConfig(projectConfig);
//        }
    }

            // TODO: rework after new Project API
//            projectConfig.setModules(modules);
            projectConfig.setSource(getSourceStorage(project));

            projectManager.updateProject(projectConfig);
        }
    }

    private static ProjectConfigImpl createConfig(FolderEntry folderEntry) throws ValueStorageException {
        ProjectConfigImpl projectConfig = new ProjectConfigImpl();
        projectConfig.setName(folderEntry.getName());
        projectConfig.setPath(folderEntry.getPath().toString());
        projectConfig.setType(MAVEN_ID);

        return projectConfig;
    }

    private static SourceStorageImpl getSourceStorage(ProjectConfig config) {
        SourceStorage sourceStorage = config.getSource();

        if (sourceStorage == null) {
            return new SourceStorageImpl("", "", Collections.emptyMap());
        }

        return new SourceStorageImpl(sourceStorage.getType(), sourceStorage.getLocation(), sourceStorage.getParameters());
    }

    private static void defineModules(FolderEntry folderEntry, List<ProjectConfig> modules) throws ServerException,
                                                                                                   ForbiddenException,
                                                                                                   IOException,
                                                                                                   ConflictException {
        VirtualFileEntry pom = folderEntry.getChild("pom.xml");

        if (pom == null) {
            return;
        }

        ProjectConfigImpl moduleConfig = createConfig(folderEntry);

        List<ProjectConfig> internalModules = new ArrayList<>();

        for (FolderEntry internalModule : folderEntry.getChildFolders()) {
            MavenClassPathConfigurator.configure(folderEntry);

            defineModules(internalModule, internalModules);
        }

        // TODO: rework after new Project API
//        moduleConfig.setModules(internalModules);

        modules.add(moduleConfig);
    }
//    private static ProjectConfigImpl createConfig(FolderEntry folderEntry) throws ValueStorageException {
//        ProjectConfigImpl projectConfig = new ProjectConfigImpl();
//        projectConfig.setName(folderEntry.getName());
//        projectConfig.setPath(folderEntry.getPath());
//        projectConfig.setType(MAVEN_ID);
//
//        return projectConfig;
//    }
//
//    private static SourceStorageImpl getSourceStorage(ProjectConfig config) {
//        SourceStorage sourceStorage = config.getSource();
//
//        if (sourceStorage == null) {
//            return new SourceStorageImpl("", "", Collections.emptyMap());
//        }
//
//        return new SourceStorageImpl(sourceStorage.getType(), sourceStorage.getLocation(), sourceStorage.getParameters());
//    }
//
//    private static void defineModules(FolderEntry folderEntry, List<ProjectConfig> modules) throws ServerException,
//                                                                                                   ForbiddenException,
//                                                                                                   IOException,
//                                                                                                   ConflictException {
//        VirtualFileEntry pom = folderEntry.getChild("pom.xml");
//
//        if (pom == null) {
//            return;
//        }
//
//        ProjectConfigImpl moduleConfig = createConfig(folderEntry);
//
//        List<ProjectConfig> internalModules = new ArrayList<>();
//
//        for (FolderEntry internalModule : folderEntry.getChildFolders()) {
//            MavenClassPathConfigurator.configure(folderEntry);
//
//            defineModules(internalModule, internalModules);
//        }
//
//        moduleConfig.setModules(internalModules);
//
//        modules.add(moduleConfig);
//    }
}
