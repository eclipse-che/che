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

package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Created by I048384 on 24/05/2016.
 */

@DTO
public interface DiffCommitFile {

    /** @return the file change type*/
    String getChangeType();

    /** set the file change type*/
    void setChangeType(String type);

    /**
     * Create a DiffCommitFile object based on a given file change type.
     *
     * @param type
     *         file change type
     * @return a DiffCommitFile object
     */
    DiffCommitFile withChangeType(String type);

    /** @return the file previous location*/
    String getOldPath();

    /** set the file previous location*/
    void setOldPath(String oldPath);

    /**
     * Create a DiffCommitFile object based on a given file previous location.
     *
     * @param oldPath
     *         file previous location
     * @return a DiffCommitFile object
     */
    DiffCommitFile withOldPath(String oldPath);

    /** @return the file new location*/
    String getNewPath();

    /** set the file new location*/
    void setNewPath(String newPath);

    /**
     * Create a DiffCommitFile object based on a given file new location.
     *
     * @param newPath
     *         file new location
     * @return a DiffCommitFile object
     */
    DiffCommitFile withNewPath(String newPath);
}
