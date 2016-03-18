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
package org.eclipse.che.git.impl.nativegit;



import org.eclipse.che.api.git.DiffPage;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.DiffRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Contains information about difference between two commits, commit and working tree,
 * working tree and index, commit and index.
 *
 * @author <a href="maito:evoevodin@codenvy.com">Eugene Voevodin</a>
 */
public class NativeGitDiffPage extends DiffPage {

    private static Logger LOG = LoggerFactory.getLogger(NativeGitDiffPage.class);
    private DiffRequest request;
    private NativeGit   nativeGit;

    public NativeGitDiffPage(DiffRequest request, NativeGit nativeGit) {
        this.request = request;
        this.nativeGit = nativeGit;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        try (PrintWriter outWriter = new PrintWriter(out);) {
            if (request.getCommitA() == null && request.getCommitB() == null && !request.isCached()) {
                workingTreeToIndex(outWriter);
            } else if (request.getCommitA() != null && request.getCommitB() == null && !request.isCached()) {
                commitToWorkingTree(request.getCommitA(), outWriter);
            } else if (request.getCommitB() == null && request.isCached()) {
                commitToIndex(request.getCommitA(), outWriter);
            } else {
                commitToCommit(request.getCommitA(), request.getCommitB(), outWriter);
            }
        } catch (GitException e) {
            LOG.error("Diff page creating exception", e);
        }
    }

    /**
     * @param commit commit
     * @return difference between index and commit
     * @throws GitException when any error occurs
     */
    private void commitToIndex(String commit, PrintWriter outWriter) throws GitException {
        outWriter.print(nativeGit.createDiffCommand()
                .setCommitA(commit)
                .setCached(true)
                .setType(request.getType().toString())
                .setFileFilter(request.getFileFilter())
                .setNoRenames(request.isNoRenames())
                .setRenamesCount(request.getRenameLimit())
                .execute());
    }

    /**
     * @return difference between working tree and index
     * @throws GitException when any error occurs
     */
    private void workingTreeToIndex(PrintWriter outWriter) throws GitException {
        outWriter.print(nativeGit.createDiffCommand()
                .setType(request.getType().toString())
                .setFileFilter(request.getFileFilter())
                .setNoRenames(request.isNoRenames())
                .setRenamesCount(request.getRenameLimit())
                .execute());
    }

    /**
     * @param commitA commit
     * @param commitB commit
     * @return difference between two commits
     * @throws GitException when any error occurs
     */
    private void commitToCommit(String commitA, String commitB, PrintWriter outWriter) throws GitException {
        outWriter.print(nativeGit.createDiffCommand()
                .setCommitA(commitA)
                .setCommitB(commitB)
                .setType(request.getType().toString())
                .setFileFilter(request.getFileFilter())
                .setNoRenames(request.isNoRenames())
                .setRenamesCount(request.getRenameLimit())
                .execute());
    }

    /**
     * @param commit commit
     * @return difference between commit and working tree
     * @throws GitException
     */
    private void commitToWorkingTree(String commit, PrintWriter outWriter) throws GitException {
         outWriter.print(nativeGit.createDiffCommand()
                 .setCommitA(commit)
                 .setType(request.getType().toString())
                 .setFileFilter(request.getFileFilter())
                 .setNoRenames(request.isNoRenames())
                 .setRenamesCount(request.getRenameLimit())
                 .execute());
    }
}
