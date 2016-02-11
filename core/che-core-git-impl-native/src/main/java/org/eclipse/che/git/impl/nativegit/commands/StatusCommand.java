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
import org.eclipse.che.api.git.shared.StatusFormat;

import java.io.File;
import java.util.List;

/**
 * Show repository status.
 *
 * @author Eugene Voevodin
 */
public class StatusCommand extends GitCommand<List<String>> {

    private StatusFormat format;

    public StatusCommand(File repository) {
        super(repository);
    }

    /** @see GitCommand#execute() */
    @Override
    public List<String> execute() throws GitException {
        reset();
        commandLine.add("status");
        if (format != null) {
            switch (format) {
                case LONG:
                    commandLine.add("--long");
                    break;
                case SHORT:
                    commandLine.add("--short");
                    break;
                case PORCELAIN:
                    commandLine.add("--porcelain");
                    break;
                default:
            }
        }
        start();
        return getLines();
    }

    /**
     * Sets the output format that will be used.
     * 
     * @param format
     *         the status format
     * @return StatusCommand with the established format parameter
     */
    public StatusCommand setFormat(final StatusFormat format) {
        this.format = format;
        return this;
    }
}
