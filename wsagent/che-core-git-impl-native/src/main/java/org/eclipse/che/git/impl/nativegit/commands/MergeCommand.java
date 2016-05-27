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

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.git.shared.MergeResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

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
            throw new GitException("Commit wasn't set");
        }
        if (committer == null) {
            throw new GitException("Committer can't be null");
        }
        if (committer.getName() == null || committer.getEmail() == null) {
            throw new GitException("Git user name and (or) email wasn't set", ErrorCodes.NO_COMMITTER_NAME_OR_EMAIL_DEFINED);
        }
        reset();
        commandLine.add("merge", commit);
        //result of merging
        MergeResult mergeResult = newDto(MergeResult.class);
        //get merge commits
        ArrayList<String> mergedCommits = new ArrayList<>(2);
        mergedCommits.add(new LogCommand(getRepository()).setCount(1).execute().get(0).getId());
        mergedCommits.add(new LogCommand(getRepository()).setBranch(commit).setCount(1).execute().get(0).getId());
        mergeResult.setMergedCommits(mergedCommits);

        setCommandEnvironment("GIT_COMMITTER_NAME", committer.getName());
        setCommandEnvironment("GIT_COMMITTER_EMAIL", committer.getEmail());

        try {
            start();
            // if not failed and not conflict
            if (lines.getFirst().startsWith("Already")) {
                mergeResult.setMergeStatus(MergeResult.MergeStatus.ALREADY_UP_TO_DATE);
            } else if (lines.getFirst().startsWith("Updating") && lines.get(1).startsWith("Fast")) {
                mergeResult.setMergeStatus(MergeResult.MergeStatus.FAST_FORWARD);
            } else {
                mergeResult.setMergeStatus(MergeResult.MergeStatus.MERGED);
            }
        } catch (GitException e) {
            clear();
            lines.addAll(Arrays.asList(e.getMessage().split("\n")));
            //if Auto-merging is first line then it is CONFLICT situation cause of exception.
            if (lines.getFirst().startsWith("Auto-merging")) {
                mergeResult.setMergeStatus(MergeResult.MergeStatus.CONFLICTING);
                List<String> conflictFiles = lines.stream().filter(outLine -> outLine.startsWith("CONFLICT"))
                                                  .map(outLine -> outLine.substring(outLine.indexOf("in") + 3))
                                                  .collect(Collectors.toCollection(LinkedList::new));
                mergeResult.setConflicts(conflictFiles);
                //if Updating is first then it is Failed situation cause of exception
            } else if (lines.getFirst().startsWith("Updating")) {
                mergeResult.setMergeStatus(MergeResult.MergeStatus.FAILED);
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
                failedFiles.addAll(lines.subList(2, lines.size()).stream().map(String::trim).collect(Collectors.toList()));
                mergeResult.setFailed(failedFiles);
            } else {
                mergeResult.setMergeStatus(MergeResult.MergeStatus.NOT_SUPPORTED);
            }
        }
        mergeResult.setNewHead(new LogCommand(getRepository()).setCount(1).execute().get(0).getId());
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
