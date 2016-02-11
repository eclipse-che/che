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
import org.eclipse.che.api.git.shared.ShowFileContentResponse;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.io.File;

/**
 * Used for getting content of the file from specified revision or branch.
 *
 * @author Igor Vinokur
 */
public class ShowFileContentCommand extends GitCommand<ShowFileContentResponse> {

    private String file;
    private String version;

    public ShowFileContentCommand(File repositoryPlace) {
        super(repositoryPlace);
    }

    /**
     * @see GitCommand#execute()
     */
    @Override
    public ShowFileContentResponse execute() throws GitException {
        if (file == null) {
            throw new GitException("No file was set.");
        }
        reset();
        commandLine.add("show");
        commandLine.add(version + ":" + file);
        start();

        String content = "";

        for (String line : lines) {
            content = content.concat(line + "\n");
        }

        return newDto(ShowFileContentResponse.class).withContent(content);
    }

    /**
     * Set up file for show file content command.
     *
     * @param file
     *         file name with its full path for showFileContent command
     * @return ShowFileContentCommand with established file
     */
    public ShowFileContentCommand withFile(String file) {
        this.file = file;
        return this;
    }

    /**
     * Set up revision or branch for show file content command.
     *
     * @param version
     *         revision or branch for showFileContent command.
     * @return ShowFileContentCommand with established revision or branch
     */
    public ShowFileContentCommand withVersion(String version) {
        this.version = version;
        return this;
    }

}
