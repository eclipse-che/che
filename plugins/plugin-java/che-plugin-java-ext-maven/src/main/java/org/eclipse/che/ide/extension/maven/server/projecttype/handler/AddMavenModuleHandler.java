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
// TODO: rework after new Project API
//package org.eclipse.che.ide.extension.maven.server.projecttype.handler;
//
//import org.eclipse.che.api.core.ConflictException;
//import org.eclipse.che.api.core.ForbiddenException;
//import org.eclipse.che.api.core.ServerException;
//import org.eclipse.che.api.project.server.FolderEntry;
//import org.eclipse.che.api.project.server.VirtualFileEntry;
//import org.eclipse.che.api.project.server.handlers.CreateModuleHandler;
//import org.eclipse.che.ide.extension.maven.server.projecttype.MavenClassPathConfigurator;
//import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
//import org.eclipse.che.ide.maven.tools.Model;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.Map;
//
///**
// * @author Vitaly Parfonov
// * @author Dmitry Shnurenko
// */
//public class AddMavenModuleHandler implements CreateModuleHandler {
//
//    private static final Logger LOG = LoggerFactory.getLogger(AddMavenModuleHandler.class);
//
//    @Override
//    public void onCreateModule(FolderEntry parentFolder,
//                               String modulePath,
//                               String moduleType,
//                               Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {
//        configureClassPath(parentFolder, modulePath);
//
//        if (!moduleType.equals(MavenAttributes.MAVEN_ID)) {
//            LOG.warn("Module must be Maven project to able be added to Maven project");
//            throw new IllegalArgumentException("Module must be Maven project to able be added to Maven project");
//        }
//        VirtualFileEntry pom = parentFolder.getChild("pom.xml");
//        if (pom == null) {
//            return;
//        }
//        try {
//            Model model = Model.readFrom(pom.getVirtualFile());
//            if ("pom".equals(model.getPackaging())) {
//                final String relativePath = modulePath.substring(parentFolder.getPath().length() + 1);
//                if (!model.getModules().contains(relativePath)) {
//                    model.addModule(relativePath);
//                    model.writeTo(pom.getVirtualFile());
//                }
//            } else {
//                throw new IllegalArgumentException("Project must have packaging 'pom' in order to adding modules.");
//            }
//        } catch (IOException e) {
//            throw new ServerException(e);
//        }
//
//    }
//
//    private static void configureClassPath(FolderEntry parentFolder, String path) throws ServerException,
//                                                                                         ForbiddenException,
//                                                                                         ConflictException {
//        String pathToModule = path.contains("/") ? path.substring(path.lastIndexOf("/")) : path;
//        VirtualFileEntry addedModule = parentFolder.getChild(pathToModule);
//
//        if (addedModule == null) {
//            return;
//        }
//
//        MavenClassPathConfigurator.configure((FolderEntry)addedModule);
//    }
//
//    @Override
//    public String getProjectType() {
//        return MavenAttributes.MAVEN_ID;
//    }
//}
