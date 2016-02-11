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
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.git.impl.nativegit.GitAskPassScript;
import org.eclipse.che.git.impl.nativegit.ssh.GitSshScriptProvider;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Used for branches renaming
 *
 * @author Eugene Voevodin
 */
public class BranchRenameCommand extends RemoteOperationCommand<Void> {

    private static final Pattern checkoutErrorPattern = Pattern.compile(".*fatal: A branch named '.*' already exists.*");

    private String oldName;
    private String newName;
    private String remote;

    public BranchRenameCommand(File repository, GitSshScriptProvider gitSshScriptProvider, CredentialsLoader credentialsLoader, GitAskPassScript gitAskPassScript) {
        super(repository, gitSshScriptProvider, credentialsLoader, gitAskPassScript);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        if (oldName == null || newName == null) {
            throw new GitException("Old name or new name was not set.");
        }
        reset();

        if (remote == null) {
            renameLocalBranch();
        } else {
            renameRemoteBranch();
        }
        return null;
    }

    private void renameLocalBranch() throws GitException {
        commandLine.add("branch");
        commandLine.add("-m", oldName, newName);
        start();
    }

    private void renameRemoteBranch() throws GitException {
        //checkout
        try {
            String branchName = remote + "/" + oldName;
            commandLine.add("checkout");
            commandLine.add("-t");
            commandLine.add(branchName);
            start();
        } catch (GitException e) {
            String errorMessage = e.getMessage();
            if (!checkoutErrorPattern.matcher(errorMessage).find()) {
                throw new GitException(errorMessage);
            }
            //local branch already exist - so ignore and try perform the next step
        }

        //rename the local branch
        reset();
        commandLine.add("branch");
        commandLine.add("-m", oldName, newName);
        start();

        //push the new local branch
        reset();
        commandLine.add("push");
        commandLine.add(remote);
        commandLine.add(newName);
        start();

        //delete the old remote branch
        reset();
        commandLine.add("push");
        commandLine.add(remote);
        commandLine.add(":" + oldName);
        start();
    }

    /**
     * @param oldName
     *         old branch name
     * @param newName
     *         new branch name
     * @return BranchRenameCommand with established old and new branch names
     */
    public BranchRenameCommand setNames(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
        return this;
    }

    /**
     * @param remoteName
     *         remote name
     * @return BranchDeleteCommand with established remote name
     */
    public BranchRenameCommand setRemote(String remoteName) {
        this.remote = remoteName;
        return this;
    }
}
