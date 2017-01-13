/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Request to add content of working tree to Git index. This action prepares content to next commit.
 *
 * @author andrew00x
 */
@DTO
public interface AddRequest {
    /** Default file pattern that will be used if {@link #getFilePattern} is not set. All content of working tree will be added in index. */
    List<String> DEFAULT_PATTERN = new ArrayList<>(singletonList("."));

    /** @return files to add content from */
    List<String> getFilePattern();
    
    void setFilePattern(List<String> pattern);
    
    AddRequest withFilePattern(List<String> filePattern);

    /**
     * @return if <code>true</code> than never stage new files, but stage modified new contents of tracked files. It will remove files from
     *         the index if the corresponding files in the working tree have been removed. If <code>false</code> then new files and
     *         modified
     *         files added to the index.
     */
    boolean isUpdate();
    
    void setUpdate(boolean isUpdate);
    
    AddRequest withUpdate(boolean isUpdate);
}
