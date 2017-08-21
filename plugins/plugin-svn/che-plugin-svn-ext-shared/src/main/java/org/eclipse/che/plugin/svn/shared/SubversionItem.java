/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.svn.shared;

import org.eclipse.che.dto.shared.DTO;

/** Definition of subversion item. */
@DTO
public interface SubversionItem {

  /**
   * ************************************************************************
   *
   * <p>Path
   *
   * <p>************************************************************************
   */
  String getPath();

  void setPath(String path);

  SubversionItem withPath(String path);

  /**
   * ************************************************************************
   *
   * <p>Name
   *
   * <p>************************************************************************
   */
  String getName();

  void setName(String name);

  SubversionItem withName(String name);

  /**
   * ************************************************************************
   *
   * <p>URL
   *
   * <p>************************************************************************
   */
  String getURL();

  void setURL(String url);

  SubversionItem withURL(String url);

  /**
   * ************************************************************************
   *
   * <p>Relative URL
   *
   * <p>************************************************************************
   */
  String getRelativeURL();

  void setRelativeURL(String relativeURL);

  SubversionItem withRelativeURL(String relativeURL);

  /**
   * ************************************************************************
   *
   * <p>Repository Root
   *
   * <p>************************************************************************
   */
  String getRepositoryRoot();

  void setRepositoryRoot(String repositoryRoot);

  SubversionItem withRepositoryRoot(String repositoryRoot);

  /**
   * ************************************************************************
   *
   * <p>Repository UUID
   *
   * <p>************************************************************************
   */
  String getRepositoryUUID();

  void setRepositoryUUID(String repositoryUUID);

  SubversionItem withRepositoryUUID(String repositoryUUID);

  /**
   * ************************************************************************
   *
   * <p>Revision
   *
   * <p>************************************************************************
   */
  String getRevision();

  void setRevision(String revision);

  SubversionItem withRevision(String revision);

  /**
   * ************************************************************************
   *
   * <p>Node Kind
   *
   * <p>************************************************************************
   */
  String getNodeKind();

  void setNodeKind(String nodeKind);

  SubversionItem withNodeKind(String nodeKind);

  /**
   * ************************************************************************
   *
   * <p>Schedule
   *
   * <p>************************************************************************
   */
  String getSchedule();

  void setSchedule(String schedule);

  SubversionItem withSchedule(String schedule);

  /**
   * ************************************************************************
   *
   * <p>Last Changed Revision
   *
   * <p>************************************************************************
   */
  String getLastChangedRev();

  void setLastChangedRev(String lastChangedRev);

  SubversionItem withLastChangedRev(String lastChangedRev);

  /**
   * ************************************************************************
   *
   * <p>Last Changed Date
   *
   * <p>************************************************************************
   */
  String getLastChangedDate();

  void setLastChangedDate(String lastChangedDate);

  SubversionItem withLastChangedDate(String lastChangedDate);

  /**
   * ************************************************************************
   *
   * <p>Project URL
   *
   * <p>************************************************************************
   */
  String getProjectUri();

  void setProjectUri(String projectUri);

  SubversionItem withProjectUri(String projectUri);
}
