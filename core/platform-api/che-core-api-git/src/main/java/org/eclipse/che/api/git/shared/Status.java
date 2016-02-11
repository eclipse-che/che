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

    List<String> getAdded();

    void setAdded(List<String> added);

    List<String> getChanged();

    void setChanged(List<String> changed);

    List<String> getRemoved();

    void setRemoved(List<String> removed);

    List<String> getMissing();

    void setMissing(List<String> missing);

    List<String> getModified();

    void setModified(List<String> modified);

    List<String> getUntracked();

    void setUntracked(List<String> untracked);

    List<String> getUntrackedFolders();

    void setUntrackedFolders(List<String> untrackedFolders);

    List<String> getConflicting();

    void setConflicting(List<String> added);
    
    String getRepositoryState();
    
    void setRepositoryState(String repositoryState);
}