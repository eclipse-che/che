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
 * Request to move or rename a file or directory.
 *
 * @author andrew00x
 */
@DTO
public interface MoveRequest {
    /** @return source */
    String getSource();
    
    void setSource(String source);

    /** @return target */
    String getTarget();
    
    void setTarget(String target);
}