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
package org.eclipse.che.api.git.params;

import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.ResetRequest.ResetType;

import java.util.ArrayList;
import java.util.List;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#reset(ResetParams)}.
 *
 * @author Igor Vinokur
 */
public class ResetParams {

    private String       commit;
    private ResetType    type;
    private List<String> filePattern;

    private ResetParams() {
    }

    /**
     * Create new {@link ResetParams} instance.
     *
     * @param commit
     *         hash of commit to which current head should be reset
     * @param type
     *         type of reset
     */
    public static ResetParams create(String commit, ResetType type) {
        return new ResetParams().withCommit(commit).withType(type);
    }

    /** @see ResetRequest#getCommit() */
    public String getCommit() {
        return commit;
    }

    /** @see ResetRequest#withCommit(String) */
    public ResetParams withCommit(String commit) {
        this.commit = commit;
        return this;
    }

    /** @see ResetRequest#getType() */
    public ResetType getType() {
        return type;
    }

    public ResetParams withType(ResetType type) {
        this.type = type;
        return this;
    }

    /** @see ResetRequest#getFilePattern() */
    public List<String> getFilePattern() {
        return filePattern == null ? new ArrayList<>() : filePattern;
    }

    /** @see ResetRequest#withFilePattern(List) */
    public ResetParams withFilePattern(List<String> filePattern) {
        this.filePattern = filePattern;
        return this;
    }
}
