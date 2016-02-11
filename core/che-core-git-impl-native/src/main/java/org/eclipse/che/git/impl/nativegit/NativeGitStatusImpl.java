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

import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.InfoPage;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.git.impl.nativegit.commands.StatusCommand;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * NativeGit implementation {@link Status}
 *
 * @author Eugene Voevodin
 */
public class NativeGitStatusImpl implements Status, InfoPage {

    private String branchName;

    private StatusFormat format;

    private boolean clean;

    private List<String> added;

    private List<String> changed;

    private List<String> removed;

    private List<String> missing;

    private List<String> modified;

    private List<String> untracked;

    private List<String> untrackedFolders;

    private List<String> conflicting;

    private NativeGit nativeGit;
    
    private String repositoryState;

    /**
     * @param branchName
     *         current repository branch name
     * @param nativeGit
     *         git commands factory
     * @param format
     *         the output format for the status
     * @throws GitException
     *         when any error occurs
     */
    public NativeGitStatusImpl(String branchName, NativeGit nativeGit, StatusFormat format) throws GitException {
        this.branchName = branchName;
        this.format = format;
        this.nativeGit = nativeGit;
        load();
    }

    /** @see InfoPage#writeTo(java.io.OutputStream) */
    @Override
    public void writeTo(OutputStream out) throws IOException {
        StatusCommand status = nativeGit.createStatusCommand().setFormat(format);
        try {
            status.execute();
            out.write(status.getText().getBytes());
        } catch (GitException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /** @see Status#isClean() */
    @Override
    public boolean isClean() {
        return clean;
    }

    /** @see Status#setClean(boolean) */
    @Override
    public void setClean(boolean clean) {
        this.clean = clean;
    }

    /** @see Status#getFormat() */
    @Override
    public StatusFormat getFormat() {
        return this.format;
    }

    /** @see Status#setFormat(StatusFormat) */
    @Override
    public void setFormat(final StatusFormat format) {
        this.format = format;
    }

    /** @see Status#getBranchName() */
    @Override
    public String getBranchName() {
        return branchName;
    }

    /** @see Status#setBranchName(String) */
    @Override
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    /** @see Status#getAdded() */
    @Override
    public List<String> getAdded() {
        if (added == null) {
            added = new ArrayList<>();
        }
        return added;
    }

    /** @see Status#setAdded(java.util.List) */
    @Override
    public void setAdded(List<String> added) {
        this.added = added;
    }

    /** @see Status#getChanged() */
    @Override
    public List<String> getChanged() {
        if (changed == null) {
            changed = new ArrayList<>();
        }
        return changed;
    }

    /** @see Status#setChanged(java.util.List) */
    @Override
    public void setChanged(List<String> changed) {
        this.changed = changed;
    }

    /** @see Status#getRemoved() */
    @Override
    public List<String> getRemoved() {
        if (removed == null) {
            removed = new ArrayList<>();
        }
        return removed;
    }

    /** @see Status#setRemoved(java.util.List) */
    @Override
    public void setRemoved(List<String> removed) {
        this.removed = removed;
    }

    /** @see Status#getMissing() */
    @Override
    public List<String> getMissing() {
        if (missing == null) {
            missing = new ArrayList<>();
        }
        return missing;
    }

    /** @see Status#setMissing(java.util.List) */
    @Override
    public void setMissing(List<String> missing) {
        this.missing = missing;
    }

    /** @see Status#getModified() */
    @Override
    public List<String> getModified() {
        if (modified == null) {
            modified = new ArrayList<>();
        }
        return modified;
    }

    /** @see Status#setModified(java.util.List) */
    @Override
    public void setModified(List<String> modified) {
        this.modified = modified;
    }

    /** @see Status#getUntracked() */
    @Override
    public List<String> getUntracked() {
        if (untracked == null) {
            untracked = new ArrayList<>();
        }
        return untracked;
    }

    /** @see Status#setUntracked(java.util.List) */
    @Override
    public void setUntracked(List<String> untracked) {
        this.untracked = untracked;
    }

    /** @see Status#getUntrackedFolders() */
    @Override
    public List<String> getUntrackedFolders() {
        if (untrackedFolders == null) {
            untrackedFolders = new ArrayList<>();
        }
        return untrackedFolders;
    }

    /** @see Status#setUntrackedFolders(java.util.List) */
    @Override
    public void setUntrackedFolders(List<String> untrackedFolders) {
        this.untrackedFolders = untrackedFolders;
    }

    /** @see Status#getConflicting() */
    @Override
    public List<String> getConflicting() {
        if (conflicting == null) {
            conflicting = new ArrayList<>();
        }
        return conflicting;
    }

    /** @see Status#setConflicting(java.util.List) */
    @Override
    public void setConflicting(List<String> conflicting) {
        this.conflicting = conflicting;
    }

    /** @see Status#getRepositoryState() */
    @Override
    public String getRepositoryState() {
        return this.repositoryState;
    }

    /** @see Status#setRepositoryState(String) */
    @Override
    public void setRepositoryState(String repositoryState) {
        this.repositoryState = repositoryState;
    }
    
    /**
     * loads status information.
     *
     * @throws GitException
     *         when it is not possible to get status information
     */
    public void load() throws GitException {
        StatusCommand status = nativeGit.createStatusCommand().setFormat(StatusFormat.PORCELAIN);
        List<String> statusOutput = status.execute();
        setClean(statusOutput.size() == 0);
        if (!isClean()) {
            added = new ArrayList<>();
            changed = new ArrayList<>();
            removed = new ArrayList<>();
            missing = new ArrayList<>();
            modified = new ArrayList<>();
            untracked = new ArrayList<>();
            untrackedFolders = new ArrayList<>();
            conflicting = new ArrayList<>();
            for (String statusLine : statusOutput) {
                //add conflict files AA, UU, any of U
                if (!(addIfMatches(conflicting, statusLine, 'A', 'A') //
                        || addIfMatches(conflicting, statusLine, 'D', 'D') //
                        || addIfMatches(conflicting, statusLine, 'U', '*') //
                        || addIfMatches(conflicting, statusLine, '*', 'U'))) {
                    // Add index-based entries
                    addIfMatches(added, statusLine, 'A', '*');
                    addIfMatches(removed, statusLine, 'D', '*');
                    addIfMatches(changed, statusLine, 'M', '*');
                    // Add working tree - based entries
                    addIfMatches(missing, statusLine, '*', 'D');
                    addIfMatches(modified, statusLine, '*', 'M');
                }
                if (statusLine.endsWith("/")) {
                    //add untracked folders
                    addIfMatches(untrackedFolders, statusLine.substring(0, statusLine.length() - 1), '?', '?');
                } else {
                    //add untracked Files
                    addIfMatches(untracked, statusLine, '?', '?');
                }
            }
        }
    }

    /**
     * Adds files to container if they matched to template.
     *
     * @param container
     *         container for matched files
     * @param statusLine
     *         short status command line
     * @param x
     *         first template parameter
     * @param y
     *         second template parameter
     */
    private static boolean addIfMatches(List<String> container, String statusLine, char x, char y) {
        if (matches(statusLine, x, y)) {
            final String filename = statusLine.substring(3);
            if (!container.contains(filename)) {
                container.add(filename);
            }
            return true;
        }
        return false;
    }

    private static boolean matches(String statusLine, char x, char y) {
        return (x == '*' || x == statusLine.charAt(0)) &&
               (y == '*' || y == statusLine.charAt(1));
    }
}
