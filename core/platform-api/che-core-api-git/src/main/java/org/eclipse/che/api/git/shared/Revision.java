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
 * Describe single commit.
 *
 * @author andrew00x
 */
@DTO
public interface Revision {
    /**
     * Parameter which shows that this revision is a fake revision (i.e. TO for Exception)
     *
     * @return
     */
    boolean isFake();

    void setFake(boolean fake);

    /** @return branch name */
    String getBranch();
    
    void setBranch(String branch);
    
    Revision withBranch(String branch);

    /** @return commit id */
    String getId();
    
    void setId(String id);
    
    Revision withId(String id);

    /** @return commit message */
    String getMessage();
    
    void setMessage(String message);
    
    Revision withMessage(String message);

    /** @return time of commit */
    long getCommitTime();
    
    Revision withCommitTime(long time);

    /** @return committer */
    GitUser getCommitter();
    
    Revision withCommitter(GitUser user);
}