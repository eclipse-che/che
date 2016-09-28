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

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.readFileToString;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class ReadmeInjectionPerformer {
    private final UtilityFolderProvider utilityFolderProvider;
    private final ReadmeContentProvider readmeContentProvider;

    @Inject
    public ReadmeInjectionPerformer(UtilityFolderProvider utilityFolderProvider, ReadmeContentProvider readmeContentProvider) {
        this.utilityFolderProvider = utilityFolderProvider;
        this.readmeContentProvider = readmeContentProvider;
    }

    public void injectReadmeTo(FolderEntry projectFolder) throws ServerException,
                                                                 IOException,
                                                                 ConflictException,
                                                                 ForbiddenException {
        final FolderEntry folder = utilityFolderProvider.get(projectFolder);
        final byte[] content = readmeContentProvider.get();
        final String readmeName = readmeContentProvider.getFilename();

        folder.createFile(readmeName, content);
    }
}
