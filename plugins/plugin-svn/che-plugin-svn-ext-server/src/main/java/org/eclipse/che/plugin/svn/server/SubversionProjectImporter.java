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
package org.eclipse.che.plugin.svn.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.importer.ProjectImporter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;

import org.eclipse.che.plugin.svn.shared.CheckoutRequest;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Implementation of {@link ProjectImporter} for Subversion.
 */
@Singleton
public class SubversionProjectImporter implements ProjectImporter {

    public static final String ID = "subversion";

    private final SubversionApi subversionApi;

    @Inject
    public SubversionProjectImporter(final SubversionApi subversionApi) {
        this.subversionApi = subversionApi;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Import project from Subversion repository URL.";
    }

    @Override
    public void importSources(FolderEntry baseFolder, SourceStorage sourceStorage)
            throws ForbiddenException, ConflictException, UnauthorizedException, IOException, ServerException {
        importSources(baseFolder, sourceStorage, LineConsumerFactory.NULL);
    }

    @Override
    public void importSources(FolderEntry baseFolder, SourceStorage sourceStorage, LineConsumerFactory lineConsumerFactory)
            throws ForbiddenException, ConflictException, UnauthorizedException, IOException, ServerException {
        if (!baseFolder.isFolder()) {
            throw new IOException("Project cannot be imported into \"" + baseFolder.getName() + "\".  "
                                  + "It is not a folder.");
        }

        this.subversionApi.setOutputLineConsumerFactory(lineConsumerFactory);
        subversionApi.checkout(newDto(CheckoutRequest.class)
                                       .withProjectPath(baseFolder.getVirtualFile().toIoFile().getAbsolutePath())
                                       .withUrl(sourceStorage.getLocation())
                                       .withUsername(sourceStorage.getParameters().remove("username"))
                                       .withPassword(sourceStorage.getParameters().remove("password")));
    }

    @Override
    public ImporterCategory getCategory() {
        return ImporterCategory.SOURCE_CONTROL;
    }
}
