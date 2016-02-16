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
//package org.eclipse.che.ide.extension.maven.server.projecttype.handler;
//
//import org.eclipse.che.api.core.ConflictException;
//import org.eclipse.che.api.core.ForbiddenException;
//import org.eclipse.che.api.core.ServerException;
//import org.eclipse.che.api.core.model.workspace.ProjectConfig;
//import org.eclipse.che.api.project.server.FolderEntry;
//import org.eclipse.che.api.project.server.VirtualFileEntry;
//import org.eclipse.che.api.project.server.handlers.RemoveModuleHandler;
//import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
//import org.eclipse.che.ide.maven.tools.Model;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//
///**
// * A handler for handling the removal of a maven module.
// *
// * @author Roman Nikitenko
// */
//public class RemoveMavenModuleHandler implements RemoveModuleHandler {
//
//    private final static Logger logger = LoggerFactory.getLogger(RemoveMavenModuleHandler.class);
//
//    @Override
//    public String getProjectType() {
//        return MavenAttributes.MAVEN_ID;
//    }
//
//    @Override
//    public void onRemoveModule(FolderEntry parentFolder, ProjectConfig moduleConfig) throws ForbiddenException,
//                                                                                            ConflictException,
//                                                                                            ServerException {
//        if (!MavenAttributes.MAVEN_ID.equals(moduleConfig.getType())) {
//            logger.warn("Module isn't Maven module");
//            throw new IllegalArgumentException("Module isn't Maven module");
//        }
//        VirtualFileEntry pom = parentFolder.getChild("pom.xml");
//        if (pom == null) {
//            return;
//        }
//        try {
//            Model model = Model.readFrom(pom.getVirtualFile());
//            String moduleName = moduleConfig.getName();
//            if (model.getModules().contains(moduleName)) {
//                model.removeModule(moduleName);
//                model.writeTo(pom.getVirtualFile());
//            }
//        } catch (IOException e) {
//            throw new ServerException(e);
//        }
//    }
//}
