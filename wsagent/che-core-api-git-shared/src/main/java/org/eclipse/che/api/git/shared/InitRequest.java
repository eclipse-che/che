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
 * Request to init git repository.
 *
 * @author andrew00x
 */
@DTO
public interface InitRequest extends GitRequest {
    /** @return working directory for new git repository */
    String getWorkingDir();

    void setWorkingDir(String workingDir);

    InitRequest withWorkingDir(String workingDir);
    
    /** @return <code>true</code> then bare repository created */
    boolean isBare();
    
    void setBare(boolean bare);
    
    InitRequest withBare(boolean bare);
}
