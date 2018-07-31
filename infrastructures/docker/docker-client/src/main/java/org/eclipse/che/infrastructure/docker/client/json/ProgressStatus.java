/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.json;

/** @author andrew00x */
public class ProgressStatus {
  private String id;
  private String status;
  private String progress;
  private String stream;
  private String error;
  private ProgressDetail progressDetail;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getProgress() {
    return progress;
  }

  public void setProgress(String progress) {
    this.progress = progress;
  }

  public String getStream() {
    return stream;
  }

  public void setStream(String stream) {
    this.stream = stream;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public ProgressDetail getProgressDetail() {
    return progressDetail;
  }

  public void setProgressDetail(ProgressDetail progressDetail) {
    this.progressDetail = progressDetail;
  }

  @Override
  public String toString() {
    return "ProgressStatus{"
        + "id='"
        + id
        + '\''
        + ", status='"
        + status
        + '\''
        + ", progress='"
        + progress
        + '\''
        + ", stream='"
        + stream
        + '\''
        + ", error='"
        + error
        + '\''
        + ", progressDetail="
        + progressDetail
        + '}';
  }
}
