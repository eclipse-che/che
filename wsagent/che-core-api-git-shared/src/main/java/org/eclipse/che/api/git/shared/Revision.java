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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;
import java.util.List;

/**
 * Describe single commit.
 *
 * @author andrew00x
 */
@DTO
public interface Revision {

    /**
     * Parameter which shows that this revision is a fake revision (i.e. TO for Exception)
     *
     * @return boolean
     */
    boolean isFake();

    /** @set boolean value which represents whether this revision is a fake revision*/
    void setFake(boolean fake);

    /** @return branch name */
    String getBranch();

    /** @set branch name*/
    void setBranch(String branch);

    /**
     * Create a Revision object based on a given branch name.
     *
     * @param branch
     *         rbranch name
     * @return a Revision object
     */
    Revision withBranch(String branch);

    /** @return commit id */
    String getId();

    /** @set commit id*/
    void setId(String id);

    /**
     * Create a Revision object based on a given commit id.
     *
     * @param id
     *         commit id
     * @return a Revision object
     */
    Revision withId(String id);

    /** @return commit message */
    String getMessage();

    /** @set commit message*/
    void setMessage(String message);

    /**
     * Create a Revision object based on a given commit message.
     *
     * @param message
     *         commit message
     * @return a Revision object
     */
    Revision withMessage(String message);

    /** @return time of commit */
    long getCommitTime();

    /** @set commit time*/
    void setCommitTime(long time);

    /**
     * Create a Revision object based on a given commit time.
     *
     * @param time
     *         commit time
     * @return a Revision object
     */
    Revision withCommitTime(long time);

    /** @return GitUser object which represents the commit committer */
    GitUser getCommitter();

    /** @set GitUser object which represents the commit committer*/
    void setCommitter(GitUser committer);

    /**
     * Create a Revision object based on a given GitUser object which represents the commit committer
     *
     * @param committer
     *         GitUser object which represents the commit committer
     * @return a Revision object
     */
    Revision withCommitter(GitUser committer);

    /** @return commit author */
    GitUser getAuthor();

    /** @set GitUser object which represents the commit author*/
    void setAuthor(GitUser author);

    /**
     * Create a Revision object based on a given GitUser object which represents the commit author
     *
     * @param author
     *         GitUser object which represents the commit author
     * @return a Revision object
     */
    Revision withAuthor(GitUser author);

    /** @return commit branches */
    List<Branch> getBranches();

    /** @set commit branches */
    void setBranches(List<Branch> branches);

    /**
     * Create a Revision object based on a given list of commit branches
     *
     * @param branches
     *         a list of commit branches
     * @return a Revision object
     */
    Revision withBranches(List<Branch> branches);

    /** @return diff commit files */
    List<DiffCommitFile> getDiffCommitFile();

    /** @set diff commit files */
    void setDiffCommitFile(List<DiffCommitFile> v);

    /**
     * Create a Revision object based on a given list of diff commit files
     *
     * @param diffCommitFiles
     *         a list of diff commit files
     * @return a Revision object
     */
    Revision withDiffCommitFile(List<DiffCommitFile> diffCommitFiles);

    /** @return commit parents */
    List<String> getCommitParent();

    /** @set commit parents */
    void setCommitParent(List<String> commitParents);

    /**
     * Create a Revision object based on a given list of commit parents
     *
     * @param commitParents
     *         a list of commit parents
     * @return a Revision object
     */
    Revision withCommitParent(List<String> commitParents);
}