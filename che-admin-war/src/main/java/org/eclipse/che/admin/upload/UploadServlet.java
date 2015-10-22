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
package org.eclipse.che.admin.upload;

import org.eclipse.che.admin.deploy.Constants;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Upload servlet allowing to upload files to the server. Request should contain a part named 'uploadedFile' for the file.
 * @author Florent Benoit
 */
@MultipartConfig
@Singleton
public class UploadServlet extends HttpServlet {

    /**
     * Serializable class;
     */
    private static final long serialVersionUID = -687991492884005033L;

    /**
     * Folder used to store uploaded files.
     */
    private String uploadFolder;

    /**
     * Create a new upload folder.
     * @param uploadFolder the path to store the uploaded files.
     */
    @Inject
    public UploadServlet(@Named(Constants.CHE_SERVLET_UPLOAD_DIRECTORY) String uploadFolder) {
        this.uploadFolder = uploadFolder;
    }

    /**
     * Handle the POST request only by allowing to upload files.
     * @param request the request with a part named 'uploadedFile'
     * @param response empty if file is uploaded
     * @throws ServletException if there is a problem while uploading
     * @throws IOException if there is a problem while uploading
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getParts().stream().forEach((filePart -> {
            try {
                String fileName = filePart.getSubmittedFileName();
                InputStream inputStream = filePart.getInputStream();
                Path path = Paths.get(uploadFolder).resolve(fileName);
                Files.createDirectories(path.getParent());
                Files.copy(inputStream, path);

            } catch (IOException e) {
                throw new RuntimeException("Unable to upload files", e);
            }
        }));
    }


}