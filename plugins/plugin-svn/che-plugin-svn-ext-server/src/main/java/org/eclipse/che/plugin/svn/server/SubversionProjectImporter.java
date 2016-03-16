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
import org.eclipse.che.dto.server.DtoFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.Map;

import org.eclipse.che.plugin.svn.server.credentials.CredentialsException;
import org.eclipse.che.plugin.svn.server.credentials.CredentialsProvider;
import org.eclipse.che.plugin.svn.shared.CheckoutRequest;
import org.eclipse.che.plugin.svn.shared.ImportParameterKeys;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ProjectImporter} for Subversion.
 */
@Singleton
public class SubversionProjectImporter implements ProjectImporter {

    public static final String ID = "subversion";

    private final SubversionApi subversionApi;

    private final CredentialsProvider credentialsProvider;

    @Inject
    public SubversionProjectImporter(final CredentialsProvider credentialsProvider,
                                     final SubversionApi subversionApi) {
        this.subversionApi = subversionApi;
        this.credentialsProvider = credentialsProvider;
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

        final String location = sourceStorage.getLocation();
        final Map<String,String> parameters = sourceStorage.getParameters();

        // can't store the credentials yet, no workspaceId/projectId
        String[] credentials = null;
        String username = "";
        String password = "";
        if (parameters != null) {
            String paramUsername = parameters.get(ImportParameterKeys.PARAMETER_USERNAME);
            if (paramUsername != null) {
                username = paramUsername;
            }
            String paramPassword = parameters.get(ImportParameterKeys.PARAMETER_PASSWORD);
            if (paramPassword != null) {
                password = paramPassword;
            }
            credentials = new String[]{username, password};
            try {
                this.credentialsProvider.storeCredential(location, new CredentialsProvider.Credentials(username, password));
            } catch (final CredentialsException e) {
                LoggerFactory.getLogger(SubversionProjectImporter.class.getName())
                             .warn("Could not store credentials - try to continue anyway." + e.getMessage());
            }
        }

        this.subversionApi.setOutputLineConsumerFactory(lineConsumerFactory);

        // Perform checkout
        this.subversionApi.checkout(DtoFactory.getInstance()
                                              .createDto(CheckoutRequest.class)
                                              .withProjectPath(baseFolder.getVirtualFile().toIoFile().getAbsolutePath())
                                              .withUrl(location),
                                    credentials);
    }

    @Override
    public ImporterCategory getCategory() {
        return ImporterCategory.SOURCE_CONTROL;
    }
}
