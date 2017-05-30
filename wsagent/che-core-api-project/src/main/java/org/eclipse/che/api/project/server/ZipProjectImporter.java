/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.project.server.importer.ProjectImporter;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import static org.eclipse.che.api.project.shared.Constants.ZIP_IMPORTER_ID;

/**
 * @author Vitaly Parfonov
 */
@Singleton
public class ZipProjectImporter implements ProjectImporter {

    @Override
    public String getId() {
        return ZIP_IMPORTER_ID;
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Import project from ZIP archive under a public URL.";
    }

    @Override
    public void importSources(FolderEntry baseFolder, SourceStorage storage) throws ForbiddenException,
                                                                                    ConflictException,
                                                                                    IOException,
                                                                                    ServerException {
        importSources(baseFolder, storage, LineConsumerFactory.NULL);
    }

    @Override
    public void importSources(FolderEntry baseFolder,
                              SourceStorage storage,
                              LineConsumerFactory importOutputConsumerFactory) throws ForbiddenException,
                                                                                      ConflictException,
                                                                                      IOException,
                                                                                      ServerException {
        URL url;
        String location = storage.getLocation();
        if (location.startsWith("http://") || location.startsWith("https://")) {
            url = new URL(location);
        } else {
            url = Thread.currentThread().getContextClassLoader().getResource(location);
            if (url == null) {
                final java.io.File file = new java.io.File(location);
                if (file.exists()) {
                    url = file.toURI().toURL();
                }
            }
        }

        if (url == null) {
            throw new IOException(String.format("Can't find %s", location));
        }

        Map<String, String> parameters = storage.getParameters();
        try (InputStream zip = url.openStream()) {
            int stripNumber = 0;
            if (parameters != null && parameters.containsKey("skipFirstLevel")) {
                stripNumber = Boolean.parseBoolean(parameters.get("skipFirstLevel")) ? 1 : 0;
            }
            baseFolder.getVirtualFile().unzip(zip, true, stripNumber);
        }
    }

    @Override
    public ImporterCategory getCategory() {
        return ImporterCategory.ARCHIVE;
    }
}
