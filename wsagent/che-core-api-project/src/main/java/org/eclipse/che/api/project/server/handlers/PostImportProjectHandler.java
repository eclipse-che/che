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
package org.eclipse.che.api.project.server.handlers;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;

import java.io.IOException;

/**
 * @author Vitaly Parfonov
 */
public interface PostImportProjectHandler extends ProjectHandler {

    void onProjectImported(FolderEntry projectFolder) throws ForbiddenException,
                                                             ConflictException,
                                                             ServerException,
                                                             IOException,
                                                             NotFoundException;
}
