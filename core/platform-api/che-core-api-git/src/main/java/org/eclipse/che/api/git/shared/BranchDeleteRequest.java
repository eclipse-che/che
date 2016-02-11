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
 * Request to delete branch.
 *
 * @author andrew00x
 */
@DTO
public interface BranchDeleteRequest extends GitRequest {
    /** @return name of branch to delete */
    String getName();
    
    void setName(String name);
    
    BranchDeleteRequest withName(String name);

    /** @return if <code>true</code> then delete branch {@link #name} even if it is not fully merged */
    boolean isForce();
    
    void setForce(boolean isForce);
    
    BranchDeleteRequest withForce(boolean force);
}