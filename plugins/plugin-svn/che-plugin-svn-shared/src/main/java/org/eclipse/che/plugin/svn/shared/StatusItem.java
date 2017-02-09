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
package org.eclipse.che.plugin.svn.shared;

/**
 * Represents the status of a path.
 */
public class StatusItem {

    public enum FileState {
        ADDED("A"),
        CONFLICTED("C"),
        DELETED("D"),
        IGNORED("I"),
        MISSING("!"),
        MODIFIED("M"),
        OBSTRUCTED("~"),
        REPLACED("R"),
        UNCHANGED(" "),
        UNVERSIONED("?"),
        UNVERSIONED_EXTERNAL_DIRECTORY("X");

        private String value;

        FileState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static FileState fromChar(final char state) {
            switch (state) {
                case ' ':
                    return UNCHANGED;
                case 'A':
                    return ADDED;
                case 'C':
                    return CONFLICTED;
                case 'D':
                    return DELETED;
                case 'I':
                    return IGNORED;
                case '!':
                    return MISSING;
                case 'M':
                    return MODIFIED;
                case '~':
                    return OBSTRUCTED;
                case 'R':
                    return REPLACED;
                case '?':
                    return UNVERSIONED;
                case 'X':
                    return UNVERSIONED_EXTERNAL_DIRECTORY;
                default:
                    throw new IllegalArgumentException("'" + state + "' is not a valid file state");
            }
        }
    }

    public enum PropertyState {
        CONFLICTED,
        MODIFIED,
        UNCHANGED;

        public static PropertyState fromChar(final char state) {
            switch (state) {
                case 'C':
                    return CONFLICTED;
                case 'M':
                    return MODIFIED;
                case ' ':
                    return UNCHANGED;
                default:
                    throw new IllegalArgumentException("'" + state + "' is not a valid property state");
            }
        }
    }

    public enum LockState {
        LOCKED,
        UNLOCKED;

        public static LockState fromChar(final char state) {
            switch (state) {
                case 'L':
                    return LOCKED;
                case ' ':
                    return UNLOCKED;
                default:
                    throw new IllegalArgumentException("'" + state + "' is not a valid lock state");
            }
        }
    }

    public enum HistoryState {
        HISTORY,
        NO_HISTORY;

        public static HistoryState fromChar(final char state) {
            switch (state) {
                case '+':
                    return HISTORY;
                case ' ':
                    return NO_HISTORY;
                default:
                    throw new IllegalArgumentException("'" + state + "' is not a valid history state");
            }
        }
    }

    public enum RemoteState {
        EXTERNAL,
        NORMAL,
        SWITCHED;

        public static RemoteState fromChar(final char state) {
            switch (state) {
                case 'X':
                    return EXTERNAL;
                case ' ':
                    return NORMAL;
                case 'S':
                    return SWITCHED;
                default:
                    throw new IllegalArgumentException("'" + state + "' is not a valid remote state");
            }
        }
    }

    public enum RepositoryLockState {
        LOCKED,
        NO_LOCK;

        public static RepositoryLockState fromChar(final char state) {
            switch (state) {
                case 'L':
                    return LOCKED;
                case ' ':
                    return NO_LOCK;
                default:
                    throw new IllegalArgumentException("'" + state + "' is not a valid repository lock state");
            }
        }
    }

    public enum TreeConflictState {
        CONFLICTED,
        NORMAL;

        public static TreeConflictState fromChar(final char state) {
            switch (state) {
                case 'C':
                    return CONFLICTED;
                case ' ':
                    return NORMAL;
                default:
                    throw new IllegalArgumentException("'" + state + "' is not a valid tree conflict state");
            }
        }
    }

    private FileState           fileState;
    private PropertyState       propertyState;
    private LockState           lockState;
    private HistoryState        historyState;
    private RemoteState         remoteState;
    private RepositoryLockState repositoryLockState;
    private TreeConflictState   treeConflictState;
    private String              path;

    public StatusItem(final String cliLine) {
        final char[] states = cliLine.substring(0, 7).toCharArray();

        fileState = FileState.fromChar(states[0]);
        propertyState = PropertyState.fromChar(states[1]);
        lockState = LockState.fromChar(states[2]);
        historyState = HistoryState.fromChar(states[3]);
        remoteState = RemoteState.fromChar(states[4]);
        repositoryLockState = RepositoryLockState.fromChar(states[5]);
        treeConflictState = TreeConflictState.fromChar(states[6]);
        path = cliLine.substring(8);
    }

    public FileState getFileState() {
        return fileState;
    }

    public void setFileState(final FileState fileState) {
        this.fileState = fileState;
    }

    public PropertyState getPropertyState() {
        return propertyState;
    }

    public void setPropertyState(final PropertyState propertyState) {
        this.propertyState = propertyState;
    }

    public LockState getLockState() {
        return lockState;
    }

    public void setLockState(final LockState lockState) {
        this.lockState = lockState;
    }

    public HistoryState getHistoryState() {
        return historyState;
    }

    public void setHistoryState(final HistoryState historyState) {
        this.historyState = historyState;
    }

    public RemoteState getRemoteState() {
        return remoteState;
    }

    public void setRemoteState(final RemoteState remoteState) {
        this.remoteState = remoteState;
    }

    public RepositoryLockState getRepositoryLockState() {
        return repositoryLockState;
    }

    public void setRepositoryLockState(final RepositoryLockState repositoryLockState) {
        this.repositoryLockState = repositoryLockState;
    }

    public TreeConflictState getTreeConflictState() {
        return treeConflictState;
    }

    public void setTreeConflictState(final TreeConflictState treeConflictState) {
        this.treeConflictState = treeConflictState;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }
}
