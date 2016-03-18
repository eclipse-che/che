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
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.dto.server.DtoFactory;


import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Used for getting list of remotes
 *
 * @author Eugene Voevodin
 */
public class RemoteListCommand extends GitCommand<List<Remote>> {

    private String remoteName;

    public RemoteListCommand(File place) {
        super(place);
    }

    /** @see GitCommand#execute() */
    @Override
    public List<Remote> execute() throws GitException {
        reset();
        commandLine.add("remote", "-v");
        start();
        //get all remotes with urls
        Map<String, String> remotes = new HashMap<>();
        for (String outLine : lines) {
            String[] outArr = outLine.split("\t");
            remotes.put(outArr[0], outArr[1].split(" ")[0]);
        }
        //parse remotes
        List<Remote> remoteList = new LinkedList<>();
        if (remoteName != null) {
            if (remotes.get(remoteName) == null) {
                throw new GitException("No remote with name " + remoteName);
            }
            Remote remote = DtoFactory.getInstance().createDto(Remote.class).withName(remoteName).withUrl(remotes.get(remoteName));
            remoteList.add(remote);
        } else {
            for (Map.Entry<String, String> entry : remotes.entrySet()) {
                Remote remote = DtoFactory.getInstance().createDto(Remote.class).withName(entry.getKey()).withUrl(entry.getValue());
                remoteList.add(remote);
            }
        }
        return remoteList;
    }

    /**
     * @param remoteName
     *         remote name
     * @return RemoteListCommand with established remote name
     */
    public RemoteListCommand setRemoteName(String remoteName) {
        this.remoteName = remoteName;
        return this;
    }

}
