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
import java.util.LinkedList;
import java.util.List;

/**
 * Used with specific git commands
 *
 * @author Eugene Voevodin
 */
public class EmptyGitCommand extends GitCommand<Void> {

    private List<String> parameters = new LinkedList<>();

    public EmptyGitCommand(File place) {
        super(place);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        reset();
        commandLine.add(parameters);
        start();
        return null;
    }

    /**
     * @param nextParameter
     *         next parameter
     * @return EmptyGitCommand with established next parameter
     */
    public EmptyGitCommand setNextParameter(String nextParameter) {
        parameters.add(nextParameter);
        return this;
    }
}
