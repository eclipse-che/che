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
package org.eclipse.che.api.vfs.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/** @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a> */
@DTO
public interface Lock {
    /** @return the owner */
    String getOwner();

    Lock withOwner(String owner);

    void setOwner(String owner);

    /** @return the lockToken */
    String getLockToken();

    Lock withLockToken(String lockToken);

    void setLockToken(String lockToken);

    /** @return the timeout */
    long getTimeout();

    Lock withTimeout(long timeout);

    void setTimeout(long timeout);
}
