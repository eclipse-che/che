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
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.dto.server.DtoFactory;


import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Get list of branches
 *
 * @author Eugene Voevodin
 */
public class BranchListCommand extends GitCommand<List<Branch>> {

    private boolean showRemotes;

    public BranchListCommand(File place) {
        super(place);
    }

    /** @see GitCommand#execute() */
    @Override
    public List<Branch> execute() throws GitException {
        reset();
        commandLine.add("branch");
        if (showRemotes) {
            commandLine.add("-r");
        }
        start();
        //parse branch list
        List<Branch> branches = new LinkedList<>();
        if (showRemotes) {
            for (String outLine : lines) {
                if (outLine.contains("->")) {
                    continue;
                }
                String remoteName = outLine.trim().split(" ")[0];
                Branch branch = DtoFactory.getInstance().createDto(Branch.class).withName("refs/remotes/" + remoteName)
                                          .withActive(false)
                                          .withDisplayName(remoteName)
                                          .withRemote(true);
                branches.add(branch);
            }
        } else {
            for (String outLine : lines) {
                String localName = outLine.substring(2);
                Branch branch = DtoFactory.getInstance().createDto(Branch.class).withName("refs/heads/" + localName)
                                          .withActive(outLine.indexOf('*') != -1)
                                          .withDisplayName(localName)
                                          .withRemote(false);
                branches.add(branch);
            }
        }
        return branches;
    }

    /**
     * @param remotes
     *         if <code>true</code> remote branches will be shown
     * @return BranchListCommand with established remotes parameter
     */
    public BranchListCommand setShowRemotes(boolean remotes) {
        this.showRemotes = remotes;
        return this;
    }
}
