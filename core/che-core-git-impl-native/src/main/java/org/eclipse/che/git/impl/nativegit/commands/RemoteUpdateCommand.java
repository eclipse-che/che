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
package org.eclipse.che.git.impl.nativegit.commands;

import org.eclipse.che.api.git.GitException;

import java.io.File;
import java.util.List;

/**
 * Update remote
 *
 * @author Eugene Voevodin
 */
public class RemoteUpdateCommand extends GitCommand<Void> {

    private boolean      addBranches;
    private String       remoteName;
    private String       newUrl;
    private List<String> branchesToAdd;
    private List<String> addUrl;
    private List<String> removeUrl;
    private List<String> addPushUrl;
    private List<String> removePushUrl;

    /** @see GitCommand */
    public RemoteUpdateCommand(File repository) {
        super(repository);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        if (remoteName == null) {
            throw new GitException("Remote name wasn't set.");
        }
        reset();
        commandLine.add("remote");
        if (branchesToAdd != null && !branchesToAdd.isEmpty()) {
            commandLine.add("set-branches");
            if (addBranches) {
                commandLine.add("--add");
            }
            commandLine.add(remoteName);
            commandLine.add(branchesToAdd);
        } else {
            commandLine.add("set-url");
            if (addUrl != null && !addUrl.isEmpty()) {
                commandLine.add("--add", remoteName);
                commandLine.add(addUrl);
            } else if (addPushUrl != null && !addPushUrl.isEmpty()) {
                commandLine.add("--push", remoteName);
                commandLine.add(addPushUrl);
            } else if (removeUrl != null && !removeUrl.isEmpty()) {
                commandLine.add("--delete", remoteName);
                commandLine.add(removeUrl);
            } else if (removePushUrl != null && !removePushUrl.isEmpty()) {
                commandLine.add("--delete", "--push", remoteName);
                commandLine.add(removePushUrl);
            } else if (newUrl != null) {
                commandLine.add(remoteName, newUrl);
            } else {
                throw new GitException("Url wasn't set.");
            }
        }
        start();
        return null;
    }

    /**
     * @param addBranches
     *         do not replace branches
     * @return RemoteUpdateCommand with established add branches parameter
     */
    public RemoteUpdateCommand setAddBranches(boolean addBranches) {
        this.addBranches = addBranches;
        return this;
    }

    /**
     * @param branchesToAdd
     *         branches to add or replace
     * @return RemoteUpdateCommand with established branches to add
     */
    public RemoteUpdateCommand setBranchesToAdd(List<String> branchesToAdd) {
        this.branchesToAdd = branchesToAdd;
        return this;
    }

    /**
     * @param addUrl
     *         url(s) that will be added to remote
     * @return RemoteUpdateCommand with established add url(s)
     */
    public RemoteUpdateCommand setAddUrl(List<String> addUrl) {
        this.addUrl = addUrl;
        return this;
    }

    /**
     * @param removeUrl
     *         url(s) that will be removed from remote
     * @return RemoteUpdateCommand with established removeUrl parameter
     */
    public RemoteUpdateCommand setRemoveUrl(List<String> removeUrl) {
        this.removeUrl = removeUrl;
        return this;
    }

    /**
     * @param addPushUrl
     *         url(s) that will be added as push to remote
     * @return RemoteUpdateCommand with established push url(s) that will be added
     */
    public RemoteUpdateCommand setAddPushUrl(List<String> addPushUrl) {
        this.addPushUrl = addPushUrl;
        return this;
    }

    /**
     * @param remoteName
     *         remote name
     * @return RemoteUpdateCommand with established remote name
     */
    public RemoteUpdateCommand setRemoteName(String remoteName) {
        this.remoteName = remoteName;
        return this;
    }

    /**
     * @param newUrl
     *         url that replaces current remote url
     * @return RemoteUpdateCommand with established new url
     */
    public RemoteUpdateCommand setNewUrl(String newUrl) {
        this.newUrl = newUrl;
        return this;
    }

    /**
     * @param removePushUrl
     *         url(s) that will be removed from push
     * @return RemoteUpdateCommand with established push url(s) that will be removed
     */
    public RemoteUpdateCommand setRemovePushUrl(List<String> removePushUrl) {
        this.removePushUrl = removePushUrl;
        return this;
    }
}
