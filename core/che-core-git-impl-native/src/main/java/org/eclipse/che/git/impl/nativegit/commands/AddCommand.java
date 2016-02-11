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
 * Used for adding new files into index(stage area).
 *
 * @author Eugene Voevodin
 */
public class AddCommand extends GitCommand<Void> {
    private boolean update;
    private List<String> filePattern;

    public AddCommand(File repositoryPlace) {
        super(repositoryPlace);
    }

    /**
     * @see GitCommand#execute()
     */
    @Override
    public Void execute() throws GitException {
        if (filePattern == null) {
            throw new GitException("No file pattern was set.");
        }
        reset();
        commandLine.add("add");
        for (String line : filePattern) {
            commandLine.add(line);
        }
        if (update) {
            commandLine.add("--update");
        }
        start();
        return null;
    }

    /**
     * Set up file pattern for add command.
     *
     * @param pattern file pattern for add command.
     * @return AddCommand with established pattern
     */
    public AddCommand setFilePattern(List<String> pattern) {
        this.filePattern = pattern;
        return this;
    }

    /**
     * @param update makes add command only for updated files.
     * @return AddCommand with established update parameter
     */
    public AddCommand setUpdate(boolean update) {
        this.update = update;
        return this;
    }
}
