/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git.params;

import java.util.ArrayList;
import java.util.List;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#log(LogParams)}.
 *
 * @author Igor Vinokur
 */
public class LogParams {

  private List<String> fileFilter;
  private String revisionRangeSince;
  private String revisionRangeUntil;
  private String filePath;
  private int skip;
  private int maxCount;

  private LogParams() {
    skip = -1;
    maxCount = -1;
  }

  /** Create new {@link LogParams} instance with default parameters. */
  public static LogParams create() {
    return new LogParams();
  }

  /** Returns the revision range since. */
  public String getRevisionRangeSince() {
    return revisionRangeSince;
  }

  /** Set revision range since. */
  public void setRevisionRangeSince(String revisionRangeSince) {
    this.revisionRangeSince = revisionRangeSince;
  }

  /**
   * Create a {@link LogParams} object based on a given revision range since.
   *
   * @param revisionRangeSince revision range since
   */
  public LogParams withRevisionRangeSince(String revisionRangeSince) {
    this.revisionRangeSince = revisionRangeSince;
    return this;
  }

  /** Returns the revision range until. */
  public String getRevisionRangeUntil() {
    return revisionRangeUntil;
  }

  /** Set revision range until. */
  public void setRevisionRangeUntil(String revisionRangeUntil) {
    this.revisionRangeUntil = revisionRangeUntil;
  }

  /**
   * Create a {@link LogParams} object based on a given revision range until.
   *
   * @param revisionRangeUntil revision range until
   */
  public LogParams withRevisionRangeUntil(String revisionRangeUntil) {
    this.revisionRangeUntil = revisionRangeUntil;
    return this;
  }

  /**
   * Returns the integer value of the number of commits that will be skipped when calling log
   * command.
   */
  public int getSkip() {
    return skip;
  }

  /**
   * Set the integer value of the number of commits that will be skipped when calling log command.
   */
  public void setSkip(int skip) {
    this.skip = skip;
  }

  /**
   * Create a {@link LogParams} object based on a given integer value of the number of commits that
   * will be skipped when calling log command
   *
   * @param skip integer value of the number of commits that will be skipped when calling log
   *     command
   */
  public LogParams withSkip(int skip) {
    this.skip = skip;
    return this;
  }

  /**
   * Returns the integer value of the number of commits that will be returned when calling log
   * command.
   */
  public int getMaxCount() {
    return maxCount;
  }

  /**
   * Set the integer value of the number of commits that will be returned when calling log command.
   */
  public void setMaxCount(int maxCount) {
    this.maxCount = maxCount;
  }

  /**
   * Create a {@link LogParams} object based on a given integer value of the number of commits that
   * will be returned when calling log command
   *
   * @param maxCount integer value of the number of commits that will be returned when calling log
   *     command
   */
  public LogParams withMaxCount(int maxCount) {
    this.maxCount = maxCount;
    return this;
  }

  /** Returns the file/folder path used when calling the log command. */
  public String getFilePath() {
    return filePath;
  }

  /** Set the file/folder path used when calling the log command. */
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  /**
   * Create a {@link LogParams} object based on a given file/folder path used when calling the log
   * command
   *
   * @param filePath file/folder path used when calling the log command
   */
  public LogParams withFilePath(String filePath) {
    this.filePath = filePath;
    return this;
  }

  /** Returns the Filter revisions list by range of files. */
  public List<String> getFileFilter() {
    return fileFilter == null ? new ArrayList<>() : fileFilter;
  }

  /** Set range of files. */
  public void setFileFilter(List<String> fileFilter) {
    this.fileFilter = fileFilter;
  }

  /**
   * Create a {@link LogParams} object based on a given range of files
   *
   * @param fileFilter range of files
   */
  public LogParams withFileFilter(List<String> fileFilter) {
    this.fileFilter = fileFilter;
    return this;
  }
}
