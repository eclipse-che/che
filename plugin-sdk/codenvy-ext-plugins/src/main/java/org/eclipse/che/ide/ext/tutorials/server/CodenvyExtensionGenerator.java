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
package org.eclipse.che.ide.ext.tutorials.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.MavenProjectGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * @author gazarenkov
 */
@Singleton
public class CodenvyExtensionGenerator implements CreateProjectHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CodenvyExtensionGenerator.class);

    private final MavenProjectGenerator mavenProjectGenerator;

    @Inject
    public CodenvyExtensionGenerator(MavenProjectGenerator mavenProjectGenerator) {
        this.mavenProjectGenerator = mavenProjectGenerator;
    }

    @Override
    public String getProjectType() {
        return "codenvy_extension";
    }

    @Override
    public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes,
                                Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {
        mavenProjectGenerator.onCreateProject(baseFolder, attributes, options);

    }

}
