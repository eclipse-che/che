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

import org.eclipse.che.api.git.shared.CloneRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#clone(CloneParams)}.
 *
 * @author Igor Vinokur
 */
public class CloneParams {

    private List<String> branchesToFetch;
    private String       remoteUrl;
    private String       workingDir;
    private String       remoteName;
    private String       username;
    private String       password;
    private int          timeout;
    private boolean      recursive;

    private CloneParams() {
    }

    /**
     * Create new {@link CloneParams} instance.
     *
     * @param remoteUrl
     *         remote url to clone from
     */
    public static CloneParams create(String remoteUrl) {
        return new CloneParams().withRemoteUrl(remoteUrl);
    }

    /** @see CloneRequest#getRemoteUri() */
    public String getRemoteUrl() {
        return remoteUrl;
    }

    /** @see CloneRequest#withRemoteUri(String) */
    public CloneParams withRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
        return this;
    }

    /** @see CloneRequest#getBranchesToFetch() */
    public List<String> getBranchesToFetch() {
        return branchesToFetch == null ? new ArrayList<>() : branchesToFetch;
    }

    /** @see CloneRequest#withBranchesToFetch(List) */
    public CloneParams withBranchesToFetch(List<String> branchesToFetch) {
        this.branchesToFetch = branchesToFetch;
        return this;
    }

    /** @see CloneRequest#getWorkingDir() */
    public String getWorkingDir() {
        return workingDir;
    }

    /** @see CloneRequest#setWorkingDir(String) */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /** @see CloneRequest#withWorkingDir(String) */
    public CloneParams withWorkingDir(String workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    /** @see CloneRequest#getRemoteName() */
    public String getRemoteName() {
        return remoteName;
    }

    /** @see CloneRequest#setRemoteName(String) */
    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    /** @see CloneRequest#withRemoteName(String) */
    public CloneParams withRemoteName(String remoteName) {
        this.remoteName = remoteName;
        return this;
    }

    /** @see CloneRequest#getTimeout() */
    public int getTimeout() {
        return timeout;
    }

    /** @see CloneRequest#withTimeout(int) */
    public CloneParams withTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /** @see CloneRequest#isRecursive() */
    public boolean isRecursive() {
        return recursive;
    }

    /** @see CloneRequest#isRecursive() */
    public CloneParams withRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    /** Returns user name for authentication. */
    public String getUsername() {
        return username;
    }

    /** Returns {@link CloneParams} with specified user name for authentication. */
    public CloneParams withUsername(String username) {
        this.username = username;
        return this;
    }

    /** Returns password for authentication. */
    public String getPassword() {
        return password;
    }

    /** Returns {@link CloneParams} with specified password for authentication. */
    public CloneParams withPassword(String password) {
        this.password = password;
        return this;
    }
}
