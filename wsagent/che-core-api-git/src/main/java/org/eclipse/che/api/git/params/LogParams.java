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
package org.eclipse.che.api.git.params;

import java.util.ArrayList;
import java.util.List;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#log(LogParams)}.
 *
 * @author Igor Vinokur
 */
public class LogParams {

    private List<String> fileFilter;
    private String       revisionRangeSince;
    private String       revisionRangeUntil;

    private LogParams() {
    }

    /**
     * Create new {@link LogParams} instance with empty parameters
     */
    public static LogParams create() {
        return new LogParams();
    }

    /** Filter revisions list by range of files. */
    public List<String> getFileFilter() {
        return fileFilter == null ? new ArrayList<>() : fileFilter;
    }

    public LogParams withFileFilter(List<String> fileFilter) {
        this.fileFilter = fileFilter;
        return this;
    }

    /** @return revision range since */
    public String getRevisionRangeSince() {
        return revisionRangeSince;
    }

    public LogParams withRevisionRangeSince(String revisionRangeSince) {
        this.revisionRangeSince = revisionRangeSince;
        return this;
    }

    /** @return revision range until */
    public String getRevisionRangeUntil() {
        return revisionRangeUntil;
    }

    public LogParams withRevisionRangeUntil(String revisionRangeUntil) {
        this.revisionRangeUntil = revisionRangeUntil;
        return this;
    }
}
