/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.server.projecttype;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.plugin.composer.shared.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ComposerProjectGenerator implements CreateProjectHandler {

    @Inject
    private VirtualFileSystemProvider virtualFileSystemProvider;

    @Override
    public void onCreateProject(Path projectPath, Map<String, AttributeValue> attributes, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        AttributeValue packageName = attributes.get(Constants.PACKAGE);
        if (packageName == null) {
            throw new ServerException("Missed some required options (package)");
        }

        VirtualFileSystem vfs = virtualFileSystemProvider.getVirtualFileSystem();
        String projectAbsolutePath = new File(vfs.getRoot().toIoFile(), projectPath.toString()).toString();
        
        String[] commandLine = { "composer", "create-project", packageName.getString(), projectAbsolutePath, "--no-install" };
        try {
            ComposerCommandExecutor.execute(commandLine, null);
        } catch (TimeoutException | IOException | InterruptedException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public String getProjectType() {
        return Constants.COMPOSER_PROJECT_TYPE_ID;
    }

}
