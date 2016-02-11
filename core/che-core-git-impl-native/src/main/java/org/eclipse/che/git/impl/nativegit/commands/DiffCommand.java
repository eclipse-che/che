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
import org.eclipse.che.api.git.shared.DiffRequest;

import java.io.File;
import java.util.List;

/**
 * Show diff
 *
 * @author Eugene Voevodin
 */
public class DiffCommand extends GitCommand<String> {

    private List<String> filesFilter;
    private String   commitA;
    private String   commitB;
    private String   type;
    private boolean  cached;
    private boolean  noRenames;
    private int      renamesCount;

    public DiffCommand(File repository) {
        super(repository);
    }

    /** @see GitCommand#execute() */
    @Override
    public String execute() throws GitException {
        reset();
        commandLine.add("diff");
        if (!(type == null || type.equals(DiffRequest.DiffType.RAW.toString()))) {
            commandLine.add(type);
        }
        if (noRenames) {
            commandLine.add("--no-renames");
        }
        if (renamesCount > 0) {
            commandLine.add("--find-renames=" + renamesCount);
        }
        if (cached) {
            commandLine.add("--cached");
        }
        if (commitA != null) {
            commandLine.add(commitA);
        }
        if (commitB != null) {
            commandLine.add(commitB);
        }
        if (filesFilter != null) {
            commandLine.add(filesFilter);
        }
        start();
        if (type == null || type.equals("--raw")) {
            return getText() + "\n";
        }
        return getText();
    }

    /**
     * @param commitA
     *         first commit
     * @return DiffCommand with established first commit
     */
    public DiffCommand setCommitA(String commitA) {
        this.commitA = commitA;
        return this;
    }

    /**
     * @param commitB
     *         second commit
     * @return DiffCommand with established second commit
     */
    public DiffCommand setCommitB(String commitB) {
        this.commitB = commitB;
        return this;
    }

    /**
     * @param filesFilter
     *         files to filter
     * @return DiffCommand with established files to filter
     */
    public DiffCommand setFileFilter(List<String> filesFilter) {
        this.filesFilter = filesFilter;
        return this;
    }

    /**
     * @param type
     *         of diff command
     * @return DiffCommand with established type
     */
    public DiffCommand setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * @param cached
     *         if <code>true</code> cached parameter will be used
     * @return DiffCommand with established cached parameter
     */
    public DiffCommand setCached(boolean cached) {
        this.cached = cached;
        return this;
    }

    /**
     * @param renamesCount
     *         count of renames
     * @return DiffCommand with established renames count
     */
    public DiffCommand setRenamesCount(int renamesCount) {
        this.renamesCount = renamesCount;
        return this;
    }

    /**
     * @param noRenames
     *         if <code>true</code> command will be executed without renames
     * @return DiffCommand with established no renames parameters
     */
    public DiffCommand setNoRenames(boolean noRenames) {
        this.noRenames = noRenames;
        return this;
    }
}
