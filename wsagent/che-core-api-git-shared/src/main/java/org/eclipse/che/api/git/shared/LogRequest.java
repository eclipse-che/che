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

    /** @return revision range since */
    String getRevisionRangeSince();

    /** @set revision range since */
    void setRevisionRangeSince(String revisionRangeSince);

    /**
     * Create a LogRequest object based on a given revision range since.
     *
     * @param revisionRangeSince
     *         revision range since
     * @return a LogRequest object
     */
    LogRequest withRevisionRangeSince(String revisionRangeSince);

    /** @return revision range until */
    String getRevisionRangeUntil();

    /** @set revision range until */
    void setRevisionRangeUntil(String revisionRangeUntil);

    /**
     * Create a LogRequest object based on a given revision range until.
     *
     * @param revisionRangeUntil
     *         revision range until
     * @return a LogRequest object
     */
    LogRequest withRevisionRangeUntil(String revisionRangeUntil);

    /** @return the integer value of the number of commits that will be skipped when calling log API */
    int getSkip();

    /**  set the integer value of the number of commits that will be skipped when calling log API */
    void setSkip(int skip);

    /**
     * Create a LogRequest object based on a given integer value of the number of commits that
     * will be skipped when calling log API
     *
     * @param skip
     *         integer value of the number of commits that will be skipped when calling log API
     * @return a LogRequest object
     */
    LogRequest withSkip(int skip);

    /** @return the integer value of the number of commits that will be returned when calling log API */
    int getMaxCount();

    /**  set the integer value of the number of commits that will be returned when calling log API */
    void setMaxCount(int maxCount);

    /**
     * Create a LogRequest object based on a given integer value of the number of commits that
     * will be returned when calling log API
     *
     * @param maxCount
     *         integer value of the number of commits that will be returned when calling log API
     * @return a LogRequest object
     */
    LogRequest withMaxCount(int maxCount);

    /** @return the file/folder path used when calling the log API */
    String getFilePath();

    /** set the file/folder path used when calling the log API */
    void setFilePath(String filePath);

    /**
     * Create a LogRequest object based on a given file/folder path used when calling the log API
     *
     * @param filePath
     *         file/folder path used when calling the log API
     * @return a LogRequest object
     */
    LogRequest withFilePath(String filePath);

    /** @return Filter revisions list by range of files. */
    List<String> getFileFilter();

    /** @set range of files */
    void setFileFilter(List<String> fileFilter);

    /**
     * Create a LogRequest object based on a given range of files
     *
     * @param fileFilter
     *         range of files
     * @return a LogRequest object
     */
    LogRequest withFileFilter(List<String> fileFilter);
}
