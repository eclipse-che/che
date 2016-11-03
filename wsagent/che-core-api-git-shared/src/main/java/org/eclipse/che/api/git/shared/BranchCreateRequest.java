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
 * Request to create new branch.
 *
 * @author andrew00x
 */
@DTO
public interface BranchCreateRequest {
    /** @return name of branch to be created */
    String getName();
    
    void setName(String name);
    
    BranchCreateRequest withName(String name);

    /** @return hash commit from which to start new branch. If <code>null</code> HEAD will be used */
    String getStartPoint();
    
    void setStartPoint(String startPoint);
    
    BranchCreateRequest withStartPoint(String startPoint);
}
