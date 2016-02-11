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
 * Initialize repository.
 *
 * @author Eugene Voevodin
 */
public class InitCommand extends GitCommand<Void> {

    private boolean bare;

    public InitCommand(File repository) {
        super(repository);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        reset();
        commandLine.add("init");
        if (bare) {
            commandLine.add("--bare");
        }
        start();
        return null;
    }

    /**
     * @param bare
     *         set up bare repository
     * @return InitCommand with established bare parameter
     */
    public InitCommand setBare(boolean bare) {
        this.bare = bare;
        return this;
    }
}
