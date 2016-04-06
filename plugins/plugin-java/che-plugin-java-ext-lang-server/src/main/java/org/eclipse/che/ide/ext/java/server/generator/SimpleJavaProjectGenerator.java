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
package org.eclipse.che.ide.ext.java.server.generator;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.ide.ext.java.shared.Constants;

import java.util.Map;

import static org.eclipse.che.ide.ext.java.shared.Constants.LIBRARY_FOLDER;
import static org.eclipse.che.ide.ext.java.shared.Constants.SIMPLE_JAVA_PROJECT_ID;

/**
 * Generates new project which contains file with default content.
 *
 * @author Valeriy Svydenko
 */
public class SimpleJavaProjectGenerator implements CreateProjectHandler {

    private static final String FILE_NAME    = "Main.java";
    private static final String PACKAGE_NAME = "/com/company";

    @Override
    public void onCreateProject(FolderEntry baseFolder,
                                Map<String, AttributeValue> attributes,
                                Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {

        FolderEntry sourceFolder = baseFolder.createFolder(Constants.DEFAULT_SOURCE_FOLDER_VALUE);
        FolderEntry defaultPackage = sourceFolder.createFolder(PACKAGE_NAME);

        defaultPackage.createFile(FILE_NAME, getClass().getClassLoader().getResourceAsStream("files/main_class_content"));

        baseFolder.createFolder(LIBRARY_FOLDER);
    }

    @Override
    public String getProjectType() {
        return SIMPLE_JAVA_PROJECT_ID;
    }
}
