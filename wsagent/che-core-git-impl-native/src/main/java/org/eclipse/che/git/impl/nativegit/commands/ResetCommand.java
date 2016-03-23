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
 * Reset repository to specifically state
 *
 * @author Eugene Voevodin
 */
public class ResetCommand extends GitCommand<Void> {

    private String       commit;
    private String       mode;
    private List<String> filePattern;

    public ResetCommand(File repository) {
        super(repository);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        if (mode == null) {
            throw new GitException("Reset mode wasn't set.");
        }
        reset();
        commandLine.add("reset");

        if (commit != null) {
            commandLine.add(commit);
        }

        if (filePattern != null && !filePattern.isEmpty()) {
            commandLine.add("--");
            for (String file : filePattern) {
                commandLine.add(file);
            }
        } else {
            //Add mode don't makes sense for reset with paths(--mixed with paths is deprecated).
            commandLine.add(mode);
        }

        start();
        return null;
    }

    /**
     * @param commit
     *         reset point
     * @return ResetCommand with established commit
     */
    public ResetCommand setCommit(String commit) {
        this.commit = commit;
        return this;
    }

    /**
     * @param mode
     *         reset mode
     * @return ResetCommand with established mode
     */
    public ResetCommand setMode(String mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Set up file pattern for reset command.
     *
     * @param pattern file pattern for reset command.
     * @return ResetCommand with established pattern
     */
    public ResetCommand setFilePattern(List<String> pattern) {
        this.filePattern = pattern;
        return this;
    }
}
