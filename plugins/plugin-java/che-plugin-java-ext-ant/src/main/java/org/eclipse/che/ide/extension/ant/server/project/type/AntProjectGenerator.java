/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.server.project.type;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.ide.ant.tools.buildfile.BuildFileGenerator;
import org.eclipse.che.ide.extension.ant.shared.AntAttributes;

import java.util.Map;

/**
 * Generates Ant-project structure.
 *
 * @author Artem Zatsarynnyi
 */
public class AntProjectGenerator implements CreateProjectHandler {

    @Override
    public String getProjectType() {
        return AntAttributes.ANT_ID;
    }

    @Override
    public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        final String buildXmlContent = new BuildFileGenerator(baseFolder.getName()).getBuildFileContent();
        baseFolder.createFile(AntAttributes.BUILD_FILE, buildXmlContent.getBytes(), "text/xml");

        AttributeValue sourceFolders = attributes.get(AntAttributes.SOURCE_FOLDER);
        if (sourceFolders != null) {
            baseFolder.createFolder(sourceFolders.getString());
        }
        AttributeValue testSourceFolders = attributes.get(AntAttributes.TEST_SOURCE_FOLDER);
        if (testSourceFolders != null) {
            baseFolder.createFolder(testSourceFolders.getString());
        }
    }
}
