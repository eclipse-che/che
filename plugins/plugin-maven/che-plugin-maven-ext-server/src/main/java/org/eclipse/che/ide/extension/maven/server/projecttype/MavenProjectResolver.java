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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.ide.maven.tools.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public class MavenProjectResolver {

    /**
     * The method allows define project structure as it is in project tree. Project can has got some modules and each module can has got
     * own modules.
     *
     * @param projectFolder
     *         base folder which represents project
     * @param projectRegistry
     *         special manager which is necessary for updating project after resolve
     * @throws ConflictException
     * @throws ForbiddenException
     * @throws ServerException
     * @throws NotFoundException
     * @throws IOException
     */
    public static void resolve(FolderEntry projectFolder, ProjectRegistry projectRegistry) throws ConflictException,
                                                                                                ForbiddenException,
                                                                                                ServerException,
                                                                                                NotFoundException,
                                                                                                IOException {

        if (projectFolder.getVirtualFile().exists() && projectFolder.getVirtualFile().isFolder()) {
            final VirtualFileEntry pom = projectFolder.getChild("pom.xml");
            if (pom == null || !pom.isFile()) {
                return;
            }

            //projectRegistry.initProject(projectFolder.getPath().toString(), MAVEN_ID);
            projectRegistry.setProjectType(projectFolder.getPath().toString(), MAVEN_ID, false);

            Model model = Model.readFrom(pom.getVirtualFile());
            MavenClassPathConfigurator.configure(projectFolder);
            String packaging = model.getPackaging();
            if ("pom".equals(packaging)) {
                final List<String> mavenModules = model.getModules();
                for (String module : mavenModules) {
                    final File file = projectFolder.getVirtualFile().toIoFile();
                    java.nio.file.Path path = Paths.get(file.getCanonicalPath());
                    final java.nio.file.Path modulePath = path.resolve(module);
                    if (modulePath.toFile().exists() && modulePath.toFile().isDirectory()) {
                        final VirtualFileSystem fileSystem = projectFolder.getVirtualFile().getFileSystem();
                        final VirtualFile root = fileSystem.getRoot();
                        final String substring = modulePath.toString().substring(root.toIoFile().getPath().length());
                        resolve(new FolderEntry(root.getChild(Path.of(substring))), projectRegistry);
                    }
                }
            }
        }
    }




}
