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

import java.util.List;

/**
 * @author andrew00x
 */
@DTO
public interface MergeResult {
    public enum MergeStatus {
        FAST_FORWARD("Fast-forward"),
        ALREADY_UP_TO_DATE("Already up-to-date"),
        FAILED("Failed"),
        MERGED("Merged"),
        CONFLICTING("Conflicting"),
        NOT_SUPPORTED("Not-yet-supported");

        private final String value;

        private MergeStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /** @return head after the merge */
    String getNewHead();

    /** @return status of merge */
    MergeStatus getMergeStatus();

    /** @return merged commits */
    List<String> getMergedCommits();

    /** @return files that has conflicts. May return <code>null</code> or empty array if there is no conflicts */
    List<String> getConflicts();

    /** @return files that failed to merge (not files that has conflicts). */
    List<String> getFailed();
}