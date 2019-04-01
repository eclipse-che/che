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
import org.eclipse.che.api.core.model.workspace.devfile.Source;

/** @author Sergii Leshchenko */
public class SourceImpl implements Source {

  private String type;
  private String location;
  private String branch;
  private String startPoint;
  private String tag;
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
    return Objects.equals(getType(), source.getType())
        && Objects.equals(getLocation(), source.getLocation())
        && Objects.equals(getBranch(), source.getBranch())
        && Objects.equals(getStartPoint(), source.getStartPoint())
        && Objects.equals(getTag(), source.getTag())
        && Objects.equals(getCommitId(), source.getCommitId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getType(), getLocation(), getBranch(), getStartPoint(), getTag(), getCommitId());
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
