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
import org.eclipse.che.git.impl.nativegit.NativeGitMergeResult;
import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.git.shared.MergeResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Join two development histories together
 *
 * @author Eugene Voevodin
 */
public class MergeCommand extends GitCommand<MergeResult> {

    private String  commit;
    private GitUser committer;

    public MergeCommand(File repository) {
        super(repository);
    }

    /** @see GitCommand#execute() */
    @Override
    public MergeResult execute() throws GitException {
        if (commit == null) {
            throw new GitException("Commit wasn't set.");
        }
        reset();
        commandLine.add("merge", commit);
        //result of merging
        NativeGitMergeResult mergeResult = new NativeGitMergeResult();
        //get merge commits
        ArrayList<String> mergedCommits = new ArrayList<>(2);
        mergedCommits.add(new LogCommand(getRepository()).setCount(1).execute().get(0).getId());
        mergedCommits.add(new LogCommand(getRepository()).setBranch(commit).setCount(1).execute().get(0).getId());
        mergeResult.setMergedCommits(mergedCommits);

        if (committer != null) {
            setCommandEnvironment("GIT_COMMITTER_NAME", committer.getName());
            setCommandEnvironment("GIT_COMMITTER_EMAIL", committer.getEmail());
        } else {
            throw new GitException("Committer can't be null");
        }

        try {
            start();
            // if not failed and not conflict
            if (lines.getFirst().startsWith("Already")) {
                mergeResult.setStatus(MergeResult.MergeStatus.ALREADY_UP_TO_DATE);
            } else if (lines.getFirst().startsWith("Updating") && lines.get(1).startsWith("Fast")) {
                mergeResult.setStatus(MergeResult.MergeStatus.FAST_FORWARD);
            } else {
                mergeResult.setStatus(MergeResult.MergeStatus.MERGED);
            }
        } catch (GitException e) {
            clear();
            lines.addAll(Arrays.asList(e.getMessage().split("\n")));
            //if Auto-merging is first line then it is CONFLICT situation cause of exception.
            if (lines.getFirst().startsWith("Auto-merging")) {
                mergeResult.setStatus(MergeResult.MergeStatus.CONFLICTING);
                List<String> conflictFiles = new LinkedList<>();
                for (String outLine : lines) {
                    if (outLine.startsWith("CONFLICT")) {
                        conflictFiles.add(outLine.substring(outLine.indexOf("in") + 3));
                    }
                }
                mergeResult.setConflicts(conflictFiles);
                //if Updating is first then it is Failed situation cause of exception
            } else if (lines.getFirst().startsWith("Updating")) {
                mergeResult.setStatus(MergeResult.MergeStatus.FAILED);
                List<String> failedFiles = new LinkedList<>();
                /*
                * First 2 lines contain not needed information:
                *
                * Updating commit1..commit2
                * error: The following untracked working tree files would be overwritten by merge:
                * file1
                * ....
                * fileN
                * Please move or remove them before you can merge.
                * Aborting
                */
                for (String line : lines.subList(2, lines.size())) {
                    failedFiles.add(line.trim());
                }
                mergeResult.setFailed(failedFiles);
            } else {
                mergeResult.setStatus(MergeResult.MergeStatus.NOT_SUPPORTED);
            }
        }
        mergeResult.setHead(new LogCommand(getRepository()).setCount(1).execute().get(0).getId());
        return mergeResult;
    }

    /**
     * @param commit
     *         commit to merge with
     * @return MergeCommand with established commit
     */
    public MergeCommand setCommit(String commit) {
        this.commit = commit;
        return this;
    }

    /**
     * @param committer
     *         committer of commit
     * @return CommitCommand with established committer
     */
    public MergeCommand setCommitter(GitUser committer) {
        this.committer = committer;
        return this;
    }
}
