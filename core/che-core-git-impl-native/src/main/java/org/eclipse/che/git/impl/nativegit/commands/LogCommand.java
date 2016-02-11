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

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.git.shared.Revision;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Show commit logs
 *
 * @author Eugene Voevodin
 */
public class LogCommand extends GitCommand<List<Revision>> {

    private int          count;
    private String       branch;
    private List<String> fileFilter;

    public LogCommand(File place) {
        super(place);
    }

    /** @see GitCommand#execute() */
    @Override
    public List<Revision> execute() throws GitException {
        reset();
        commandLine.add("log")
                   .add("--format=%an#%ae#%cn#%ce#%cd#%H#%s")
                   .add("--date=raw");
        if (branch != null) {
            commandLine.add(branch);
        }
        if (count > 0) {
            commandLine.add("-" + count);
        }
        commandLine.add(fileFilter);
        start();
        List<Revision> list = new LinkedList<>();
        final DtoFactory dtoFactory = DtoFactory.getInstance();
        for (String oneRev : lines) {
            String[] elements = oneRev.split("#");
            GitUser committer = dtoFactory.createDto(GitUser.class).withName(elements[2]).withEmail(elements[3]);
            long commitTime = Long.parseLong(elements[4].substring(0, elements[4].indexOf(" "))) * 1000L;
            String commitId = elements[5];
            StringBuilder commitMessage = new StringBuilder();
            for (int i = 6; i < elements.length; i++) {
                commitMessage.append(elements[i]);
            }
            Revision revision = dtoFactory.createDto(Revision.class)
                                          .withId(commitId)
                                          .withMessage(commitMessage.toString())
                                          .withCommitTime(commitTime)
                                          .withCommitter(committer);
            list.add(revision);
        }
        return list;
    }

    /**
     * @param count
     *         log objects limit
     * @return LogCommand with established limit of log objects
     */
    public LogCommand setCount(int count) {
        this.count = count;
        return this;
    }

    /**
     * @param branch
     *         branch
     * @return LogCommand with established branch
     */
    public LogCommand setBranch(String branch) {
        this.branch = branch;
        return this;
    }

    /**
     * @param fileFilter
     *         range of files to filter revisions list
     * @return LogCommand with established fileFilter
     */
    public LogCommand setFileFilter(List<String> fileFilter) {
        this.fileFilter = fileFilter;
        return this;
    }
}
