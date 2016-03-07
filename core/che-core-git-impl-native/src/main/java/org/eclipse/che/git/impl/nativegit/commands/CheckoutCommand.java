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

import java.util.List;

import org.eclipse.che.api.git.GitException;

import java.io.File;

/**
 * Checkout branch
 *
 * @author Eugene Voevodin
 */
public class CheckoutCommand extends GitCommand<Void> {

    private boolean      createNew;
    private boolean      noTrack;
    private String       branchName;
    private String       trackBranch;
    private String       startPoint;
    private List<String> filePaths;

    public CheckoutCommand(File place) {
        super(place);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        if (startPoint != null && trackBranch != null) {
            throw new GitException("Start point and track branch can not be used together.");
        }

        if (createNew && branchName == null) {
            throw new GitException("Branch name must be set when createNew equals to true.");
        }

        reset();
        commandLine.add("checkout");

        if (filePaths != null && !filePaths.isEmpty()) {
            for (String file : filePaths) {
                commandLine.add(file);
            }
        } else {
            if (createNew) {
                commandLine.add("-b");
                commandLine.add(branchName);
            } else if (branchName != null) {
                commandLine.add(branchName);
            }

            if (trackBranch != null) {
                commandLine.add("-t");
                commandLine.add(trackBranch);
            } else if (startPoint != null) {
                commandLine.add(startPoint);
            }
        }

        if (noTrack) {
            commandLine.add("--no-track");
        }

        start();
        return null;
    }

    /**
     * @param createNew
     *         if <code>true</code> new branch will be created
     * @return CheckoutCommand with established create new branch parameter
     */
    public CheckoutCommand setCreateNew(boolean createNew) {
        this.createNew = createNew;
        return this;
    }

    /**
     * @param branchName
     *         branch to checkout
     * @return CheckoutCommand with established branch to checkout
     */
    public CheckoutCommand setBranchName(String branchName) {
        this.branchName = branchName;
        return this;
    }

    /**
     * @param trackBranch
     *         branch to track
     * @return CheckoutCommand with track branch
     */
    public CheckoutCommand setTrackBranch(String trackBranch) {
        this.trackBranch = trackBranch;
        return this;
    }

    /**
     * @param startPoint
     *         checkout start point
     * @return CheckoutCommand with start point
     */
    public CheckoutCommand setStartPoint(String startPoint) {
        this.startPoint = startPoint;
        return this;
    }

    /**
     * @param filePaths
     *         checkout specific files(s)
     * @return CheckoutCommand with file paths point
     */
    public CheckoutCommand setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
        return this;
    }

    /**
     * @param noTrack
     *         checkout without setting upstream
     * @return CheckoutCommand with no-track option
     */
    public CheckoutCommand setNoTrack(boolean noTrack) {
        this.noTrack = noTrack;
        return this;
    }

}
