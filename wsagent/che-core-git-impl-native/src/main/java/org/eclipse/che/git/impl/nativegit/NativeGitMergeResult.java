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
package org.eclipse.che.git.impl.nativegit;


import org.eclipse.che.api.git.shared.MergeResult;

import java.util.List;

/**
 * NativeGit implementation of org.exoplatform.ide.git.shared.MergeResult
 *
 * @author <a href="maito:evoevodin@codenvy.com">Eugene Voevodin</a>
 */
public class NativeGitMergeResult implements MergeResult {

    private String head;
    private MergeStatus status;
    private List<String> conflicts;
    private List<String> failed;
    private List<String> mergedCommits;

    /**
     * @param mergedCommits commits that was merged
     */
    public void setMergedCommits(List<String> mergedCommits) {
        this.mergedCommits = mergedCommits;
    }

    /**
     * @param failed file names that failed after merge
     */
    public void setFailed(List<String> failed) {
        this.failed = failed;
    }

    /**
     * @param conflicts file names that conflicting after merge
     */
    public void setConflicts(List<String> conflicts) {
        this.conflicts = conflicts;
    }

    /**
     * @param status status after merge
     */
    public void setStatus(MergeStatus status) {
        this.status = status;
    }

    /**
     * @param head head after merge
     */
    public void setHead(String head) {
        this.head = head;
    }

    @Override
    public String getNewHead() {
        return head;
    }

    @Override
    public MergeStatus getMergeStatus() {
        return status;
    }

    @Override
    public List<String> getMergedCommits() {
        return mergedCommits;
    }

    @Override
    public List<String> getConflicts() {
        return conflicts;
    }

    @Override
    public List<String> getFailed() {
        return failed;
    }
}
