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
package org.eclipse.che.api.workspace.server.model.impl.devfile;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.eclipse.che.api.core.model.workspace.devfile.Source;

/** @author Sergii Leshchenko */
@Embeddable
public class SourceImpl implements Source {

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "location", nullable = false)
  private String location;

  @Column(name = "branch")
  private String branch;

  @Column(name = "start_point")
  private String startPoint;

  @Column(name = "tag")
  private String tag;

  @Column(name = "commit_id")
  private String commitId;

  public SourceImpl() {}

  public SourceImpl(
      String type, String location, String branch, String startPoint, String tag, String commitId) {
    this.type = type;
    this.location = location;
    this.branch = branch;
    this.startPoint = startPoint;
    this.tag = tag;
    this.commitId = commitId;
  }

  public SourceImpl(Source source) {
    this(
        source.getType(),
        source.getLocation(),
        source.getBranch(),
        source.getStartPoint(),
        source.getTag(),
        source.getCommitId());
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  @Override
  public String getStartPoint() {
    return startPoint;
  }

  public void setStartPoint(String startPoint) {
    this.startPoint = startPoint;
  }

  @Override
  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  @Override
  public String getCommitId() {
    return commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SourceImpl)) {
      return false;
    }
    SourceImpl source = (SourceImpl) o;
    return Objects.equals(type, source.type)
        && Objects.equals(location, source.location)
        && Objects.equals(branch, source.branch)
        && Objects.equals(startPoint, source.startPoint)
        && Objects.equals(tag, source.tag)
        && Objects.equals(commitId, source.commitId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, location, branch, startPoint, tag, commitId);
  }

  @Override
  public String toString() {
    return "SourceImpl{"
        + "type='"
        + type
        + '\''
        + ", location='"
        + location
        + '\''
        + ", branch='"
        + branch
        + '\''
        + ", startPoint='"
        + startPoint
        + '\''
        + ", tag='"
        + tag
        + '\''
        + ", commitId='"
        + commitId
        + '\''
        + '}';
  }
}
