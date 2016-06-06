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
package org.eclipse.che.plugin.jsonexample.generator;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.eclipse.che.plugin.jsonexample.shared.Constants.JSON_EXAMPLE_PROJECT_TYPE_ID;

/**
 * Generates a new project which contains a package.json with default content
 * and a default person.json file within an myJsonFiles folder.
 */
public class JsonExampleCreateProjectHandler implements CreateProjectHandler {

    private static final String FILE_NAME = "package.json";

    @Override
    public void onCreateProject(FolderEntry baseFolder,
                                Map<String, AttributeValue> attributes,
                                Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {

        try (InputStream packageJson = getClass().getClassLoader().getResourceAsStream("files/default_package");
             InputStream personJson = getClass().getClassLoader().getResourceAsStream("files/default_person")) {
            FolderEntry myJsonFiles = baseFolder.createFolder("myJsonFiles");
            baseFolder.createFile(FILE_NAME, packageJson);
            myJsonFiles.createFile("person.json", personJson);
        } catch (IOException ioEx) {
            throw new ServerException(ioEx.getLocalizedMessage(), ioEx);
        }
    }

    @Override
    public String getProjectType() {
        return JSON_EXAMPLE_PROJECT_TYPE_ID;
    }
}
