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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;
import java.util.List;

/**
 * Request to commit current state of index in new commit.
 *
 * @author andrew00x
 */
@DTO
public interface CommitRequest {
    /** @return commit message */
    String getMessage();

    void setMessage(String message);

    CommitRequest withMessage(String message);

    /** @return <code>true</code> if need automatically stage files that have been modified and deleted */
    boolean isAll();

    void setAll(boolean isAll);

    CommitRequest withAll(boolean all);

    /** @return <code>true</code> in case when commit is amending a previous commit. */
    boolean isAmend();

    void setAmend(boolean isAmend);

    CommitRequest withAmend(boolean amend);

    /**
     * Set the files to be commited (ignoring index).
     * 
     * @param files the files to commit
     */
    void setFiles(List<String> files);

    /**
     * Returns the files to be commited (ignoring index).
     * 
     * @return the commited files
     */
    List<String> getFiles();

    /**
     * Set the files to be commited (ignoring index).
     * 
     * @param files the files to commit
     * @return this object
     */
    CommitRequest withFiles(List<String> files);
}