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

/**
 * Request to pull (fetch and merge) changes from remote repository to local branch.
 *
 * @author andrew00x
 */
@DTO
public interface PullRequest {
    /** @return refspec to fetch */
    String getRefSpec();
    
    void setRefSpec(String refSpec);
    
    PullRequest withRefSpec(String refSpec);

    /** @return remote name. If <code>null</code> then 'origin' will be used */
    String getRemote();
    
    void setRemote(String remote);
    
    PullRequest withRemote(String remote);

    /** @return time (in seconds) to wait without data transfer occurring before aborting fetching data from remote repository */
    int getTimeout();
    
    void setTimeout(int timeout);
    
    PullRequest withTimeout(int timeout);

    /** Returns user name for authentication */
    String getUsername();

    /** Set user name for authentication. */
    void setUsername(String username);

    /** @return {@link PullRequest} with specified user name for authentication */
    PullRequest withUsername(String username);

    /** @return password for authentication */
    String getPassword();

    /** Set password for authentication. */
    void setPassword(String password);

    /** @return {@link PullRequest} with specified password for authentication */
    PullRequest withPassword(String password);
}