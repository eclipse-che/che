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

import org.eclipse.che.api.git.shared.CommitRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#commit(CommitParams)}.
 *
 * @author Igor Vinokur
 */
public class CommitParams {

    private List<String> files;
    private String       message;
    private boolean      isAll;
    private boolean      isAmend;

    private CommitParams() {
    }

    /**
     * Create new {@link CommitParams} instance.
     *
     * @param message
     *         commit message
     */
    public static CommitParams create(String message) {
        return new CommitParams().withMessage(message);
    }

    /** @see CommitRequest#getMessage() */
    public String getMessage() {
        return message;
    }

    /** @see CommitRequest#withMessage(String) */
    public CommitParams withMessage(String message) {
        this.message = message;
        return this;
    }

    /** @see CommitRequest#getFiles() */
    public List<String> getFiles() {
        return files == null ? new ArrayList<>() : files;
    }

    /** @see CommitRequest#withFiles(List) */
    public CommitParams withFiles(List<String> files) {
        this.files = files;
        return this;
    }

    /** @see CommitRequest#isAll() */
    public boolean isAll() {
        return isAll;
    }

    /** @see CommitRequest#withAll(boolean) */
    public CommitParams withAll(boolean all) {
        isAll = all;
        return this;
    }

    /** @see CommitRequest#isAmend() */
    public boolean isAmend() {
        return isAmend;
    }

    /** @see CommitRequest#withAmend(boolean) */
    public CommitParams withAmend(boolean amend) {
        isAmend = amend;
        return this;
    }
}
