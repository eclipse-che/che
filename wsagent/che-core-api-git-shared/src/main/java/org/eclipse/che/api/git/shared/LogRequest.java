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

import java.util.List;

import org.eclipse.che.dto.shared.DTO;

/**
 * Request to get commit logs.
 *
 * @author andrew00x
 */
@DTO
public interface LogRequest extends GitRequest {

    /** Returns the revision range since. */
    String getRevisionRangeSince();

    /** Set revision range since. */
    void setRevisionRangeSince(String revisionRangeSince);

    /**
     * Create a {@link LogRequest} object based on a given revision range since.
     *
     * @param revisionRangeSince
     *         revision range since
     */
    LogRequest withRevisionRangeSince(String revisionRangeSince);

    /** Returns the revision range until. */
    String getRevisionRangeUntil();

    /** Set revision range until. */
    void setRevisionRangeUntil(String revisionRangeUntil);

    /**
     * Create a {@link LogRequest} object based on a given revision range until.
     *
     * @param revisionRangeUntil
     *         revision range until
     */
    LogRequest withRevisionRangeUntil(String revisionRangeUntil);

    /** Returns the integer value of the number of commits that will be skipped when calling log command. */
    Integer getSkip();

    /**  Set the integer value of the number of commits that will be skipped when calling log command. */
    void setSkip(Integer skip);

    /**
     * Create a {@link LogRequest} object based on a given integer value of the number of commits that
     * will be skipped when calling log command
     *
     * @param skip
     *         integer value of the number of commits that will be skipped when calling log command
     */
    LogRequest withSkip(Integer skip);

    /** Returns the integer value of the number of commits that will be returned when calling log command. */
    Integer getMaxCount();

    /**  Set the integer value of the number of commits that will be returned when calling log command. */
    void setMaxCount(Integer maxCount);

    /**
     * Create a {@link LogRequest} object based on a given integer value of the number of commits that
     * will be returned when calling log command
     *
     * @param maxCount
     *         integer value of the number of commits that will be returned when calling log command
     */
    LogRequest withMaxCount(Integer maxCount);

    /** Returns the file/folder path used when calling the log command. */
    String getFilePath();

    /** Set the file/folder path used when calling the log command. */
    void setFilePath(String filePath);

    /**
     * Create a {@link LogRequest} object based on a given file/folder path used when calling the log command
     *
     * @param filePath
     *         file/folder path used when calling the log command
     */
    LogRequest withFilePath(String filePath);

    /** Returns the Filter revisions list by range of files. */
    List<String> getFileFilter();

    /** Set range of files. */
    void setFileFilter(List<String> fileFilter);

    /**
     * Create a {@link LogRequest} object based on a given range of files
     *
     * @param fileFilter
     *         range of files
     */
    LogRequest withFileFilter(List<String> fileFilter);
}
