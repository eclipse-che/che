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
public interface File extends Item {
    /** @return version id */
    String getVersionId();

    File withVersionId(String versionId);

    void setVersionId(String versionId);

    /** @return content length */
    long getLength();

    File withLength(long length);

    void setLength(long length);

    /** @return date of last modification */
    long getLastModificationDate();

    File withLastModificationDate(long lastModificationDate);

    void setLastModificationDate(long lastModificationDate);

    /** @return <code>true</code> if object locked and <code>false</code> otherwise */
    boolean isLocked();

    File withLocked(boolean locked);

    void setLocked(boolean locked);
}
