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

/** @author Dmitriy Vyshinskiy */
@DTO
public interface Status {
    boolean isClean();

    void setClean(boolean isClean);

    StatusFormat getFormat();

    void setFormat(StatusFormat format);

    String getBranchName();

    void setBranchName(String branchName);

    /**
     * New files that are staged in index.
     */
    List<String> getAdded();

    void setAdded(List<String> added);

    /**
     * New files that are not staged in index.
     */
    List<String> getUntracked();

    void setUntracked(List<String> untracked);

    /**
     * Modified files that are staged in index.
     */
    List<String> getChanged();

    void setChanged(List<String> changed);

    /**
     * Modified files that are not staged in index.
     */
    List<String> getModified();

    void setModified(List<String> modified);

    /**
     * Deleted files that are staged in index.
     */
    List<String> getRemoved();

    void setRemoved(List<String> removed);

    /**
     * Deleted files that are not staged in index.
     */
    List<String> getMissing();

    void setMissing(List<String> missing);

    /**
     * Folders that contain only untracked files.
     * @see #getUntracked()
     */
    List<String> getUntrackedFolders();

    void setUntrackedFolders(List<String> untrackedFolders);

    /**
     * Files that have conflicts.
     */
    List<String> getConflicting();

    void setConflicting(List<String> added);
    
    String getRepositoryState();
    
    void setRepositoryState(String repositoryState);
}