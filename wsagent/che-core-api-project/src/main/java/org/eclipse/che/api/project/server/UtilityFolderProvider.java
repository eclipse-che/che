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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class UtilityFolderProvider {
    public static final String DEFAULT_FOLDER_NAME = ".che";

    @Inject(optional = true)
    @Named("project.hidden.utility.folder.name")
    private String folderName = DEFAULT_FOLDER_NAME;


    /**
     * Gets a folder in a specified project or create a new folder if utility folder is not present and gets it.
     *
     * @param projectFolder
     *         project folder where to get an utility folder
     *
     * @return utility folder
     *
     * @throws ServerException
     * @throws ConflictException
     * @throws ForbiddenException
     */
    public FolderEntry get(FolderEntry projectFolder) throws ServerException, ConflictException, ForbiddenException {
        final FolderEntry folder = projectFolder.getChildFolder(folderName);
        if (folder == null) {
            return projectFolder.createFolder(folderName);
        } else {
            return folder;
        }
    }

    public String getFolderName(){
        return folderName;
    }
}
