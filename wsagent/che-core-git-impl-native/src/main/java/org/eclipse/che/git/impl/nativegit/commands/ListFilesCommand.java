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
 * Get list of files
 *
 * @author Eugene Voevodin
 */
public class ListFilesCommand extends GitCommand<List<String>> {

    private boolean others;
    private boolean modified;
    private boolean staged;
    private boolean cached;
    private boolean deleted;
    private boolean ignored;
    private boolean excludeStandard;

    public ListFilesCommand(File place) {
        super(place);
    }

    /** @see GitCommand#execute() */
    @Override
    public List<String> execute() throws GitException {
        reset();
        commandLine.add("ls-files");
        if (others) {
            commandLine.add("-o");
        }
        if (modified) {
            commandLine.add("-m");
        }
        if (staged) {
            commandLine.add("-s");
        }
        if (cached) {
            commandLine.add("-c");
        }
        if (deleted) {
            commandLine.add("-d");
        }
        if (ignored) {
            commandLine.add("-i");
        }
        if (excludeStandard) {
            commandLine.add("--exclude-standard");
        }
        start();
        return getLines();
    }

    /**
     * @param others
     *         if <code>true</code> other files will be selected
     * @return ListFilesCommand with established others parameter
     */
    public ListFilesCommand setOthers(boolean others) {
        this.others = others;
        return this;
    }

    /**
     * @param modified
     *         if <code>true</code> modified files will be selected
     * @return ListFilesCommand with established modified files parameter
     */
    public ListFilesCommand setModified(boolean modified) {
        this.modified = modified;
        return this;
    }

    /**
     * @param staged
     *         if <code>true</code> staged files will be selected
     * @return ListFilesCommand with established staged files parameter
     */
    public ListFilesCommand setStaged(boolean staged) {
        this.staged = staged;
        return this;
    }

    /**
     * @param ignored
     *         if <code>true</code> ignored files will be selected
     * @return ListFilesCommand with established ignored files parameter
     */
    public ListFilesCommand setIgnored(boolean ignored) {
        this.ignored = ignored;
        return this;
    }

    /**
     * @param deleted
     *         if <code>true</code> deleted files will be selected
     * @return ListFilesCommand with established deleted files parameter
     */
    public ListFilesCommand setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    /**
     * @param cached
     *         if <code>true</code> cached files will be selected
     * @return ListFilesCommand with established cached files parameter
     */
    public ListFilesCommand setCached(boolean cached) {
        this.cached = cached;
        return this;
    }

    /**
     * @param excludeStandard
     *         if <code>true</code> excludeStandard parameter will be used
     * @return ListFilesCommand with established excludeStandard parameter
     */
    public ListFilesCommand setExcludeStandard(boolean excludeStandard) {
        this.excludeStandard = excludeStandard;
        return this;
    }
}


