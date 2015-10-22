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
package org.eclipse.che.plugin.internal.resolver;

import org.eclipse.che.plugin.internal.api.PluginResolver;
import org.eclipse.che.plugin.internal.api.PluginResolverException;
import org.eclipse.che.plugin.internal.api.PluginResolverNotFoundException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * LocalUpload Resolve will resolve jars that have been uploaded to the che instance.
 * @author Florent Benoit
 */
@Singleton
public class LocalUploadResolver implements PluginResolver {

    /**
     * Constant used to describe the path to the servlet upload directory when uploading files.
     */
    public static final String CHE_SERVLET_UPLOAD_DIRECTORY = "che.servlet.upload.directory";

    /**
     * Folder used to store uploaded files.
     */
    private Path uploadFolder;

    /**
     * Create a new resolver using the local upload folder.
     * @param uploadFolder the path to store the uploaded files.
     */
    @Inject
    public LocalUploadResolver(@Named(CHE_SERVLET_UPLOAD_DIRECTORY) String uploadFolder) {
        this.uploadFolder = Paths.get(uploadFolder);
    }

    /**
     * Resolve provided url
     * @param pluginRef reference of the plugin
     * @return
     * @throws PluginResolverException
     */
    public Path download(@NotNull final String pluginRef) throws PluginResolverException, PluginResolverNotFoundException {
        String path = pluginRef;
        if (path.startsWith(getProtocol())) {
            path = path.substring(getProtocol().length());
        }

        // resolve the file from the upload folder
        Path uploadedFile = this.uploadFolder.resolve(path);

        if (Files.notExists(uploadedFile)) {
            throw new PluginResolverNotFoundException(String.format("The provided file %s was not found as uploaded file",
                                                                    uploadedFile.toString()));
        }
        return uploadedFile;

    }

    @Override
    public String getProtocol() {
        return "upload:";
    }


}
