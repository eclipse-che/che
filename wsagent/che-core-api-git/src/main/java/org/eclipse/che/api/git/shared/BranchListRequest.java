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
 * @author andrew00x
 */
@DTO
public interface BranchListRequest extends GitRequest {
    /**
     * Show both remote and local branches. <br/>
     * Corresponds to -a option in C git.
     */
    public static final String LIST_ALL    = "a";
    /**
     * Show both remote branches. <br/>
     * Corresponds to -r option in C git.
     */
    public static final String LIST_REMOTE = "r";
    public static final String LIST_LOCAL  = null;

    /** @return branches list mode */
    String getListMode();
    
    void setListMode(String listMode);
    
    BranchListRequest withListMode(String listMode);
}