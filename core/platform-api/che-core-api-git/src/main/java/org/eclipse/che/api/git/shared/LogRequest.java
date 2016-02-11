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

import java.util.List;

import org.eclipse.che.dto.shared.DTO;

/**
 * Request to get commit logs.
 *
 * @author andrew00x
 */
@DTO
public interface LogRequest extends GitRequest {
    /** Filter revisions list by range of files. */
    List<String> getFileFilter();

    void setFileFilter(List<String> fileFilter);

    LogRequest withFileFilter(List<String> fileFilter);
    
    /** @return revision range since */
    String getRevisionRangeSince();
    /** @return revision range since */
    String getRevisionRangeUntil();
    
    void setRevisionRangeSince(String revisionRangeSince);
    void setRevisionRangeUntil(String revisionRangeUntil);	
    // private List<String> fileFilter;
    // private boolean noRenames = true;
    // private int renameLimit;
}