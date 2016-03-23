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

/**
 * Move files.
 *
 * @author Eugene Voevodin
 */
public class MoveCommand extends GitCommand<Void> {

    private String target;
    private String source;

    public MoveCommand(File repository) {
        super(repository);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        if (source == null || target == null) {
            throw new GitException("Target or source wasn't set.");
        }
        reset();
        commandLine.add("mv", source, target);
        start();
        return null;
    }

    /**
     * @param target
     *         file where source will be moved
     * @return MoveCommand with established target parameter
     */
    public MoveCommand setTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * @param source
     *         file that will be moved to target
     * @return MoveCommand with established source parameter
     */
    public MoveCommand setSource(String source) {
        this.source = source;
        return this;
    }
}
