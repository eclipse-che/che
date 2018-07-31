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
package org.eclipse.che.infrastructure.docker.client.exception;

import java.io.IOException;

/** @author andrew00x */
public class DockerException extends IOException {
  private final int status;
  private final String originError;

  public DockerException(String message, int status) {
    super(message);
    this.status = status;
    this.originError = null;
  }

  public DockerException(String message, String originError, int status) {
    super(message);
    this.status = status;
    this.originError = originError;
  }

  public int getStatus() {
    return status;
  }

  public String getOriginError() {
    return originError;
  }
}
