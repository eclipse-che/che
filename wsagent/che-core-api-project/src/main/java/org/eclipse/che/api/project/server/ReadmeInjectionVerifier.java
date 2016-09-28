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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.io.File.separator;
import static org.eclipse.che.api.project.server.UtilityFolderProvider.DEFAULT_FOLDER_NAME;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class ReadmeInjectionVerifier {
    private static final Logger LOG = LoggerFactory.getLogger(ReadmeInjectionVerifier.class);

    public static final String DEFAULT_README_NAME = "README.md";

    @Inject(optional = true)
    @Named("project.default.readme.filename")
    private String readmeName = DEFAULT_README_NAME;

    public boolean isRootProject(FolderEntry projectFolder) {
        final Path projectFolderPath = projectFolder.getPath();
        final Path projectFolderParentPath = projectFolderPath.getParent();

        if (projectFolderPath.isRoot() || projectFolderParentPath == null) {
            final String msg = "Project folder cannot be a root folder";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return projectFolderParentPath.isRoot();
    }

    public boolean isReadmeNotPresent(FolderEntry projectFolder) throws ServerException {
        final String defaultReadmeName = DEFAULT_FOLDER_NAME + separator + readmeName;
        return projectFolder.getChild(readmeName) == null &&
               projectFolder.getChild(defaultReadmeName) == null;
    }
}

