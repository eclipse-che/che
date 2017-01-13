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
 * Clone repository.
 *
 * @author andrew00x
 */
@DTO
public interface CloneRequest {
    /** @return URI of repository to be cloned */
    String getRemoteUri();

    void setRemoteUri(String remoteUri);

    CloneRequest withRemoteUri(String remoteUri);

    /** @return list of remote branches to fetch in cloned repository */
    List<String> getBranchesToFetch();

    void setBranchesToFetch(List<String> branchesToFetch);

    CloneRequest withBranchesToFetch(List<String> branchesToFetch);

    /** @return work directory for cloning */
    String getWorkingDir();

    void setWorkingDir(String workingDir);

    CloneRequest withWorkingDir(String workingDir);

    /** @return remote name. If <code>null</code> then 'origin' will be used */
    String getRemoteName();

    void setRemoteName(String remoteName);

    CloneRequest withRemoteName(String remoteName);

    /** @return true if 'Recursive' parameter enabled */
    boolean isRecursive();

    void setRecursive(boolean recursive);

    CloneRequest withRecursive(boolean recursive);

    /**
     * @return time (in seconds) to wait without data transfer occurring before aborting fetching data from remote repository. If 0 then
     * default timeout may be used. This is implementation specific
     */
    int getTimeout();

    void setTimeout(int timeout);

    CloneRequest withTimeout(int timeout);

    /** Returns user name for authentication */
    String getUsername();

    /** Set user name for authentication. */
    void setUsername(String username);

    /** @return {@link CloneRequest} with specified user name for authentication */
    CloneRequest withUsername(String username);

    /** @return password for authentication */
    String getPassword();

    /** Set password for authentication. */
    void setPassword(String password);

    /** @return {@link CloneRequest} with specified password for authentication */
    CloneRequest withPassword(String password);
}
