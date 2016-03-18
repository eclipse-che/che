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
 * Show information about files in the index and the working tree.
 *
 * @author Sergii Kabashniuk
 */
@DTO
public interface LsFilesRequest {
    /**
     * Show other (i.e. untracked) files in the output
     */
    boolean isOthers();

    void setOthers(boolean isOthers);

    LsFilesRequest withOthers(boolean isOthers);

    /**
     * Show modified files in the output
     */
    boolean isModified();

    void setModified(boolean isModified);

    LsFilesRequest withModified(boolean isModified);

    /**
     * Show staged contents' object name, mode bits and stage number in the output.
     */
    boolean isStaged();

    void setStaged(boolean isStaged);

    LsFilesRequest withStaged(boolean isStaged);

    /**
     * Show cached files in the output (default)
     */
    boolean isCached();

    void setCached(boolean isCached);

    LsFilesRequest withCached(boolean isCached);


    /**
     * Show deleted files in the output
     */
    boolean isDeleted();

    void setDeleted(boolean isDeleted);

    LsFilesRequest withDeleted(boolean isDeleted);

    /**
     * Show only ignored files in the output. When showing files in the index,
     * print only those matched by an exclude pattern.
     * When showing "other" files, show only those matched by an exclude pattern.
     */
    boolean isIgnored();

    void setIgnored(boolean isIgnored);

    LsFilesRequest withIgnored(boolean isIgnored);

    /**
     * Add the standard Git exclusions: .git/info/exclude,
     * .gitignore in each directory, and the user’s global exclusion file.
     */
    boolean isExcludeStandard();

    void setExcludeStandard(boolean isExcludeStandard);

    LsFilesRequest withExcludeStandard(boolean isExcludeStandard);

}
